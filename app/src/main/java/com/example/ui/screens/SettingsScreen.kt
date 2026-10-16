package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GamePreferences
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    var controlMode by remember { mutableStateOf(prefs.controlMode) }
    var autoAccelerate by remember { mutableStateOf(prefs.autoAccelerate) }
    var sensitivity by remember { mutableStateOf(prefs.sensitivity) }
    var musicVolume by remember { mutableStateOf(prefs.musicVolume) }
    var sfxVolume by remember { mutableStateOf(prefs.sfxVolume) }
    var graphicsQuality by remember { mutableStateOf(prefs.graphicsQuality) }
    var targetFps by remember { mutableStateOf(prefs.targetFps) }

    fun save() {
        prefs.controlMode = controlMode
        prefs.autoAccelerate = autoAccelerate
        prefs.sensitivity = sensitivity
        prefs.musicVolume = musicVolume
        prefs.sfxVolume = sfxVolume
        prefs.graphicsQuality = graphicsQuality
        prefs.targetFps = targetFps
    }

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
                onClick = {
                    save()
                    onBack()
                },
                modifier = Modifier.clip(CircleShape).background(DarkSurfaceElevated).testTag("settings_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה", tint = Color.White)
            }

            Text(
                text = "הגדרות המשחק",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TurboGold,
                    fontWeight = FontWeight.Black
                )
            )

            Icon(Icons.Default.Tune, contentDescription = null, tint = TurboCyan)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // CONTROLS SECTION
            item {
                Text(text = "מערכת שליטה באנדרואיד:", style = MaterialTheme.typography.titleMedium.copy(color = TurboCyan))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Pair(0, "כפתורי מגע 🎮"),
                        Pair(1, "הגה וירטואלי 🏎️"),
                        Pair(2, "חיישני הטיה 📱")
                    )
                    modes.forEach { (m, label) ->
                        val isSel = controlMode == m
                        Surface(
                            onClick = {
                                controlMode = m
                                save()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) TurboOrange else DarkSurface,
                            modifier = Modifier.weight(1f).border(1.dp, if (isSel) TurboGold else Color.Transparent, RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "האצה אוטומטית (פול גז):", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = autoAccelerate,
                        onCheckedChange = {
                            autoAccelerate = it
                            save()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = TurboOrange)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "רגישות היגוי: ${(sensitivity * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = sensitivity,
                        onValueChange = {
                            sensitivity = it
                            save()
                        },
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.width(180.dp),
                        colors = SliderDefaults.colors(thumbColor = TurboCyan, activeTrackColor = TurboCyan)
                    )
                }
            }

            // AUDIO SECTION
            item {
                HorizontalDivider(color = DarkSurfaceElevated)
                Spacer(Modifier.height(6.dp))
                Text(text = "עוצמת שמע ואפקטים:", style = MaterialTheme.typography.titleMedium.copy(color = TurboCyan))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "אפקטי קול ומנוע (SFX): ${(sfxVolume * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = sfxVolume,
                        onValueChange = {
                            sfxVolume = it
                            save()
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.width(180.dp),
                        colors = SliderDefaults.colors(thumbColor = TurboGold, activeTrackColor = TurboGold)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "מוזיקת רקע: ${(musicVolume * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = musicVolume,
                        onValueChange = {
                            musicVolume = it
                            save()
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.width(180.dp),
                        colors = SliderDefaults.colors(thumbColor = TurboPurple, activeTrackColor = TurboPurple)
                    )
                }
            }

            // GRAPHICS & PERFORMANCE SECTION
            item {
                HorizontalDivider(color = DarkSurfaceElevated)
                Spacer(Modifier.height(6.dp))
                Text(text = "גרפיקה וביצועים (אופטימיזציה לאנדרואיד):", style = MaterialTheme.typography.titleMedium.copy(color = TurboCyan))
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("LOW" to "נמוכה (חסכוני)", "MEDIUM" to "בינונית", "HIGH" to "גבוהה (איכות שיא)").forEach { (q, label) ->
                        val isSel = graphicsQuality == q
                        Surface(
                            onClick = {
                                graphicsQuality = q
                                save()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) TurboCyan else DarkSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30 to "30 FPS", 60 to "60 FPS (מומלץ)").forEach { (fps, label) ->
                        val isSel = targetFps == fps
                        Surface(
                            onClick = {
                                targetFps = fps
                                save()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) AccentGreen else DarkSurface,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
