package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable
fun RaceScreen(
    track: Track,
    gameMode: GameMode,
    playerCar: Car,
    playerUpgrade: CarUpgrade,
    controlMode: Int,
    autoAccelerate: Boolean,
    sensitivity: Float,
    audioEngine: TurboAudioEngine,
    onRaceFinished: (rank: Int, timeSeconds: Float, coinsEarned: Int) -> Unit,
    onExitToMenu: () -> Unit
) {
    val context = LocalContext.current

    // Racers setup: Player + 5 AI Opponents
    val playerRacer = remember {
        RacerPhysics("player", "אתה", isPlayer = true, car = playerCar, upgrade = playerUpgrade)
    }

    val aiRacers = remember {
        val pool = CarCatalog.cars.filter { it.id != playerCar.id }
        listOf(
            RacerPhysics("ai_1", "ברק הטורבו", false, pool[0]).apply { distanceAlongTrack = -4f; lateralPosition = 0.4f },
            RacerPhysics("ai_2", "סופה אדומה", false, pool[1]).apply { distanceAlongTrack = -8f; lateralPosition = -0.4f },
            RacerPhysics("ai_3", "מלך החולות", false, pool[2]).apply { distanceAlongTrack = -12f; lateralPosition = 0.2f },
            RacerPhysics("ai_4", "פנטום המרוץ", false, pool[3]).apply { distanceAlongTrack = -16f; lateralPosition = -0.3f },
            RacerPhysics("ai_5", "קפטן נייטרו", false, pool[4]).apply { distanceAlongTrack = -20f; lateralPosition = 0.5f }
        )
    }

    val aiControllers = remember {
        aiRacers.mapIndexed { idx, racer ->
            TurboAiController(
                racer = racer,
                preferredOffset = ((idx % 3) - 1) * 0.35f,
                skillLevel = if (gameMode == GameMode.CHAMPIONS_TOURNAMENT) 1.25f else 1.0f
            )
        }
    }

    val allRacers = remember { listOf(playerRacer) + aiRacers }

    // Race loop state
    var countdownTimer by remember { mutableStateOf(3) }
    var countdownText by remember { mutableStateOf("3") }
    var isCountingDown by remember { mutableStateOf(true) }
    var raceTimeSeconds by remember { mutableStateOf(0f) }
    var isPaused by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var finalRank by remember { mutableStateOf(1) }
    var stuntToast by remember { mutableStateOf<String?>(null) }

    // Player PowerUp state
    var storedPowerUp by remember { mutableStateOf<PowerUpType?>(null) }
    var isItemBoxSpinning by remember { mutableStateOf(false) }

    // Controls input state
    var leftPressed by remember { mutableStateOf(false) }
    var rightPressed by remember { mutableStateOf(false) }
    var gasPressed by remember { mutableStateOf(false) }
    var brakePressed by remember { mutableStateOf(false) }
    var driftPressed by remember { mutableStateOf(false) }
    var steeringWheelAngle by remember { mutableStateOf(0f) }
    var tiltSteerInput by remember { mutableStateOf(0f) }

    // Accelerometer / Tilt Sensor support
    DisposableEffect(controlMode) {
        if (controlMode == 2) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        // In landscape mode, y-axis tilt dictates steering
                        val rawY = it.values[1]
                        tiltSteerInput = (-rawY / 4.5f).coerceIn(-1f, 1f)
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager?.registerListener(listener, accel, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager?.unregisterListener(listener) }
        } else {
            onDispose {}
        }
    }

    // Countdown Coroutine
    LaunchedEffect(Unit) {
        audioEngine.start()
        audioEngine.playCountdownBeep(false)
        delay(1000)
        countdownTimer = 2
        countdownText = "2"
        audioEngine.playCountdownBeep(false)
        delay(1000)
        countdownTimer = 1
        countdownText = "1"
        audioEngine.playCountdownBeep(false)
        delay(1000)
        countdownTimer = 0
        countdownText = "צא!"
        audioEngine.playCountdownBeep(true)
        delay(800)
        isCountingDown = false
    }

    // Game Physics & Animation Loop (60 FPS tick)
    var frameTick by remember { mutableStateOf(0L) }
    LaunchedEffect(isCountingDown, isPaused, isFinished) {
        if (!isCountingDown && !isPaused && !isFinished) {
            var lastTime = System.nanoTime()
            while (true) {
                val now = System.nanoTime()
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.005f, 0.033f)
                lastTime = now

                raceTimeSeconds += dt

                // Player input evaluation
                val steerInput = when (controlMode) {
                    1 -> (steeringWheelAngle / 75f).coerceIn(-1f, 1f) * sensitivity
                    2 -> tiltSteerInput * sensitivity
                    else -> (if (rightPressed) 1f else if (leftPressed) -1f else 0f) * sensitivity
                }

                val throttleInput = if (autoAccelerate) {
                    if (brakePressed) -1f else 1f
                } else {
                    if (gasPressed) 1f else if (brakePressed) -1f else 0f
                }

                // Update player physics
                playerRacer.update(
                    dt = dt,
                    throttleInput = throttleInput,
                    steerInput = steerInput,
                    driftButton = driftPressed,
                    track = track
                )

                // Check Stunt result
                playerRacer.lastStuntResult?.let {
                    stuntToast = it.stuntNameHe
                    playerRacer.lastStuntResult = null
                }

                // Update AI opponents
                aiControllers.forEach { controller ->
                    controller.update(dt, track, allRacers)
                }

                // Item box pickups
                val trackLenUnits = 1000f
                val playerSegIdx = ((playerRacer.distanceAlongTrack % trackLenUnits) / trackLenUnits * track.segments.size).toInt().coerceIn(0, track.segments.size - 1)
                if (track.segments[playerSegIdx].hasItemBox && storedPowerUp == null && !isItemBoxSpinning) {
                    isItemBoxSpinning = true
                    audioEngine.playPowerUpCollected()
                    storedPowerUp = PowerUpType.getRandom()
                    isItemBoxSpinning = false
                }

                // Lap progression & Finish check
                val totalTrackDist = trackLenUnits * track.defaultLaps
                playerRacer.currentLap = ((playerRacer.distanceAlongTrack / trackLenUnits).toInt() + 1).coerceAtMost(track.defaultLaps)

                if (playerRacer.distanceAlongTrack >= totalTrackDist && !playerRacer.raceFinished) {
                    playerRacer.raceFinished = true
                    playerRacer.finishTimeSeconds = raceTimeSeconds
                    isFinished = true
                    audioEngine.playVictoryFanfare()
                }

                // Update Audio Engine real-time params
                audioEngine.engineRpmFactor = (playerRacer.forwardSpeed / playerRacer.maxSpeed).coerceIn(0.1f, 1f)
                audioEngine.isDrifting = playerRacer.isDrifting
                audioEngine.isTurboActive = playerRacer.activeTurboTime > 0f

                frameTick++
                delay(16)
            }
        }
    }

    // Dynamic Positions Calculation
    val sortedRacers = remember(frameTick) {
        allRacers.sortedByDescending { it.distanceAlongTrack }
    }
    val playerRank = sortedRacers.indexOfFirst { it.id == "player" } + 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(track.world.skyColorBottom)
    ) {
        // 3D CANVAS VIEW
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. SKYBOX & HORIZON
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(track.world.skyColorTop, track.world.skyColorBottom),
                    startY = 0f,
                    endY = h * 0.45f
                ),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.45f)
            )

            // Ground plane
            drawRect(
                color = track.world.groundColor,
                topLeft = Offset(0f, h * 0.45f),
                size = androidx.compose.ui.geometry.Size(w, h * 0.55f)
            )

            // 2. CAMERA CHASE POSITION
            val camDistance = 5.8f
            val camHeight = 2.4f + playerRacer.height * 0.5f
            val camYaw = Math.toRadians((playerRacer.headingAngle * 0.5f).toDouble()).toFloat()
            val camPitch = 0.22f - (playerRacer.verticalVelocity * 0.015f)

            val camera = Camera3D(
                position = Vector3(
                    x = playerRacer.lateralPosition * 4f,
                    y = camHeight,
                    z = playerRacer.distanceAlongTrack - camDistance
                ),
                yaw = camYaw,
                pitch = camPitch,
                roll = Math.toRadians((playerRacer.inAirRotationRoll).toDouble()).toFloat()
            )

            // 3. RENDER 3D ROAD RIBBON
            val renderDistanceSegs = 28
            val trackUnits = 1000f
            val startDist = playerRacer.distanceAlongTrack - 5f

            for (i in 0 until renderDistanceSegs) {
                val z1 = startDist + (i * 12f)
                val z2 = z1 + 12f

                val segIdx1 = (((z1 % trackUnits) / trackUnits) * track.segments.size).toInt().coerceIn(0, track.segments.size - 1)
                val seg1 = track.segments[segIdx1]

                val roadWidth = 5.2f
                val roadColor = if (i % 2 == 0) track.world.roadColor else track.world.roadColor.copy(alpha = 0.92f)
                val curbColor = if (i % 2 == 0) Color(0xFFFF1744) else Color.White

                val p1L = camera.project(Vector3(-roadWidth, 0f, z1), w, h)
                val p1R = camera.project(Vector3(roadWidth, 0f, z1), w, h)
                val p2L = camera.project(Vector3(-roadWidth, 0f, z2), w, h)
                val p2R = camera.project(Vector3(roadWidth, 0f, z2), w, h)

                if (p1L != null && p1R != null && p2L != null && p2R != null) {
                    // Road asphalt quad
                    val roadPath = Path().apply {
                        moveTo(p1L.x, p1L.y)
                        lineTo(p1R.x, p1R.y)
                        lineTo(p2R.x, p2R.y)
                        lineTo(p2L.x, p2L.y)
                        close()
                    }
                    drawPath(roadPath, roadColor)

                    // Curbs
                    drawLine(curbColor, Offset(p1L.x, p1L.y), Offset(p2L.x, p2L.y), strokeWidth = 5f * p1L.scale)
                    drawLine(curbColor, Offset(p1R.x, p1R.y), Offset(p2R.x, p2R.y), strokeWidth = 5f * p1R.scale)

                    // Center stripes
                    if (i % 2 == 0) {
                        val c1 = camera.project(Vector3(0f, 0.02f, z1), w, h)
                        val c2 = camera.project(Vector3(0f, 0.02f, z2), w, h)
                        if (c1 != null && c2 != null) {
                            drawLine(track.world.roadStripeColor, Offset(c1.x, c1.y), Offset(c2.x, c2.y), strokeWidth = 4f * c1.scale)
                        }
                    }

                    // Boost pad visual
                    if (seg1.hasBoostPad && i % 4 == 0) {
                        val bp1 = camera.project(Vector3(-1.8f, 0.05f, z1), w, h)
                        val bp2 = camera.project(Vector3(1.8f, 0.05f, z2), w, h)
                        if (bp1 != null && bp2 != null) {
                            drawCircle(TurboCyan, radius = 18f * bp1.scale, center = Offset((bp1.x + bp2.x) * 0.5f, (bp1.y + bp2.y) * 0.5f))
                        }
                    }

                    // Ice patch visual
                    if (seg1.hasIcePatch && i % 4 == 0) {
                        val ip = camera.project(Vector3(0f, 0.05f, z1), w, h)
                        if (ip != null) {
                            drawCircle(Color(0x9980D8FF), radius = 24f * ip.scale, center = Offset(ip.x, ip.y))
                        }
                    }
                }

                // Roadside Scenery (Trees, Rocks, Towers)
                if (i % 3 == 0) {
                    val side = if (i % 6 == 0) 1 else -1
                    val sceneryPos = Vector3(side * (roadWidth + 2.8f), 0f, z1)
                    val sceneryPolys = TurboMesh3D.createScenery(track.world.sceneryItem, sceneryPos)
                    sceneryPolys.forEach { poly ->
                        val projPts = poly.vertices.mapNotNull { camera.project(it, w, h) }
                        if (projPts.size >= 3) {
                            val path = Path().apply {
                                moveTo(projPts[0].x, projPts[0].y)
                                for (k in 1 until projPts.size) lineTo(projPts[k].x, projPts[k].y)
                                close()
                            }
                            drawPath(path, poly.color)
                        }
                    }
                }

                // 3D Item Boxes
                if (seg1.hasItemBox && i % 6 == 0) {
                    val boxPos = Vector3(0f, 1.2f, z1)
                    val boxPolys = TurboMesh3D.createItemBox(boxPos, spinAngleDeg = (frameTick * 5f) % 360f)
                    boxPolys.forEach { poly ->
                        val projPts = poly.vertices.mapNotNull { camera.project(it, w, h) }
                        if (projPts.size >= 3) {
                            val path = Path().apply {
                                moveTo(projPts[0].x, projPts[0].y)
                                for (k in 1 until projPts.size) lineTo(projPts[k].x, projPts[k].y)
                                close()
                            }
                            drawPath(path, poly.color)
                        }
                    }
                }
            }

            // 4. RENDER ALL RACERS (Sorted by depth)
            val renderRacers = allRacers.map { racer ->
                val racerPos = Vector3(
                    x = racer.lateralPosition * 4f,
                    y = racer.height,
                    z = racer.distanceAlongTrack
                )
                val polys = TurboMesh3D.createCarPolygons(
                    car = racer.car,
                    center = racerPos,
                    headingAngleDeg = racer.headingAngle,
                    pitchDeg = if (racer.isAirborne) racer.inAirRotationPitch else 0f,
                    rollDeg = if (racer.isAirborne) racer.inAirRotationRoll else (racer.headingAngle * 0.2f),
                    steerAngleDeg = racer.steerAngle * 15f,
                    wheelSpinAngleDeg = racer.distanceAlongTrack * 60f,
                    isGhost = racer.activeGhostTime > 0f,
                    isShielded = racer.activeShieldTime > 0f
                )
                Pair(racer, polys)
            }

            renderRacers.flatMap { it.second }
                .sortedByDescending { it.averageDepth }
                .forEach { poly ->
                    val projPts = poly.vertices.mapNotNull { camera.project(it, w, h) }
                    if (projPts.size >= 3) {
                        val path = Path().apply {
                            moveTo(projPts[0].x, projPts[0].y)
                            for (k in 1 until projPts.size) lineTo(projPts[k].x, projPts[k].y)
                            close()
                        }
                        drawPath(path, poly.color)
                    }
                }

            // Turbo boost flame particle effect at rear
            if (playerRacer.activeTurboTime > 0f) {
                val flameProj = camera.project(
                    Vector3(playerRacer.lateralPosition * 4f, playerRacer.height + 0.6f, playerRacer.distanceAlongTrack - 1.2f),
                    w,
                    h
                )
                if (flameProj != null) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(TurboGold, TurboOrange, Color.Transparent),
                            center = Offset(flameProj.x, flameProj.y),
                            radius = 45f * flameProj.scale
                        ),
                        radius = 45f * flameProj.scale,
                        center = Offset(flameProj.x, flameProj.y)
                    )
                }
            }
        }

        // HUD OVERLAYS
        // Top HUD Bar: Lap, Position, Time, Pause
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position Badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = TurboGold,
                modifier = Modifier.shadow(6.dp, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "מקום $playerRank / 6",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }

            // Lap Counter
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface.copy(alpha = 0.85f)
            ) {
                Text(
                    text = "הקפה ${playerRacer.currentLap} / ${track.defaultLaps}",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Race Timer
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurface.copy(alpha = 0.85f)
            ) {
                val mins = (raceTimeSeconds / 60).toInt()
                val secs = (raceTimeSeconds % 60).toInt()
                val millis = ((raceTimeSeconds % 1) * 10).toInt()
                Text(
                    text = String.format("%02d:%02d.%d", mins, secs, millis),
                    style = MaterialTheme.typography.titleMedium.copy(color = TurboCyan, fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Pause Button
            IconButton(
                onClick = { isPaused = true },
                modifier = Modifier.clip(CircleShape).background(DarkSurface.copy(alpha = 0.85f)).testTag("pause_race_button")
            ) {
                Icon(Icons.Default.Pause, contentDescription = "השהה", tint = Color.White)
            }
        }

        // Speedometer (Bottom Left)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${playerRacer.forwardSpeed.toInt()}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = if (playerRacer.activeTurboTime > 0f) TurboOrange else TurboCyan,
                        fontWeight = FontWeight.Black
                    )
                )
                Text(
                    text = "קמ\"ש",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }
        }

        // Minimap Radar (Top Right below header)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface.copy(alpha = 0.75f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 50.dp, end = 16.dp)
                .size(70.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                // Circular radar ring
                drawCircle(Color(0xFF334155), style = androidx.compose.ui.graphics.drawscope.Stroke(2f))

                // Track lap dots
                val trackLen = 1000f
                allRacers.forEach { racer ->
                    val angle = ((racer.distanceAlongTrack % trackLen) / trackLen) * 2 * Math.PI - (Math.PI / 2)
                    val r = size.width * 0.38f
                    val dotX = size.width * 0.5f + (cos(angle) * r).toFloat()
                    val dotY = size.height * 0.5f + (sin(angle) * r).toFloat()

                    if (racer.isPlayer) {
                        drawCircle(TurboGold, radius = 4.5f, center = Offset(dotX, dotY))
                    } else {
                        drawCircle(TurboCyan, radius = 3f, center = Offset(dotX, dotY))
                    }
                }
            }
        }

        // Stunt Hebrew Toast Banner
        stuntToast?.let { toast ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TurboOrange.copy(alpha = 0.95f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
            ) {
                Text(
                    text = toast,
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            LaunchedEffect(toast) {
                delay(2200)
                stuntToast = null
            }
        }

        // PowerUp Button (Center-Right above gas)
        storedPowerUp?.let { powerUp ->
            Surface(
                onClick = {
                    // Activate powerup!
                    when (powerUp) {
                        PowerUpType.TURBO, PowerUpType.DOUBLE_NITRO -> playerRacer.activeTurboTime = powerUp.durationSeconds
                        PowerUpType.SHIELD -> playerRacer.activeShieldTime = powerUp.durationSeconds
                        PowerUpType.GHOST -> playerRacer.activeGhostTime = powerUp.durationSeconds
                        PowerUpType.MEGA_JUMP -> {
                            playerRacer.isAirborne = true
                            playerRacer.verticalVelocity = 18f
                        }
                        PowerUpType.LIGHTNING, PowerUpType.TORNADO -> {
                            aiRacers.forEach { it.applyHit(powerUp) }
                        }
                        PowerUpType.PAINT_MISSILE -> {
                            val ahead = aiRacers.firstOrNull { it.distanceAlongTrack > playerRacer.distanceAlongTrack }
                            ahead?.applyHit(powerUp)
                        }
                        else -> playerRacer.activeTurboTime = 2.5f
                    }
                    storedPowerUp = null
                },
                shape = CircleShape,
                color = powerUp.badgeColor,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .size(62.dp)
                    .shadow(12.dp, CircleShape)
                    .testTag("use_powerup_button")
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = powerUp.iconSymbol, fontSize = 28.sp)
                }
            }
        }

        // CONTROLS OVERLAY
        when (controlMode) {
            1 -> {
                // VIRTUAL STEERING WHEEL (Left Side)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 16.dp)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = { steeringWheelAngle = 0f },
                                onDragCancel = { steeringWheelAngle = 0f }
                            ) { change, dragAmount ->
                                change.consume()
                                steeringWheelAngle = (steeringWheelAngle + dragAmount.x * 1.5f).coerceIn(-75f, 75f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Adjust,
                        contentDescription = "הגה",
                        tint = TurboCyan,
                        modifier = Modifier.size(72.dp).rotate(steeringWheelAngle)
                    )
                }
            }
            0 -> {
                // TOUCH BUTTONS: Left & Right Steer (Left side)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ControlButton(
                        icon = Icons.Default.ArrowBack,
                        tag = "steer_left_button",
                        color = Color(0xFF1E293B),
                        sizeDp = 58
                    ) { leftPressed = it }

                    ControlButton(
                        icon = Icons.Default.ArrowForward,
                        tag = "steer_right_button",
                        color = Color(0xFF1E293B),
                        sizeDp = 58
                    ) { rightPressed = it }
                }
            }
        }

        // RIGHT CONTROLS: Gas, Brake, Drift
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Drift button
            ControlButton(
                icon = Icons.Default.FlashOn,
                tag = "drift_button",
                color = TurboPurple,
                sizeDp = 52
            ) { driftPressed = it }

            // Brake button
            ControlButton(
                icon = Icons.Default.VerticalAlignBottom,
                tag = "brake_button",
                color = Color(0xFFC62828),
                sizeDp = 54
            ) { brakePressed = it }

            // Gas button (hidden if auto-accelerate is ON)
            if (!autoAccelerate) {
                ControlButton(
                    icon = Icons.Default.VerticalAlignTop,
                    tag = "gas_button",
                    color = Color(0xFF2E7D32),
                    sizeDp = 62
                ) { gasPressed = it }
            }
        }

        // COUNTDOWN OVERLAY ("3... 2... 1... צא!")
        if (isCountingDown) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = countdownText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        color = if (countdownText == "צא!") AccentGreen else TurboGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 80.sp
                    )
                )
            }
        }

        // PAUSE DIALOG
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DarkSurface,
                    modifier = Modifier.width(320.dp).padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "המשחק מושהה", style = MaterialTheme.typography.titleLarge.copy(color = TurboGold, fontWeight = FontWeight.Bold))

                        Button(
                            onClick = { isPaused = false },
                            colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("resume_race_button")
                        ) {
                            Text(text = "המשך מרוץ", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                audioEngine.stop()
                                onExitToMenu()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("quit_race_button")
                        ) {
                            Text(text = "יציאה לתפריט הראשי", color = Color.White)
                        }
                    }
                }
            }
        }

        // RACE FINISH RESULTS OVERLAY
        if (isFinished) {
            val coinsEarned = when (playerRank) {
                1 -> 500
                2 -> 350
                3 -> 200
                4 -> 100
                5 -> 75
                else -> 50
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = DarkSurface,
                    modifier = Modifier.width(420.dp).padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (playerRank == 1) "🏆 ניצחון מושלם! מקום ראשון!" else "סיום מרוץ: מקום $playerRank",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (playerRank == 1) TurboGold else Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )

                        Text(
                            text = "זמן סיום: ${String.format("%.2f", playerRacer.finishTimeSeconds)} שניות",
                            style = MaterialTheme.typography.bodyLarge.copy(color = TurboCyan)
                        )

                        Text(
                            text = "פרס מרוץ: +$coinsEarned 🪙",
                            style = MaterialTheme.typography.titleMedium.copy(color = TurboGold, fontWeight = FontWeight.Bold)
                        )

                        // Leaderboard summary
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(DarkSurfaceElevated).padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sortedRacers.take(4).forEachIndexed { idx, racer ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${racer.nameHe}",
                                        color = if (racer.isPlayer) TurboGold else Color.White,
                                        fontWeight = if (racer.isPlayer) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (racer.isPlayer) "אתה" else "${racer.car.nameHe}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                audioEngine.stop()
                                onRaceFinished(playerRank, playerRacer.finishTimeSeconds, coinsEarned)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("continue_after_race_button")
                        ) {
                            Text(text = "המשך לתפריט", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tag: String,
    color: Color,
    sizeDp: Int,
    onPressChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.85f))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val isDown = event.changes.any { it.pressed }
                        onPressChanged(isDown)
                    }
                }
            }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size((sizeDp * 0.5f).dp))
    }
}
