package com.example.ui.screens.library

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Song
import com.example.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectLibraryScreen(
    musicViewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allSongs by musicViewModel.allSongs.collectAsState()
    val favoriteSongs by musicViewModel.favoriteSongs.collectAsState()
    val downloadedSongs by musicViewModel.downloadedSongs.collectAsState()
    val projects by musicViewModel.projects.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val currentPlayingSong by musicViewModel.currentPlayingSong.collectAsState()

    var activeTab by remember { mutableStateOf("All") } // "All", "Favs", "Offline", "Folders"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Library", fontWeight = FontWeight.Black, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("project_library_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF140D2A))
                    .padding(4.dp)
            ) {
                listOf("All", "Favorites", "Offline", "Folders").forEach { tab ->
                    val active = (tab == "All" && activeTab == "All") ||
                               (tab == "Favorites" && activeTab == "Favs") ||
                               (tab == "Offline" && activeTab == "Offline") ||
                               (tab == "Folders" && activeTab == "Folders")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (active) Color(0xFF9F75FF) else Color.Transparent)
                            .clickable {
                                activeTab = when (tab) {
                                    "Favorites" -> "Favs"
                                    "Offline" -> "Offline"
                                    "Folders" -> "Folders"
                                    else -> "All"
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (active) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Target List selection
            val filteredSongs = when (activeTab) {
                "Favs" -> favoriteSongs
                "Offline" -> downloadedSongs
                else -> allSongs
            }

            if (activeTab == "Folders") {
                // Folders display
                if (projects.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.FolderOpen, contentDescription = "Empty", tint = Color(0x33FFFFFF), modifier = Modifier.size(56.dp))
                            Text("No folders configured", color = Color(0xFF9E93B3), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Create folders on the main Home dashboard.", color = Color(0x55FFFFFF), fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(projects) { project ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Filled.Folder, contentDescription = "Folder", tint = Color(0xFFF9D142), modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(project.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(project.description, color = Color(0xFF9E93B3), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(onClick = { musicViewModel.deleteProjectFolder(project.id) }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEFB8C8))
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(120.dp)) }
                    }
                }
            } else {
                // Songs display
                if (filteredSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.QueueMusic, contentDescription = "Empty", tint = Color(0x33FFFFFF), modifier = Modifier.size(56.dp))
                            val emptyMsg = when (activeTab) {
                                "Favs" -> "No favorite songs yet"
                                "Offline" -> "No offline downloads"
                                else -> "No creations found"
                            }
                            Text(emptyMsg, color = Color(0xFF9E93B3), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Create a song to expand your creative archive.", color = Color(0x55FFFFFF), fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSongs) { song ->
                            val isCurrent = currentPlayingSong?.id == song.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("library_song_card_${song.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) Color(0xFF1C1337) else Color(0xFF140D2A)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCurrent) Color(0xFFF9D142).copy(alpha = 0.5f) else Color(0xFF2E244E)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { musicViewModel.togglePlayback(song) },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isCurrent && isPlaying) Color(0xFFF9D142) else Color(0xFF9F75FF).copy(alpha = 0.15f)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isCurrent && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = "Play",
                                            tint = if (isCurrent && isPlaying) Color.Black else Color(0xFFF9D142)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${song.genre} • ${song.singerVoice}",
                                            color = Color(0xFF9E93B3),
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Action buttons row
                                    IconButton(onClick = {
                                        musicViewModel.toggleDownload(song)
                                        val msg = if (song.isDownloaded) "Removed from local storage" else "Saved offline!"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(
                                            imageVector = if (song.isDownloaded) Icons.Filled.FileDownloadDone else Icons.Filled.FileDownload,
                                            contentDescription = "Download",
                                            tint = if (song.isDownloaded) Color(0xFFF9D142) else Color(0xFF9E93B3)
                                        )
                                    }

                                    IconButton(onClick = { musicViewModel.toggleFavorite(song) }) {
                                        Icon(
                                            imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Fav",
                                            tint = if (song.isFavorite) Color(0xFFF9D142) else Color(0xFF9E93B3)
                                        )
                                    }

                                    IconButton(onClick = { musicViewModel.deleteSong(song.id) }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEFB8C8))
                                    }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(120.dp)) }
                    }
                }
            }
        }
    }
}
