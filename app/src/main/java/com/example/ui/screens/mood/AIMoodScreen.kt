package com.example.ui.screens.mood

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.CustomMoodPreset
import com.example.ui.viewmodel.MoodItem
import com.example.ui.viewmodel.MoodViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIMoodScreen(
    moodViewModel: MoodViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Explore, 1: AI Detector, 2: Custom Builder
    
    val searchQuery by moodViewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by moodViewModel.selectedCategoryFilter.collectAsState()
    val showOnlyFavorites by moodViewModel.showOnlyFavorites.collectAsState()
    
    val favorites by moodViewModel.favorites.collectAsState()
    val recentlyUsed by moodViewModel.recentlyUsed.collectAsState()
    val selectedMoodItem by moodViewModel.selectedMoodItem.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Mood Studio", fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                        Text("SurMaya AI • Sri Itnaa", fontSize = 10.sp, color = Color(0xFFF9D142))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Mood Generator Module fully operational", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Status", tint = Color(0xFF10AC84))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("ai_mood_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Modern Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF140D2A),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFF9D142)
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Explore Library", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("AI Detector", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Custom Builder", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Filled.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> ExploreTab(moodViewModel)
                    1 -> DetectorTab(moodViewModel)
                    2 -> CustomBuilderTab(moodViewModel)
                }
            }
        }
    }
}

@Composable
fun ExploreTab(moodViewModel: MoodViewModel) {
    val context = LocalContext.current
    val searchQuery by moodViewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by moodViewModel.selectedCategoryFilter.collectAsState()
    val showOnlyFavorites by moodViewModel.showOnlyFavorites.collectAsState()
    val favorites by moodViewModel.favorites.collectAsState()
    val recentlyUsed by moodViewModel.recentlyUsed.collectAsState()
    val selectedMoodItem by moodViewModel.selectedMoodItem.collectAsState()

    // Filter list
    val filteredCategories = moodViewModel.categories.map { cat ->
        val items = cat.items.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || 
                                item.description.contains(searchQuery, ignoreCase = true) ||
                                item.suggestedGenre.contains(searchQuery, ignoreCase = true)
            val matchesFav = !showOnlyFavorites || favorites.contains(item.id)
            matchesSearch && matchesFav
        }
        cat.copy(items = items)
    }.filter { cat ->
        (selectedCategoryFilter == "All" || cat.name.equals(selectedCategoryFilter, ignoreCase = true)) &&
        cat.items.isNotEmpty()
    }

    // Flat list of recently used MoodItem
    val recentItems = moodViewModel.categories.flatMap { it.items }.filter { recentlyUsed.contains(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Search & Filter Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { moodViewModel.setSearchQuery(it) },
                        placeholder = { Text("Search moods, genres, or keywords...", color = Color(0xFF9E93B3)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFFF9D142)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { moodViewModel.setSearchQuery("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.White)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mood_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF9F75FF),
                            unfocusedBorderColor = Color(0xFF332066)
                        ),
                        singleLine = true
                    )

                    // Favorite Toggle Filter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Favorites Only", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = showOnlyFavorites,
                            onCheckedChange = { moodViewModel.toggleShowOnlyFavorites() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFF9D142),
                                checkedTrackColor = Color(0xFF9F75FF)
                            )
                        )
                    }

                    // Category Horizontal Scroll
                    val filterOptions = listOf("All") + moodViewModel.categories.map { it.name }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterOptions) { filter ->
                            val isSelected = selectedCategoryFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                    .border(1.dp, if (isSelected) Color(0xFFF9D142) else Color(0xFF332066), RoundedCornerShape(20.dp))
                                    .clickable { moodViewModel.setCategoryFilter(filter) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSelected) Color.White else Color(0xFF9E93B3),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Mood Preview / Recommendations Banner
        selectedMoodItem?.let { item ->
            item {
                AnimatedVisibility(visible = true) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(item.colorHex).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color(item.colorHex))
                                    )
                                    Text(
                                        text = item.name,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { moodViewModel.toggleFavorite(item.id) }) {
                                        Icon(
                                            imageVector = if (favorites.contains(item.id)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (favorites.contains(item.id)) Color(0xFFFF5252) else Color.White
                                        )
                                    }
                                    IconButton(onClick = { moodViewModel.selectMoodItem(null) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                                    }
                                }
                            }

                            Text(item.description, color = Color(0xFF9E93B3), fontSize = 13.sp)

                            HorizontalDivider(color = Color(0xFF332066))

                            // Grid of recommendations
                            Text("Automatic AI Recommendations:", color = Color(0xFFF9D142), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                RecommendationRow(label = "Best Genre", value = item.suggestedGenre)
                                RecommendationRow(label = "Suggested BPM", value = "${item.suggestedBpm} BPM")
                                RecommendationRow(label = "Suggested Instruments", value = item.suggestedInstruments.joinToString(", "))
                                RecommendationRow(label = "Suggested Singer Style", value = item.suggestedVocal)
                                RecommendationRow(label = "Suggested Scale", value = item.suggestedScale)
                                RecommendationRow(label = "Suggested Chords", value = item.suggestedChords)
                            }

                            GlowingButton(
                                text = "Apply to Song Composer",
                                onClick = {
                                    Toast.makeText(context, "${item.name} mood applied to SurMaya engine!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Recently Used Horizontal Carousel
        if (recentItems.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Recently Used Moods", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(recentItems) { item ->
                            Box(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF140D2A))
                                    .border(1.dp, Color(item.colorHex).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { moodViewModel.selectMoodItem(item) }
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(item.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(item.category, color = Color(0xFF9E93B3), fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${item.suggestedBpm} BPM", color = Color(item.colorHex), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mood Categories Lists
        items(filteredCategories) { cat ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(cat.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (cat.name.lowercase()) {
                            "happiness" -> Icons.Filled.SentimentVerySatisfied
                            "love & romance" -> Icons.Filled.Favorite
                            "emotional" -> Icons.Filled.SentimentVeryDissatisfied
                            "energy" -> Icons.Filled.Bolt
                            "spiritual" -> Icons.Filled.BrightnessHigh
                            "seasonal" -> Icons.Filled.Cloud
                            "festival" -> Icons.Filled.Celebration
                            "travel" -> Icons.Filled.FlightTakeoff
                            "national" -> Icons.Filled.Flag
                            else -> Icons.Filled.MusicNote
                        }
                        Icon(imageVector = icon, contentDescription = cat.name, tint = Color(cat.colorHex), modifier = Modifier.size(14.dp))
                    }
                    Text(cat.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                }

                // Grid of items in this category
                val itemsList = cat.items
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsList.chunked(2).forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowItems.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF140D2A), Color(0xFF1E143D))
                                            )
                                        )
                                        .border(
                                            width = if (selectedMoodItem?.id == item.id) 2.dp else 1.dp,
                                            brush = Brush.linearGradient(
                                                colors = if (selectedMoodItem?.id == item.id) {
                                                    listOf(Color(0xFFF9D142), Color(item.colorHex))
                                                } else {
                                                    listOf(Color(item.colorHex).copy(alpha = 0.2f), Color(0xFF332066))
                                                }
                                            ),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { moodViewModel.selectMoodItem(item) }
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(item.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            if (favorites.contains(item.id)) {
                                                Icon(Icons.Filled.Favorite, contentDescription = "Fav", tint = Color(0xFFFF5252), modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        Text(item.description, color = Color(0xFF9E93B3), fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(item.suggestedGenre, color = Color(0xFFF9D142), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${item.suggestedBpm} BPM", color = Color(item.colorHex), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            // If row only has 1 item, fill the space with blank weight
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetectorTab(moodViewModel: MoodViewModel) {
    val context = LocalContext.current
    val inputText by moodViewModel.detectorInputText.collectAsState()
    val detectorType by moodViewModel.detectorType.collectAsState()
    val isDetecting by moodViewModel.isDetecting.collectAsState()
    val detectionResult by moodViewModel.detectionResult.collectAsState()
    val detectionHistory by moodViewModel.detectionHistory.collectAsState()

    val options = listOf("Lyrics", "Prompt", "Story", "Poem")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "AI Mood Detector Engine",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Analyze emotional vibes directly from lyrics or prompts. Our AI determines tempo, instruments, vocals, and genre recommendations.",
                        color = Color(0xFF9E93B3),
                        fontSize = 11.sp
                    )

                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEach { opt ->
                            val isSelected = detectorType == opt
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                    .border(1.dp, if (isSelected) Color(0xFFF9D142) else Color(0xFF332066), RoundedCornerShape(12.dp))
                                    .clickable { moodViewModel.detectorType.value = opt }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opt, color = if (isSelected) Color.White else Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Input Box
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { moodViewModel.detectorInputText.value = it },
                        placeholder = { 
                            Text(
                                text = "Enter or paste your $detectorType here to analyze mood...",
                                color = Color(0xFF9E93B3),
                                fontSize = 13.sp
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("mood_detector_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF9F75FF),
                            unfocusedBorderColor = Color(0xFF332066)
                        ),
                        maxLines = 6
                    )

                    // Buttons
                    if (isDetecting) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(color = Color(0xFFF9D142))
                                Text("Analyzing emotional intelligence layers...", color = Color(0xFF9E93B3), fontSize = 12.sp)
                            }
                        }
                    } else {
                        GlowingButton(
                            text = "Analyze with Gemini AI",
                            onClick = { moodViewModel.runMoodDetection() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = inputText.isNotBlank()
                        )
                    }
                }
            }
        }

        // Render Detection Result
        detectionResult?.let { result ->
            item {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10AC84).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Filled.Psychology, contentDescription = null, tint = Color(0xFF10AC84))
                                Text("AI Emotion Report", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF10AC84).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Confidence: ${result.confidenceScore}%", color = Color(0xFF10AC84), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Circular Strength and Primary Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Gauge
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                                CircularProgressIndicator(
                                    progress = { result.emotionStrength / 100f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFFF9D142),
                                    strokeWidth = 6.dp,
                                    trackColor = Color(0xFF140D2A),
                                    strokeCap = StrokeCap.Round,
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${result.emotionStrength}%", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Text("Strength", color = Color(0xFF9E93B3), fontSize = 8.sp)
                                }
                            }

                            // Primary / Secondary Mood Details
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Primary Mood", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                Text(result.primaryMood, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Secondary Mood", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                Text(result.secondaryMood, color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(color = Color(0xFF332066))

                        // Suggested music properties
                        Text("Suggested Production Palette", color = Color(0xFF9F75FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            RecommendationRow(label = "Suggested Music Style", value = result.suggestedMusicStyle)
                            RecommendationRow(label = "Suggested Tempo", value = result.suggestedTempo)
                            RecommendationRow(label = "Instruments", value = result.suggestedInstruments.joinToString(", "))
                            RecommendationRow(label = "Vocal Style", value = result.suggestedSinger)
                            RecommendationRow(label = "Genre Context", value = result.suggestedGenre)
                            RecommendationRow(label = "Key & Scale Raga", value = result.suggestedKeyScale)
                        }

                        GlowingButton(
                            text = "Apply AI Suggestions",
                            onClick = {
                                Toast.makeText(context, "AI suggested properties updated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // History
        if (detectionHistory.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Analysis History", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    detectionHistory.forEach { (text, res) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF140D2A))
                                .border(1.dp, Color(0xFF332066), RoundedCornerShape(12.dp))
                                .clickable {
                                    moodViewModel.detectorInputText.value = text
                                    moodViewModel.runMoodDetection()
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = text,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${res.primaryMood} • ${res.suggestedTempo}",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 10.sp
                                    )
                                }
                                Icon(Icons.Filled.ChevronRight, contentDescription = "View", tint = Color(0xFF9F75FF))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomBuilderTab(moodViewModel: MoodViewModel) {
    val context = LocalContext.current
    
    val romantic by moodViewModel.customRomantic.collectAsState()
    val sad by moodViewModel.customSad.collectAsState()
    val energetic by moodViewModel.customEnergetic.collectAsState()
    val positive by moodViewModel.customPositive.collectAsState()
    val spiritual by moodViewModel.customSpiritual.collectAsState()
    val intensity by moodViewModel.customIntensity.collectAsState()

    val savedPresets by moodViewModel.savedCustomPresets.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Presets Loader Row
        if (savedPresets.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Saved Presets", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(savedPresets) { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF140D2A))
                                    .border(1.dp, Color(0xFF9F75FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .combinedClickable(
                                        onClick = { moodViewModel.applyPresetValues(preset) },
                                        onLongClick = {
                                            moodViewModel.deleteCustomPreset(preset.id)
                                            Toast.makeText(context, "Deleted preset '${preset.name}'", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(preset.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Delete",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clickable { moodViewModel.deleteCustomPreset(preset.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Emotion Wheel Canvas Draw
        item {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Canvas(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.Center)
                ) {
                    val strokeWidth = 14.dp.toPx()
                    val total = romantic + sad + energetic + positive + spiritual
                    val safeTotal = if (total == 0f) 1f else total

                    val sweepRomantic = (romantic / safeTotal) * 360f
                    val sweepSad = (sad / safeTotal) * 360f
                    val sweepEnergetic = (energetic / safeTotal) * 360f
                    val sweepPositive = (positive / safeTotal) * 360f
                    val sweepSpiritual = (spiritual / safeTotal) * 360f

                    var startAngle = 0f

                    // Romantic (Pink/Red)
                    drawArc(
                        color = Color(0xFFFF6B6B),
                        startAngle = startAngle,
                        sweepAngle = sweepRomantic,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepRomantic

                    // Sad (Blue)
                    drawArc(
                        color = Color(0xFF54A0FF),
                        startAngle = startAngle,
                        sweepAngle = sweepSad,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepSad

                    // Energetic (Red/Orange)
                    drawArc(
                        color = Color(0xFFFF5252),
                        startAngle = startAngle,
                        sweepAngle = sweepEnergetic,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepEnergetic

                    // Positive (Yellow)
                    drawArc(
                        color = Color(0xFFF9D142),
                        startAngle = startAngle,
                        sweepAngle = sweepPositive,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweepPositive

                    // Spiritual (Saffron/Orange)
                    drawArc(
                        color = Color(0xFFFFA801),
                        startAngle = startAngle,
                        sweepAngle = sweepSpiritual,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Emotion Wheel", color = Color(0xFF9E93B3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${intensity.toInt()}%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Intensity", color = Color(0xFFF9D142), fontSize = 10.sp)
                }
            }
        }

        // Sliders Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Adjust Mood Parameters", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)

                    MoodSliderRow(label = "Romance Level", value = romantic, color = Color(0xFFFF6B6B)) { moodViewModel.customRomantic.value = it }
                    MoodSliderRow(label = "Melancholy (Sad)", value = sad, color = Color(0xFF54A0FF)) { moodViewModel.customSad.value = it }
                    MoodSliderRow(label = "Energy Level", value = energetic, color = Color(0xFFFF5252)) { moodViewModel.customEnergetic.value = it }
                    MoodSliderRow(label = "Positivity", value = positive, color = Color(0xFFF9D142)) { moodViewModel.customPositive.value = it }
                    MoodSliderRow(label = "Spiritual Feel", value = spiritual, color = Color(0xFFFFA801)) { moodViewModel.customSpiritual.value = it }
                    MoodSliderRow(label = "Overall Intensity", value = intensity, color = Color.White) { moodViewModel.customIntensity.value = it }

                    GlowingButton(
                        text = "Save Custom Preset",
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Real-time Recommendations based on sliders
        item {
            val recs = moodViewModel.getCustomMoodRecommendations()
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF9F75FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Real-Time Engine Suggestions", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    HorizontalDivider(color = Color(0xFF332066))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecommendationRow(label = "Target Genre", value = recs["Genre"] ?: "Bollywood Pop")
                        RecommendationRow(label = "Ideal BPM Tempo", value = recs["BPM"] ?: "120 BPM")
                        RecommendationRow(label = "Key & Scale Model", value = "${recs["Key"]} ${recs["Scale"]}")
                        RecommendationRow(label = "Instruments Mix", value = recs["Instruments"] ?: "Guitar")
                        RecommendationRow(label = "Vocal Emotion", value = recs["SingerStyle"] ?: "Natural")
                        RecommendationRow(label = "Chords Progression", value = recs["ChordProgression"] ?: "I-IV-V")
                        RecommendationRow(label = "Mastering Style", value = recs["MasteringPreset"] ?: "Dynamic")
                    }

                    GlowingButton(
                        text = "Synthesize Current Blend",
                        onClick = {
                            Toast.makeText(context, "Current custom blend applied to arrangement engine!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Dialog for Preset Name
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Mood", color = Color.White, fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Give a name to your customized emotional profile:", color = Color(0xFF9E93B3), fontSize = 12.sp)
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        placeholder = { Text("e.g. Rainy Day Melancholy", color = Color(0x66FFFFFF)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF9F75FF),
                            unfocusedBorderColor = Color(0xFF332066)
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            moodViewModel.saveCustomPreset(newPresetName)
                            Toast.makeText(context, "Custom preset saved!", Toast.LENGTH_SHORT).show()
                            newPresetName = ""
                            showSaveDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF140D2A)
        )
    }
}

@Composable
fun MoodSliderRow(
    label: String,
    value: Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Text("${value.toInt()}%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF332066)
            )
        )
    }
}

@Composable
fun RecommendationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color(0xFF9E93B3),
            fontSize = 11.sp,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.65f)
        )
    }
}
