package com.example.ui.screens.melody

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.domain.model.melody.*
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.MelodyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMelodyScreen(
    viewModel: MelodyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val allProjects by viewModel.allProjects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    val generatedPlan by viewModel.generatedPlan.collectAsState()
    val exportedContent by viewModel.exportedContent.collectAsState()
    val activeNoteIndex by viewModel.activePlayingNoteIndex.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var exportFormatSelected by remember { mutableStateOf("") }

    // New Project Form Variables
    var newTitle by remember { mutableStateOf("") }
    var newLyrics by remember { mutableStateOf("") }
    var newChords by remember { mutableStateOf("") }
    var newPrompt by remember { mutableStateOf("") }
    var newEmotion by remember { mutableStateOf("Romantic") }
    var newGenre by remember { mutableStateOf("Bollywood") }
    var newMood by remember { mutableStateOf("Soulful") }
    var newScale by remember { mutableStateOf("C Major") }
    var newRaga by remember { mutableStateOf("Yaman") }
    var newTempo by remember { mutableStateOf(90) }
    var newVocalStyle by remember { mutableStateOf("Duet") }
    var newSectionType by remember { mutableStateOf("Chorus") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI MELODY GENERATOR",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "SurMaya Music OS • Classical Ragas & Sargam Engine",
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
                            if (selectedProject != null) {
                                viewModel.selectProject(null)
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("melody_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (selectedProject != null) {
                        IconButton(onClick = { viewModel.generateMelody() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = Color(0xFFF9D142)
                            )
                        }
                    } else {
                        IconButton(onClick = { showCreateDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Project",
                                tint = Color(0xFF2FD6AA)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A)
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedProject == null) {
                // MASTER DASHBOARD VIEW
                if (allProjects.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF9D142).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "No Melodies",
                                tint = Color(0xFFF9D142),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Melody Blueprints Yet",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Start by creating a classical Raga and Sargam-based Indian melody project.",
                            color = Color(0xFF9E93B3),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                        
                        GlowingButton(
                            text = "Create Melody Project",
                            onClick = { showCreateDialog = true },
                            height = 44.dp,
                            testTag = "create_melody_project_btn"
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Active Melody Blueprints",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        items(allProjects.size) { index ->
                            val proj = allProjects[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectProject(proj) }
                                    .testTag("melody_project_item_${proj.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF9F75FF).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Audiotrack,
                                            contentDescription = "Melody Project",
                                            tint = Color(0xFF9F75FF)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = proj.title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Raga ${proj.raga} • ${proj.scale} • ${proj.tempo} BPM",
                                            color = Color(0xFF9E93B3),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProject(proj.id) },
                                        modifier = Modifier.testTag("delete_melody_project_${proj.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF5350)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE PROJECT WORKSPACE
                val project = selectedProject!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Project Context Panel
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = project.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "Style: ${project.genre} • Mood: ${project.mood} • Scale: ${project.scale}",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 11.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF9D142).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "RAGA ${project.raga.uppercase()}",
                                        color = Color(0xFFF9D142),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            if (project.lyrics.isNotBlank()) {
                                Divider(color = Color(0xFF2E244E))
                                Text(
                                    text = "Lyrics input:",
                                    color = Color(0xFF9F75FF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "\"${project.lyrics}\"",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Input parameter adjustment section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                        border = BorderStroke(1.dp, Color(0xFF2E244E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "MELODY PIPELINE CONTROLS",
                                color = Color(0xFF2FD6AA),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = project.lyrics,
                                    onValueChange = { viewModel.updateProjectDetails(it, project.chords, project.prompt, project.emotion, project.genre, project.mood, project.scale, project.raga, project.tempo, project.vocalStyle, project.sectionType) },
                                    label = { Text("Active Lyrics", color = Color(0xFF9E93B3), fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF9F75FF),
                                        unfocusedBorderColor = Color(0xFF2E244E)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = project.chords,
                                    onValueChange = { viewModel.updateProjectDetails(project.lyrics, it, project.prompt, project.emotion, project.genre, project.mood, project.scale, project.raga, project.tempo, project.vocalStyle, project.sectionType) },
                                    label = { Text("Chords Progression", color = Color(0xFF9E93B3), fontSize = 10.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF9F75FF),
                                        unfocusedBorderColor = Color(0xFF2E244E)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Tempo adjustment slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tempo: ${project.tempo} BPM", color = Color.White, fontSize = 11.sp)
                                    Text("Taal Match: Keharwa (8B)", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                }
                                Slider(
                                    value = project.tempo.toFloat(),
                                    onValueChange = { viewModel.updateProjectDetails(project.lyrics, project.chords, project.prompt, project.emotion, project.genre, project.mood, project.scale, project.raga, it.toInt(), project.vocalStyle, project.sectionType) },
                                    valueRange = 60f..180f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF9F75FF),
                                        activeTrackColor = Color(0xFF9F75FF)
                                    )
                                )
                            }

                            // Trigger Button with Loading States
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                when (generationState) {
                                    MelodyGenerationState.IDLE, MelodyGenerationState.COMPLETE, MelodyGenerationState.ERROR -> {
                                        GlowingButton(
                                            text = if (generatedPlan != null) "RE-SYNTHESIZE AI MELODY" else "SYNTHESIZE AI MELODY",
                                            onClick = { viewModel.generateMelody() },
                                            height = 40.dp,
                                            testTag = "generate_melody_trigger"
                                        )
                                    }
                                    else -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            CircularProgressIndicator(color = Color(0xFFF9D142), modifier = Modifier.size(24.dp))
                                            Text(
                                                text = when (generationState) {
                                                    MelodyGenerationState.PLANNING -> "🎼 PIPELINE STEP 1/3: EXAMINING POETIC METER..."
                                                    MelodyGenerationState.GENERATING -> "🎹 PIPELINE STEP 2/3: DEPLOYING CLOUD/OFFLINE CLASSICAL SCALES..."
                                                    MelodyGenerationState.EVALUATING -> "📈 PIPELINE STEP 3/3: RUNNING QUALITY EVALUATION MODELS..."
                                                    else -> "GENERATING..."
                                                },
                                                color = Color(0xFFF9D142),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Display Generated Results
                    val plan = generatedPlan
                    if (plan != null && generationState == MelodyGenerationState.COMPLETE) {
                        
                        // SECTION: Real-time dynamic pitch roll Canvas!
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C071C)),
                            border = BorderStroke(1.dp, Color(0xFF2E244E)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = "Melody Graph",
                                            tint = Color(0xFF9F75FF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "DYNAMIC REAL-TIME PIANO ROLL",
                                            color = Color(0xFF9F75FF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Local Playback Controls
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = {
                                                if (activeNoteIndex != null && activeNoteIndex!! >= 0) {
                                                    viewModel.stopMelodyPlayback()
                                                } else {
                                                    viewModel.playMelodyPlayback()
                                                }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFF9D142).copy(alpha = 0.2f))
                                        ) {
                                            Icon(
                                                imageVector = if (activeNoteIndex != null && activeNoteIndex!! >= 0) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play notes",
                                                tint = Color(0xFFF9D142),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        if (activeNoteIndex != null && activeNoteIndex!! >= 0) {
                                            IconButton(
                                                onClick = { viewModel.stopMelodyPlayback() },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.1f))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Stop,
                                                    contentDescription = "Stop",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Interactive Melody Graph Canvas
                                val notes = plan.noteSequence
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(Color(0xFF05030B))
                                        .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val padLeft = 40f
                                        val padBottom = 30f
                                        val canvasWidth = size.width - padLeft
                                        val canvasHeight = size.height - padBottom

                                        // Draw static lines for notes representation
                                        val numGridLines = 6
                                        for (g in 0 until numGridLines) {
                                            val y = (canvasHeight / numGridLines) * g
                                            drawLine(
                                                color = Color(0xFF140D2A),
                                                start = Offset(padLeft, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = 1f
                                            )
                                        }

                                        if (notes.isNotEmpty()) {
                                            val totalDur = notes.sumOf { it.durationSeconds.toDouble() }.toFloat()
                                            val minHz = notes.minOf { it.pitchHz }
                                            val maxHz = notes.maxOf { it.pitchHz }
                                            val rangeHz = (maxHz - minHz).coerceAtLeast(10f)

                                            var currentX = padLeft
                                            notes.forEachIndexed { i, note ->
                                                val noteWidth = (note.durationSeconds / totalDur) * canvasWidth
                                                // Normalized Y coordinate: higher pitch = higher on screen
                                                val rawNorm = (note.pitchHz - minHz) / rangeHz
                                                val noteY = canvasHeight - (rawNorm * (canvasHeight - 40f)) - 20f

                                                // Color glows if current note is active in real-time synth play
                                                val isNoteActive = activeNoteIndex == i
                                                val baseColor = if (isNoteActive) Color(0xFFF9D142) else Color(0xFF9F75FF)
                                                val shadowAlpha = if (isNoteActive) 0.6f else 0.25f

                                                // Draw note block
                                                drawRoundRect(
                                                    color = baseColor.copy(alpha = shadowAlpha),
                                                    topLeft = Offset(currentX + 2f, noteY),
                                                    size = Size(noteWidth - 4f, 24f),
                                                    cornerRadius = CornerRadius(4f, 4f)
                                                )
                                                drawRoundRect(
                                                    color = baseColor,
                                                    topLeft = Offset(currentX + 4f, noteY + 2f),
                                                    size = Size(noteWidth - 8f, 20f),
                                                    cornerRadius = CornerRadius(4f, 4f)
                                                )

                                                currentX += noteWidth
                                            }
                                        }
                                    }
                                }

                                // Interactive sargam slider buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    notes.forEachIndexed { i, note ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (activeNoteIndex == i) Color(0xFFF9D142) else Color(0xFF140D2A)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (activeNoteIndex == i) Color(0xFFF9D142) else Color(0xFF2E244E),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = note.sargamEquivalent,
                                                    color = if (activeNoteIndex == i) Color.Black else Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "${note.noteName} (${note.ornamentation})",
                                                    color = if (activeNoteIndex == i) Color.Black.copy(alpha = 0.7f) else Color(0xFF9E93B3),
                                                    fontSize = 8.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION: EXPLAIN WHY / COMPOSER DECISION RATIONALE
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0927)),
                            border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Melody Rationale",
                                        tint = Color(0xFFF9D142),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🎼 EXPLAIN WHY: MELODY GENERATION RATIONALE",
                                        color = Color(0xFFF9D142),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text("🎵 Raga Scale Grammar: ", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(plan.indianAesthetics.ragaGrammar, color = Color.White, fontSize = 11.sp)
                                    }
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text("⚡ Ornamentation Reasoning: ", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(plan.indianAesthetics.ornamentReason, color = Color.White, fontSize = 11.sp)
                                    }
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text("🥁 Taal/Rhythm Reason: ", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(plan.indianAesthetics.layakariPattern, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // SECTION: MOTIF & INDIAN CLASSICAL AESTHETICS DETAILS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                            border = BorderStroke(1.dp, Color(0xFF2E244E))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "🏆 PRIMARY MOTIF & CLASSICAL AESTHETICS",
                                    color = Color(0xFFF9D142),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                Row {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("MOTIF CONTOUR", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                        Text(plan.motif.sargamContour, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("HOOK QUALITY", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                        Text("${(plan.motif.hookStrength * 100).toInt()}% Strength", color = Color(0xFF2FD6AA), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Divider(color = Color(0xFF2E244E))

                                Row {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("PAKAD (RAGA SIGNATURE)", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                        Text(plan.indianAesthetics.pakadSargam, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("TAAL SYNC", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                        Text(plan.taal, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Divider(color = Color(0xFF2E244E))

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Raga Classical Analysis:", color = Color(0xFF9F75FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(plan.indianAesthetics.sargamReason, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                                }
                            }
                        }

                        // SECTION: AI PIPELINE CONFIDENCE & INTEGRITY
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                            border = BorderStroke(1.dp, Color(0xFF2E244E))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Confidence",
                                        tint = Color(0xFF2FD6AA),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI PIPELINE INTEGRITY ANALYSIS",
                                        color = Color(0xFF2FD6AA),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Row {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("EVAL CONFIDENCE", color = Color(0xFF9E93B3), fontSize = 9.sp)
                                        Text("${(plan.pipelineConfidence * 100).toInt()}% Score", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text("PIPELINE ANNOTATION", color = Color(0xFF9E93B3), fontSize = 9.sp)
                                        Text(plan.promptInsight, color = Color.White, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }

                        // SECTION: QUALITY EVALUATION RATINGS
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                            border = BorderStroke(1.dp, Color(0xFF2E244E))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "🎯 SYSTEM QUALITY EVALUATION MATRIX",
                                    color = Color(0xFF9F75FF),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                val metrics = listOf(
                                    "Singability" to plan.evaluation.singabilityScore,
                                    "Originality" to plan.evaluation.originalityScore,
                                    "Musical Flow" to plan.evaluation.musicalFlowScore,
                                    "Genre Match" to plan.evaluation.genreMatchScore,
                                    "Pitch Stability" to plan.evaluation.pitchStabilityScore,
                                    "Human-likeness" to plan.evaluation.humanLikenessScore
                                )

                                metrics.forEach { (label, value) ->
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(label, color = Color.White, fontSize = 11.sp)
                                            Text("${(value * 100).toInt()}%", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { value },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF9F75FF),
                                            trackColor = Color(0xFF2E244E)
                                        )
                                    }
                                }

                                if (plan.evaluation.recommendations.isNotEmpty()) {
                                    Divider(color = Color(0xFF2E244E), modifier = Modifier.padding(vertical = 4.dp))
                                    Text("📋 SYSTEM OPTIMIZATION RECOMMENDATIONS:", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    plan.evaluation.recommendations.forEach { rec ->
                                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 2.dp)) {
                                            Text("• ", color = Color(0xFFF9D142), fontSize = 11.sp)
                                            Text(rec, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION: EXPORTS LAYER
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                            border = BorderStroke(1.dp, Color(0xFF2E244E))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "💾 PROFESSIONAL EXPORTS DESK",
                                    color = Color(0xFF2FD6AA),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            exportFormatSelected = "MIDI"
                                            viewModel.exportMelody(exportFormatSelected)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FD6AA).copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, Color(0xFF2FD6AA)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).testTag("export_midi_btn")
                                    ) {
                                        Text("EXPORT MIDI", color = Color(0xFF2FD6AA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            exportFormatSelected = "MUSICXML"
                                            viewModel.exportMelody(exportFormatSelected)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF).copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.1f).testTag("export_xml_btn")
                                    ) {
                                        Text("EXPORT MUSICXML", color = Color(0xFF9F75FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (exportedContent != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF09041A))
                                            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("GENERATED $exportFormatSelected CONTENT:", color = Color(0xFFF9D142), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(exportedContent!!))
                                                        Toast.makeText(context, "$exportFormatSelected copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(24.dp).testTag("copy_export_btn")
                                                ) {
                                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = exportedContent!!,
                                                color = Color(0xFFE5DFFF),
                                                fontSize = 9.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                maxLines = 8,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (generatedPlan == null && generationState == MelodyGenerationState.IDLE) {
                        // Empty Success layout to advise trigger
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF140D2A))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Ready", tint = Color(0xFF9F75FF), modifier = Modifier.size(36.dp))
                                Text("Ready to Synthesize", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                Text("Click the button above to generate a customized classical Raga and sargam structure.", color = Color(0xFF9E93B3), fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // New Project Dialog
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF140D2A),
                border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Create Melody Blueprint",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Project Title", color = Color(0xFF9E93B3)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF3E3556)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_melody_title_field")
                    )

                    OutlinedTextField(
                        value = newLyrics,
                        onValueChange = { newLyrics = it },
                        label = { Text("Lyrics Input (Optional)", color = Color(0xFF9E93B3)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF3E3556)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newRaga,
                            onValueChange = { newRaga = it },
                            label = { Text("Raga Name", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF3E3556)
                            ),
                            modifier = Modifier.weight(1f).testTag("new_melody_raga_field")
                        )
                        OutlinedTextField(
                            value = newScale,
                            onValueChange = { newScale = it },
                            label = { Text("Scale / Tonic", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF3E3556)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = newGenre,
                            onValueChange = { newGenre = it },
                            label = { Text("Genre", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF3E3556)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newEmotion,
                            onValueChange = { newEmotion = it },
                            label = { Text("Emotion", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF3E3556)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCreateDialog = false },
                            border = BorderStroke(1.dp, Color(0xFF3E3556)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    viewModel.createProject(
                                        title = newTitle,
                                        lyrics = newLyrics,
                                        chords = newChords,
                                        prompt = newPrompt,
                                        emotion = newEmotion,
                                        genre = newGenre,
                                        mood = newMood,
                                        scale = newScale,
                                        raga = newRaga,
                                        tempo = newTempo,
                                        vocalStyle = newVocalStyle,
                                        sectionType = newSectionType
                                    )
                                    newTitle = ""
                                    newLyrics = ""
                                    showCreateDialog = false
                                    Toast.makeText(context, "Melody project created!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.weight(1f).testTag("save_melody_project_btn")
                        ) {
                            Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
