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
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.model.GameMode
import com.example.model.Track
import com.example.model.TrackCatalog
import com.example.model.WorldTheme
import com.example.ui.theme.*

data class ChampionshipCup(
    val id: String,
    val nameHe: String,
    val descriptionHe: String,
    val tracks: List<Track>,
    val trophyReward: String,
    val coinPrize: Int
)

@Composable
fun ChampionshipScreen(
    getCupTrophy: (String) -> Int,
    onStartChampionshipCup: (ChampionshipCup) -> Unit,
    onBack: () -> Unit
) {
    val cups = remember {
        listOf(
            ChampionshipCup(
                id = "cup_beach_desert",
                nameHe = "גביע החופים והדיונות",
                descriptionHe = "4 מרוצים לאורך חוף הטורבו ומדבר הסערות",
                tracks = listOf(
                    TrackCatalog.getTrackById("beach_1"),
                    TrackCatalog.getTrackById("desert_1"),
                    TrackCatalog.getTrackById("beach_2"),
                    TrackCatalog.getTrackById("desert_2")
                ),
                trophyReward = "גביע החופים",
                coinPrize = 1500
            ),
            ChampionshipCup(
                id = "cup_ice_fire",
                nameHe = "גביע הקרח והאש",
                descriptionHe = "קרב איתנים בין פסגת הקרח להר הגעש הרותח",
                tracks = listOf(
                    TrackCatalog.getTrackById("ice_1"),
                    TrackCatalog.getTrackById("volcano_1"),
                    TrackCatalog.getTrackById("ice_2"),
                    TrackCatalog.getTrackById("volcano_2")
                ),
                trophyReward = "גביע הקרח והאש",
                coinPrize = 2500
            ),
            ChampionshipCup(
                id = "cup_jungle_city",
                nameHe = "גביע הפרא והאורות",
                descriptionHe = "מסלולים מהירים בג'ונגל הפראי ורחובות הניאון",
                tracks = listOf(
                    TrackCatalog.getTrackById("jungle_1"),
                    TrackCatalog.getTrackById("neon_1"),
                    TrackCatalog.getTrackById("jungle_2"),
                    TrackCatalog.getTrackById("neon_2")
                ),
                trophyReward = "גביע האורות",
                coinPrize = 3500
            ),
            ChampionshipCup(
                id = "cup_legendary",
                nameHe = "גביע האלופים האגדי",
                descriptionHe = "4 מסלולי אקסטרים: מסלול החלל, טירת המרוצים וכביש העננים",
                tracks = listOf(
                    TrackCatalog.getTrackById("space_2"),
                    TrackCatalog.getTrackById("castle_2"),
                    TrackCatalog.getTrackById("champions_3"),
                    TrackCatalog.getTrackById("clouds_3")
                ),
                trophyReward = "גביע המאסטר האגדי",
                coinPrize = 5000
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.clip(CircleShape).background(DarkSurfaceElevated).testTag("championship_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה", tint = Color.White)
            }

            Text(
                text = "אליפויות טורבו קארט",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TurboGold,
                    fontWeight = FontWeight.Black
                )
            )

            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = TurboGold)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cups) { cup ->
                val trophy = getCupTrophy(cup.id)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (trophy) {
                                            3 -> TurboGold.copy(alpha = 0.25f)
                                            2 -> Color(0xFFCFD8DC).copy(alpha = 0.25f)
                                            1 -> Color(0xFFCD7F32).copy(alpha = 0.25f)
                                            else -> DarkSurfaceElevated
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (trophy) {
                                        3 -> "🥇"
                                        2 -> "🥈"
                                        1 -> "🥉"
                                        else -> "🏆"
                                    },
                                    fontSize = 22.sp
                                )
                            }

                            Column {
                                Text(
                                    text = cup.nameHe,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = cup.descriptionHe,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "פרס זכייה: ${cup.coinPrize} 🪙",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TurboGold)
                                )
                            }
                        }

                        Button(
                            onClick = { onStartChampionshipCup(cup) },
                            colors = ButtonDefaults.buttonColors(containerColor = TurboGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("start_cup_${cup.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text(text = "התחל גביע", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
