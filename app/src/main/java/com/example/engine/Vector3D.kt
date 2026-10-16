package com.example.engine

import kotlin.math.*

/**
 * 3D Vector with essential game math operations.
 */
data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float): Vector3 = Vector3(x / scalar, y / scalar, z / scalar)

    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun normalized(): Vector3 {
        val l = length()
        return if (l > 0.0001f) this / l else Vector3()
    }

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z
    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun rotateY(angleRad: Float): Vector3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector3(
            x = x * cosA + z * sinA,
            y = y,
            z = -x * sinA + z * cosA
        )
    }

    fun rotateX(angleRad: Float): Vector3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector3(
            x = x,
            y = y * cosA - z * sinA,
            z = y * sinA + z * cosA
        )
    }

    fun rotateZ(angleRad: Float): Vector3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vector3(
            x = x * cosA - y * sinA,
            y = x * sinA + y * cosA,
            z = z
        )
    }
}

data class ProjectedPoint(
    val x: Float,
    val y: Float,
    val depth: Float,
    val scale: Float
)

class Camera3D(
    var position: Vector3 = Vector3(0f, 3.5f, -6f),
    var target: Vector3 = Vector3(0f, 1f, 0f),
    var fovDegrees: Float = 65f,
    var pitch: Float = 0f,
    var yaw: Float = 0f,
    var roll: Float = 0f
) {
    fun project(point: Vector3, screenWidth: Float, screenHeight: Float): ProjectedPoint? {
        // Transform point relative to camera position
        val rel = point - position

        // Apply camera yaw
        val rotatedYaw = rel.rotateY(-yaw)
        // Apply camera pitch
        val rotatedPitch = rotatedYaw.rotateX(-pitch)
        // Apply camera roll
        val finalPoint = rotatedPitch.rotateZ(-roll)

        val nearClip = 0.5f
        if (finalPoint.z <= nearClip) return null // Behind camera

        val fovRad = Math.toRadians(fovDegrees.toDouble()).toFloat()
        val f = 1f / tan(fovRad / 2f)
        val aspect = screenWidth / screenHeight

        val projX = (finalPoint.x / (finalPoint.z * aspect)) * f
        val projY = (finalPoint.y / finalPoint.z) * f

        val screenX = (projX * 0.5f + 0.5f) * screenWidth
        val screenY = (-projY * 0.5f + 0.5f) * screenHeight
        val scale = f / finalPoint.z

        return ProjectedPoint(screenX, screenY, finalPoint.z, scale)
    }
}
