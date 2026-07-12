package com.example.ui.screens.mixing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.layout
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.MixingViewModel
import com.example.domain.model.mixing.*
import com.example.core.audio.export.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMixingScreen(
    viewModel: MixingViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val projects by viewModel.projects.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val synthesisResult by viewModel.synthesisResult.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val targetLoudness by viewModel.targetLoudness.collectAsState()
    val newTitle by viewModel.newProjectTitle.collectAsState()
    val masterFaderLevelDb by viewModel.masterFaderLevelDb.collectAsState()

    var showBounceDialog by remember { mutableStateOf(false) }

    // Interactive channel states
    var activeTab by remember { mutableStateOf("Mixer Console") } // "Mixer Console", "Audit & Diagnostics", "AI Explainable"
    var selectedChannelIdForExplain by remember { mutableStateOf<String?>("track_1") }
    var soloedTracks = remember { mutableStateMapOf<String, Boolean>() }
    var mutedTracks = remember { mutableStateMapOf<String, Boolean>() }

    // Onboarding predefined track setup
    val sampleTracks = listOf(
        Pair("Lead Vocal", "Vocal"),
        Pair("Bansuri Flute", "Melody"),
        Pair("Tanpura Chords", "Chord"),
        Pair("Acoustic Bass", "Bass"),
        Pair("Tabla Percussion", "Drum")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Mixing Studio (AMIE)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "AI Mixing Intelligence Engine v1.1.0",
                            color = Color(0xFFB0A2C9),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0C1B),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0A0814)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0C1B), Color(0xFF0A0814))
                    )
                )
        ) {
            if (currentProject == null) {
                // Onboarding & On-creation screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SettingsInputComponent,
                        contentDescription = "AMIE Mixer",
                        tint = Color(0xFF8A2BE2),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AI Mixing Workspace",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Transform raw multitrack performances into genre-balanced, streaming-ready master audio sums with full analytical explanation.",
                        fontSize = 14.sp,
                        color = Color(0xFF9C91B5),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 450.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    GlassCard(
                        modifier = Modifier.widthIn(max = 500.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Naya Mix Project Create Karein",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { viewModel.newProjectTitle.value = it },
                                label = { Text("Project Title") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8A2BE2),
                                    unfocusedBorderColor = Color(0xFF2C254A),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mix_title_input")
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Genre Archetype Style Select Karein",
                                fontSize = 13.sp,
                                color = Color(0xFFB0A2C9),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val genres = listOf("Bollywood", "Classical", "Pop", "EDM", "Ghazal")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(genres) { g ->
                                    val isSelected = g == selectedGenre
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectedGenre.value = g },
                                        label = { Text(g) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF8A2BE2),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF16122C),
                                            labelColor = Color(0xFFB0A2C9)
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Target Loudness: ${targetLoudness.toInt()} LUFS",
                                fontSize = 13.sp,
                                color = Color(0xFFB0A2C9)
                            )
                            Slider(
                                value = targetLoudness,
                                onValueChange = { viewModel.targetLoudness.value = it },
                                valueRange = -24.0f..-6.0f,
                                steps = 17,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFF8A2BE2),
                                    thumbColor = Color.White
                                )
                            )
                            Text(
                                text = "Note: Standard streaming (Spotify) is -14 LUFS. CD/Club audio can go up to -8 LUFS.",
                                fontSize = 11.sp,
                                color = Color(0xFF887D9F)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            GlowingButton(
                                text = "Synthesize AI Mix Sum",
                                onClick = {
                                    viewModel.createProject(
                                        title = newTitle,
                                        tracks = sampleTracks,
                                        genre = selectedGenre,
                                        loudness = targetLoudness
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("create_mix_button")
                            )
                        }
                    }

                    if (projects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Ya Purane Mixes me se Select Karein:",
                            fontSize = 13.sp,
                            color = Color(0xFF887D9F),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF130F25)),
                            modifier = Modifier.widthIn(max = 500.dp)
                        ) {
                            Column {
                                projects.forEach { proj ->
                                    ListItem(
                                        headlineContent = { Text(proj.title, color = Color.White) },
                                        supportingContent = { Text("Style: ${proj.genreStyle} | ${proj.targetLoudnessLufs} LUFS", color = Color(0xFF9E93B3)) },
                                        trailingContent = {
                                            IconButton(onClick = { viewModel.deleteProject(proj.id) }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
                                            }
                                        },
                                        modifier = Modifier.clickable { viewModel.selectProject(proj.id) },
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                    )
                                    HorizontalDivider(color = Color(0xFF2C254A))
                                }
                            }
                        }
                    }
                }
            } else {
                // Workspace Display Screen
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top tab selectors
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F0C1B))
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Mixer Console", "AI Explainable", "Audit & Diagnostics").forEach { tab ->
                            val isSelected = tab == activeTab
                            Button(
                                onClick = { activeTab = tab },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF8A2BE2) else Color.Transparent,
                                    contentColor = if (isSelected) Color.White else Color(0xFFB0A2C9)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(tab, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = { viewModel.selectProject("") },
                            modifier = Modifier.testTag("exit_project")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Project", tint = Color.White)
                        }
                    }

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF8A2BE2))
                        }
                    } else if (synthesisResult == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Button(onClick = { viewModel.synthesizeCurrentMix(currentProject!!.genreStyle, currentProject!!.targetLoudnessLufs) }) {
                                Text("Load AI Synthesis Result")
                            }
                        }
                    } else {
                        val result = synthesisResult!!

                        when (activeTab) {
                            "Mixer Console" -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState())
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Track Strips
                                    result.trackAnalyses.forEach { track ->
                                        val isSoloed = soloedTracks[track.trackId] == true
                                        val isMuted = mutedTracks[track.trackId] == true
                                        val gain = result.blueprint.gainStagingMap[track.trackId]
                                        val spatial = result.blueprint.spatialMixingMap[track.trackId]
                                        val eq = result.blueprint.eqIntelligenceMap[track.trackId]

                                        if (gain != null && spatial != null) {
                                            ChannelStrip(
                                                track = track,
                                                gain = gain,
                                                spatial = spatial,
                                                eq = eq,
                                                isSoloed = isSoloed,
                                                isMuted = isMuted,
                                                onFaderChange = { viewModel.updateFaderLevel(track.trackId, it) },
                                                onPanChange = { viewModel.updatePan(track.trackId, it) },
                                                onWidthChange = { viewModel.updateStereoWidth(track.trackId, it) },
                                                onAuxChange = { r, d -> viewModel.updateAuxSends(track.trackId, r, d) },
                                                onMuteToggle = { mutedTracks[track.trackId] = !isMuted },
                                                onSoloToggle = { soloedTracks[track.trackId] = !isSoloed },
                                                onExplainSelect = { selectedChannelIdForExplain = track.trackId; activeTab = "AI Explainable" }
                                            )
                                        }
                                    }

                                    // Master Channel Strip
                                    MasterStrip(
                                        blueprint = result.blueprint,
                                        loudness = result.loudness,
                                        phase = result.phaseReport,
                                        masterFaderLevelDb = masterFaderLevelDb,
                                        onMasterFaderChange = { viewModel.updateMasterFader(it) },
                                        onSynthesizeReq = { viewModel.synthesizeCurrentMix(result.blueprint.genreStyle, result.blueprint.targetLoudnessLufs) },
                                        onBounceClick = { showBounceDialog = true }
                                    )
                                }
                            }

                            "AI Explainable" -> {
                                val selectedTrackAnalysis = result.trackAnalyses.find { it.trackId == selectedChannelIdForExplain }
                                val selectedEQ = result.blueprint.eqIntelligenceMap[selectedChannelIdForExplain]
                                val selectedGain = result.blueprint.gainStagingMap[selectedChannelIdForExplain]
                                val selectedSpatial = result.blueprint.spatialMixingMap[selectedChannelIdForExplain]
                                val selectedComp = result.compressors.find { it.trackId == selectedChannelIdForExplain }
                                val selectedDeEsser = result.deEsserReport.find { it.trackId == selectedChannelIdForExplain }
                                val selectedNoise = result.noiseReport.find { it.trackId == selectedChannelIdForExplain }

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Channel Selector Column
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF130F25)),
                                        modifier = Modifier
                                            .width(220.dp)
                                            .fillMaxHeight()
                                    ) {
                                        Column {
                                            Text(
                                                text = "Select Strip to Explain:",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB0A2C9),
                                                modifier = Modifier.padding(12.dp)
                                            )
                                            HorizontalDivider(color = Color(0xFF2C254A))
                                            LazyColumn {
                                                items(result.trackAnalyses) { track ->
                                                    val isSelected = track.trackId == selectedChannelIdForExplain
                                                    Text(
                                                        text = "🎚️ ${track.trackName}",
                                                        color = if (isSelected) Color.White else Color(0xFFB0A2C9),
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 14.sp,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(if (isSelected) Color(0xFF21193D) else Color.Transparent)
                                                            .clickable { selectedChannelIdForExplain = track.trackId }
                                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                                    )
                                                    HorizontalDivider(color = Color(0xFF2C254A))
                                                }
                                            }
                                        }
                                    }

                                    // Explanation Body Column
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF110E21)),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            if (selectedTrackAnalysis != null) {
                                                Text(
                                                    text = "Explainable AI Decision Details: '${selectedTrackAnalysis.trackName}'",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))

                                                // EQ Decisions Explanation
                                                if (selectedEQ != null) {
                                                    ExplainBlock(
                                                        title = "🎸 Intelligent EQ Slot Design",
                                                        reason = selectedEQ.explainableReason,
                                                        details = selectedEQ.bands.joinToString("\n") { band ->
                                                            "• ${band.bandName}: Frequency ${band.frequencyHz.toInt()}Hz | Gain ${String.format("%+.1f", band.gainDb)}dB | Purpose: ${band.purpose}"
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                }

                                                // Dynamics / Compressor Decisions
                                                if (selectedGain != null && selectedComp != null) {
                                                    ExplainBlock(
                                                        title = "🔥 Dynamics & Compressor Settings",
                                                        reason = "Auto dynamics configured ratio ${selectedComp.ratio}:1, with attack ${selectedComp.attackMs}ms and release ${selectedComp.releaseMs}ms to match ${selectedTrackAnalysis.trackType} peaks.",
                                                        details = "• Input Gain Trim Correction: ${String.format("%+.1f", selectedGain.recommendedTrimDb)} dB\n" +
                                                                "• Suggested Fader Volume: ${selectedGain.targetFaderLevelDb} dB\n" +
                                                                "• Compressor Threshold: ${selectedComp.thresholdDb} dB\n" +
                                                                "• Suggested Make-up Gain: ${selectedComp.makeupGainDb} dB\n" +
                                                                "• Parallel Blend Routing: ${if (selectedComp.isParallelEnabled) "Active (35% Wet)" else "Bypassed"}\n" +
                                                                "• Dynamic Sidechain: ${if (selectedComp.sidechainSource != "None") "Keyed to Kick drum track" else "None"}"
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                }

                                                // De-Esser Specifics
                                                if (selectedDeEsser != null) {
                                                    ExplainBlock(
                                                        title = "🗣️ Vocal De-Esser Controls",
                                                        reason = "Sibilance detector targeted harsh high frequencies between 5.5kHz and 7.5kHz, clamping down on ${selectedDeEsser.detectedSibilants.joinToString(", ")} consonant bursts.",
                                                        details = "• Targeted Sibilance Center: ${selectedDeEsser.frequencyHz.toInt()} Hz\n" +
                                                                "• Dynamic Attenuation Threshold: ${selectedDeEsser.thresholdDb} dB\n" +
                                                                "• Gain Suppression Applied: -${selectedDeEsser.reductionDb} dB"
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                }

                                                // Noise Gate Specifics
                                                if (selectedNoise != null) {
                                                    ExplainBlock(
                                                        title = "🛑 Analog Noise Intelligence Gate",
                                                        reason = if (selectedNoise.detectedNoises.isNotEmpty()) {
                                                            "Identified AC power ground noise / ambient hum. Engaging a high-order spectral gate to cleanly pass audio transients."
                                                        } else {
                                                            "Audio feed is clean. Standard high-fidelity expansion engaged below the dynamic floor."
                                                        },
                                                        details = "• Detected Noise Types: ${if (selectedNoise.detectedNoises.isNotEmpty()) selectedNoise.detectedNoises.joinToString(", ") else "None (Pristine)"}\n" +
                                                                "• Hiss Floor: ${selectedNoise.hissLevelDb} dBFS\n" +
                                                                "• Hum Floor: ${selectedNoise.humLevelDb} dBFS\n" +
                                                                "• Room Noise Ambience: ${selectedNoise.roomNoiseDb} dBFS"
                                                    )
                                                }
                                            } else {
                                                Text("Select a channel to analyze the decisions.", color = Color(0xFFB0A2C9))
                                            }
                                        }
                                    }
                                }
                            }

                            "Audit & Diagnostics" -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Explainable Mix text report
                                    item {
                                        Text(
                                            text = "📋 Full Professional AMIE Mixing Audit Report",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF130F25)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = result.explainableMixReport,
                                                fontSize = 13.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                color = Color(0xFFC7BED8),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                                    .horizontalScroll(rememberScrollState())
                                            )
                                        }
                                    }

                                    // Clashing frequencies report
                                    item {
                                        Text(
                                            text = "⚠️ Masking & Frequency Clashes Diagnostic",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        result.frequencyConflicts.forEach { conflict ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C101B)),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(
                                                        text = "Overlap detected around ${conflict.clashingFreqHz.toInt()} Hz",
                                                        color = Color(0xFFFF8A80),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "Severity Index: ${conflict.maskingSeverity.toInt()}%",
                                                        color = Color(0xFFFFB4AB),
                                                        fontSize = 12.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Solution Strategy: ${conflict.suggestion}",
                                                        color = Color.White,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showBounceDialog) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.cancelExport()
                    showBounceDialog = false 
                },
                title = {
                    Text("📁 OFFLINE BOUNCE ENGINE", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                containerColor = Color(0xFF1E1332),
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Select output deliverable format for rendering current mixer configuration:", color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Export Progress Card inside Dialog ---
                        when (val status = exportStatus) {
                            is ExportStatus.Rendering -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C00)),
                                    border = BorderStroke(1.dp, Color(0xFFFF9800)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⚡ RENDERING MIXDOWN...", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text("${(status.progress * 100).toInt()}% (${String.format("%.1f", status.speedMultiplier)}x)", color = Color.White, fontSize = 10.sp)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { status.progress },
                                            modifier = Modifier.fillMaxWidth(),
                                            color = Color(0xFFFF9800),
                                            trackColor = Color(0x33, 0xFF, 0x98, 0x00)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Button(
                                                onClick = { viewModel.cancelExport() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp).testTag("mix_cancel_export_btn")
                                            ) {
                                                Text("CANCEL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            is ExportStatus.Success -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF002E00)),
                                    border = BorderStroke(1.dp, Color.Green),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("✨ BOUNCE COMPLETE!", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("File: ${status.file.name}", color = Color.White, fontSize = 11.sp)
                                        Text("Size: ${status.sizeKb} KB", color = Color.LightGray, fontSize = 11.sp)
                                        Text("Path: SurMaya_Exports/", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }
                            }
                            is ExportStatus.Failed -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E0000)),
                                    border = BorderStroke(1.dp, Color.Red),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("❌ BOUNCE FAILED", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(status.error, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                            is ExportStatus.Cancelled -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                    border = BorderStroke(1.dp, Color.Gray),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("⏸ EXPORT CANCELLED", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                            else -> {
                                // Show format selectors when idle
                                val options = listOf(
                                    com.example.core.audio.export.ExportFormat.WAV_24 to "WAV 24-bit (Lossless Studio)",
                                    com.example.core.audio.export.ExportFormat.WAV_16 to "WAV 16-bit (Lossless CD)",
                                    com.example.core.audio.export.ExportFormat.WAV_32_FLOAT to "WAV 32-bit Float (HDR)",
                                    com.example.core.audio.export.ExportFormat.FLAC to "FLAC Lossless Compressed",
                                    com.example.core.audio.export.ExportFormat.MP3 to "MP3 High Quality (320 kbps)",
                                    com.example.core.audio.export.ExportFormat.AAC to "AAC Optimized (256 kbps)",
                                    com.example.core.audio.export.ExportFormat.OGG to "OGG Vorbis"
                                )

                                options.forEach { (fmt, label) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .background(Color(0xFF2A1C44), RoundedCornerShape(4.dp))
                                            .clickable { viewModel.startExport(fmt) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(label, color = Color.White, fontSize = 11.sp)
                                        Text("BOUNCE", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("mix_bounce_${fmt.name}"))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.cancelExport()
                        showBounceDialog = false 
                    }) {
                        Text("CLOSE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun ExplainBlock(title: String, reason: String, details: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1835)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFFD0BCFF), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(reason, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(details, color = Color(0xFFB0A2C9), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun ChannelStrip(
    track: TrackAnalysis,
    gain: GainStaging,
    spatial: SpatialMixing,
    eq: EQIntelligence?,
    isSoloed: Boolean,
    isMuted: Boolean,
    onFaderChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit,
    onWidthChange: (Float) -> Unit,
    onAuxChange: (Float, Float) -> Unit,
    onMuteToggle: () -> Unit,
    onSoloToggle: () -> Unit,
    onExplainSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isMuted) Color(0xFF120E1C) else Color(0xFF171329)
        ),
        modifier = Modifier
            .width(145.dp)
            .fillMaxHeight()
            .border(
                1.dp,
                if (isSoloed) Color(0xFF8A2BE2) else Color(0xFF2C254A),
                RoundedCornerShape(8.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Track Header
            Text(
                text = track.trackName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.trackType,
                fontSize = 9.sp,
                color = Color(0xFFB0A2C9)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pan Control Knob (Interactive representation)
            Text("PAN", fontSize = 8.sp, color = Color(0xFF8E82A9))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = { onPanChange((spatial.pan - 0.1f).coerceIn(-1.0f, 1.0f)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Pan Left", tint = Color.White)
                }
                Text(
                    text = when {
                        spatial.pan < -0.05f -> "${(spatial.pan * -100).toInt()}%L"
                        spatial.pan > 0.05f -> "${(spatial.pan * 100).toInt()}%R"
                        else -> "C"
                    },
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = { onPanChange((spatial.pan + 0.1f).coerceIn(-1.0f, 1.0f)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Pan Right", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Width Control (Slider)
            Text("WIDTH: ${spatial.stereoSpread.toInt()}%", fontSize = 8.sp, color = Color(0xFF8E82A9))
            Slider(
                value = spatial.stereoSpread,
                onValueChange = onWidthChange,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF8A2BE2),
                    thumbColor = Color.White
                ),
                modifier = Modifier.height(20.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Aux Sends (Reverb & Delay)
            Text("REVERB SEND", fontSize = 8.sp, color = Color(0xFF8E82A9))
            Slider(
                value = spatial.reverbSendDb,
                onValueChange = { onAuxChange(it, spatial.delaySendDb) },
                valueRange = -40f..0f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFFE040FB),
                    thumbColor = Color.White
                ),
                modifier = Modifier.height(20.dp)
            )

            Text("DELAY SEND", fontSize = 8.sp, color = Color(0xFF8E82A9))
            Slider(
                value = spatial.delaySendDb,
                onValueChange = { onAuxChange(spatial.reverbSendDb, it) },
                valueRange = -40f..0f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF00E5FF),
                    thumbColor = Color.White
                ),
                modifier = Modifier.height(20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fader (Decibel volume fader)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(modifier = Modifier.fillMaxHeight()) {
                    // Meter column
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF0F0C1B), RoundedCornerShape(4.dp))
                    ) {
                        // Simulated real-time level peaking
                        val peakRatio = ((gain.targetFaderLevelDb + 18f) / 18f).coerceIn(0.1f, 1.0f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(peakRatio)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFE57373), Color(0xFF81C784))
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Slider column
                    Box(modifier = Modifier.fillMaxHeight()) {
                        Slider(
                            value = gain.targetFaderLevelDb,
                            onValueChange = onFaderChange,
                            valueRange = -18.0f..6.0f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFF9575CD),
                                thumbColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(36.dp)
                                .rotateVertical()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${String.format("%+.1f", gain.targetFaderLevelDb)} dB",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Solo / Mute Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onSoloToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSoloed) Color(0xFFFBC02D) else Color(0xFF2C254A)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(width = 36.dp, height = 24.dp)
                        .testTag("solo_${track.trackId}"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("S", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSoloed) Color.Black else Color.White)
                }

                Button(
                    onClick = onMuteToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMuted) Color(0xFFE57373) else Color(0xFF2C254A)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(width = 36.dp, height = 24.dp)
                        .testTag("mute_${track.trackId}"),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("M", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Explain Select Trigger
            IconButton(
                onClick = onExplainSelect,
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFF21193D), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Explain decisions",
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MasterStrip(
    blueprint: MixingBlueprint,
    loudness: LoudnessIntelligence?,
    phase: PhaseIntelligence?,
    masterFaderLevelDb: Float,
    onMasterFaderChange: (Float) -> Unit,
    onSynthesizeReq: () -> Unit,
    onBounceClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1332)),
        modifier = Modifier
            .width(155.dp)
            .fillMaxHeight()
            .border(2.dp, Color(0xFF9c27b0), RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MASTER SUM",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "${blueprint.genreStyle} Standard",
                fontSize = 9.sp,
                color = Color(0xFFFFB74D),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Phase correlation indicator
            if (phase != null) {
                Text("PHASE: ${String.format("%.2f", phase.correlation)}", fontSize = 8.sp, color = Color(0xFFB0A2C9))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(Color(0xFF0F0C1B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val positionOffset = ((phase.correlation + 1f) / 2f).coerceIn(0.0f, 1.0f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(positionOffset)
                            .height(4.dp)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Master Limiter Ceiling
            Text("LIMITER CEILING", fontSize = 8.sp, color = Color(0xFFB0A2C9))
            Text("${blueprint.masterLimiterCeilingDb} dB", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(10.dp))

            // Master Fader
            Text("MASTER FADER", fontSize = 8.sp, color = Color(0xFFE040FB))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Slider(
                    value = masterFaderLevelDb,
                    onValueChange = onMasterFaderChange,
                    valueRange = -30.0f..6.0f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFFE040FB),
                        thumbColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(36.dp)
                        .rotateVertical()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${String.format("%+.1f", masterFaderLevelDb)} dB",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sum Peak & RMS Meters
            if (loudness != null) {
                Text("LUFS: ${String.format("%.1f", loudness.integratedLufs)}", fontSize = 10.sp, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("TruePeak: ${String.format("%.1f", loudness.truePeakDb)} dB", fontSize = 9.sp, color = Color.White)
                Text("Crest: ${String.format("%.1f", loudness.dynamicRangeDb)} dB", fontSize = 9.sp, color = Color(0xFFB0A2C9))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Synthesize re-synthesis trigger button row
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSynthesizeReq,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9c27b0)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(30.dp)
                ) {
                    Text("RE-MIX", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBounceClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(30.dp).testTag("mix_bounce_trigger_btn")
                ) {
                    Text("BOUNCE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Custom modifier to rotate sliders vertically for classic analog look
fun Modifier.rotateVertical(): Modifier {
    return this.layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = constraints.minHeight,
                maxWidth = constraints.maxHeight,
                minHeight = constraints.minWidth,
                maxHeight = constraints.maxWidth
            )
        )
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2)
            )
        }
    }
}
