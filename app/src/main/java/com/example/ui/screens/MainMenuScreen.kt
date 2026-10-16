package com.example.ui.screens

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.Camera3D
import com.example.engine.TurboMesh3D
import com.example.engine.Vector3
import com.example.model.CarCatalog
import com.example.ui.theme.*

@Composable
fun MainMenuScreen(
    coins: Int,
    selectedCarId: String,
    onStartRace: () -> Unit,
    onChampionships: () -> Unit,
    onOpenGarage: () -> Unit,
    onSelectTrack: () -> Unit,
    onTrackEditor: () -> Unit,
    onRecords: () -> Unit,
    onSettings: () -> Unit
) {
    val selectedCar = remember(selectedCarId) { CarCatalog.getCarById(selectedCarId) }

    var manualRotation by remember { mutableStateOf(35f) }
    val infiniteTransition = rememberInfiniteTransition(label = "idle_car_rotation")
    val idleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idle_angle"
    )

    val currentRotation = manualRotation + idleAngle

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0F1D),
                        Color(0xFF131C31),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        // TOP HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "טורבו קארט",
                    style = MaterialTheme.typography.displayLarge.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(TurboOrange, TurboGold, TurboCyan)
                        ),
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "מרוצים בקצב אחר",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TurboCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }

            // Coin Balance Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, TurboGold.copy(alpha = 0.5f)),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$coins",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TurboGold,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(text = "🪙", fontSize = 18.sp)
                }
            }
        }

        // MAIN CONTENT AREA: Left = 3D Car Showcase Stage, Right = Buttons Menu
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 58.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LEFT HALF: 3D Car Interactive Showcase Stage
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0A0F1D).copy(alpha = 0.7f))
                    .border(1.dp, Color(0xFF1E2B4A), RoundedCornerShape(20.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            manualRotation -= dragAmount.x * 0.7f
                        }
                    }
            ) {
                // 3D Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val camera = Camera3D(
                        position = Vector3(0f, 2.0f, -4.8f),
                        fovDegrees = 52f,
                        pitch = 0.22f
                    )

                    // Platform glowing ring
                    val stageCenter = Offset(w * 0.5f, h * 0.72f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(TurboCyan.copy(alpha = 0.35f), Color.Transparent),
                            center = stageCenter,
                            radius = w * 0.38f
                        ),
                        radius = w * 0.38f,
                        center = stageCenter
                    )

                    val polys = TurboMesh3D.createCarPolygons(
                        car = selectedCar,
                        center = Vector3(0f, 0f, 0f),
                        headingAngleDeg = currentRotation,
                        wheelSpinAngleDeg = currentRotation * 2f
                    ).sortedByDescending { it.averageDepth }

                    polys.forEach { poly ->
                        val projected = poly.vertices.mapNotNull { camera.project(it, w, h) }
                        if (projected.size >= 3) {
                            val path = Path().apply {
                                moveTo(projected[0].x, projected[0].y)
                                for (i in 1 until projected.size) {
                                    lineTo(projected[i].x, projected[i].y)
                                }
                                close()
                            }
                            drawPath(path, poly.color)
                        }
                    }
                }

                // Car info badge overlay
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(
                            text = selectedCar.nameHe,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            text = selectedCar.taglineHe,
                            style = MaterialTheme.typography.bodySmall.copy(color = TurboCyan, fontSize = 11.sp)
                        )
                    }
                }

                // Switch car button in garage
                Button(
                    onClick = onOpenGarage,
                    colors = ButtonDefaults.buttonColors(containerColor = TurboCyan),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "החלף במוסך", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // RIGHT HALF: Menu Action Buttons
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // BIG PRIMARY BUTTON: START RACE
                Button(
                    onClick = onStartRace,
                    colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(10.dp, RoundedCornerShape(14.dp))
                        .testTag("start_race_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "התחל מרוץ", tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "התחל מרוץ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }

                // ROW 1: Championships & Garage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MenuCard(
                        title = "אליפויות",
                        subtitle = "גביעים ופרסים",
                        icon = Icons.Default.EmojiEvents,
                        accentColor = TurboGold,
                        modifier = Modifier.weight(1f).testTag("championships_button"),
                        onClick = onChampionships
                    )
                    MenuCard(
                        title = "המוסך שלי",
                        subtitle = "20 מכוניות ושדרוגים",
                        icon = Icons.Default.DirectionsCar,
                        accentColor = TurboCyan,
                        modifier = Modifier.weight(1f).testTag("garage_button"),
                        onClick = onOpenGarage
                    )
                }

                // ROW 2: Track Select & Track Editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MenuCard(
                        title = "בחירת מסלול",
                        subtitle = "36 מסלולים",
                        icon = Icons.Default.Map,
                        accentColor = AccentGreen,
                        modifier = Modifier.weight(1f).testTag("track_select_button"),
                        onClick = onSelectTrack
                    )
                    MenuCard(
                        title = "עורך מסלולים",
                        subtitle = "בנה מסלול מקורי",
                        icon = Icons.Default.Construction,
                        accentColor = TurboPurple,
                        modifier = Modifier.weight(1f).testTag("track_editor_button"),
                        onClick = onTrackEditor
                    )
                }

                // ROW 3: Records & Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MenuCard(
                        title = "השיאים שלי",
                        subtitle = "זמני הקפות",
                        icon = Icons.Default.Timer,
                        accentColor = Color(0xFFFF4081),
                        modifier = Modifier.weight(1f).testTag("records_button"),
                        onClick = onRecords
                    )
                    MenuCard(
                        title = "הגדרות",
                        subtitle = "שליטה ושמע",
                        icon = Icons.Default.Settings,
                        accentColor = Color(0xFFB0BEC5),
                        modifier = Modifier.weight(1f).testTag("settings_button"),
                        onClick = onSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2B4A)),
        modifier = modifier.height(58.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 9.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
