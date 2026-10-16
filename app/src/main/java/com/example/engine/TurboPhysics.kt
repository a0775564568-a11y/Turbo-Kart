package com.example.engine

import com.example.model.Car
import com.example.model.CarUpgrade
import com.example.model.PowerUpType
import com.example.model.Track
import kotlin.math.*

data class StuntResult(
    val stuntNameHe: String,
    val turboRewardSeconds: Float,
    val coinBonus: Int
)

class RacerPhysics(
    val id: String,
    val nameHe: String,
    val isPlayer: Boolean,
    val car: Car,
    val upgrade: CarUpgrade = CarUpgrade()
) {
    // World space position along track
    var distanceAlongTrack: Float = 0f
    var lateralPosition: Float = 0f // -1.0 (left curb) to +1.0 (right curb)
    var height: Float = 0f          // elevation above track
    var verticalVelocity: Float = 0f

    // Motion parameters
    var forwardSpeed: Float = 0f
    var steerAngle: Float = 0f
    var headingAngle: Float = 0f

    // Drift mechanics
    var isDrifting: Boolean = false
    var driftDirection: Int = 0 // -1 left, +1 right
    var driftCharge: Float = 0f // builds up for mini-turbo boost (0.0 to 3.0)

    // Air / Stunt mechanics
    var isAirborne: Boolean = false
    var inAirRotationRoll: Float = 0f
    var inAirRotationPitch: Float = 0f
    var stuntRotationsCount: Int = 0
    var lastStuntResult: StuntResult? = null

    // Powerup states
    var activeShieldTime: Float = 0f
    var activeTurboTime: Float = 0f
    var activeSpinoutTime: Float = 0f
    var activeSlowdownTime: Float = 0f
    var activeGhostTime: Float = 0f
    var hasMagnet: Boolean = false

    // Lap tracking
    var currentLap: Int = 1
    var lapProgress: Float = 0f // 0.0 to 1.0
    var raceFinished: Boolean = false
    var finishTimeSeconds: Float = 0f

    // Computed effective attributes based on upgrades
    val maxSpeed: Float get() = (50f + car.baseSpeed * 40f + upgrade.engineLevel * 4f) *
            (if (activeTurboTime > 0f) 1.45f else 1.0f) *
            (if (activeSlowdownTime > 0f) 0.55f else 1.0f)

    val acceleration: Float get() = (18f + car.baseAccel * 15f + upgrade.engineLevel * 2f) *
            (if (activeTurboTime > 0f) 2.2f else 1.0f)

    val handling: Float get() = (1.8f + car.baseHandling * 1.5f + upgrade.handlingLevel * 0.25f)
    val stability: Float get() = (0.7f + car.baseStability * 0.3f + upgrade.bodyLevel * 0.05f)

    fun update(
        dt: Float,
        throttleInput: Float, // -1.0 (brake/reverse) to +1.0 (full gas)
        steerInput: Float,    // -1.0 (left) to +1.0 (right)
        driftButton: Boolean,
        track: Track
    ) {
        // Handle spinout (e.g. ice trap or paint missile)
        if (activeSpinoutTime > 0f) {
            activeSpinoutTime -= dt
            forwardSpeed = (forwardSpeed - 25f * dt).coerceAtLeast(5f)
            headingAngle += 15f * dt
            return
        }

        // Countdown powerups
        if (activeTurboTime > 0f) activeTurboTime -= dt
        if (activeShieldTime > 0f) activeShieldTime -= dt
        if (activeSlowdownTime > 0f) activeSlowdownTime -= dt
        if (activeGhostTime > 0f) activeGhostTime -= dt

        // Surface detection
        val trackLengthUnits = 1000f
        val currentSegIndex = ((distanceAlongTrack % trackLengthUnits) / trackLengthUnits * track.segments.size).toInt().coerceIn(0, track.segments.size - 1)
        val currentSeg = track.segments[currentSegIndex]

        var surfaceFriction = 0.98f
        if (abs(lateralPosition) > 1.05f) {
            // Offroad sand/grass
            surfaceFriction = 0.88f
        }
        if (currentSeg.hasIcePatch) {
            surfaceFriction = 0.995f // slippery
        }
        if (currentSeg.hasBoostPad && !isAirborne) {
            // Boost pad trigger!
            activeTurboTime = 2.0f
        }

        // Airborne and Jump physics
        val gravity = 28f
        if (isAirborne) {
            height += verticalVelocity * dt
            verticalVelocity -= gravity * dt

            // Air stunt rotations
            if (steerInput != 0f) {
                inAirRotationRoll += steerInput * 360f * dt
                if (abs(inAirRotationRoll) >= 360f) {
                    stuntRotationsCount++
                    inAirRotationRoll %= 360f
                }
            }

            if (height <= 0f) {
                // Landed!
                height = 0f
                verticalVelocity = 0f
                isAirborne = false

                // Evaluate stunt landing
                if (stuntRotationsCount > 0) {
                    val rewardTurbo = (stuntRotationsCount * 1.2f).coerceAtMost(3.5f)
                    val rewardCoins = stuntRotationsCount * 30
                    val toast = when (stuntRotationsCount) {
                        1 -> "נחיתה מוצלחת! +טורבו"
                        2 -> "סיבוב כפול מדהים! +סופר טורבו"
                        else -> "פעלול אגדי! +מגה בונוס"
                    }
                    lastStuntResult = StuntResult(toast, rewardTurbo, rewardCoins)
                    activeTurboTime = rewardTurbo
                }
                stuntRotationsCount = 0
                inAirRotationRoll = 0f
            }
        } else {
            // Check for ramp launch
            if (currentSeg.hasRamp && forwardSpeed > 20f) {
                isAirborne = true
                verticalVelocity = 12f + (forwardSpeed / maxSpeed) * 6f
                stuntRotationsCount = 0
                inAirRotationRoll = 0f
            }
        }

        // Drift system
        if (driftButton && !isAirborne && abs(steerInput) > 0.3f && forwardSpeed > 25f) {
            if (!isDrifting) {
                isDrifting = true
                driftDirection = if (steerInput > 0) 1 else -1
            }
            driftCharge += dt * 1.5f
        } else {
            if (isDrifting) {
                // Drift release! Mini-turbo awarded if charged
                if (driftCharge > 1.8f) {
                    activeTurboTime = 2.2f // Orange fire boost!
                } else if (driftCharge > 0.8f) {
                    activeTurboTime = 1.0f // Blue spark boost!
                }
                isDrifting = false
                driftCharge = 0f
            }
        }

        // Throttle & Acceleration
        val targetSpeed = when {
            throttleInput > 0 -> maxSpeed * throttleInput
            throttleInput < 0 -> -maxSpeed * 0.35f * abs(throttleInput)
            else -> 0f
        }

        if (forwardSpeed < targetSpeed) {
            forwardSpeed += acceleration * dt
            if (forwardSpeed > targetSpeed) forwardSpeed = targetSpeed
        } else if (forwardSpeed > targetSpeed) {
            val decel = if (throttleInput < 0) 45f else 15f
            forwardSpeed -= decel * dt
            if (forwardSpeed < targetSpeed && throttleInput >= 0) forwardSpeed = targetSpeed
        }

        forwardSpeed *= surfaceFriction

        // Steering response
        val steerSpeed = handling * (if (isDrifting) 1.35f else 1.0f)
        steerAngle = steerInput * steerSpeed

        // Track curvature influence
        val curvePull = currentSeg.curve * 1.8f

        // Move position
        distanceAlongTrack += forwardSpeed * dt
        val lateralDelta = (steerAngle * forwardSpeed * 0.035f - curvePull * (forwardSpeed / 30f)) * dt
        lateralPosition += lateralDelta

        // Road curb boundaries
        lateralPosition = lateralPosition.coerceIn(-1.5f, 1.5f)

        // Heading visual alignment
        headingAngle = steerInput * (if (isDrifting) 35f * driftDirection else 18f)
    }

    fun applyHit(powerUp: PowerUpType) {
        if (activeShieldTime > 0f) {
            // Shield absorbed the hit!
            activeShieldTime = 0f
            return
        }
        when (powerUp) {
            PowerUpType.ICE, PowerUpType.PAINT_MISSILE -> {
                activeSpinoutTime = 1.6f
            }
            PowerUpType.TORNADO, PowerUpType.LIGHTNING -> {
                activeSlowdownTime = 3.0f
            }
            else -> {}
        }
    }
}
