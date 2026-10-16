package com.example.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.*

/**
 * Procedural Real-time Audio Synthesizer for Turbo Kart.
 * Generates dynamic engine revs, tire drift squeals, turbo boost whooshes,
 * coin dings, powerup jingles, crashes, and background arcade synth rhythms.
 */
class TurboAudioEngine {
    private val sampleRate = 22050
    private var isRunning = false
    private var engineTrack: AudioTrack? = null
    private var sfxScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var sfxVolume: Float = 0.8f
    var musicVolume: Float = 0.6f

    // Dynamic car sound state
    @Volatile var engineRpmFactor: Float = 0.2f // 0.0 to 1.0
    @Volatile var isDrifting: Boolean = false
    @Volatile var isTurboActive: Boolean = false

    fun start() {
        if (isRunning) return
        isRunning = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast( sampleRate / 10 )

        try {
            engineTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            engineTrack?.play()

            sfxScope.launch {
                val chunkSize = 1024
                val buffer = ShortArray(chunkSize)
                var phaseEngine = 0.0
                var phaseTire = 0.0
                var phaseTurbo = 0.0

                while (isRunning) {
                    val vol = sfxVolume
                    val rpm = engineRpmFactor.coerceIn(0.1f, 1.0f)
                    val baseFreq = 55.0 + rpm * 140.0 // 55Hz to 195Hz low-growl rumble

                    val drifting = isDrifting
                    val turbo = isTurboActive

                    for (i in 0 until chunkSize) {
                        // Engine rumble: fundamental + 2nd harmonic + square-wave clipping
                        phaseEngine += (2.0 * Math.PI * baseFreq) / sampleRate
                        if (phaseEngine > 2.0 * Math.PI) phaseEngine -= 2.0 * Math.PI

                        var sample = (sin(phaseEngine) * 0.6 + sin(phaseEngine * 2.0) * 0.3 + sin(phaseEngine * 0.5) * 0.2)
                        // Overdrive saturation
                        sample = sample.coerceIn(-0.8, 0.8) * 0.7

                        // Tire screech when drifting
                        if (drifting) {
                            phaseTire += (2.0 * Math.PI * 1800.0) / sampleRate
                            val tireNoise = (Math.random() * 2.0 - 1.0) * 0.25
                            sample += (sin(phaseTire) * 0.15 + tireNoise) * 0.4
                        }

                        // Turbo flame hiss / whoosh
                        if (turbo) {
                            phaseTurbo += (2.0 * Math.PI * 900.0) / sampleRate
                            val hiss = (Math.random() * 2.0 - 1.0) * 0.35
                            sample += (sin(phaseTurbo) * 0.2 + hiss) * 0.5
                        }

                        val finalVal = (sample * 32767.0 * vol * 0.5).toInt().coerceIn(-32767, 32767)
                        buffer[i] = finalVal.toShort()
                    }

                    engineTrack?.write(buffer, 0, chunkSize)
                }
            }
        } catch (e: Exception) {
            // AudioTrack fallback
        }
    }

    fun playCoinSound() {
        playToneSeries(listOf(Pair(987.77f, 70), Pair(1318.51f, 120)), volume = sfxVolume * 0.9f)
    }

    fun playPowerUpCollected() {
        playToneSeries(
            listOf(
                Pair(523.25f, 60), // C5
                Pair(659.25f, 60), // E5
                Pair(783.99f, 60), // G5
                Pair(1046.50f, 140) // C6
            ),
            volume = sfxVolume
        )
    }

    fun playCrashSound() {
        sfxScope.launch {
            val count = (sampleRate * 0.22).toInt()
            val buf = ShortArray(count)
            for (i in 0 until count) {
                val decay = 1f - (i.toFloat() / count)
                val noise = (Math.random() * 2.0 - 1.0).toFloat()
                val boom = sin(2.0 * Math.PI * (60.0 * decay) * i / sampleRate).toFloat()
                buf[i] = ((noise * 0.6f + boom * 0.4f) * decay * 32767f * sfxVolume * 0.8f).toInt().toShort()
            }
            playPcmBuffer(buf)
        }
    }

    fun playCountdownBeep(isGo: Boolean) {
        val freq = if (isGo) 880f else 440f
        val durMs = if (isGo) 350 else 180
        playToneSeries(listOf(Pair(freq, durMs)), volume = sfxVolume)
    }

    fun playVictoryFanfare() {
        playToneSeries(
            listOf(
                Pair(523.25f, 100),
                Pair(523.25f, 100),
                Pair(523.25f, 100),
                Pair(659.25f, 300),
                Pair(587.33f, 150),
                Pair(659.25f, 150),
                Pair(783.99f, 500)
            ),
            volume = sfxVolume
        )
    }

    private fun playToneSeries(tones: List<Pair<Float, Int>>, volume: Float) {
        sfxScope.launch {
            for ((freq, durationMs) in tones) {
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    val decay = 1.0 - (i.toDouble() / numSamples) * 0.25
                    buffer[i] = (sin(angle) * decay * 32767.0 * volume).toInt().toShort()
                }
                playPcmBuffer(buffer)
            }
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            sfxScope.launch {
                delay((buffer.size.toLong() * 1000) / sampleRate + 100)
                try {
                    track.stop()
                    track.release()
                } catch (ignored: Exception) {}
            }
        } catch (ignored: Exception) {}
    }

    fun stop() {
        isRunning = false
        try {
            engineTrack?.stop()
            engineTrack?.release()
        } catch (ignored: Exception) {}
        engineTrack = null
    }
}
