package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GamePreferences
import com.example.model.TrackCatalog
import com.example.ui.theme.*

@Composable
fun RecordsScreen(
    prefs: GamePreferences,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape).background(DarkSurfaceElevated).testTag("records_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה", tint = Color.White)
            }

            Text(
                text = "השיאים וההישגים שלי",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TurboGold,
                    fontWeight = FontWeight.Black
                )
            )

            Icon(Icons.Default.Timer, contentDescription = null, tint = TurboCyan)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TrackCatalog.allTracks) { track ->
                val best = prefs.getBestLapTime(track.id)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = track.nameHe, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            Text(text = "${track.world.nameHe} • אורך: ${track.lengthKm} ק\"מ", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
                        }

                        if (best < 90f) {
                            Text(
                                text = "${String.format("%.2f", best)} שניות",
                                style = MaterialTheme.typography.titleMedium.copy(color = AccentGreen, fontWeight = FontWeight.Black)
                            )
                        } else {
                            Text(text = "טרם נקבע שיא", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        }
                    }
                }
            }
        }
    }
}
