package com.example.ui.screens.arrangement

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.arrangement.*
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.ArrangementViewModel
import com.example.ui.viewmodel.ArrangementGenerationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIArrangementScreen(
    viewModel: ArrangementViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val allProjects by viewModel.allProjects.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()
    val playbackTimeSec by viewModel.playbackTimeSeconds.collectAsState()
    val activeSecIndex by viewModel.activeSectionIndex.collectAsState()
    val errorMsg by viewModel.error.collectAsState()
    val exportedTextResult by viewModel.exportedText.collectAsState()

    // Integrations list
    val lyricsList by viewModel.lyricsProjects.collectAsState()
    val melodyList by viewModel.melodyProjects.collectAsState()
    val chordList by viewModel.chordProjects.collectAsState()

    // Composer memory
    val favArrStyle by viewModel.favArrStyle.collectAsState()
    val favInsts by viewModel.favInsts.collectAsState()
    val favTrackCount by viewModel.favTrackCount.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormatSelected by remember { mutableStateOf("") }
    var showCustomTrackDialog by remember { mutableStateOf(false) }

    // Dialog form values
    var dTitle by remember { mutableStateOf("") }
    var dLyrics by remember { mutableStateOf("") }
    var dPrompt by remember { mutableStateOf("") }
    var dGenre by remember { mutableStateOf("Bollywood Pop") }
    var dMood by remember { mutableStateOf("Uplifting") }
    var dEmotion by remember { mutableStateOf("Happy") }
    var dBpm by remember { mutableStateOf(105) }
    var dKey by remember { mutableStateOf("C") }
    var dScale by remember { mutableStateOf("Major") }
    var dRaga by remember { mutableStateOf("Bhupali") }
    var dDuration by remember { mutableStateOf(180) }
    var dSinger by remember { mutableStateOf("Male Solo") }
    var dLanguage by remember { mutableStateOf("Hindi") }
    var dAudience by remember { mutableStateOf("Global Fusion Lovers") }
    var dStructure by remember { mutableStateOf("Bollywood Song Structure") }

    // Linked IDs
    var dLyricsId by remember { mutableStateOf<String?>(null) }
    var dMelodyId by remember { mutableStateOf<String?>(null) }
    var dChordId by remember { mutableStateOf<String?>(null) }

    // Custom track values
    var customTrackName by remember { mutableStateOf("") }
    var customTrackColor by remember { mutableStateOf("#4CAF50") }

    // Memory edit values
    var editStyle by remember { mutableStateOf("") }
    var editInsts by remember { mutableStateOf("") }
    var editTrackCount by remember { mutableStateOf("") }

    LaunchedEffect(favArrStyle, favInsts, favTrackCount) {
        editStyle = favArrStyle
        editInsts = favInsts
        editTrackCount = favTrackCount
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI ARRANGEMENT WORKSPACE",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "SurMaya Music OS • Digital Music Director",
                            fontSize = 9.sp,
                            color = Color(0xFFF9D142),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentProject != null) {
                                viewModel.stopPlayback()
                                @Suppress("UNCHECKED_CAST")
                                val nullProj: ArrangementProject? = null
                                viewModel.selectProject(nullProj as ArrangementProject)
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("arrangement_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (currentProject != null) {
                        IconButton(
                            onClick = {
                                if (isPlaying) viewModel.stopPlayback() else viewModel.startPlayback()
                            },
                            modifier = Modifier.testTag("arr_play_pause")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Stop",
                                tint = if (isPlaying) Color(0xFFF9D142) else Color.White
                            )
                        }
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("arr_export_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Export Session",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showMemoryDialog = true },
                            modifier = Modifier.testTag("composer_memory_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = "Composer Memory",
                                tint = Color(0xFFF9D142)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF09041A)
                )
            )
        },
        containerColor = Color(0xFF09041A)
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF09041A), Color(0xFF140D2A))
                    )
                )
        ) {
            if (currentProject == null) {
                // Landing Screen: Projects List & Onboarding Card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "Arrangement Engine",
                                    tint = Color(0xFFF9D142),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Arrangement Director",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Transform mere chord progressions and melody blocks into high-fidelity song arrangements. Plan structural sections, orchestrate dynamic curves, automate parameters, and construct complete evaluation blueprints for downstream singers and mixing systems.",
                                fontSize = 12.sp,
                                color = Color(0xFFB0A7CC),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            GlowingButton(
                                text = "CREATE ARRANGEMENT PLAN",
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "create_arr_plan_btn"
                            )
                        }
                    }

                    // Active Composer Memory Stats
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Memory,
                                    contentDescription = "Memory",
                                    tint = Color(0xFF9F75FF),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Active Composer Memory",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                Text("Preferred Style:", fontSize = 11.sp, color = Color(0xFF817799))
                                Text(favArrStyle, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                            ) {
                                Text("Instrument Bundle:", fontSize = 11.sp, color = Color(0xFF817799))
                                Text(favInsts, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Preferred Tracks Limit:", fontSize = 11.sp, color = Color(0xFF817799))
                                Text(favTrackCount, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "YOUR ARRANGEMENT ARCHIVES",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF9F75FF),
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )

                    if (allProjects.isEmpty()) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "No active arrangement plans found.\nTap 'Create' above to design your first structure.",
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF817799),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            allProjects.forEach { proj ->
                                Card(
                                    onClick = { viewModel.selectProject(proj) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("arr_project_item_${proj.id}"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0x301E173C)
                                    ),
                                    border = BorderStroke(1.dp, Color(0x20FFD700))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = proj.title.uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFF9F75FF).copy(0.2f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(proj.genre, fontSize = 9.sp, color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold)
                                                }
                                                Text("BPM: ${proj.bpm}", fontSize = 10.sp, color = Color(0xFF817799))
                                                Text("•", fontSize = 10.sp, color = Color(0xFF817799))
                                                Text(proj.key + " " + proj.scale, fontSize = 10.sp, color = Color(0xFFF9D142), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteProject(proj.id)
                                                    Toast.makeText(context, "Arrangement Project Deleted", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.testTag("arr_delete_${proj.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Delete Project",
                                                    tint = Color(0xFFFF5252).copy(0.7f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Filled.ChevronRight,
                                                contentDescription = "Open Project",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Workspace View: Active arrangement project workspace
                val proj = currentProject!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Title and Meta Section
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = proj.title.uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Duration: ${proj.songDurationSeconds}s | Structure: ${proj.songStructureType}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB0A7CC)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.runAIGeneration(useOfflineAI = false) },
                                    modifier = Modifier.testTag("regenerate_cloud_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudUpload,
                                        contentDescription = "Cloud AI Orchestrate",
                                        tint = Color(0xFFF9D142)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Stats Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "BPM" to "${proj.bpm}",
                                    "KEY" to "${proj.key} ${proj.scale}",
                                    "RAGA" to proj.raga
                                ).forEach { (label, value) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color(0x20100A24)),
                                        border = BorderStroke(0.5.dp, Color(0x15FFFFFF))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(label, fontSize = 9.sp, color = Color(0xFF817799), fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Playback Slider / Seek Controls
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                        contentDescription = "Status",
                                        tint = Color(0xFFF9D142),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPlaying) "Playing Session Plan..." else "Session Paused",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = String.format("%.1fs / %ds", playbackTimeSec, proj.songDurationSeconds),
                                    fontSize = 11.sp,
                                    color = Color(0xFF9F75FF),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = playbackProgress,
                                onValueChange = { viewModel.seekPlayback(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("playback_progress_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF9D142),
                                    activeTrackColor = Color(0xFF9F75FF),
                                    inactiveTrackColor = Color(0xFF1F1B35)
                                )
                            )
                        }
                    }

                    // Section timeline (DAW mode)
                    Text(
                        text = "SONG SECTIONS TIMELINE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color(0xFF9F75FF),
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )

                    HorizontalScrollView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            proj.sections.forEachIndexed { index, section ->
                                val isActive = index == activeSecIndex
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isActive) Color(0xFF9F75FF).copy(0.25f)
                                            else Color(0x203A2B70)
                                        )
                                        .border(
                                            width = if (isActive) 1.5.dp else 0.5.dp,
                                            color = if (isActive) Color(0xFFF9D142) else Color(0x30FFFFFF),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            // Seek to the start time of the section
                                            var cumulative = 0f
                                            for (s in 0 until index) {
                                                cumulative += proj.sections[s].durationSeconds
                                            }
                                            val total = proj.songDurationSeconds.toFloat()
                                            viewModel.seekPlayback(cumulative / total)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = section.sectionName.uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isActive) Color(0xFFF9D142) else Color.White
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.White.copy(0.1f), CircleShape)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${section.bars}B",
                                                    fontSize = 8.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Energy: ${section.energyLevel}/10",
                                            fontSize = 10.sp,
                                            color = Color(0xFFB0A7CC)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = section.dynamics,
                                            fontSize = 9.sp,
                                            color = Color(0xFF817799),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Instrument tracks panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INSTRUMENTATION TRACKS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF9F75FF)
                        )
                        IconButton(
                            onClick = { showCustomTrackDialog = true },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("add_custom_track_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Track",
                                tint = Color(0xFFF9D142)
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            proj.tracks.forEach { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F0922).copy(0.5f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(
                                                    Color(android.graphics.Color.parseColor(track.trackColorHex)),
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = track.instrumentName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = track.rhythmPattern,
                                                fontSize = 9.sp,
                                                color = Color(0xFF817799),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Mute Toggle Button
                                        IconButton(
                                            onClick = { viewModel.toggleTrackMute(track.id) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("mute_${track.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (track.isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                                contentDescription = "Mute",
                                                tint = if (track.isMuted) Color(0xFFFF5252) else Color(0xFF817799),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        // Solo Toggle Button
                                        IconButton(
                                            onClick = { viewModel.toggleTrackSolo(track.id) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("solo_${track.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Headphones,
                                                contentDescription = "Solo",
                                                tint = if (track.isSoloed) Color(0xFFF9D142) else Color(0xFF817799),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        // Lock Toggle Button
                                        IconButton(
                                            onClick = { viewModel.toggleTrackLock(track.id) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("lock_${track.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (track.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                                contentDescription = "Lock",
                                                tint = if (track.isLocked) Color(0xFF9F75FF) else Color(0xFF817799),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Evaluation Card / Scoring
                    proj.evaluation?.let { eval ->
                        Text(
                            text = "METADATA & ORCHESTRATION EVALUATION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF9F75FF),
                            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                        )

                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "DIRECTOR'S REPORT CARD",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF9D142).copy(0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "SCORE: ${eval.overallQualityScore}/100",
                                            fontSize = 10.sp,
                                            color = Color(0xFFF9D142),
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Detailed report bullets
                                listOf(
                                    "Commercial Readiness" to eval.commercialReadinessScore,
                                    "Human Performance Feel" to eval.humanLikenessScore,
                                    "Energy Arc Compliance" to eval.energyFlowScore,
                                    "Transitions Quality" to eval.transitionQualityScore
                                ).forEach { (label, score) ->
                                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(label, fontSize = 11.sp, color = Color(0xFFB0A7CC))
                                            Text("$score%", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { score / 100f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = Color(0xFF9F75FF),
                                            trackColor = Color(0xFF1E153E)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = eval.detailedFeedback,
                                    fontSize = 11.sp,
                                    color = Color(0xFF817799)
                                )
                            }
                        }
                    }
                }
            }

            // Create Project Dialog
            if (showCreateDialog) {
                Dialog(onDismissRequest = { showCreateDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                            .padding(8.dp)
                            .testTag("create_project_dialog"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                        border = BorderStroke(1.dp, Color(0xFF9F75FF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "ARRANGEMENT GENERATOR SETUP",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Title Field
                            OutlinedTextField(
                                value = dTitle,
                                onValueChange = { dTitle = it },
                                label = { Text("Song Plan Title") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("dialog_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF9D142),
                                    unfocusedBorderColor = Color(0x30FFFFFF),
                                    focusedLabelColor = Color(0xFFF9D142)
                                )
                            )

                            // Prompt Field
                            OutlinedTextField(
                                value = dPrompt,
                                onValueChange = { dPrompt = it },
                                label = { Text("AI Director Instructions (E.g. Hindustani sitar solo, soft bansuri lines...)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("dialog_prompt_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF9D142),
                                    unfocusedBorderColor = Color(0x30FFFFFF),
                                    focusedLabelColor = Color(0xFFF9D142)
                                )
                            )

                            // Linking details
                            Text("LINK CORE ENGINE OUTPUTS (OPTIONAL)", fontSize = 10.sp, color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                            // Linked Lyrics Dropdown
                            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                var expanded by remember { mutableStateOf(false) }
                                val selectedLabel = lyricsList.find { it.id == dLyricsId }?.title ?: "No Linked Lyrics"
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().testTag("lyrics_dropdown"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x15FFFFFF))
                                ) {
                                    Text(selectedLabel, color = Color.White)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("No Linked Lyrics") },
                                        onClick = { dLyricsId = null; expanded = false }
                                    )
                                    lyricsList.forEach { ly ->
                                        DropdownMenuItem(
                                            text = { Text(ly.title) },
                                            onClick = { dLyricsId = ly.id; dLyrics = ly.content; expanded = false }
                                        )
                                    }
                                }
                            }

                            // Linked Melody Dropdown
                            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                var expanded by remember { mutableStateOf(false) }
                                val selectedLabel = melodyList.find { it.id == dMelodyId }?.title ?: "No Linked Melody Block"
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().testTag("melody_dropdown"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x15FFFFFF))
                                ) {
                                    Text(selectedLabel, color = Color.White)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("No Linked Melody Block") },
                                        onClick = { dMelodyId = null; expanded = false }
                                    )
                                    melodyList.forEach { me ->
                                        DropdownMenuItem(
                                            text = { Text(me.title) },
                                            onClick = { dMelodyId = me.id; expanded = false }
                                        )
                                    }
                                }
                            }

                            // Linked Chord Dropdown
                            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                var expanded by remember { mutableStateOf(false) }
                                val selectedLabel = chordList.find { it.id == dChordId }?.title ?: "No Linked Chord Block"
                                Button(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth().testTag("chord_dropdown"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x15FFFFFF))
                                ) {
                                    Text(selectedLabel, color = Color.White)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("No Linked Chord Block") },
                                        onClick = { dChordId = null; expanded = false }
                                    )
                                    chordList.forEach { ch ->
                                        DropdownMenuItem(
                                            text = { Text(ch.title) },
                                            onClick = { dChordId = ch.id; expanded = false }
                                        )
                                    }
                                }
                            }

                            // Sub Meta Inputs
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dGenre,
                                    onValueChange = { dGenre = it },
                                    label = { Text("Genre") },
                                    modifier = Modifier.weight(1f).testTag("dialog_genre_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                                )
                                OutlinedTextField(
                                    value = dMood,
                                    onValueChange = { dMood = it },
                                    label = { Text("Mood") },
                                    modifier = Modifier.weight(1f).testTag("dialog_mood_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dKey,
                                    onValueChange = { dKey = it },
                                    label = { Text("Key") },
                                    modifier = Modifier.weight(1f).testTag("dialog_key_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                                )
                                OutlinedTextField(
                                    value = dScale,
                                    onValueChange = { dScale = it },
                                    label = { Text("Scale") },
                                    modifier = Modifier.weight(1f).testTag("dialog_scale_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dBpm.toString(),
                                    onValueChange = { dBpm = it.toIntOrNull() ?: 90 },
                                    label = { Text("Tempo (BPM)") },
                                    modifier = Modifier.weight(1f).testTag("dialog_bpm_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                                )
                                OutlinedTextField(
                                    value = dRaga,
                                    onValueChange = { dRaga = it },
                                    label = { Text("Raga") },
                                    modifier = Modifier.weight(1f).testTag("dialog_raga_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = dStructure,
                                    onValueChange = { dStructure = it },
                                    label = { Text("Song Structure") },
                                    modifier = Modifier.weight(1f).testTag("dialog_structure_input"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                                )
                            }

                            // Submit buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showCreateDialog = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("CANCEL")
                                }
                                Button(
                                    onClick = {
                                        if (dTitle.isBlank()) {
                                            Toast.makeText(context, "Please enter song title", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.createNewProject(
                                                title = dTitle,
                                                lyricsProjectId = dLyricsId,
                                                melodyProjectId = dMelodyId,
                                                chordProjectId = dChordId,
                                                lyrics = dLyrics,
                                                prompt = dPrompt,
                                                genre = dGenre,
                                                mood = dMood,
                                                emotion = dEmotion,
                                                bpm = dBpm,
                                                key = dKey,
                                                scale = dScale,
                                                raga = dRaga,
                                                songDurationSeconds = dDuration,
                                                singerType = dSinger,
                                                language = dLanguage,
                                                targetAudience = dAudience,
                                                songStructureType = dStructure
                                            )
                                            showCreateDialog = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("submit_creation_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color(0xFF09041A))
                                ) {
                                    Text("ORCHESTRATE", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Composer Memory Dialog
            if (showMemoryDialog) {
                Dialog(onDismissRequest = { showMemoryDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                            .testTag("composer_memory_dialog"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                        border = BorderStroke(1.dp, Color(0xFFF9D142))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "COMPOSER MEMORY SETTINGS",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            OutlinedTextField(
                                value = editStyle,
                                onValueChange = { editStyle = it },
                                label = { Text("Favorite Style") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("mem_style_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                            )
                            OutlinedTextField(
                                value = editInsts,
                                onValueChange = { editInsts = it },
                                label = { Text("Favorite Instruments List") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("mem_insts_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                            )
                            OutlinedTextField(
                                value = editTrackCount,
                                onValueChange = { editTrackCount = it },
                                label = { Text("Tracks Limit Pref") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).testTag("mem_tracks_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showMemoryDialog = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("CLOSE")
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateComposerMemory(editStyle, editInsts, editTrackCount)
                                        Toast.makeText(context, "Composer Preferences Saved", Toast.LENGTH_SHORT).show()
                                        showMemoryDialog = false
                                    },
                                    modifier = Modifier.weight(1f).testTag("mem_save_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color(0xFF09041A))
                                ) {
                                    Text("SAVE MEMORY", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Custom Track Dialog
            if (showCustomTrackDialog) {
                Dialog(onDismissRequest = { showCustomTrackDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                            .testTag("custom_track_dialog"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                        border = BorderStroke(1.dp, Color(0xFF9F75FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ADD INSTRUMENT TRACK",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            OutlinedTextField(
                                value = customTrackName,
                                onValueChange = { customTrackName = it },
                                label = { Text("Instrument Name (e.g. Sitar, Violin, Synth)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("custom_track_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                            )
                            OutlinedTextField(
                                value = customTrackColor,
                                onValueChange = { customTrackColor = it },
                                label = { Text("Hex Color (e.g. #FFC107)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).testTag("custom_track_color_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showCustomTrackDialog = false },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                ) {
                                    Text("CLOSE")
                                }
                                Button(
                                    onClick = {
                                        if (customTrackName.isBlank()) {
                                            Toast.makeText(context, "Please specify instrument", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.addCustomTrack(customTrackName, customTrackColor)
                                            Toast.makeText(context, "$customTrackName Track Added", Toast.LENGTH_SHORT).show()
                                            customTrackName = ""
                                            showCustomTrackDialog = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f).testTag("custom_track_submit_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color(0xFF09041A))
                                ) {
                                    Text("ADD TRACK", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Export Dialog
            if (showExportDialog) {
                Dialog(onDismissRequest = { showExportDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(8.dp)
                            .testTag("export_dialog"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                        border = BorderStroke(1.dp, Color(0xFFF9D142))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "EXPORT SESSION PLAN",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            listOf("MIDI", "MUSICXML", "DAW_METADATA", "JSON").forEach { format ->
                                Card(
                                    onClick = {
                                        exportFormatSelected = format
                                        viewModel.exportSessionPlan(format)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .testTag("export_option_$format"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (exportFormatSelected == format) Color(0xFF9F75FF).copy(0.25f) else Color(0x10FFFFFF)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = format,
                                            tint = if (exportFormatSelected == format) Color(0xFFF9D142) else Color.White
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(format, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (exportedTextResult.isNotEmpty()) {
                                OutlinedTextField(
                                    value = exportedTextResult,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Export Payload") },
                                    modifier = Modifier.fillMaxWidth().height(140.dp).padding(bottom = 12.dp).testTag("export_payload_text"),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF9F75FF))
                                )
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(exportedTextResult))
                                        Toast.makeText(context, "Export Payload Copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).testTag("copy_payload_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF))
                                ) {
                                    Text("COPY PAYLOAD", fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.clearExportedText()
                                    exportFormatSelected = ""
                                    showExportDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("CLOSE")
                            }
                        }
                    }
                }
            }

            // AI generation overlay spinner
            if (generationState == ArrangementGenerationState.GENERATING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFF9D142))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "SurMaya AI Director is orchestrating sections & automation lines...",
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalScrollView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        content()
    }
}
