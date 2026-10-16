package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.Track
import com.example.model.TrackSegment
import com.example.model.WorldTheme
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

enum class EditorPieceType(val nameHe: String, val icon: String, val segment: TrackSegment) {
    STRAIGHT("ישר", "⬆️", TrackSegment(curve = 0f, hill = 0f, length = 15)),
    TURN_LEFT("פנייה שמאלה", "⬅️", TrackSegment(curve = -0.6f, hill = 0f, length = 20)),
    TURN_RIGHT("פנייה ימינה", "➡️", TrackSegment(curve = 0.6f, hill = 0f, length = 20)),
    RAMP("רמפת קפיצה", "🚀", TrackSegment(curve = 0f, hill = 0.6f, hasRamp = true, length = 18)),
    ITEM_BOXES("קופסאות הפתעה", "🎁", TrackSegment(curve = 0f, hill = 0f, hasItemBox = true, length = 14)),
    BOOST_PAD("פד האצה", "⚡", TrackSegment(curve = 0f, hill = 0f, hasBoostPad = true, length = 14)),
    ICE_TRAP("משטח קרח", "❄️", TrackSegment(curve = 0f, hill = 0f, hasIcePatch = true, length = 16)),
    OBSTACLE("מחסום סלעים", "🚧", TrackSegment(curve = 0f, hill = 0f, hasObstacle = true, obstacleOffset = 0f, length = 16))
}

@Composable
fun TrackEditorScreen(
    onTestDrive: (Track) -> Unit,
    onBack: () -> Unit
) {
    var trackName by remember { mutableStateOf("המסלול שלי") }
    var selectedTheme by remember { mutableStateOf(WorldTheme.BEACH) }
    val segments = remember {
        mutableStateListOf(
            TrackSegment(curve = 0f, length = 15),
            TrackSegment(curve = 0f, hasItemBox = true, length = 12),
            TrackSegment(curve = 0.5f, length = 20),
            TrackSegment(curve = 0f, hasRamp = true, length = 18),
            TrackSegment(curve = -0.5f, hasBoostPad = true, length = 20),
            TrackSegment(curve = 0f, length = 15)
        )
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
                onClick = onBack,
                modifier = Modifier.clip(CircleShape).background(DarkSurfaceElevated).testTag("editor_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה", tint = Color.White)
            }

            Text(
                text = "עורך המסלולים של טורבו קארט",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TurboGold,
                    fontWeight = FontWeight.Black
                )
            )

            // Test Drive button
            Button(
                onClick = {
                    val fullSegments = mutableListOf<TrackSegment>()
                    fullSegments.add(TrackSegment(curve = 0f, length = 15)) // start line
                    fullSegments.addAll(segments)
                    fullSegments.add(TrackSegment(curve = 0f, length = 15)) // finish line
                    val customTrack = Track(
                        id = "custom_${System.currentTimeMillis()}",
                        world = selectedTheme,
                        trackNumber = 1,
                        nameHe = trackName,
                        descriptionHe = "מסלול מקורי שנבנה בעורך המסלולים",
                        defaultLaps = 2,
                        segments = fullSegments
                    )
                    onTestDrive(customTrack)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("test_drive_custom_track_button")
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(6.dp))
                Text(text = "נסיעת מבחן בתלת־ממד", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // MAIN CONTENT
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 54.dp, bottom = 80.dp, start = 14.dp, end = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LEFT: 2D Track Preview Canvas
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "מפת המסלול (${segments.size} קטעים)", style = MaterialTheme.typography.labelLarge.copy(color = Color.White))
                        IconButton(onClick = { if (segments.isNotEmpty()) segments.removeAt(segments.size - 1) }) {
                            Icon(Icons.Default.Delete, contentDescription = "מחק קטע אחרון", tint = Color(0xFFFF5252))
                        }
                    }

                    // Draw procedural track path in 2D
                    Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val centerX = size.width * 0.5f
                        val centerY = size.height * 0.75f
                        var curX = centerX
                        var curY = centerY
                        var angle = -90.0 // facing up

                        drawCircle(Color(0xFF00E676), radius = 6f, center = Offset(curX, curY))

                        segments.forEachIndexed { idx, seg ->
                            val turnAngle = seg.curve * 30.0
                            val segLen = seg.length * 1.5f
                            angle += turnAngle

                            val nextX = curX + (cos(Math.toRadians(angle)) * segLen).toFloat()
                            val nextY = curY + (sin(Math.toRadians(angle)) * segLen).toFloat()

                            val segCol = when {
                                seg.hasRamp -> TurboOrange
                                seg.hasBoostPad -> TurboCyan
                                seg.hasItemBox -> TurboGold
                                seg.hasIcePatch -> Color(0xFF80D8FF)
                                seg.hasObstacle -> Color(0xFFFF5252)
                                else -> Color(0xFFCFD8DC)
                            }

                            drawLine(
                                color = segCol,
                                start = Offset(curX, curY),
                                end = Offset(nextX, nextY),
                                strokeWidth = 8f
                            )

                            curX = nextX
                            curY = nextY
                        }

                        // Finish line mark
                        drawCircle(Color(0xFFFF1744), radius = 7f, center = Offset(curX, curY))
                    }
                }
            }

            // RIGHT: Theme & Track Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "הגדרות המסלול:", style = MaterialTheme.typography.titleMedium.copy(color = TurboGold))

                OutlinedTextField(
                    value = trackName,
                    onValueChange = { trackName = it },
                    label = { Text("שם המסלול") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "בחר עולם רקע:", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WorldTheme.values()) { world ->
                        val isSel = world == selectedTheme
                        Surface(
                            onClick = { selectedTheme = world },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) TurboCyan else DarkSurfaceElevated,
                            modifier = Modifier.border(1.dp, if (isSel) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = world.nameHe,
                                fontSize = 11.sp,
                                color = if (isSel) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM TOOLBAR: Piece Palette
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
            items(EditorPieceType.values()) { piece ->
                Surface(
                    onClick = { segments.add(piece.segment) },
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(105.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = piece.icon, fontSize = 16.sp)
                        Text(
                            text = piece.nameHe,
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
