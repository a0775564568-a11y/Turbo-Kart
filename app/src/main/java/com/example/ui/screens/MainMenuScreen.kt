package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // 3D Car turntable animation
    val infiniteTransition = rememberInfiniteTransition(label = "car_turntable")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "car_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        // 3D Turntable Car Background View
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val camera = Camera3D(
                position = Vector3(0f, 2.2f, -5.2f),
                fovDegrees = 55f,
                pitch = 0.2f
            )

            // Draw glowing ground platform
            val groundY = h * 0.72f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TurboCyan.copy(alpha = 0.25f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(w * 0.65f, groundY),
                    radius = w * 0.35f
                ),
                radius = w * 0.35f,
                center = androidx.compose.ui.geometry.Offset(w * 0.65f, groundY)
            )

            // Render rotating 3D car at offset
            val carCenter = Vector3(1.2f, 0f, 0f)
            val polys = TurboMesh3D.createCarPolygons(
                car = selectedCar,
                center = carCenter,
                headingAngleDeg = rotationAngle,
                wheelSpinAngleDeg = rotationAngle * 2f
            ).sortedByDescending { it.averageDepth }

            polys.forEach { poly ->
                val projected = poly.vertices.mapNotNull { camera.project(it, w, h) }
                if (projected.size >= 3) {
                    val path = androidx.compose.ui.graphics.Path().apply {
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

        // Header bar: Title, Tagline, and Coins
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
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
                        fontSize = 36.sp
                    )
                )
                Text(
                    text = "מרוצים בקצב אחר",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TurboCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            // Coin Balance Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$coins",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TurboGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(text = "🪙", fontSize = 18.sp)
                }
            }
        }

        // Left Navigation Menu Panel
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp, top = 60.dp, bottom = 16.dp)
                .width(280.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Big primary Start Race button
            Button(
                onClick = onStartRace,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TurboOrange
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .testTag("start_race_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "התחל מרוץ", tint = Color.White)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuGridItem(
                    title = "אליפויות",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = TurboGold,
                    modifier = Modifier.weight(1f).testTag("championships_button"),
                    onClick = onChampionships
                )
                MenuGridItem(
                    title = "המוסך שלי",
                    icon = Icons.Default.DirectionsCar,
                    accentColor = TurboCyan,
                    modifier = Modifier.weight(1f).testTag("garage_button"),
                    onClick = onOpenGarage
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuGridItem(
                    title = "בחירת מסלול",
                    icon = Icons.Default.Map,
                    accentColor = Color(0xFF00E676),
                    modifier = Modifier.weight(1f).testTag("track_select_button"),
                    onClick = onSelectTrack
                )
                MenuGridItem(
                    title = "עורך מסלולים",
                    icon = Icons.Default.Construction,
                    accentColor = TurboPurple,
                    modifier = Modifier.weight(1f).testTag("track_editor_button"),
                    onClick = onTrackEditor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuGridItem(
                    title = "השיאים שלי",
                    icon = Icons.Default.Timer,
                    accentColor = Color(0xFFFF4081),
                    modifier = Modifier.weight(1f).testTag("records_button"),
                    onClick = onRecords
                )
                MenuGridItem(
                    title = "הגדרות",
                    icon = Icons.Default.Settings,
                    accentColor = Color(0xFFB0BEC5),
                    modifier = Modifier.weight(1f).testTag("settings_button"),
                    onClick = onSettings
                )
            }
        }

        // Active Car badge on the bottom right
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .clickable { onOpenGarage() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = "שדרג", tint = TurboCyan)
                Column {
                    Text(
                        text = selectedCar.nameHe,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = selectedCar.taglineHe,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuGridItem(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.85f),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                maxLines = 1
            )
        }
    }
}
