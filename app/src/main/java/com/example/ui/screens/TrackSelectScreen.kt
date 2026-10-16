package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.Track
import com.example.model.TrackCatalog
import com.example.model.WorldTheme
import com.example.ui.theme.*

@Composable
fun TrackSelectScreen(
    onTrackSelected: (Track, GameMode) -> Unit,
    getBestLap: (String) -> Float,
    onBack: () -> Unit
) {
    var selectedWorld by remember { mutableStateOf(WorldTheme.BEACH) }
    var selectedGameMode by remember { mutableStateOf(GameMode.QUICK_RACE) }

    val currentWorldTracks = remember(selectedWorld) {
        TrackCatalog.allTracks.filter { it.world == selectedWorld }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape).background(DarkSurfaceElevated).testTag("track_select_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה", tint = Color.White)
            }

            Text(
                text = "בחירת מסלול ומצב מרוץ",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TurboGold,
                    fontWeight = FontWeight.Black
                )
            )

            // Game Mode Dropdown / Selector Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = TurboPurple.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, TurboPurple)
            ) {
                Text(
                    text = selectedGameMode.nameHe,
                    style = MaterialTheme.typography.labelLarge.copy(color = TurboCyan),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, bottom = 12.dp, start = 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LEFT COLUMN: 12 Worlds selection
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(8.dp)
            ) {
                Text(
                    text = "12 העולמות של טורבו:",
                    style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary, fontSize = 11.sp),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(WorldTheme.values()) { world ->
                        val isSel = world == selectedWorld
                        Surface(
                            onClick = { selectedWorld = world },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) DarkSurfaceElevated else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSel) 1.5.dp else 0.dp,
                                    color = if (isSel) TurboCyan else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(world.skyColorTop)
                                )
                                Text(
                                    text = world.nameHe,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = if (isSel) Color.White else TextSecondary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // RIGHT COLUMN: 3 Tracks in World + Game Mode Picker
            Column(
                modifier = Modifier
                    .weight(1.8f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Game Mode selector pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GameMode.values()) { mode ->
                        val isSel = mode == selectedGameMode
                        Surface(
                            onClick = { selectedGameMode = mode },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) TurboOrange else DarkSurface,
                            modifier = Modifier.border(
                                width = if (isSel) 1.dp else 0.dp,
                                color = TurboGold,
                                shape = RoundedCornerShape(12.dp)
                            )
                        ) {
                            Text(
                                text = mode.nameHe,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 3 Tracks Cards
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(currentWorldTracks) { track ->
                        val bestLap = getBestLap(track.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = track.nameHe,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (track.difficultyHe) {
                                                "קל" -> Color(0xFF2E7D32)
                                                "קשה" -> Color(0xFFC62828)
                                                else -> Color(0xFFE65100)
                                            }
                                        ) {
                                            Text(
                                                text = track.difficultyHe,
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = track.descriptionHe,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                    )

                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "אורך: ${track.lengthKm} ק\"מ", fontSize = 11.sp, color = TurboCyan)
                                        Text(text = "מזג אוויר: ${track.weather.nameHe}", fontSize = 11.sp, color = TurboGold)
                                        if (bestLap < 90f) {
                                            Text(text = "שיא: ${String.format("%.2f", bestLap)}ש'", fontSize = 11.sp, color = AccentGreen)
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onTrackSelected(track, selectedGameMode) },
                                    colors = ButtonDefaults.buttonColors(containerColor = TurboOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.padding(start = 8.dp).testTag("race_track_${track.id}")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(text = "מרוץ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
