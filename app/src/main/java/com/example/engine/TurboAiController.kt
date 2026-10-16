package com.example.engine

import com.example.model.Track
import kotlin.math.abs
import kotlin.math.sin

/**
 * Local AI driver for computer-controlled racers.
 * Drives with dynamic obstacle avoidance, cornering line, overtaking, and powerup collection.
 */
class TurboAiController(
    val racer: RacerPhysics,
    val preferredOffset: Float, // -0.6 to +0.6 preferred track lane
    val skillLevel: Float = 1.0f // 0.8 to 1.3 multiplier
) {
    private var thinkTimer: Float = 0f
    private var currentSteer: Float = 0f
    private var currentThrottle: Float = 1.0f
    private var randomWander: Float = (Math.random().toFloat() - 0.5f) * 0.2f

    fun update(
        dt: Float,
        track: Track,
        otherRacers: List<RacerPhysics>
    ) {
        thinkTimer += dt
        val trackLengthUnits = 1000f
        val segIndex = ((racer.distanceAlongTrack % trackLengthUnits) / trackLengthUnits * track.segments.size).toInt().coerceIn(0, track.segments.size - 1)
        val currentSeg = track.segments[segIndex]

        // Lookahead segment
        val nextIndex = (segIndex + 2) % track.segments.size
        val nextSeg = track.segments[nextIndex]

        // 1. Follow the racing line through turns (turn anticipation)
        var targetLateral = preferredOffset + randomWander
        if (nextSeg.curve > 0.3f) {
            // Approaching sharp right curve: stay slightly left before turning in
            targetLateral = -0.3f
        } else if (nextSeg.curve < -0.3f) {
            // Approaching sharp left curve: stay slightly right before turning in
            targetLateral = 0.3f
        }

        // 2. Obstacle avoidance
        if (currentSeg.hasObstacle) {
            if (abs(currentSeg.obstacleOffset - racer.lateralPosition) < 0.45f) {
                targetLateral = if (currentSeg.obstacleOffset > 0) -0.55f else 0.55f
            }
        }

        // 3. Aim for Item Boxes and Boost Pads
        if (currentSeg.hasItemBox || currentSeg.hasBoostPad) {
            targetLateral = 0f // center lane
        }

        // 4. Overtaking logic: check if another racer is directly ahead
        for (other in otherRacers) {
            if (other.id != racer.id) {
                val distanceAhead = other.distanceAlongTrack - racer.distanceAlongTrack
                if (distanceAhead in 2f..25f && abs(other.lateralPosition - racer.lateralPosition) < 0.35f) {
                    // Overtake by moving to the opposite side
                    targetLateral = if (other.lateralPosition >= 0) -0.5f else 0.5f
                    break
                }
            }
        }

        // Calculate steering toward target lateral position
        val lateralError = targetLateral - racer.lateralPosition
        val desiredSteer = (lateralError * 2.2f + currentSeg.curve * 1.5f).coerceIn(-1f, 1f)
        currentSteer += (desiredSteer - currentSteer) * (dt * 8f * skillLevel)

        // Throttle management: brake slightly for extreme hairpins
        val isSharpTurn = abs(currentSeg.curve) > 0.75f
        currentThrottle = if (isSharpTurn && racer.forwardSpeed > 45f) {
            0.6f
        } else {
            1.0f
        }

        val wantDrift = isSharpTurn && racer.forwardSpeed > 30f

        racer.update(
            dt = dt,
            throttleInput = currentThrottle,
            steerInput = currentSteer,
            driftButton = wantDrift,
            track = track
        )
    }
}
