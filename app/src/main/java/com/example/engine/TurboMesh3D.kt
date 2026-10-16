package com.example.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.model.Car
import com.example.model.CarBodyType
import com.example.model.SceneryType
import kotlin.math.cos
import kotlin.math.sin

data class Polygon3D(
    val vertices: List<Vector3>,
    val color: Color,
    val averageDepth: Float
)

object TurboMesh3D {

    /**
     * Builds cartoon 3D polygons for a kart.
     */
    fun createCarPolygons(
        car: Car,
        center: Vector3,
        headingAngleDeg: Float,
        pitchDeg: Float = 0f,
        rollDeg: Float = 0f,
        steerAngleDeg: Float = 0f,
        wheelSpinAngleDeg: Float = 0f,
        primaryColor: Color = car.bodyColor,
        isGhost: Boolean = false,
        isShielded: Boolean = false
    ): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()
        val headingRad = Math.toRadians(headingAngleDeg.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitchDeg.toDouble()).toFloat()
        val rollRad = Math.toRadians(rollDeg.toDouble()).toFloat()

        fun transform(v: Vector3): Vector3 {
            val rotZ = v.rotateZ(rollRad)
            val rotX = rotZ.rotateX(pitchRad)
            val rotY = rotX.rotateY(headingRad)
            return rotY + center
        }

        val alpha = if (isGhost) 0.45f else 1.0f
        val bodyCol = primaryColor.copy(alpha = alpha)
        val secCol = car.secondaryColor.copy(alpha = alpha)
        val darkBody = Color(
            (bodyCol.red * 0.7f).coerceIn(0f, 1f),
            (bodyCol.green * 0.7f).coerceIn(0f, 1f),
            (bodyCol.blue * 0.7f).coerceIn(0f, 1f),
            alpha
        )
        val wheelTireCol = Color(0xFF212121).copy(alpha = alpha)
        val wheelRimCol = car.wheelColor.copy(alpha = alpha)
        val glassCol = Color(0xFF80DEEA).copy(alpha = if (isGhost) 0.3f else 0.85f)

        // 1. Chassis Main Body
        val halfW = 0.85f
        val halfL = 1.4f
        val bodyH = 0.55f
        val groundY = 0.45f

        // Top hood/roof
        val v1 = transform(Vector3(-halfW * 0.8f, groundY + bodyH, -halfL * 0.6f))
        val v2 = transform(Vector3(halfW * 0.8f, groundY + bodyH, -halfL * 0.6f))
        val v3 = transform(Vector3(halfW * 0.6f, groundY + bodyH * 0.8f, halfL * 0.9f))
        val v4 = transform(Vector3(-halfW * 0.6f, groundY + bodyH * 0.8f, halfL * 0.9f))
        polys.add(Polygon3D(listOf(v1, v2, v3, v4), bodyCol, (v1.z + v2.z + v3.z + v4.z) / 4f))

        // Left side
        val v5 = transform(Vector3(-halfW, groundY, -halfL))
        val v6 = transform(Vector3(-halfW, groundY, halfL))
        polys.add(Polygon3D(listOf(v5, v1, v4, v6), darkBody, (v5.z + v1.z + v4.z + v6.z) / 4f))

        // Right side
        val v7 = transform(Vector3(halfW, groundY, -halfL))
        val v8 = transform(Vector3(halfW, groundY, halfL))
        polys.add(Polygon3D(listOf(v7, v8, v3, v2), darkBody, (v7.z + v8.z + v3.z + v2.z) / 4f))

        // Front bumper
        val frontBumperCol = secCol
        polys.add(Polygon3D(listOf(v6, v4, v3, v8), frontBumperCol, (v6.z + v4.z + v3.z + v8.z) / 4f))

        // Rear engine & exhaust area
        val rearCol = Color(0xFF1E293B).copy(alpha = alpha)
        polys.add(Polygon3D(listOf(v5, v7, v2, v1), rearCol, (v5.z + v7.z + v2.z + v1.z) / 4f))

        // Windshield
        val ws1 = transform(Vector3(-halfW * 0.65f, groundY + bodyH, -halfL * 0.1f))
        val ws2 = transform(Vector3(halfW * 0.65f, groundY + bodyH, -halfL * 0.1f))
        val ws3 = transform(Vector3(halfW * 0.5f, groundY + bodyH * 1.5f, -halfL * 0.4f))
        val ws4 = transform(Vector3(-halfW * 0.5f, groundY + bodyH * 1.5f, -halfL * 0.4f))
        polys.add(Polygon3D(listOf(ws1, ws2, ws3, ws4), glassCol, (ws1.z + ws2.z + ws3.z + ws4.z) / 4f))

        // Headlights (glowing yellow)
        val hlCol = Color(0xFFFFEB3B).copy(alpha = alpha)
        val hl1 = transform(Vector3(-halfW * 0.5f, groundY + bodyH * 0.5f, halfL * 0.92f))
        val hl2 = transform(Vector3(-halfW * 0.25f, groundY + bodyH * 0.5f, halfL * 0.92f))
        val hl3 = transform(Vector3(-halfW * 0.25f, groundY + bodyH * 0.25f, halfL * 0.92f))
        val hl4 = transform(Vector3(-halfW * 0.5f, groundY + bodyH * 0.25f, halfL * 0.92f))
        polys.add(Polygon3D(listOf(hl1, hl2, hl3, hl4), hlCol, (hl1.z + hl2.z + hl3.z + hl4.z) / 4f))

        val hr1 = transform(Vector3(halfW * 0.25f, groundY + bodyH * 0.5f, halfL * 0.92f))
        val hr2 = transform(Vector3(halfW * 0.5f, groundY + bodyH * 0.5f, halfL * 0.92f))
        val hr3 = transform(Vector3(halfW * 0.5f, groundY + bodyH * 0.25f, halfL * 0.92f))
        val hr4 = transform(Vector3(halfW * 0.25f, groundY + bodyH * 0.25f, halfL * 0.92f))
        polys.add(Polygon3D(listOf(hr1, hr2, hr3, hr4), hlCol, (hr1.z + hr2.z + hr3.z + hr4.z) / 4f))

        // Spoiler at rear
        if (car.spoilerType > 0) {
            val spCol = secCol
            val spH = groundY + bodyH * (1.3f + car.spoilerType * 0.15f)
            val sp1 = transform(Vector3(-halfW * 0.95f, spH, -halfL * 0.85f))
            val sp2 = transform(Vector3(halfW * 0.95f, spH, -halfL * 0.85f))
            val sp3 = transform(Vector3(halfW * 0.95f, spH + 0.15f, -halfL * 0.95f))
            val sp4 = transform(Vector3(-halfW * 0.95f, spH + 0.15f, -halfL * 0.95f))
            polys.add(Polygon3D(listOf(sp1, sp2, sp3, sp4), spCol, (sp1.z + sp2.z + sp3.z + sp4.z) / 4f))
        }

        // 4 Chunky 3D Cartoon Wheels
        val wheelRadius = 0.38f * car.wheelRadius
        val wheelWidth = 0.26f

        fun addWheel(centerOffset: Vector3, steer: Float) {
            val steerRad = Math.toRadians(steer.toDouble()).toFloat()
            val wPos = transform(centerOffset)
            val segments = 6
            val rimPts = mutableListOf<Vector3>()
            val side = if (centerOffset.x > 0) 1 else -1

            for (i in 0 until segments) {
                val angle = (i * 2 * Math.PI / segments) + Math.toRadians(wheelSpinAngleDeg.toDouble())
                val localY = (sin(angle) * wheelRadius).toFloat()
                val localZ = (cos(angle) * wheelRadius).toFloat()

                val vWheel = Vector3(
                    x = centerOffset.x + (side * wheelWidth * 0.5f),
                    y = centerOffset.y + localY,
                    z = centerOffset.z + localZ
                )
                rimPts.add(transform(vWheel))
            }
            if (rimPts.size >= 4) {
                polys.add(Polygon3D(rimPts.take(4), wheelRimCol, wPos.z))
            }
        }

        // Front wheels (can steer)
        addWheel(Vector3(-halfW * 0.95f, groundY + wheelRadius * 0.8f, halfL * 0.7f), steerAngleDeg)
        addWheel(Vector3(halfW * 0.95f, groundY + wheelRadius * 0.8f, halfL * 0.7f), steerAngleDeg)

        // Rear wheels (fixed)
        addWheel(Vector3(-halfW * 0.95f, groundY + wheelRadius * 0.85f, -halfL * 0.7f), 0f)
        addWheel(Vector3(halfW * 0.95f, groundY + wheelRadius * 0.85f, -halfL * 0.7f), 0f)

        // Shield dome effect
        if (isShielded) {
            val sCol = Color(0x6600E5FF)
            val sRadius = 1.9f
            val s1 = transform(Vector3(-sRadius, 0.2f, -sRadius))
            val s2 = transform(Vector3(sRadius, 0.2f, -sRadius))
            val s3 = transform(Vector3(sRadius, 0.2f, sRadius))
            val s4 = transform(Vector3(-sRadius, 0.2f, sRadius))
            polys.add(Polygon3D(listOf(s1, s2, s3, s4), sCol, center.z))
        }

        return polys
    }

    /**
     * Builds 3D scenery items (trees, rocks, towers) along the roadside.
     */
    fun createScenery(type: SceneryType, position: Vector3): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()
        when (type) {
            SceneryType.PALM_TREE -> {
                // Trunk
                val trunkCol = Color(0xFF6D4C41)
                val t1 = position + Vector3(-0.3f, 0f, 0f)
                val t2 = position + Vector3(0.3f, 0f, 0f)
                val t3 = position + Vector3(0.2f, 3.5f, 0.2f)
                val t4 = position + Vector3(-0.2f, 3.5f, 0.2f)
                polys.add(Polygon3D(listOf(t1, t2, t3, t4), trunkCol, position.z))
                // Fronds (leaves)
                val leafCol = Color(0xFF2E7D32)
                val topPos = position + Vector3(0f, 3.5f, 0f)
                val l1 = topPos + Vector3(-2f, 0.4f, -1f)
                val l2 = topPos + Vector3(2f, 0.4f, -1f)
                val l3 = topPos + Vector3(1f, -0.6f, 1.5f)
                val l4 = topPos + Vector3(-1f, -0.6f, 1.5f)
                polys.add(Polygon3D(listOf(l1, l2, l3, l4), leafCol, topPos.z))
            }
            SceneryType.CACTUS -> {
                val cCol = Color(0xFF388E3C)
                val p1 = position + Vector3(-0.4f, 0f, 0f)
                val p2 = position + Vector3(0.4f, 0f, 0f)
                val p3 = position + Vector3(0.4f, 2.6f, 0f)
                val p4 = position + Vector3(-0.4f, 2.6f, 0f)
                polys.add(Polygon3D(listOf(p1, p2, p3, p4), cCol, position.z))
            }
            SceneryType.ICE_CRYSTAL -> {
                val iceCol = Color(0xFFB2EBF2)
                val p1 = position + Vector3(-0.8f, 0f, 0f)
                val p2 = position + Vector3(0.8f, 0f, 0f)
                val p3 = position + Vector3(0f, 3.2f, 0f)
                polys.add(Polygon3D(listOf(p1, p2, p3), iceCol, position.z))
            }
            SceneryType.LAVA_ROCK -> {
                val rockCol = Color(0xFF3E2723)
                val p1 = position + Vector3(-1.2f, 0f, -0.8f)
                val p2 = position + Vector3(1.2f, 0f, -0.8f)
                val p3 = position + Vector3(0.8f, 1.8f, 0.8f)
                val p4 = position + Vector3(-0.8f, 1.8f, 0.8f)
                polys.add(Polygon3D(listOf(p1, p2, p3, p4), rockCol, position.z))
            }
            SceneryType.NEON_PILLAR -> {
                val neonCol = Color(0xFF00E5FF)
                val p1 = position + Vector3(-0.35f, 0f, 0f)
                val p2 = position + Vector3(0.35f, 0f, 0f)
                val p3 = position + Vector3(0.35f, 4.5f, 0f)
                val p4 = position + Vector3(-0.35f, 4.5f, 0f)
                polys.add(Polygon3D(listOf(p1, p2, p3, p4), neonCol, position.z))
            }
            else -> {
                // Default pine/tree
                val trunkCol = Color(0xFF5D4037)
                val p1 = position + Vector3(-0.25f, 0f, 0f)
                val p2 = position + Vector3(0.25f, 0f, 0f)
                val p3 = position + Vector3(0.2f, 1.5f, 0f)
                val p4 = position + Vector3(-0.2f, 1.5f, 0f)
                polys.add(Polygon3D(listOf(p1, p2, p3, p4), trunkCol, position.z))

                val pineCol = Color(0xFF1B5E20)
                val t1 = position + Vector3(-1.5f, 1.2f, 0f)
                val t2 = position + Vector3(1.5f, 1.2f, 0f)
                val t3 = position + Vector3(0f, 4.0f, 0f)
                polys.add(Polygon3D(listOf(t1, t2, t3), pineCol, position.z))
            }
        }
        return polys
    }

    /**
     * Builds floating spinning 3D Mystery Item Box.
     */
    fun createItemBox(center: Vector3, spinAngleDeg: Float): List<Polygon3D> {
        val polys = mutableListOf<Polygon3D>()
        val rad = Math.toRadians(spinAngleDeg.toDouble()).toFloat()
        val s = 0.6f // half size
        val c = Color(0xFFFFD600) // glistening bright yellow
        val borderCol = Color(0xFFFF9100)

        fun rot(v: Vector3): Vector3 = v.rotateY(rad) + center

        val f1 = rot(Vector3(-s, -s, -s))
        val f2 = rot(Vector3(s, -s, -s))
        val f3 = rot(Vector3(s, s, -s))
        val f4 = rot(Vector3(-s, s, -s))
        polys.add(Polygon3D(listOf(f1, f2, f3, f4), c, (f1.z + f2.z + f3.z + f4.z) / 4f))

        val b1 = rot(Vector3(-s, -s, s))
        val b2 = rot(Vector3(s, -s, s))
        val b3 = rot(Vector3(s, s, s))
        val b4 = rot(Vector3(-s, s, s))
        polys.add(Polygon3D(listOf(b1, b2, b3, b4), borderCol, (b1.z + b2.z + b3.z + b4.z) / 4f))

        // Top face
        polys.add(Polygon3D(listOf(f4, f3, b3, b4), Color(0xFFFFF9C4), (f4.z + f3.z + b3.z + b4.z) / 4f))

        return polys
    }
}
