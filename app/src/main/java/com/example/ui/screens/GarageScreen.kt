package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.Camera3D
import com.example.engine.TurboMesh3D
import com.example.engine.Vector3
import com.example.model.Car
import com.example.model.CarCatalog
import com.example.model.CarUpgrade
import com.example.ui.theme.*

@Composable
fun GarageScreen(
    coins: Int,
    selectedCarId: String,
    unlockedCars: Set<String>,
    getUpgrade: (String) -> CarUpgrade,
    onSelectCar: (String) -> Unit,
    onBuyCar: (Car) -> Unit,
    onUpgradeStat: (carId: String, stat: String, cost: Int) -> Unit,
    onCustomColorChanged: (carId: String, colorHex: Long) -> Unit,
    onBack: () -> Unit
) {
    var activeCarIndex by remember {
        mutableStateOf(CarCatalog.cars.indexOfFirst { it.id == selectedCarId }.coerceAtLeast(0))
    }
    val currentCar = CarCatalog.cars[activeCarIndex]
    val isUnlocked = unlockedCars.contains(currentCar.id)
    val isSelected = currentCar.id == selectedCarId
    val currentUpgrade = getUpgrade(currentCar.id)

    // 3D Car rotation angle (user dragable)
    var rotationAngle by remember { mutableStateOf(45f) }

    // Color options for customization
    val customPalette = listOf(
        Color(0xFFFF9800), Color(0xFFE53935), Color(0xFF00E5FF),
        Color(0xFF76FF03), Color(0xFF9C27B0), Color(0xFFFFD700),
        Color(0xFFFFFFFF), Color(0xFF212121), Color(0xFF1E88E5)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .testTag("garage_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה", tint = Color.White)
            }

            Text(
                text = "המוסך של טורבו קארט",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TurboGold,
                    fontWeight = FontWeight.Black
                )
            )

            // Coin Balance
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "$coins", style = MaterialTheme.typography.titleMedium.copy(color = TurboGold, fontWeight = FontWeight.Bold))
                    Text(text = "🪙", fontSize = 16.sp)
                }
            }
        }

        // MAIN CONTENT (Split: Left = 3D Turntable, Right = Specs & Upgrades)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LEFT PANEL: 3D Car Inspector
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotationAngle -= dragAmount.x * 0.6f
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val camera = Camera3D(
                        position = Vector3(0f, 2.5f, -5.2f),
                        fovDegrees = 52f,
                        pitch = 0.28f
                    )

                    // Platform grid
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(TurboCyan.copy(alpha = 0.25f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.7f),
                            radius = w * 0.4f
                        ),
                        radius = w * 0.4f,
                        center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.7f)
                    )

                    val paintColor = if (currentUpgrade.customColorHex != null) {
                        Color(currentUpgrade.customColorHex)
                    } else {
                        currentCar.bodyColor
                    }

                    val polys = TurboMesh3D.createCarPolygons(
                        car = currentCar,
                        center = Vector3(0f, 0f, 0f),
                        headingAngleDeg = rotationAngle,
                        primaryColor = paintColor
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

                // Drag instruction hint
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "גרור כדי לסובב 360°", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                }

                // Car Name & Price overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = currentCar.nameHe,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(
                        text = currentCar.taglineHe,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TurboCyan)
                    )
                }
            }

            // RIGHT PANEL: Specs, Upgrades, Color Customizer
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // STAT BARS
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatBar("מהירות", currentCar.baseSpeed + currentUpgrade.engineLevel * 0.05f, TurboOrange)
                    StatBar("תאוצה", currentCar.baseAccel + currentUpgrade.engineLevel * 0.04f, TurboGold)
                    StatBar("שליטה", currentCar.baseHandling + currentUpgrade.handlingLevel * 0.05f, TurboCyan)
                    StatBar("טורבו", currentCar.baseTurbo + currentUpgrade.turboLevel * 0.05f, TurboPurple)
                    StatBar("יציבות", currentCar.baseStability + currentUpgrade.bodyLevel * 0.04f, AccentGreen)
                }

                // COLOR PALETTE
                Column {
                    Text(text = "צבע מרכב מותאם אישית:", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        customPalette.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .clickable {
                                        onCustomColorChanged(currentCar.id, col.value.toLong())
                                    }
                            )
                        }
                    }
                }

                // UPGRADES SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UpgradeButton("מנוע", currentUpgrade.engineLevel, Modifier.weight(1f)) {
                        onUpgradeStat(currentCar.id, "engine", (currentUpgrade.engineLevel + 1) * 350)
                    }
                    UpgradeButton("שליטה", currentUpgrade.handlingLevel, Modifier.weight(1f)) {
                        onUpgradeStat(currentCar.id, "handling", (currentUpgrade.handlingLevel + 1) * 300)
                    }
                    UpgradeButton("טורבו", currentUpgrade.turboLevel, Modifier.weight(1f)) {
                        onUpgradeStat(currentCar.id, "turbo", (currentUpgrade.turboLevel + 1) * 400)
                    }
                    UpgradeButton("שלדה", currentUpgrade.bodyLevel, Modifier.weight(1f)) {
                        onUpgradeStat(currentCar.id, "body", (currentUpgrade.bodyLevel + 1) * 250)
                    }
                }

                // ACTION: Select or Buy
                if (isUnlocked) {
                    Button(
                        onClick = { onSelectCar(currentCar.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF2E7D32) else TurboOrange
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("select_car_button")
                    ) {
                        Icon(if (isSelected) Icons.Default.Check else Icons.Default.DirectionsCar, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isSelected) "המכונית הנבחרת למרוץ" else "בחר מכונית זו",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = { onBuyCar(currentCar) },
                        enabled = coins >= currentCar.price,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TurboGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("buy_car_button")
                    ) {
                        Text(
                            text = "פתח מכונית ב-${currentCar.price} 🪙",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // BOTTOM CAROUSEL OF ALL 20 CARS
        LazyRow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(CarCatalog.cars) { car ->
                val unlocked = unlockedCars.contains(car.id)
                val isActive = car.id == currentCar.id
                val chosen = car.id == selectedCarId

                Surface(
                    onClick = {
                        activeCarIndex = CarCatalog.cars.indexOfFirst { it.id == car.id }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isActive) DarkSurfaceElevated else DarkSurface,
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                        .border(
                            width = if (isActive) 2.dp else if (chosen) 1.dp else 0.dp,
                            color = if (isActive) TurboCyan else if (chosen) TurboGold else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = car.nameHe,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (unlocked) Color.White else TextSecondary,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!unlocked) {
                                Icon(Icons.Default.Lock, contentDescription = "נעול", tint = TurboGold, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(2.dp))
                                Text(text = "${car.price}", fontSize = 10.sp, color = TurboGold)
                            } else if (chosen) {
                                Text(text = "בשימוש ★", fontSize = 10.sp, color = AccentGreen, fontWeight = FontWeight.Bold)
                            } else {
                                Text(text = "נרכש ✓", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBar(title: String, value: Float, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall.copy(color = Color.White), modifier = Modifier.width(45.dp))
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
            modifier = Modifier.width(32.dp)
        )
    }
}

@Composable
private fun UpgradeButton(title: String, level: Int, modifier: Modifier = Modifier, onUpgrade: () -> Unit) {
    Surface(
        onClick = onUpgrade,
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
            Text(text = "★".repeat(level) + "☆".repeat(5 - level), color = TurboGold, fontSize = 10.sp)
            Text(text = "+שדרג", color = TurboCyan, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold))
        }
    }
}
