package com.example.ui.screens.instrument

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.viewmodel.InstrumentViewModel
import com.example.ui.viewmodel.Instrument
import com.example.ui.viewmodel.StudioTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIInstrumentScreen(
    instrumentViewModel: InstrumentViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val instruments by instrumentViewModel.instruments.collectAsState()
    val searchQuery by instrumentViewModel.searchQuery.collectAsState()
    val selectedCategory by instrumentViewModel.selectedCategoryFilter.collectAsState()
    val showOnlyFavorites by instrumentViewModel.showOnlyFavorites.collectAsState()
    val selectedInstrument by instrumentViewModel.selectedInstrument.collectAsState()
    val recentInstrumentIds by instrumentViewModel.recentInstrumentIds.collectAsState()

    // Studio Tracks
    val studioTracks by instrumentViewModel.studioTracks.collectAsState()

    // Global Parameters
    val paramTempo by instrumentViewModel.paramTempo.collectAsState()
    val paramScale by instrumentViewModel.paramScale.collectAsState()
    val paramKey by instrumentViewModel.paramKey.collectAsState()
    val paramTimeSig by instrumentViewModel.paramTimeSignature.collectAsState()
    val paramGroove by instrumentViewModel.paramGroove.collectAsState()
    val paramSwing by instrumentViewModel.paramSwing.collectAsState()
    val paramComplexity by instrumentViewModel.paramComplexity.collectAsState()
    val paramHumanization by instrumentViewModel.paramHumanization.collectAsState()
    val paramStyle by instrumentViewModel.paramPerformanceStyle.collectAsState()
    val paramEnergy by instrumentViewModel.paramPerformanceEnergy.collectAsState()
    val paramVelocity by instrumentViewModel.paramVelocity.collectAsState()
    val paramWidth by instrumentViewModel.paramStereoWidth.collectAsState()
    val paramExpression by instrumentViewModel.paramExpression.collectAsState()

    // Rhythm Creator State
    val rhythmStyle by instrumentViewModel.rhythmStyle.collectAsState()
    val rhythmSpeed by instrumentViewModel.rhythmGrooveSpeed.collectAsState()
    val rhythmFill by instrumentViewModel.rhythmFillFrequency.collectAsState()
    val isRhythmPlaying by instrumentViewModel.isRhythmPlaying.collectAsState()

    // Melody Creator State
    val melodyRaga by instrumentViewModel.melodyRagaScale.collectAsState()
    val melodyComp by instrumentViewModel.melodyComplexity.collectAsState()
    val melodyHarm by instrumentViewModel.melodyHarmonyLevel.collectAsState()
    val isMelodyPlaying by instrumentViewModel.isMelodyPlaying.collectAsState()

    // Audio / Synthesis State
    val isPlayingPreview by instrumentViewModel.isPlayingPreview.collectAsState()
    val previewingInstrumentId by instrumentViewModel.previewingInstrumentId.collectAsState()
    val previewWaves by instrumentViewModel.previewWaves.collectAsState()

    val isGeneratingAI by instrumentViewModel.isGeneratingAI.collectAsState()
    val generationProgress by instrumentViewModel.generationProgress.collectAsState()
    val generatedOutputReport by instrumentViewModel.generatedOutputReport.collectAsState()
    val outputFormat by instrumentViewModel.selectedOutputFormat.collectAsState()

    var textPromptInput by remember { mutableStateOf("") }
    var configurationTab by remember { mutableStateOf("Studio Mixer") } // Studio Mixer, AI Generator, Rhythm & Melody
    var selectedTrackForEQ by remember { mutableStateOf<String?>(null) }

    val filteredInstruments = remember(instruments, searchQuery, selectedCategory, showOnlyFavorites) {
        instruments.filter { inst ->
            val matchesSearch = inst.name.contains(searchQuery, ignoreCase = true) || 
                                inst.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || inst.category.equals(selectedCategory, ignoreCase = true) ||
                                 inst.subCategories.any { it.contains(selectedCategory, ignoreCase = true) }
            val matchesFavorite = !showOnlyFavorites || inst.isFavorite
            matchesSearch && matchesCategory && matchesFavorite
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Instrument Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Virtual Acoustic Performance & Multi-Track Stems",
                            color = Color(0xFF9E93B3),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { instrumentViewModel.toggleShowOnlyFavorites() },
                        modifier = Modifier.testTag("instrument_favorite_filter_toggle")
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Show Favorites Only",
                            tint = if (showOnlyFavorites) Color(0xFFF9D142) else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF09041A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF09041A)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner image with custom visual asset
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    val context = LocalContext.current
                    val drawableId = context.resources.getIdentifier(
                        "ai_inst_hero_1783118754910", "drawable", context.packageName
                    )
                    if (drawableId != 0) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "AI Instrument Studio Hero",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Dark Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xDD09041A))
                                )
                            )
                    )
                    
                    // Banner Title Text
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Acoustic Instrument Synthesizer",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Synthesize expressive multi-track loops & regional Indian stems",
                            color = Color(0xFFF9D142),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search Bar & Categories Filter
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { instrumentViewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search acoustic instruments...", color = Color(0xFF9E93B3)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("instrument_search_input"),
                        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF9E93B3)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { instrumentViewModel.updateSearchQuery("") }) {
                                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear", tint = Color(0xFF9E93B3))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF1C1337),
                            focusedContainerColor = Color(0xFF140D2A),
                            unfocusedContainerColor = Color(0xFF140D2A)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )

                    // Categories Row
                    val filterCategories = listOf("All", "Percussion", "Strings", "Wind", "Keyboard", "Electronic", "Indian Classical", "Indian Folk", "Western")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterCategories) { category ->
                            val isSelected = category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color(0xFFF9D142) else Color(0xFF140D2A)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFF9D142) else Color(0x33FFD700),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { instrumentViewModel.updateCategoryFilter(category) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Horizontal Scroll of Instruments
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Acoustic Models Deck",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (filteredInstruments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF140D2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No instruments match your selection.",
                                color = Color(0xFF9E93B3),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredInstruments) { inst ->
                                val isSelected = inst.id == selectedInstrument?.id
                                val isInstPreviewing = isPlayingPreview && previewingInstrumentId == inst.id

                                Box(
                                    modifier = Modifier
                                        .width(225.dp)
                                        .height(185.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color(0xFF211648) else Color(0xFF140D2A))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFF9D142) else Color(0xFF1C1337),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { instrumentViewModel.selectInstrument(inst) }
                                        .padding(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Top Row: Category badge & Favorite
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF9F75FF).copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = inst.category,
                                                    color = Color(0xFFF9D142),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { instrumentViewModel.toggleFavorite(inst.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (inst.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                        contentDescription = "Favorite",
                                                        tint = if (inst.isFavorite) Color(0xFFF9D142) else Color(0xFF9E93B3),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Name, details, description
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(
                                                text = inst.name,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = inst.qualityLevel,
                                                    color = Color(0xFF9E93B3),
                                                    fontSize = 10.sp
                                                )
                                                Icon(
                                                    imageVector = when (inst.status) {
                                                        "Offline Ready" -> Icons.Filled.CheckCircle
                                                        "Downloading" -> Icons.Filled.Downloading
                                                        "Cloud AI Required" -> Icons.Filled.Cloud
                                                        else -> Icons.Filled.Download
                                                    },
                                                    contentDescription = inst.status,
                                                    tint = when (inst.status) {
                                                        "Offline Ready" -> Color(0xFF00FF88)
                                                        "Downloading" -> Color(0xFFF9D142)
                                                        "Cloud AI Required" -> Color(0xFF00C8FF)
                                                        else -> Color(0xFF9E93B3)
                                                    },
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = inst.description,
                                                color = Color(0xFFE5DFFF),
                                                fontSize = 10.sp,
                                                lineHeight = 13.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Bottom Row: Get Pack & Play preview
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!inst.isDownloaded && inst.status != "Cloud AI Required") {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color.White.copy(alpha = 0.05f))
                                                        .clickable { instrumentViewModel.startDownload(inst.id) }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Download,
                                                        contentDescription = "Get Pack",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (inst.status == "Downloading") "Getting..." else "Offline Pack",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CloudQueue,
                                                        contentDescription = "Status",
                                                        tint = Color(0xFF9F75FF),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (inst.status == "Cloud AI Required") "Cloud AI" else "Offline Ready",
                                                        color = Color(0xFF9E93B3),
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = { instrumentViewModel.togglePreview(inst) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isInstPreviewing) Color(0xFFF9D142) else Color(0xFF9F75FF))
                                            ) {
                                                Icon(
                                                    imageVector = if (isInstPreviewing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                    contentDescription = "Preview",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(14.dp)
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

            // Real-time Preview Waveform Box
            if (isPlayingPreview && selectedInstrument != null) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.VolumeUp,
                                        contentDescription = "Playing Preview",
                                        tint = Color(0xFFF9D142),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audible AI Preview: ${selectedInstrument?.name}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { instrumentViewModel.stopAudio() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Stop Preview",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            WaveformVisualizer(
                                waves = previewWaves,
                                activeColor = Color(0xFFF9D142),
                                inactiveColor = Color(0xFF9F75FF),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            )
                        }
                    }
                }
            }

            // Tabs for configurations: Studio Mixer, AI Generator, Rhythm & Melody
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140D2A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Studio Mixer", "AI Generator", "Rhythm & Melody")
                    tabs.forEach { tab ->
                        val isSel = tab == configurationTab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF9F75FF) else Color.Transparent)
                                .clickable { configurationTab = tab }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Primary Configuration Panels
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (configurationTab) {
                            "Studio Mixer" -> {
                                Text(
                                    text = "Multi-Instrument Studio Stems",
                                    color = Color(0xFFF9D142),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (studioTracks.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No active tracks. Select an instrument and add below.", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        studioTracks.forEach { track ->
                                            val isEqSelected = selectedTrackForEQ == track.id
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF140D2A))
                                                    .border(1.dp, Color(track.trackColorHex).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Header row: Color dot, Track name, Mute/Solo, Delete
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(track.trackColorHex))
                                                        )
                                                        Text(
                                                            text = track.instrument.name,
                                                            color = Color.White,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "(${track.instrument.category})",
                                                            color = Color(0xFF9E93B3),
                                                            fontSize = 9.sp
                                                        )
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        // EQ Toggle
                                                        IconButton(
                                                            onClick = { 
                                                                selectedTrackForEQ = if (isEqSelected) null else track.id 
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Tune,
                                                                contentDescription = "Equalizer",
                                                                tint = if (isEqSelected) Color(0xFFF9D142) else Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }

                                                        // Mute Button
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .background(if (track.isMuted) Color(0xFFFF5E5E) else Color.White.copy(alpha = 0.05f))
                                                                .clickable { instrumentViewModel.toggleMuteTrack(track.id) },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "M",
                                                                color = if (track.isMuted) Color.White else Color(0xFF9E93B3),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        // Solo Button
                                                        Box(
                                                            modifier = Modifier
                                                                .size(24.dp)
                                                                .clip(CircleShape)
                                                                .background(if (track.isSoloed) Color(0xFF00C8FF) else Color.White.copy(alpha = 0.05f))
                                                                .clickable { instrumentViewModel.toggleSoloTrack(track.id) },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "S",
                                                                color = if (track.isSoloed) Color.Black else Color(0xFF9E93B3),
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        // Harmony Mode
                                                        IconButton(
                                                            onClick = { instrumentViewModel.toggleTrackHarmony(track.id) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Group,
                                                                contentDescription = "Harmony",
                                                                tint = if (track.isHarmonyActive) Color(0xFF00FF88) else Color(0xFF9E93B3),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }

                                                        // Delete track
                                                        IconButton(
                                                            onClick = { instrumentViewModel.removeTrackFromStudio(track.id) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Delete,
                                                                contentDescription = "Remove Track",
                                                                tint = Color(0xFFFF5E5E),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                // Volume Slider
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.VolumeMute, contentDescription = "Volume", tint = Color(0xFF9E93B3), modifier = Modifier.size(12.dp))
                                                    Slider(
                                                        value = track.volume,
                                                        onValueChange = { instrumentViewModel.updateTrackVolume(track.id, it) },
                                                        valueRange = 0f..100f,
                                                        colors = SliderDefaults.colors(
                                                            thumbColor = Color(track.trackColorHex),
                                                            activeTrackColor = Color(track.trackColorHex)
                                                        ),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text("${track.volume.toInt()}%", color = Color.White, fontSize = 9.sp, modifier = Modifier.width(24.dp))
                                                }

                                                // EQ Panel (Conditional expanded dropdown slider deck)
                                                AnimatedVisibility(visible = isEqSelected) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 4.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text("3-Band Equalizer Adjust (dB)", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        
                                                        // Low Band
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("Bass Low", color = Color(0xFF9E93B3), fontSize = 9.sp, modifier = Modifier.width(60.dp))
                                                            Slider(
                                                                value = track.eqLow,
                                                                onValueChange = { instrumentViewModel.updateTrackEQ(track.id, it, track.eqMid, track.eqHigh) },
                                                                valueRange = -12f..12f,
                                                                modifier = Modifier.weight(1f),
                                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF), activeTrackColor = Color(0xFF9F75FF))
                                                            )
                                                            Text("${track.eqLow.toInt()}dB", color = Color.White, fontSize = 9.sp, modifier = Modifier.width(30.dp))
                                                        }

                                                        // Mid Band
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("Mid Core", color = Color(0xFF9E93B3), fontSize = 9.sp, modifier = Modifier.width(60.dp))
                                                            Slider(
                                                                value = track.eqMid,
                                                                onValueChange = { instrumentViewModel.updateTrackEQ(track.id, track.eqLow, it, track.eqHigh) },
                                                                valueRange = -12f..12f,
                                                                modifier = Modifier.weight(1f),
                                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF), activeTrackColor = Color(0xFF9F75FF))
                                                            )
                                                            Text("${track.eqMid.toInt()}dB", color = Color.White, fontSize = 9.sp, modifier = Modifier.width(30.dp))
                                                        }

                                                        // High Band
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text("High Treb", color = Color(0xFF9E93B3), fontSize = 9.sp, modifier = Modifier.width(60.dp))
                                                            Slider(
                                                                value = track.eqHigh,
                                                                onValueChange = { instrumentViewModel.updateTrackEQ(track.id, track.eqLow, track.eqMid, it) },
                                                                valueRange = -12f..12f,
                                                                modifier = Modifier.weight(1f),
                                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF), activeTrackColor = Color(0xFF9F75FF))
                                                            )
                                                            Text("${track.eqHigh.toInt()}dB", color = Color.White, fontSize = 9.sp, modifier = Modifier.width(30.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Button to add selected instrument to studio track layout
                                selectedInstrument?.let { inst ->
                                    Button(
                                        onClick = { instrumentViewModel.addTrackToStudio(inst) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_track_to_studio_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add", tint = Color.Black)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add '${inst.name}' to Multi-Track Studio", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }

                            "AI Generator" -> {
                                Text(
                                    text = "AI Performance Generator Prompt Deck",
                                    color = Color(0xFFF9D142),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Text Prompt Field
                                OutlinedTextField(
                                    value = textPromptInput,
                                    onValueChange = { textPromptInput = it },
                                    placeholder = { 
                                        Text(
                                            "Describe your desired performance (e.g. 'Generate a slow, traditional classical sitar solo in Yaman raga accompanied by a crisp, low-energy tabla theka')",
                                            color = Color(0xFF9E93B3),
                                            fontSize = 11.sp
                                        ) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .testTag("instrument_generator_prompt"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF9F75FF),
                                        unfocusedBorderColor = Color(0xFF1C1337),
                                        focusedContainerColor = Color(0xFF140D2A),
                                        unfocusedContainerColor = Color(0xFF140D2A)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Generation Parameters Sliders
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // Scale & Key Selection Deck
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Scale Raga", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            val scales = listOf("Yaman", "Bhairav", "Kalyani", "Bhairavi", "Minor", "Major")
                                            var expandedScale by remember { mutableStateOf(false) }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF140D2A))
                                                    .clickable { expandedScale = true }
                                                    .padding(10.dp)
                                            ) {
                                                Text(paramScale, color = Color.White, fontSize = 11.sp)
                                                DropdownMenu(
                                                    expanded = expandedScale,
                                                    onDismissRequest = { expandedScale = false }
                                                ) {
                                                    scales.forEach { s ->
                                                        DropdownMenuItem(
                                                            text = { Text(s) },
                                                            onClick = { 
                                                                instrumentViewModel.paramScale.value = s
                                                                expandedScale = false 
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Key Tuning", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            val keys = listOf("C#", "D", "G", "A", "E", "F")
                                            var expandedKey by remember { mutableStateOf(false) }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF140D2A))
                                                    .clickable { expandedKey = true }
                                                    .padding(10.dp)
                                            ) {
                                                Text(paramKey, color = Color.White, fontSize = 11.sp)
                                                DropdownMenu(
                                                    expanded = expandedKey,
                                                    onDismissRequest = { expandedKey = false }
                                                ) {
                                                    keys.forEach { k ->
                                                        DropdownMenuItem(
                                                            text = { Text(k) },
                                                            onClick = { 
                                                                instrumentViewModel.paramKey.value = k
                                                                expandedKey = false 
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Performance Style
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Performance Style Preset", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        val styles = listOf("Expressive Bollywood", "Classical Traditional", "Modern Fusion", "Cinematic Background")
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(styles) { style ->
                                                val isSelected = style == paramStyle
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) Color(0xFFF9D142) else Color(0xFF140D2A))
                                                        .border(1.dp, Color(0xFF1C1337), RoundedCornerShape(8.dp))
                                                        .clickable { instrumentViewModel.paramPerformanceStyle.value = style }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = style,
                                                        color = if (isSelected) Color.Black else Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFF1C1337))

                                    // Tempo Selector
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Tempo BPM: ${paramTempo.toInt()}", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                        Slider(
                                            value = paramTempo,
                                            onValueChange = { instrumentViewModel.paramTempo.value = it },
                                            valueRange = 60f..200f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF), activeTrackColor = Color(0xFF9F75FF))
                                        )
                                    }

                                    // Humanization
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Humanize: ${paramHumanization.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                        Slider(
                                            value = paramHumanization,
                                            onValueChange = { instrumentViewModel.paramHumanization.value = it },
                                            valueRange = 0f..100f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142), activeTrackColor = Color(0xFFF9D142))
                                        )
                                    }

                                    // Complexity
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Complexity: ${paramComplexity.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                        Slider(
                                            value = paramComplexity,
                                            onValueChange = { instrumentViewModel.paramComplexity.value = it },
                                            valueRange = 0f..100f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(thumbColor = Color(0xFF00FF88), activeTrackColor = Color(0xFF00FF88))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Generate / Stop Synthesis buttons
                                if (isGeneratingAI) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF140D2A))
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Synthesizing Neural Stems... ${(generationProgress * 100).toInt()}%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        LinearProgressIndicator(
                                            progress = { generationProgress },
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = Color(0xFFF9D142),
                                            trackColor = Color(0x22FFFFFF)
                                        )
                                        Button(
                                            onClick = { instrumentViewModel.stopAIGeneration() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E5E)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Cancel", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    GlowingButton(
                                        text = "Generate AI Acoustic Performance",
                                        onClick = { 
                                            val prompt = textPromptInput.ifBlank { "Unprompted classical raga jam" }
                                            instrumentViewModel.startAIGeneration(prompt, "", "Text Prompt Generator")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("submit_instrument_generation_btn")
                                    )
                                }

                                // Render Generated Output Logs & Export Panel
                                generatedOutputReport?.let { report ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("Generation Stems Success", color = Color(0xFF00FF88), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        
                                        // Report card
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF09041A))
                                                .border(1.dp, Color(0xFF1C1337), RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = report,
                                                color = Color(0xFFE5DFFF),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 12.sp
                                            )
                                        }

                                        // Export Selectors
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                val formats = listOf("WAV", "MP3", "FLAC", "MIDI")
                                                formats.forEach { fmt ->
                                                    val isSel = fmt == outputFormat
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (isSel) Color(0xFF00FF88) else Color(0xFF140D2A))
                                                            .clickable { instrumentViewModel.selectedOutputFormat.value = fmt }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(fmt, color = if (isSel) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = { /* Export simulation trigger */ },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(imageVector = Icons.Filled.Share, contentDescription = "Export", tint = Color.Black, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Export Mix", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            "Rhythm & Melody" -> {
                                // Rhythm section
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("AI Rhythm Creator Engine", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Groove Preset", color = Color.White, fontSize = 10.sp)
                                            val rhythmStyles = listOf("Tabla Keharwa", "Tabla Dadra", "Dholak Garba", "Lofi Drum Groove", "Carnatic Adi Tala")
                                            var showRhyMenu by remember { mutableStateOf(false) }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF140D2A))
                                                    .clickable { showRhyMenu = true }
                                                    .padding(8.dp)
                                            ) {
                                                Text(rhythmStyle, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                                DropdownMenu(expanded = showRhyMenu, onDismissRequest = { showRhyMenu = false }) {
                                                    rhythmStyles.forEach { s ->
                                                        DropdownMenuItem(text = { Text(s) }, onClick = { 
                                                            instrumentViewModel.rhythmStyle.value = s
                                                            showRhyMenu = false 
                                                        })
                                                    }
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Groove Speed", color = Color.White, fontSize = 10.sp)
                                            Slider(
                                                value = rhythmSpeed,
                                                onValueChange = { instrumentViewModel.rhythmGrooveSpeed.value = it },
                                                valueRange = 60f..180f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF))
                                            )
                                        }
                                    }

                                    // Play Rhythm Engine Button
                                    Button(
                                        onClick = { instrumentViewModel.toggleRhythmPlaying() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isRhythmPlaying) Color(0xFFFF5E5E) else Color(0xFF00FF88)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isRhythmPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                            contentDescription = "Rhythm",
                                            tint = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isRhythmPlaying) "Stop Rhythm Generator" else "Start Live Rhythm Generator",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF1C1337), modifier = Modifier.padding(vertical = 4.dp))

                                // Melody section
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("AI Melody Creator Engine", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Melodic Raga", color = Color.White, fontSize = 10.sp)
                                            val ragaScales = listOf("Raga Bhairav", "Raga Yaman", "Raga Bhairavi", "Raga Bilawal", "Raga Kafi")
                                            var showRagaMenu by remember { mutableStateOf(false) }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF140D2A))
                                                    .clickable { showRagaMenu = true }
                                                    .padding(8.dp)
                                            ) {
                                                Text(melodyRaga, color = Color.White, fontSize = 10.sp, maxLines = 1)
                                                DropdownMenu(expanded = showRagaMenu, onDismissRequest = { showRagaMenu = false }) {
                                                    ragaScales.forEach { s ->
                                                        DropdownMenuItem(text = { Text(s) }, onClick = { 
                                                            instrumentViewModel.melodyRagaScale.value = s
                                                            showRagaMenu = false 
                                                        })
                                                    }
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Complexity Level", color = Color.White, fontSize = 10.sp)
                                            Slider(
                                                value = melodyComp,
                                                onValueChange = { instrumentViewModel.melodyComplexity.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                            )
                                        }
                                    }

                                    // Play Melody Engine Button
                                    Button(
                                        onClick = { instrumentViewModel.toggleMelodyPlaying() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isMelodyPlaying) Color(0xFFFF5E5E) else Color(0xFF00C8FF)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMelodyPlaying) Icons.Filled.Stop else Icons.Filled.MusicNote,
                                            contentDescription = "Melody",
                                            tint = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isMelodyPlaying) "Stop Melodic Sitar Synthesizer" else "Start Live Melodic Sitar Synthesizer",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Instrument-Specific Parameters Panel
            selectedInstrument?.let { currentInst ->
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Instrument Parameters: ${currentInst.name}",
                                color = Color(0xFF9F75FF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            when (currentInst.id) {
                                "tabla" -> {
                                    val openStroke by instrumentViewModel.tabOpenStroke.collectAsState()
                                    val closedStroke by instrumentViewModel.tabClosedStroke.collectAsState()
                                    val speedMultiplier by instrumentViewModel.tabSpeed.collectAsState()
                                    val fillDensity by instrumentViewModel.tabFillDensity.collectAsState()
                                    val traditionalMode by instrumentViewModel.tabTraditionalMode.collectAsState()

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Rhythmic Theka Speed: $speedMultiplier", color = Color.White, fontSize = 10.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("Single (Ektaal)", "Double", "Chaugun").forEach { spd ->
                                                val isSelected = spd == speedMultiplier
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelected) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                                        .clickable { instrumentViewModel.tabSpeed.value = spd }
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(spd, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp)
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Gaba (Open Stroke)", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(120.dp))
                                            Slider(
                                                value = openStroke,
                                                onValueChange = { instrumentViewModel.tabOpenStroke.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Syahi (Closed Stroke)", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(120.dp))
                                            Slider(
                                                value = closedStroke,
                                                onValueChange = { instrumentViewModel.tabClosedStroke.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Fill Density", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(120.dp))
                                            Slider(
                                                value = fillDensity,
                                                onValueChange = { instrumentViewModel.tabFillDensity.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF00FF88))
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Traditional Gharana Mode", color = Color.White, fontSize = 10.sp)
                                            Switch(
                                                checked = traditionalMode,
                                                onCheckedChange = { instrumentViewModel.tabTraditionalMode.value = it },
                                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF9F75FF))
                                            )
                                        }
                                    }
                                }

                                "dholak" -> {
                                    val folkStyle by instrumentViewModel.dholFolkStyle.collectAsState()
                                    val grooveStr by instrumentViewModel.dholGrooveStrength.collectAsState()

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Folk Style Selection", color = Color.White, fontSize = 10.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("Bhangra", "Garba", "Qawwali", "Lavani").forEach { style ->
                                                val isSelected = style == folkStyle
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelected) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                                        .clickable { instrumentViewModel.dholFolkStyle.value = style }
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(style, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp)
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Groove Accent", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                            Slider(
                                                value = grooveStr,
                                                onValueChange = { instrumentViewModel.dholGrooveStrength.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                            )
                                        }
                                    }
                                }

                                "bansuri" -> {
                                    val fluteBreath by instrumentViewModel.fluteBreath.collectAsState()
                                    val fluteVib by instrumentViewModel.fluteVibrato.collectAsState()
                                    val fluteLeg by instrumentViewModel.fluteLegato.collectAsState()
                                    val fluteAir by instrumentViewModel.fluteAirNoise.collectAsState()

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Breath Power", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                            Slider(
                                                value = fluteBreath,
                                                onValueChange = { instrumentViewModel.fluteBreath.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF))
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Vocalic Vibrato", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                            Slider(
                                                value = fluteVib,
                                                onValueChange = { instrumentViewModel.fluteVibrato.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF))
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Smooth Legato", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                            Slider(
                                                value = fluteLeg,
                                                onValueChange = { instrumentViewModel.fluteLegato.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF))
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Chiff Air Noise", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(100.dp))
                                            Slider(
                                                value = fluteAir,
                                                onValueChange = { instrumentViewModel.fluteAirNoise.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF))
                                            )
                                        }
                                    }
                                }

                                "sitar" -> {
                                    val meend by instrumentViewModel.sitarMeend.collectAsState()
                                    val gamak by instrumentViewModel.sitarGamak.collectAsState()
                                    val sympathetic by instrumentViewModel.sitarSympathetic.collectAsState()
                                    val pickingStyle by instrumentViewModel.sitarPickingStyle.collectAsState()

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Classical Picking (Mizrab)", color = Color.White, fontSize = 10.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("Da-Ra-Di-Ra", "Da-Dir-Dir", "Continuous").forEach { st ->
                                                val isSelected = st == pickingStyle
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelected) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                                        .clickable { instrumentViewModel.sitarPickingStyle.value = st }
                                                        .padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(st, color = if (isSelected) Color.Black else Color.White, fontSize = 9.sp)
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Meend (Portamento)", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(120.dp))
                                            Slider(
                                                value = meend,
                                                onValueChange = { instrumentViewModel.sitarMeend.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Gamak (Ornamentation)", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(120.dp))
                                            Slider(
                                                value = gamak,
                                                onValueChange = { instrumentViewModel.sitarGamak.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Sympathetic Fret Buzz", color = Color(0xFF9E93B3), fontSize = 10.sp, modifier = Modifier.width(120.dp))
                                            Slider(
                                                value = sympathetic,
                                                onValueChange = { instrumentViewModel.sitarSympathetic.value = it },
                                                valueRange = 0f..100f,
                                                colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    // Generic modern strings / keyboard controls
                                    Text("Standard Neural performance parameters loaded for ${currentInst.name}. Feel free to use the global AI controls inside 'AI Generator' or multi-channel values in 'Studio Mixer' for full flexibility.", color = Color(0xFF9E93B3), fontSize = 11.sp, lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
