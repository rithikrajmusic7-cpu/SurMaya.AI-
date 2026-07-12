package com.example.ui.screens.singer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.viewmodel.SingerViewModel
import com.example.ui.viewmodel.VoiceCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISingerScreen(
    singerViewModel: SingerViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val voices by singerViewModel.voices.collectAsState()
    val searchQuery by singerViewModel.searchQuery.collectAsState()
    val selectedCategory by singerViewModel.selectedCategoryFilter.collectAsState()
    val showOnlyFavorites by singerViewModel.showOnlyFavorites.collectAsState()
    val selectedVoice by singerViewModel.selectedVoice.collectAsState()

    // Parameters States
    val paramPitch by singerViewModel.paramPitch.collectAsState()
    val paramTone by singerViewModel.paramTone.collectAsState()
    val paramEmotion by singerViewModel.paramEmotion.collectAsState()
    val paramExpression by singerViewModel.paramExpression.collectAsState()
    val paramVibrato by singerViewModel.paramVibrato.collectAsState()
    val paramBreathiness by singerViewModel.paramBreathiness.collectAsState()
    val paramPower by singerViewModel.paramPower.collectAsState()
    val paramSoftness by singerViewModel.paramSoftness.collectAsState()
    val paramPronunciation by singerViewModel.paramPronunciation.collectAsState()

    // Duet Mode States
    val duetType by singerViewModel.duetType.collectAsState()
    val duetS1Pitch by singerViewModel.duetSinger1Pitch.collectAsState()
    val duetS1Emotion by singerViewModel.duetSinger1Emotion.collectAsState()
    val duetS1Tone by singerViewModel.duetSinger1Tone.collectAsState()
    val duetS1Volume by singerViewModel.duetSinger1Volume.collectAsState()
    val duetS2Pitch by singerViewModel.duetSinger2Pitch.collectAsState()
    val duetS2Emotion by singerViewModel.duetSinger2Emotion.collectAsState()
    val duetS2Tone by singerViewModel.duetSinger2Tone.collectAsState()
    val duetS2Volume by singerViewModel.duetSinger2Volume.collectAsState()

    // Choir Mode States
    val choirCount by singerViewModel.choirSingersCount.collectAsState()
    val choirHarmony by singerViewModel.choirHarmonyDepth.collectAsState()
    val choirStereo by singerViewModel.choirStereoWidth.collectAsState()
    val choirStyle by singerViewModel.choirStyle.collectAsState()

    // Indian Classical Mode States
    val classicalSubtype by singerViewModel.classicalSubtype.collectAsState()
    val classicalMeend by singerViewModel.classicalMeend.collectAsState()
    val classicalGamak by singerViewModel.classicalGamak.collectAsState()
    val classicalMurki by singerViewModel.classicalMurki.collectAsState()

    // Rap Mode States
    val rapSpeed by singerViewModel.rapFlowSpeed.collectAsState()
    val rapAggression by singerViewModel.rapAggression.collectAsState()
    val rapTightness by singerViewModel.rapRhythmTightness.collectAsState()
    val rapStyle by singerViewModel.rapStyle.collectAsState()

    // Singing Controls States
    val ctrlTempo by singerViewModel.controlTempo.collectAsState()
    val ctrlKey by singerViewModel.controlKey.collectAsState()
    val ctrlScale by singerViewModel.controlScale.collectAsState()
    val ctrlOctave by singerViewModel.controlOctave.collectAsState()
    val ctrlReverb by singerViewModel.controlReverb.collectAsState()
    val ctrlDelay by singerViewModel.controlDelay.collectAsState()
    val ctrlStereoWidth by singerViewModel.controlStereoWidth.collectAsState()
    val ctrlAutoTune by singerViewModel.controlAutoTune.collectAsState()
    val ctrlBreathControl by singerViewModel.controlBreathControl.collectAsState()

    // Playback & Synthesis States
    val isPlayingPreview by singerViewModel.isPlayingPreview.collectAsState()
    val previewingVoiceId by singerViewModel.previewingVoiceId.collectAsState()
    val previewWaves by singerViewModel.previewWaves.collectAsState()

    val isSynthesizing by singerViewModel.isSynthesizing.collectAsState()
    val synthesisProgress by singerViewModel.synthesisProgress.collectAsState()
    val synthesizedOutput by singerViewModel.synthesizedLyricsOutput.collectAsState()
    val synthesizedDuration by singerViewModel.synthesizedAudioDuration.collectAsState()
    val outputFormat by singerViewModel.selectedOutputFormat.collectAsState()

    var userLyrics by remember { mutableStateOf("") }
    var configurationTab by remember { mutableStateOf("Basic Parameters") } // Basic Parameters, Advanced Singing, Special Modes

    // Filtered Voices List based on query, category, and favorite filter
    val filteredVoices = remember(voices, searchQuery, selectedCategory, showOnlyFavorites) {
        voices.filter { voice ->
            val matchesSearch = voice.name.contains(searchQuery, ignoreCase = true) || 
                                voice.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || voice.category.equals(selectedCategory, ignoreCase = true)
            val matchesFavorite = !showOnlyFavorites || voice.isFavorite
            matchesSearch && matchesCategory && matchesFavorite
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Singer Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Powered by SurMaya Neural Voice Models",
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
                        onClick = { singerViewModel.toggleShowOnlyFavorites() },
                        modifier = Modifier.testTag("favorite_filter_toggle")
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
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    val context = LocalContext.current
                    val drawableId = context.resources.getIdentifier(
                        "ai_singer_hero_1783117679733", "drawable", context.packageName
                    )
                    if (drawableId != 0) {
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = "AI Singer Studio",
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
                                    colors = listOf(Color.Transparent, Color(0xCC09041A))
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
                            text = "Acoustic Vocal Synthesizer",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Generate professional stems in custom formats",
                            color = Color(0xFFF9D142),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Search and Categories Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { singerViewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search virtual singers...", color = Color(0xFF9E93B3)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("singer_search_input"),
                        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color(0xFF9E93B3)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { singerViewModel.updateSearchQuery("") }) {
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

                    // Categories Tab Row
                    val filterCategories = listOf("All", "Male", "Female", "Kids", "Duet", "Choir", "Classical", "Special")
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
                                    .clickable { singerViewModel.updateCategoryFilter(category) }
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

            // Grid of Singers
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Available Neural Singers",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (filteredVoices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF140D2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No singers match your search/filter.",
                                color = Color(0xFF9E93B3),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // Horizontal Grid-like Scroll of Singers for easy exploration
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredVoices) { voice ->
                                val isSelected = voice.id == selectedVoice?.id
                                val isVoicePreviewing = isPlayingPreview && previewingVoiceId == voice.id

                                Box(
                                    modifier = Modifier
                                        .width(220.dp)
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color(0xFF211648) else Color(0xFF140D2A))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFFF9D142) else Color(0xFF1C1337),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { singerViewModel.selectVoice(voice) }
                                        .padding(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Header Row: Category Badge & Star
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
                                                    text = voice.category,
                                                    color = Color(0xFFF9D142),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Favorite Button
                                                IconButton(
                                                    onClick = { singerViewModel.toggleFavorite(voice.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (voice.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                                        contentDescription = "Favorite",
                                                        tint = if (voice.isFavorite) Color(0xFFF9D142) else Color(0xFF9E93B3),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Name, Version, and brief Description
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(
                                                text = voice.name,
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
                                                    text = voice.version,
                                                    color = Color(0xFF9E93B3),
                                                    fontSize = 10.sp
                                                )
                                                // Download status badges
                                                Icon(
                                                    imageVector = when (voice.status) {
                                                        "Offline Ready" -> Icons.Filled.CheckCircle
                                                        "Downloading" -> Icons.Filled.Downloading
                                                        "Cloud AI Required" -> Icons.Filled.Cloud
                                                        else -> Icons.Filled.Download
                                                    },
                                                    contentDescription = voice.status,
                                                    tint = when (voice.status) {
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
                                                text = voice.description,
                                                color = Color(0xFFE5DFFF),
                                                fontSize = 10.sp,
                                                lineHeight = 13.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Lower Action Row: Play Preview & Download Package
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Action/Download button
                                            if (!voice.isDownloaded && voice.status != "Cloud AI Required") {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color.White.copy(alpha = 0.05f))
                                                        .clickable { singerViewModel.startDownload(voice.id) }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Download,
                                                        contentDescription = "Download Model",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (voice.status == "Downloading") "Downloading..." else "Get Offline Pack",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.CloudQueue,
                                                        contentDescription = "Cloud Synced",
                                                        tint = Color(0xFF9F75FF),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (voice.status == "Cloud AI Required") "Cloud Only" else "Offline Ready",
                                                        color = Color(0xFF9E93B3),
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }

                                            // Play preview button
                                            IconButton(
                                                onClick = { singerViewModel.togglePreview(voice) },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isVoicePreviewing) Color(0xFFF9D142) else Color(0xFF9F75FF))
                                            ) {
                                                Icon(
                                                    imageVector = if (isVoicePreviewing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                                    contentDescription = "Preview Voice",
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
            if (isPlayingPreview && selectedVoice != null) {
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
                                        contentDescription = "Playing Vocal Preview",
                                        tint = Color(0xFFF9D142),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Audible AI Voice preview: ${selectedVoice?.name}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { singerViewModel.stopAudio() },
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

                            // Dynamic interactive waveform rendering
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

            // Tabs for configurations: Basic Parameters, Advanced Singing, Special Modes
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF140D2A))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Basic Parameters", "Special Modes", "Advanced Singing")
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

            // Detailed Parameters Configuration Panels
            item {
                selectedVoice?.let { currentVoice ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (configurationTab) {
                                "Basic Parameters" -> {
                                    // 1. PITCH SELECTION
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Voice Pitch Level", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            val pitches = listOf("Low", "Medium", "High")
                                            pitches.forEach { pitch ->
                                                val isSelected = pitch == paramPitch
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) Color(0xFFF9D142).copy(alpha = 0.15f) else Color(0x11FFFFFF))
                                                        .border(
                                                            1.dp,
                                                            if (isSelected) Color(0xFFF9D142) else Color.Transparent,
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable { singerViewModel.paramPitch.value = pitch }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = pitch,
                                                        color = if (isSelected) Color(0xFFF9D142) else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 2. TONE CHARACTER
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Voice Tone Color", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            val tones = listOf("Warm", "Bright", "Dark", "Deep", "Natural")
                                            items(tones) { tone ->
                                                val isSelected = tone == paramTone
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            if (isSelected) Color(0xFF9F75FF) else Color(0xFF140D2A)
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) Color(0xFF9F75FF) else Color(0x22FFFFFF),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable { singerViewModel.paramTone.value = tone }
                                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = tone,
                                                        color = if (isSelected) Color.Black else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 3. EMOTION ENGINE
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("AI Emotion Engine", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        val emotions = listOf("Happy", "Romantic", "Sad", "Angry", "Energetic", "Peaceful", "Devotional", "Patriotic", "Emotional")
                                        
                                        // Custom 3x3 wrap style layout using Rows
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            val chunked = emotions.chunked(3)
                                            chunked.forEach { rowEmotions ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    rowEmotions.forEach { emo ->
                                                        val isSelected = emo == paramEmotion
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isSelected) Color(0xFF9F75FF).copy(alpha = 0.15f) else Color(0xFF140D2A))
                                                                .border(
                                                                    1.dp,
                                                                    if (isSelected) Color(0xFF9F75FF) else Color(0xFF1C1337),
                                                                    RoundedCornerShape(8.dp)
                                                                )
                                                                .clickable { singerViewModel.paramEmotion.value = emo }
                                                                .padding(vertical = 8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = emo,
                                                                color = if (isSelected) Color.White else Color(0xFF9E93B3),
                                                                fontSize = 10.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // 4. MULTILINGUAL PRONUNCIATION
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Multilingual Pronunciation Dictionary", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        val languages = listOf("Standard Hindi", "Odia", "English", "Punjabi", "Tamil", "Telugu", "Bengali", "Marathi", "Custom")
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(languages) { lang ->
                                                val isSelected = lang == paramPronunciation
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSelected) Color(0xFFF9D142) else Color(0xFF140D2A))
                                                        .border(1.dp, Color(0xFF1C1337), RoundedCornerShape(10.dp))
                                                        .clickable { singerViewModel.paramPronunciation.value = lang }
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = lang,
                                                        color = if (isSelected) Color.Black else Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                "Special Modes" -> {
                                    // DYNAMIC CUSTOM OPTIONS BASED ON SINGERS
                                    Text(
                                        text = "Custom Settings for '${currentVoice.name}'",
                                        color = Color(0xFFF9D142),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (currentVoice.category == "Duet") {
                                        // DUET CONFIGURATIONS
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Duet Coupling Presets", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF9F75FF))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(duetType, color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                                }
                                            }

                                            // Toggle Couplings
                                            val duetCombos = listOf("Male + Female", "Male + Male", "Female + Female", "Adult + Kid", "Lead + Harmony")
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(duetCombos) { combo ->
                                                    val isSel = combo == duetType
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSel) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color.Transparent)
                                                            .border(1.dp, if (isSel) Color(0xFF9F75FF) else Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                                            .clickable { singerViewModel.duetType.value = combo }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(combo, color = Color.White, fontSize = 9.sp)
                                                    }
                                                }
                                            }

                                            HorizontalDivider(color = Color(0xFF1C1337))

                                            // Dual Singers Independent Parameters
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                // Singer 1 Column
                                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text("Singer 1 (Lead)", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    
                                                    Text("Volume: ${duetS1Volume.toInt()}%", color = Color(0xFF9E93B3), fontSize = 9.sp)
                                                    Slider(
                                                        value = duetS1Volume,
                                                        onValueChange = { singerViewModel.duetSinger1Volume.value = it },
                                                        valueRange = 0f..100f,
                                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF), activeTrackColor = Color(0xFF9F75FF))
                                                    )

                                                    Text("Tone: $duetS1Tone", color = Color(0xFF9E93B3), fontSize = 9.sp)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        listOf("Warm", "Bright", "Dark").forEach { t ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(if (duetS1Tone == t) Color(0xFF9F75FF) else Color(0x22000000))
                                                                    .clickable { singerViewModel.duetSinger1Tone.value = t }
                                                                    .padding(vertical = 4.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(t, color = Color.White, fontSize = 8.sp)
                                                            }
                                                        }
                                                    }
                                                }

                                                // Singer 2 Column
                                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text("Singer 2 (Harmony)", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    
                                                    Text("Volume: ${duetS2Volume.toInt()}%", color = Color(0xFF9E93B3), fontSize = 9.sp)
                                                    Slider(
                                                        value = duetS2Volume,
                                                        onValueChange = { singerViewModel.duetSinger2Volume.value = it },
                                                        valueRange = 0f..100f,
                                                        colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142), activeTrackColor = Color(0xFFF9D142))
                                                    )

                                                    Text("Tone: $duetS2Tone", color = Color(0xFF9E93B3), fontSize = 9.sp)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        listOf("Warm", "Bright", "Dark").forEach { t ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(4.dp))
                                                                    .background(if (duetS2Tone == t) Color(0xFFF9D142) else Color(0x22000000))
                                                                    .clickable { singerViewModel.duetSinger2Tone.value = t }
                                                                    .padding(vertical = 4.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(t, color = Color.White, fontSize = 8.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else if (currentVoice.category == "Choir") {
                                        // CHOIR CONFIGURATIONS
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Choir Ensemble Preset", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("Style: $choirStyle", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Choir styles chips
                                            val choirStyles = listOf("Indian Devotional", "Cinematic Choir", "Children Choir", "Mixed Choir")
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(choirStyles) { s ->
                                                    val isSel = s == choirStyle
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSel) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                                            .clickable { singerViewModel.choirStyle.value = s }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(s, color = if (isSel) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            // Choir sliders
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Number of Active Singers: $choirCount", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = choirCount.toFloat(),
                                                    onValueChange = { singerViewModel.choirSingersCount.value = it.toInt() },
                                                    valueRange = 4f..64f,
                                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142))
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Harmony Overlap Depth: ${choirHarmony.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = choirHarmony,
                                                    onValueChange = { singerViewModel.choirHarmonyDepth.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Stereo Fields Width: ${choirStereo.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = choirStereo,
                                                    onValueChange = { singerViewModel.choirStereoWidth.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }
                                        }
                                    } else if (currentVoice.category == "Classical") {
                                        // INDIAN CLASSICAL CONFIGURATIONS
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text("Classical Sub-Type (Gharana Tuning)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            val classicalTypes = listOf("Male Classical", "Female Classical", "Semi-Classical", "Bhajan", "Alaap", "Sargam", "Khayal-inspired", "Light Classical")
                                            
                                            // Flow Row replacement with chunked rows
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                classicalTypes.chunked(4).forEach { itemsRow ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        itemsRow.forEach { type ->
                                                            val isSel = type == classicalSubtype
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(if (isSel) Color(0xFFF9D142) else Color(0xFF140D2A))
                                                                    .clickable { singerViewModel.classicalSubtype.value = type }
                                                                    .padding(vertical = 6.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = type,
                                                                    color = if (isSel) Color.Black else Color.White,
                                                                    fontSize = 8.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Acoustic Ornamentations (Swar Shringar)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Meend (Glide Slur) Strength: ${classicalMeend.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = classicalMeend,
                                                    onValueChange = { singerViewModel.classicalMeend.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Gamak (Vocal Pulsation): ${classicalGamak.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = classicalGamak,
                                                    onValueChange = { singerViewModel.classicalGamak.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Murki (Rapid Grace Notes): ${classicalMurki.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = classicalMurki,
                                                    onValueChange = { singerViewModel.classicalMurki.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }
                                        }
                                    } else if (currentVoice.id == "mc_shanti") {
                                        // RAP MODE CONFIGURATIONS
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Rap Syllable Flow Style", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text(rapStyle, color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            val rapStyles = listOf("Street Style", "Commercial Style", "Trap Style", "Old School Style")
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                items(rapStyles) { s ->
                                                    val isSel = s == rapStyle
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(if (isSel) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                                            .clickable { singerViewModel.rapStyle.value = s }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(s, color = if (isSel) Color.Black else Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Flow Pace Speed: ${rapSpeed.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = rapSpeed,
                                                    onValueChange = { singerViewModel.rapFlowSpeed.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Vocal Aggression/Presence: ${rapAggression.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = rapAggression,
                                                    onValueChange = { singerViewModel.rapAggression.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text("Rhythm Beat Tightness: ${rapTightness.toInt()}%", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Slider(
                                                    value = rapTightness,
                                                    onValueChange = { singerViewModel.rapRhythmTightness.value = it },
                                                    valueRange = 0f..100f
                                                )
                                            }
                                        }
                                    } else {
                                        // Standard voice has custom modifiers
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("Analog Expression Parameters", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            
                                            Column {
                                                Text("Expression Level: ${paramExpression.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                Slider(value = paramExpression, onValueChange = { singerViewModel.paramExpression.value = it }, valueRange = 0f..100f)
                                            }
                                            Column {
                                                Text("Vocal Power (Chest Presence): ${paramPower.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                Slider(value = paramPower, onValueChange = { singerViewModel.paramPower.value = it }, valueRange = 0f..100f)
                                            }
                                            Column {
                                                Text("Breathiness (Acoustic Leak): ${paramBreathiness.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                Slider(value = paramBreathiness, onValueChange = { singerViewModel.paramBreathiness.value = it }, valueRange = 0f..100f)
                                            }
                                        }
                                    }
                                }

                                "Advanced Singing" -> {
                                    // SINGER METRIC AND ACOUSTIC CONFIGS
                                    Text("Neural Audio & Tuning Rack", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)

                                    // Key, Scale & Octave Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Key
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Scale Key", color = Color.White, fontSize = 10.sp)
                                            val keys = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "B")
                                            var keyExpanded by remember { mutableStateOf(false) }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF140D2A))
                                                    .clickable { keyExpanded = true }
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(ctrlKey, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                DropdownMenu(
                                                    expanded = keyExpanded,
                                                    onDismissRequest = { keyExpanded = false },
                                                    modifier = Modifier.background(Color(0xFF140D2A))
                                                ) {
                                                    keys.forEach { k ->
                                                        DropdownMenuItem(
                                                            text = { Text(k, color = Color.White) },
                                                            onClick = {
                                                                singerViewModel.controlKey.value = k
                                                                keyExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Scale
                                        Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Raga/Scale Mode", color = Color.White, fontSize = 10.sp)
                                            val scales = listOf("Major", "Minor", "Bhairav", "Yaman", "Kalyani")
                                            var scaleExpanded by remember { mutableStateOf(false) }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF140D2A))
                                                    .clickable { scaleExpanded = true }
                                                    .padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(ctrlScale, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                DropdownMenu(
                                                    expanded = scaleExpanded,
                                                    onDismissRequest = { scaleExpanded = false },
                                                    modifier = Modifier.background(Color(0xFF140D2A))
                                                ) {
                                                    scales.forEach { sc ->
                                                        DropdownMenuItem(
                                                            text = { Text(sc, color = Color.White) },
                                                            onClick = {
                                                                singerViewModel.controlScale.value = sc
                                                                scaleExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Octave
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("Octave Shift", color = Color.White, fontSize = 10.sp)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF140D2A)),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                IconButton(
                                                    onClick = { singerViewModel.controlOctave.value = (ctrlOctave - 1).coerceAtLeast(-2) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Remove, contentDescription = "Minus", tint = Color.White, modifier = Modifier.size(12.dp))
                                                }
                                                Text(
                                                    text = if (ctrlOctave >= 0) "+$ctrlOctave" else "$ctrlOctave",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(
                                                    onClick = { singerViewModel.controlOctave.value = (ctrlOctave + 1).coerceAtMost(2) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Plus", tint = Color.White, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Sliders for Tempo, Auto-Tune, Reverb, Delay
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Composition Tempo (BPM)", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                Text("${ctrlTempo.toInt()} BPM", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(value = ctrlTempo, onValueChange = { singerViewModel.controlTempo.value = it }, valueRange = 60f..200f)
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Auto-Tune: ${ctrlAutoTune.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                Slider(value = ctrlAutoTune, onValueChange = { singerViewModel.controlAutoTune.value = it }, valueRange = 0f..100f)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Reverb Space: ${ctrlReverb.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                Slider(value = ctrlReverb, onValueChange = { singerViewModel.controlReverb.value = it }, valueRange = 0f..100f)
                                            }
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Delay Echo: ${ctrlDelay.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                Slider(value = ctrlDelay, onValueChange = { singerViewModel.controlDelay.value = it }, valueRange = 0f..100f)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Stereo Width: ${ctrlStereoWidth.toInt()}%", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                Slider(value = ctrlStereoWidth, onValueChange = { singerViewModel.controlStereoWidth.value = it }, valueRange = 0f..100f)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Lyrics Box
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Vocal Lyrics Input",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = userLyrics,
                        onValueChange = { userLyrics = it },
                        placeholder = {
                            Text(
                                text = "Type or paste your lyrics here to generate vocal singing audio track...",
                                color = Color(0xFF9E93B3),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("singer_lyrics_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF1C1337),
                            focusedContainerColor = Color(0xFF140D2A),
                            unfocusedContainerColor = Color(0xFF140D2A)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Quick Sample Lyrics Filler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                userLyrics = """
                                    [Verse]
                                    Naye suron se saji hai raatein,
                                    SurMaya sang ho rahi hai baatein.
                                    Pyaar ki is nagri mein aao,
                                    Sangeet ki khushboo bikhraao!
                                """.trimIndent()
                            },
                            border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sample 1 (Hindi)", color = Color(0xFF9F75FF), fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                userLyrics = """
                                    [Chorus]
                                    SurMaya aalayam, isaiyin nava rāgam
                                    En idhayathil unathu kural thaan ketkuthu
                                    Swara ganga ponguthundhee anbe
                                    Nalla paattu unakkaga uruvaaguthu!
                                """.trimIndent()
                            },
                            border = BorderStroke(1.dp, Color(0xFFF9D142)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sample 2 (Tamil/Telugu)", color = Color(0xFFF9D142), fontSize = 10.sp)
                        }
                    }
                }
            }

            // Synthesis Settings and Trigger Button
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Export Format Configuration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vocal Output Format", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val formats = listOf("MP3", "WAV", "FLAC", "Vocal Stem")
                            formats.forEach { fmt ->
                                val isSelected = fmt == outputFormat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFF9D142) else Color(0xFF140D2A))
                                        .border(1.dp, Color(0xFF1C1337), RoundedCornerShape(8.dp))
                                        .clickable { singerViewModel.selectedOutputFormat.value = fmt }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = fmt,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Main Synthesize vocal button
                    if (isSynthesizing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(25.dp))
                                .background(Color(0xFF140D2A))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        color = Color(0xFFF9D142),
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Synthesizing Vocal Stems...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${(synthesisProgress * 100).toInt()}%", color = Color(0xFFF9D142), fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                            LinearProgressIndicator(
                                progress = { synthesisProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF9F75FF),
                                trackColor = Color(0xFF09041A)
                            )
                            Text(
                                text = "Running pitch tuning, phonetic pronunciation, and reverb layering on selected voice.",
                                color = Color(0xFF9E93B3),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        GlowingButton(
                            text = if (selectedVoice != null) "Synthesize Vocal with ${selectedVoice?.name}" else "Select a Singer to Synthesize",
                            onClick = { singerViewModel.startVocalSynthesis(userLyrics) },
                            enabled = selectedVoice != null && userLyrics.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("synthesize_vocal_btn")
                        )
                    }
                }
            }

            // Synthesized Output Report & Review Player
            synthesizedOutput?.let { report ->
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LibraryMusic,
                                        contentDescription = "Vocal Stem Output",
                                        tint = Color(0xFF00FF88),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Synthesis Successful!", color = Color(0xFF00FF88), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "Duration: $synthesizedDuration",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }

                            // Output Report Content
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF09041A))
                                    .border(1.dp, Color(0xFF1C1337), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = report,
                                    color = Color(0xFFE5DFFF),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }

                            // Share / Save Stems Row Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { /* Share simulated file */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Export Vocal Stem", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { /* Save project */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF140D2A)),
                                    border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Filled.Save, contentDescription = "Save Project", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add to Library", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Spacing at the bottom
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
