package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Song

@Composable
fun MusicPlayerController(
    currentSong: Song?,
    isPlaying: Boolean,
    progress: Float,
    waves: List<Float>,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    var showLyricsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("music_player_controller")
    ) {
        // Main glass bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xEB140D2A),
            tonalElevation = 12.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Linear Progress bar along top edge
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = Color(0xFFF9D142),
                    trackColor = Color(0x22FFFFFF)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small vinyl style circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF331D6B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lyrics,
                            contentDescription = "Song",
                            tint = Color(0xFFF9D142)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Description
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("player_song_title")
                        )
                        Text(
                            text = "${currentSong.genre} • ${currentSong.singerVoice}",
                            color = Color(0xFF9E93B3),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Mini Visualizer (only if playing)
                    if (isPlaying) {
                        Row(
                            modifier = Modifier
                                .height(24.dp)
                                .width(36.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            waves.take(4).forEach { waveVal ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(waveVal.coerceIn(0.1f, 1f))
                                        .background(Color(0xFFF9D142), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Lyrics button
                    IconButton(
                        onClick = { showLyricsDialog = true },
                        modifier = Modifier.testTag("lyrics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lyrics,
                            contentDescription = "Show Lyrics",
                            tint = Color(0xFF9F75FF)
                        )
                    }

                    // Play/Pause icon button
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF9F75FF).copy(alpha = 0.2f))
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color(0xFFF9D142)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Stop/Close button
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.testTag("player_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Player",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }

    // Lyrics Sing Along Dialog
    if (showLyricsDialog) {
        Dialog(onDismissRequest = { showLyricsDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clip(RoundedCornerShape(28.dp)),
                color = Color(0xFF140D2A),
                border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentSong.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sing-along Karaoke Player Mode",
                                color = Color(0xFFF9D142),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { showLyricsDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0x1AFFFFFF))

                    // Bouncing studio wave representation inside lyrics viewer
                    WaveformVisualizer(
                        waves = waves,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF09041A))
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = currentSong.lyrics,
                                color = Color(0xFFF1EEF7),
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color(0xFFF9D142))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color(0xFF09041A),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
