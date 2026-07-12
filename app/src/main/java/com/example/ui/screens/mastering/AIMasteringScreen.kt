package com.example.ui.screens.mastering

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.MasteringViewModel
import com.example.domain.model.mastering.*
import com.example.domain.mastering.MasteringResult
import com.example.core.audio.export.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMasteringScreen(
    viewModel: MasteringViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val projects by viewModel.projects.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val masteringResult by viewModel.masteringResult.collectAsState()
    val releaseBlueprint by viewModel.releaseBlueprint.collectAsState()
    val exportStatus by viewModel.exportStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val targetLoudness by viewModel.targetLoudness.collectAsState()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsState()
    val exciterIntensity by viewModel.exciterIntensity.collectAsState()
    val limiterCeiling by viewModel.limiterCeiling.collectAsState()
    val stereoWidthMultiplier by viewModel.stereoWidthMultiplier.collectAsState()
    val ditherBitDepth by viewModel.ditherBitDepth.collectAsState()

    var activeTab by remember { mutableStateOf("Mastering Console") } // "Mastering Console", "Release Package Builder", "AI Audit Specs"
    
    // Release metadata inputs
    var releaseTitle by remember { mutableStateOf("") }
    var releaseArtist by remember { mutableStateOf("") }
    var releaseIsrc by remember { mutableStateOf("") }
    var releaseUpc by remember { mutableStateOf("") }

    // Init fields when a new project loads
    LaunchedEffect(currentProject) {
        currentProject?.let {
            releaseTitle = it.title
            releaseArtist = "Artist"
            releaseIsrc = "IN-UM1-26-" + (10000..99999).random()
            releaseUpc = "8" + (10000000000..99999999999).random()
        }
    }

    // Gold/Champagne Luxury Palette
    val darkGoldBg = Color(0xFF0F0E0A)
    val trueGold = Color(0xFFE5C158)
    val paleGold = Color(0xFFD4AF37)
    val charcoalBg = Color(0xFF1E1C15)
    val textMuted = Color(0xFF9E998B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Mastering Suite (AIME)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "SurMaya AI - Release Engine (AIME)",
                            color = trueGold,
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
                    containerColor = darkGoldBg,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = darkGoldBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(darkGoldBg, Color(0xFF070604))
                    )
                )
        ) {
            if (currentProject == null) {
                // Onboarding & Project Selection/Creation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Album,
                        contentDescription = "AIME Mastering",
                        tint = trueGold,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AI Release & Mastering Studio",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Engage high-precision loudness normalization, multiband stereo correction, and tube-analog exciter processing to prepare your track for global distribution.",
                        fontSize = 14.sp,
                        color = textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 500.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    GlassCard(
                        modifier = Modifier.widthIn(max = 500.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Naya Mastering Project Shuru Karein",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = trueGold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            var tempTitle by remember { mutableStateOf("SurMaya Dynamic Master") }
                            var tempGenre by remember { mutableStateOf("Bollywood") }
                            val genresList = listOf("Bollywood", "Hollywood", "EDM", "Pop", "Classical", "Devotional", "Folk", "Rock", "Cinematic")

                            OutlinedTextField(
                                value = tempTitle,
                                onValueChange = { tempTitle = it },
                                label = { Text("Song Title / Project Name", color = textMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = trueGold,
                                    unfocusedBorderColor = charcoalBg,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("mastering_title_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Reference Genre Profile", color = textMuted, fontSize = 12.sp)
                            LazyRow(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(genresList) { g ->
                                    FilterChip(
                                        selected = tempGenre == g,
                                        onClick = { tempGenre = g },
                                        label = { Text(g) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = trueGold,
                                            selectedLabelColor = Color.Black,
                                            containerColor = charcoalBg,
                                            labelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            GlowingButton(
                                text = "AI MASTERING SYNTHESIS START",
                                onClick = {
                                    viewModel.createProject(tempTitle, tempGenre, -14.0f, listOf("Spotify", "Apple Music", "YouTube Music"))
                                },
                                modifier = Modifier.fillMaxWidth().testTag("create_project_button")
                            )
                        }
                    }

                    if (projects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Existing Master Sessions", color = trueGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.widthIn(max = 500.dp),
                            colors = CardDefaults.cardColors(containerColor = charcoalBg)
                        ) {
                            Column {
                                projects.forEach { proj ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectProject(proj.id) }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(proj.title, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text("Genre: ${proj.genreStyle} | Target: ${proj.targetLoudnessLufs} LUFS", color = textMuted, fontSize = 12.sp)
                                        }
                                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Open", tint = trueGold)
                                    }
                                    HorizontalDivider(color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            } else {
                // Active Mastering Studio Workspace
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Navigation Tabs inside mastering session
                    TabRow(
                        selectedTabIndex = when(activeTab) {
                            "Mastering Console" -> 0
                            "Release Package Builder" -> 1
                            else -> 2
                        },
                        containerColor = darkGoldBg,
                        contentColor = trueGold,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[when(activeTab) {
                                    "Mastering Console" -> 0
                                    "Release Package Builder" -> 1
                                    else -> 2
                                }]),
                                color = trueGold
                            )
                        }
                    ) {
                        Tab(
                            selected = activeTab == "Mastering Console",
                            onClick = { activeTab = "Mastering Console" },
                            text = { Text("🎚️ MASTERING STUDIO", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == "Release Package Builder",
                            onClick = { activeTab = "Release Package Builder" },
                            text = { Text("📀 RELEASE MANAGER", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = activeTab == "AI Audit Specs",
                            onClick = { activeTab = "AI Audit Specs" },
                            text = { Text("📊 TECHNICAL AUDIT", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Project status ribbon
                            Card(
                                colors = CardDefaults.cardColors(containerColor = charcoalBg),
                                border = BorderStroke(1.dp, trueGold.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Project: ${currentProject?.title}", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Profile: ${currentProject?.genreStyle} Master Suite", color = textMuted, fontSize = 12.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.remasterCurrentTrack() },
                                            colors = ButtonDefaults.buttonColors(containerColor = trueGold, contentColor = Color.Black),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Remaster")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("REMASTER", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.deleteProject(currentProject!!.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                            border = BorderStroke(1.dp, Color.Red),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Filled.Delete, contentDescription = "Exit")
                                        }
                                    }
                                }
                            }
                        }

                        if (activeTab == "Mastering Console") {
                            item {
                                // METERS AND VISUALIZERS SECTION
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // LUFS Meter
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("INTEGRATED LUFS METER", color = trueGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                                    .padding(8.dp)
                                            ) {
                                                // Dynamic width animation representing meter levels
                                                val progress = ((targetLoudness + 60) / 60f).coerceIn(0f, 1f)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(progress)
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                colors = listOf(Color(0xFFE5C158), Color(0xFFFF8F00))
                                                            ),
                                                            RoundedCornerShape(2.dp)
                                                        )
                                                )
                                                Text(
                                                    text = "${"%.1f".format(targetLoudness)} LUFS",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("-60", color = textMuted, fontSize = 9.sp)
                                                Text("-23 (TV)", color = textMuted, fontSize = 9.sp)
                                                Text("-14 (Web)", color = textMuted, fontSize = 9.sp)
                                                Text("-9 (CD)", color = textMuted, fontSize = 9.sp)
                                            }
                                        }
                                    }

                                    // True Peak Meter
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("TRUE PEAK METER (ISP)", color = trueGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(60.dp)
                                                    .background(Color.Black, RoundedCornerShape(4.dp))
                                                    .padding(8.dp)
                                            ) {
                                                val progress = ((limiterCeiling + 6) / 6f).coerceIn(0f, 1f)
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .fillMaxWidth(progress)
                                                        .background(Color(0xFF00C853), RoundedCornerShape(2.dp))
                                                )
                                                Text(
                                                    text = "${"%.2f".format(limiterCeiling)} dBTP",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("-6 dB", color = textMuted, fontSize = 9.sp)
                                                Text("-2 dB", color = textMuted, fontSize = 9.sp)
                                                Text("-1 dB", color = textMuted, fontSize = 9.sp)
                                                Text("0 dB", color = textMuted, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                // STEREO ANALYZER AND CORRELATION
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("STEREO CORRELATION & WIDTH", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(
                                                text = if (stereoWidthMultiplier > 1.3f) "⚠️ Stereo Collapse Risk" else "✅ Phase Coherent",
                                                color = if (stereoWidthMultiplier > 1.3f) Color.Yellow else Color.Green,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Correlation Slider visualization
                                        val correlationVal = (1.0f - (stereoWidthMultiplier - 1.0f) * 0.5f).coerceIn(-1.0f, 1.0f)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(16.dp)
                                                .background(Color.Black, RoundedCornerShape(8.dp))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .align(
                                                        Alignment.CenterStart
                                                    )
                                                    .offset(x = (((correlationVal + 1.0f) / 2.0f) * 300f).dp) // dummy responsive displacement
                                                    .background(trueGold, CircleShape)
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("-1.0 (Out of Phase)", color = Color.Red, fontSize = 9.sp)
                                            Text("0.0 (Wide Stereo)", color = textMuted, fontSize = 9.sp)
                                            Text("+1.0 (Mono Solid)", color = Color.Green, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }

                            item {
                                // DSP SUB-ENGINE CONTROLS
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("DSP SUB-ENGINES PARAMETER MATRIX", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Loudness Normalization Slider
                                        Text("Target Loudness Normalization: ${"%.1f".format(targetLoudness)} LUFS", color = Color.White, fontSize = 13.sp)
                                        Slider(
                                            value = targetLoudness,
                                            onValueChange = { viewModel.targetLoudness.value = it },
                                            valueRange = -24.0f..-6.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = trueGold,
                                                activeTrackColor = trueGold
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Limiter Ceiling Slider
                                        Text("True Peak Limiter Ceiling: ${"%.2f".format(limiterCeiling)} dBTP", color = Color.White, fontSize = 13.sp)
                                        Slider(
                                            value = limiterCeiling,
                                            onValueChange = { viewModel.limiterCeiling.value = it },
                                            valueRange = -3.0f..-0.1f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = trueGold,
                                                activeTrackColor = trueGold
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Exciter Intensity
                                        Text("Harmonic Saturation Intensity: ${"%.0f".format(exciterIntensity * 100)}%", color = Color.White, fontSize = 13.sp)
                                        Slider(
                                            value = exciterIntensity,
                                            onValueChange = { viewModel.exciterIntensity.value = it },
                                            valueRange = 0.0f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = trueGold,
                                                activeTrackColor = trueGold
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Stereo Width
                                        Text("Stereo Widening Coefficient: ${"%.2f".format(stereoWidthMultiplier)}x", color = Color.White, fontSize = 13.sp)
                                        Slider(
                                            value = stereoWidthMultiplier,
                                            onValueChange = { viewModel.stereoWidthMultiplier.value = it },
                                            valueRange = 0.5f..2.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = trueGold,
                                                activeTrackColor = trueGold
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Dither settings
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Dither Bit Depth Export", color = Color.White, fontSize = 13.sp)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                listOf("16 Bit", "24 Bit", "32 Float").forEach { b ->
                                                    ElevatedButton(
                                                        onClick = { viewModel.ditherBitDepth.value = b },
                                                        colors = ButtonDefaults.elevatedButtonColors(
                                                            containerColor = if (ditherBitDepth == b) trueGold else Color.DarkGray,
                                                            contentColor = if (ditherBitDepth == b) Color.Black else Color.White
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(b, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                // STREAMING PLATFORM NORMALIZATION METADATA MATRIX
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("TARGET PLATFORM CODEC SUITE", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        val platforms = listOf("Spotify", "Apple Music", "YouTube Music", "Amazon Music", "JioSaavn", "CD")
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            platforms.forEach { platform ->
                                                val isChecked = selectedPlatforms.contains(platform)
                                                val spec = when (platform) {
                                                    "Spotify" -> "-14.0 LUFS | -1.0 dBTP"
                                                    "Apple Music" -> "-16.0 LUFS | -1.0 dBTP"
                                                    "YouTube Music" -> "-14.0 LUFS | -1.0 dBTP"
                                                    "Amazon Music" -> "-14.0 LUFS | -1.0 dBTP"
                                                    "JioSaavn" -> "-14.0 LUFS | -1.0 dBTP"
                                                    else -> "-9.0 LUFS | -0.1 dBTP (RedBook)"
                                                }
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                        .padding(8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Checkbox(
                                                            checked = isChecked,
                                                            onCheckedChange = { viewModel.togglePlatform(platform) },
                                                            colors = CheckboxDefaults.colors(checkedColor = trueGold)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(platform, color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(spec, color = trueGold, fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                // REFERENCE GENRE MATCHING BLUEPRINT
                                masteringResult?.referenceMatching?.let { report ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("🎯 GENRE REFERENCE MATCHING MATRIX", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Spectral Match", color = textMuted, fontSize = 11.sp)
                                                    Text("${"%.1f".format(report.spectralMatchPercentage)}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Dynamics Match", color = textMuted, fontSize = 11.sp)
                                                    Text("${"%.1f".format(report.dynamicsMatchPercentage)}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Stereo Match", color = textMuted, fontSize = 11.sp)
                                                    Text("${"%.1f".format(report.stereoMatchPercentage)}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("AIME Recommendations:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            report.recommendations.forEach { r ->
                                                Text("• $r", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }

                        } else if (activeTab == "Release Package Builder") {
                            item {
                                // metadata input form
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("💿 GLOBAL DISTRIBUTOR METADATA", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = releaseTitle,
                                            onValueChange = { releaseTitle = it },
                                            label = { Text("Release Song Title", color = textMuted) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = trueGold, unfocusedBorderColor = darkGoldBg),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = releaseArtist,
                                            onValueChange = { releaseArtist = it },
                                            label = { Text("Primary Artist / Composer", color = textMuted) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = trueGold, unfocusedBorderColor = darkGoldBg),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = releaseIsrc,
                                            onValueChange = { releaseIsrc = it },
                                            label = { Text("ISRC (International Standard Recording Code)", color = textMuted) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = trueGold, unfocusedBorderColor = darkGoldBg),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = releaseUpc,
                                            onValueChange = { releaseUpc = it },
                                            label = { Text("UPC / EAN (Barcode Identification)", color = textMuted) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = trueGold, unfocusedBorderColor = darkGoldBg),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))

                                        GlowingButton(
                                            text = "GENERATE RELEASE DELIVERABLE PACKAGE",
                                            onClick = {
                                                viewModel.generateRelease(releaseTitle, releaseArtist, releaseIsrc, releaseUpc)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            if (releaseBlueprint != null) {
                                item {
                                    // RELEASE EXPORTS BLUEPRINT
                                    val r = releaseBlueprint!!
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = charcoalBg),
                                        border = BorderStroke(1.dp, trueGold)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("✅ RELEASE BLUEPRINT READY", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Ready", tint = Color.Green)
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text("Metadata Ledger:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Title: ${r.title}", color = Color.White, fontSize = 12.sp)
                                            Text("Artist: ${r.artist}", color = Color.White, fontSize = 12.sp)
                                            Text("ISRC: ${r.isrc}", color = Color.White, fontSize = 12.sp)
                                            Text("UPC/EAN: ${r.upcEan}", color = Color.White, fontSize = 12.sp)
                                            Text("Release Date: ${r.releaseDate}", color = Color.White, fontSize = 12.sp)
                                            Text("Final Audio LUFS: ${r.masteredLoudnessLufs} LUFS", color = Color.White, fontSize = 12.sp)
                                            Text("Final True Peak: ${r.masteredTruePeakDb} dBTP", color = Color.White, fontSize = 12.sp)

                                            Spacer(modifier = Modifier.height(12.dp))
                                            // --- Export Progress Panel ---
                                            when (val status = exportStatus) {
                                                is ExportStatus.Rendering -> {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1500)),
                                                        border = BorderStroke(1.dp, trueGold),
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(12.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text("⚡ OFFLINE BOUNCING & ENCODING...", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                                Text("${(status.progress * 100).toInt()}% (${String.format("%.1f", status.speedMultiplier)}x speed)", color = Color.White, fontSize = 11.sp)
                                                            }
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            LinearProgressIndicator(
                                                                progress = { status.progress },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                color = trueGold,
                                                                trackColor = Color(0x33, 0xD4, 0xAF, 0x37)
                                                            )
                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End
                                                            ) {
                                                                Button(
                                                                    onClick = { viewModel.cancelExport() },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                                    modifier = Modifier.height(32.dp).testTag("cancel_export_btn")
                                                                ) {
                                                                    Text("CANCEL", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                is ExportStatus.Success -> {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF001E00)),
                                                        border = BorderStroke(1.dp, Color.Green),
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(12.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text("✨ BOUNCE SUCCESSFUL!", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                Icon(imageVector = Icons.Filled.Check, contentDescription = "Done", tint = Color.Green, modifier = Modifier.size(16.dp))
                                                            }
                                                            Spacer(modifier = Modifier.height(6.dp))
                                                            Text("Format: ${status.format.displayName}", color = Color.White, fontSize = 12.sp)
                                                            Text("File: ${status.file.name}", color = Color.White, fontSize = 11.sp)
                                                            Text("Size: ${status.sizeKb} KB", color = Color.Gray, fontSize = 11.sp)
                                                            Text("Saved to: SurMaya_Exports/", color = Color.Gray, fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                                is ExportStatus.Failed -> {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0000)),
                                                        border = BorderStroke(1.dp, Color.Red),
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(12.dp)) {
                                                            Text("❌ EXPORT FAILED", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(status.error, color = Color.White, fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                                is ExportStatus.Cancelled -> {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C)),
                                                        border = BorderStroke(1.dp, Color.Gray),
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(12.dp)) {
                                                            Text("⏸ EXPORT CANCELLED", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        }
                                                    }
                                                }
                                                else -> {}
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("📁 EXPORT DELIVERABLE ASSETS (M3 ENGINE):", color = trueGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            val formats = listOf(
                                                com.example.core.audio.export.ExportFormat.WAV_24 to "WAV 24-bit (Lossless Studio Master)",
                                                com.example.core.audio.export.ExportFormat.WAV_16 to "WAV 16-bit (Lossless CD Quality)",
                                                com.example.core.audio.export.ExportFormat.WAV_32_FLOAT to "WAV 32-bit Float (High Dynamic Range)",
                                                com.example.core.audio.export.ExportFormat.FLAC to "FLAC Lossless Compressed (Archive)",
                                                com.example.core.audio.export.ExportFormat.MP3 to "MP3 High Quality (320 kbps CBR)",
                                                com.example.core.audio.export.ExportFormat.AAC to "AAC Streaming Optimized (256 kbps)",
                                                com.example.core.audio.export.ExportFormat.OGG to "OGG Vorbis (Streaming Web Format)"
                                            )

                                            formats.forEach { (fmt, label) ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 4.dp)
                                                        .background(Color(0xFF1C1C1C), RoundedCornerShape(4.dp))
                                                        .clickable {
                                                            viewModel.startExport(
                                                                fmt,
                                                                r.title,
                                                                r.artist,
                                                                "Indian Classical Fusion",
                                                                r.isrc,
                                                                r.upcEan
                                                            )
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Filled.PlayArrow,
                                                            contentDescription = "Export",
                                                            tint = trueGold,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(label, color = Color.White, fontSize = 12.sp)
                                                    }
                                                    Text(
                                                        "BOUNCE",
                                                        color = trueGold,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.testTag("export_btn_${fmt.name}")
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("Platform QC Status Matrix:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            r.releasePlatformStatuses.forEach { (plat, status) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("• $plat Normalization Match", color = Color.White, fontSize = 11.sp)
                                                    Text(status, color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // TECHNICAL AUDIT TAB
                            item {
                                masteringResult?.explainableReport?.let { report ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = charcoalBg)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("📊 EXPLAINABLE MASTERING DECISION REPORT", color = trueGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.height(12.dp))

                                            Text("1. Spectral & Equalization Blueprint:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(report.whyEQWasApplied, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                                            Text("2. Multiband Dynamics Glue Philosophy:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(report.whyCompressionWasApplied, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                                            Text("3. Spatial Field & MS Coherence Strategy:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(report.whyStereoWidthChanged, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                                            Text("4. Analog Harmonic Excitation Polish:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(report.whyHarmonicExcitationApplied, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                                            Text("5. Peak Headroom & ISPs Safety Guard:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(report.whyLimiterActed, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                                            Text("6. Loudness Matching Matrix (LUFS):", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(report.whyLoudnessChanged, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                                            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))
                                            Text("CTO Architectural Audit Verdict:", color = trueGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(report.overallSummary, color = Color.White, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = trueGold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            error?.let { err ->
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK", color = trueGold)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text(err)
                }
            }
        }
    }
}
