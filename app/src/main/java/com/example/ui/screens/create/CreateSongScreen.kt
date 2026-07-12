package com.example.ui.screens.create

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.domain.model.Song
import com.example.domain.model.Project
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.components.WaveformVisualizer
import com.example.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSongScreen(
    musicViewModel: MusicViewModel,
    initialStyle: String?, // Preset triggered from home
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val songGenState by musicViewModel.songGenerationState.collectAsState()
    val projects by musicViewModel.projects.collectAsState()
    val allSongs by musicViewModel.allSongs.collectAsState()

    // Form states
    var songTitle by remember { mutableStateOf("") }
    var songPrompt by remember { mutableStateOf("") }
    var songLyrics by remember { mutableStateOf("") }
    var selectedCreationMode by remember { mutableStateOf("Prompt") } // "Prompt", "Lyrics", "Story", "Emotion", "Image", "Video", "Continue", "Extend", "Remix"
    
    // Additional input states
    var songStory by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf("Shringara") }
    var customEmotion by remember { mutableStateOf("") }
    
    // Media Mock States
    var selectedMockImage by remember { mutableStateOf<String?>(null) }
    var userUploadedImageName by remember { mutableStateOf<String?>(null) }
    var selectedMockVideo by remember { mutableStateOf<String?>(null) }
    var userUploadedVideoName by remember { mutableStateOf<String?>(null) }
    
    // Continuation / Extension / Remix tracks
    var selectedTrackToModify by remember { mutableStateOf<Song?>(null) }
    var trackDropdownExpanded by remember { mutableStateOf(false) }
    var continuationPrompt by remember { mutableStateOf("") }
    var selectedExtensionDuration by remember { mutableStateOf("+1 Min") }
    var selectedExtensionSection by remember { mutableStateOf("Vocal Verse") }

    var selectedLanguage by remember { mutableStateOf("Hindi") }
    var selectedGenre by remember { mutableStateOf(initialStyle ?: "Bollywood") }
    var selectedMood by remember { mutableStateOf("Peaceful") }
    var selectedTempo by remember { mutableStateOf("Medium") }
    var selectedDuration by remember { mutableStateOf("2 Min") }
    
    var voiceGender by remember { mutableStateOf("Male") } // Male, Female, Duet, User Voice
    var voiceName by remember { mutableStateOf("Ajit") }
    var voiceMatchPercent by remember { mutableStateOf(85f) }
    var weirdnessPercent by remember { mutableStateOf(10f) }
    var styleInfluencePercent by remember { mutableStateOf(70f) }
    
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var selectedProjectName by remember { mutableStateOf("No Folder") }
    var mockUploadAudioPath by remember { mutableStateOf<String?>(null) }

    // AI Composer elements states
    var composeMelody by remember { mutableStateOf(true) }
    var composeChords by remember { mutableStateOf(true) }
    var composeHarmony by remember { mutableStateOf(true) }
    var composeRhythm by remember { mutableStateOf(true) }
    var composeHookLine by remember { mutableStateOf(true) }
    var composeChorus by remember { mutableStateOf(true) }
    var composeBridge by remember { mutableStateOf(true) }
    var composeIntro by remember { mutableStateOf(true) }
    var composeOutro by remember { mutableStateOf(true) }

    // Dropdown expanded states
    var langExpanded by remember { mutableStateOf(false) }
    var genreExpanded by remember { mutableStateOf(false) }
    var moodExpanded by remember { mutableStateOf(false) }
    var voiceExpanded by remember { mutableStateOf(false) }
    var projectExpanded by remember { mutableStateOf(false) }

    val languages = listOf("Hindi", "Odia", "English", "Bengali", "Punjabi", "Tamil", "Telugu", "Kannada", "Malayalam", "Marathi", "Bhojpuri", "Sanskrit")
    val genres = listOf("Bollywood", "Indian Pop", "Odia", "Bhajan", "Romantic", "Sad", "Rock", "Hip Hop", "Classical", "Folk", "Electronic")
    val moods = listOf("Calm", "Peaceful", "Energetic", "Happy", "Sad", "Nostalgic")
    val voices = listOf(
        "Ajit" to "Male",
        "Shrija" to "Female",
        "Sidhu Style" to "Male",
        "Lata Style" to "Female",
        "Sitar Synth" to "Duet",
        "My Voice Clone" to "User Voice"
    )

    // Periodic simulation waveform for generation page
    var generationWaves by remember { mutableStateOf(List(16) { 0.2f }) }
    LaunchedEffect(songGenState) {
        if (songGenState is com.example.ui.viewmodel.SongState.Loading) {
            while (true) {
                generationWaves = List(16) { 0.1f + Math.random().toFloat() * 0.8f }
                delay(120)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09041A))
    ) {
        when (val state = songGenState) {
            is com.example.ui.viewmodel.SongState.Idle -> {
                // Main Configuration Form
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("AI Music Composer", fontWeight = FontWeight.Black, color = Color.White) },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
                        )
                    },
                    containerColor = Color(0xFF09041A)
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Aesthetic Music Parameters",
                            color = Color(0xFFF9D142),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // AI Song Generator: Horizontal scrollable row of 9 composition modes
                        Text("Select Composition Source / Feature:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modesList = listOf(
                                Triple("Prompt", "From Prompt", Icons.Filled.MusicNote),
                                Triple("Lyrics", "From Lyrics", Icons.Filled.List),
                                Triple("Story", "From Story", Icons.Filled.Book),
                                Triple("Emotion", "From Emotion", Icons.Filled.Favorite),
                                Triple("Image", "From Image", Icons.Filled.Image),
                                Triple("Video", "From Video", Icons.Filled.Movie),
                                Triple("Continue", "Continue Song", Icons.Filled.SkipNext),
                                Triple("Extend", "Extend Song", Icons.Filled.Add),
                                Triple("Remix", "Remix Song", Icons.Filled.Tune)
                            )
                            modesList.forEach { (modeId, modeLabel, icon) ->
                                val isSelected = selectedCreationMode == modeId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCreationMode = modeId },
                                    label = {
                                        Text(
                                            text = modeLabel,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = modeLabel,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF9F75FF),
                                        selectedLabelColor = Color.Black,
                                        selectedLeadingIconColor = Color.Black,
                                        containerColor = Color(0xFF140D2A),
                                        labelColor = Color.White,
                                        iconColor = Color(0xFF9E93B3)
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFFF9D142) else Color(0xFF2E244E)
                                    ),
                                    modifier = Modifier.testTag("chip_mode_$modeId")
                                )
                            }
                        }

                        // Conditionally render input based on the chosen creation source
                        when (selectedCreationMode) {
                            "Prompt" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = songPrompt,
                                        onValueChange = { songPrompt = it },
                                        label = {
                                            Text(
                                                text = "Describe your song prompt (e.g. Sitar raga with high tempo beats)",
                                                color = Color(0xFF9E93B3)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF140D2A),
                                            unfocusedContainerColor = Color(0xFF140D2A)
                                        ),
                                        minLines = 3,
                                        maxLines = 5,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("prompt_input_field")
                                    )
                                    
                                    Text("Try Quick Suggestions:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val suggestions = listOf(
                                            "Sitar & Tabla fusion with upbeat Bollywood rhythms",
                                            "Sufi acoustic track with deep flute accents",
                                            "Classical Carnatic violin over cinematic sub-bass",
                                            "Modern Indian Pop with groovy Sarod licks"
                                        )
                                        suggestions.forEach { suggestion ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF1E143F))
                                                    .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(12.dp))
                                                    .clickable { songPrompt = suggestion }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(suggestion, color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                            "Lyrics" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = songLyrics,
                                        onValueChange = { songLyrics = it },
                                        label = {
                                            Text(
                                                text = "Type, paste, or select your custom song lyrics",
                                                color = Color(0xFF9E93B3)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF140D2A),
                                            unfocusedContainerColor = Color(0xFF140D2A)
                                        ),
                                        minLines = 4,
                                        maxLines = 6,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("lyrics_input_field")
                                    )
                                    
                                    Text("Quick Indian Lyric Stems:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val lyricTemplates = listOf(
                                            "Romantic Gazal" to "Tere khayal mein khoya hoon har dum,\nTu hi hai mera saaya, tu hi hai sargam.\nSitar ki dhun pe, dhadkan chalegi,\nTera bina yeh zindagi adhoori rahegi.",
                                            "Sufi Devotional" to "Dum dum mast qalandar, mera yaara,\nSufi raga gaata phire banjaara.\nBhakti ke rang mein ranga asmaan,\nHar saans mein tera hi naam o jaan.",
                                            "Festive Folk" to "Aayo re monsoon, barsat ki ritu aayi,\nSitar aur dholak ne masti machayi.\nKhet khalihaan sab jhoom uthe aaj,\nSur aur taal ka saj gaya hai taaj."
                                        )
                                        lyricTemplates.forEach { (name, content) ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF1E143F))
                                                    .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(12.dp))
                                                    .clickable { songLyrics = content }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(name, color = Color(0xFF9F75FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            "Story" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = songStory,
                                        onValueChange = { songStory = it },
                                        label = {
                                            Text(
                                                text = "Write or paste a story. AI will transform it into a musical ballad.",
                                                color = Color(0xFF9E93B3)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF140D2A),
                                            unfocusedContainerColor = Color(0xFF140D2A)
                                        ),
                                        minLines = 4,
                                        maxLines = 6,
                                        modifier = Modifier.fillMaxWidth().testTag("story_input_field")
                                    )
                                    
                                    Text("Select Story Archetype:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val storyArchetypes = listOf(
                                            "Radha & Krishna" to "An ancient spiritual story set in the lush gardens of Vrindavan, where a divine flute melody unites Radha and Krishna under a full monsoon moon, expressing cosmic devotion.",
                                            "Kalinga War Legend" to "A dramatic tale of Emperor Ashoka witnessing the tragic aftermath of the Kalinga battle on the banks of the Daya river, leading him to renounce violence and seek peace in Buddhism.",
                                            "Desert Caravan" to "A mesmerizing story of a wandering desert merchant and a classical folk dancer who lock eyes across a dusty campfire in Thar Desert, separated by borders but joined in song."
                                        )
                                        storyArchetypes.forEach { (title, narrative) ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFF1E143F))
                                                    .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(12.dp))
                                                    .clickable { songStory = narrative }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(title, color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                            "Emotion" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Select Aesthetic Indian Rasa (Emotion):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    
                                    val rasas = listOf(
                                        "Shringara" to "💖 Love & Romance",
                                        "Shanta" to "🕊️ Peace & Serenity",
                                        "Karuna" to "😢 Melancholy",
                                        "Bhakti" to "🙏 Devotion",
                                        "Veera" to "🔥 Heroism",
                                        "Adbhuta" to "🌀 Wonder"
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rasas.forEach { (rasaName, description) ->
                                            val active = selectedEmotion == rasaName
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (active) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color(0xFF140D2A))
                                                    .border(
                                                        1.dp, 
                                                        if (active) Color(0xFFF9D142) else Color(0xFF2E244E), 
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedEmotion = rasaName }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(description, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Rasa: $rasaName", color = if (active) Color(0xFFF9D142) else Color(0xFF9E93B3), fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                    
                                    OutlinedTextField(
                                        value = customEmotion,
                                        onValueChange = { customEmotion = it },
                                        label = { Text("Describe specific custom feeling (Optional)", color = Color(0xFF9E93B3)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF140D2A),
                                            unfocusedContainerColor = Color(0xFF140D2A)
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().testTag("custom_emotion_field")
                                    )
                                }
                            }
                            "Image" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("AI Visual Analyzer (Image to Song):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "AI analyzes the colors, depth, and elements of an image to compose an expressive raga style.",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 11.sp
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val imageOptions = listOf(
                                            "Himalayan Sunrise" to "🌄 Himalayan Sunrise",
                                            "Monsoon Ghats" to "🌧️ Varanasi Rain Ghats",
                                            "Peacock Forest" to "🦚 Peacock Dance"
                                        )
                                        imageOptions.forEach { (imgId, name) ->
                                            val active = selectedMockImage == imgId
                                            Box(
                                                modifier = Modifier
                                                    .width(140.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (active) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color(0xFF140D2A))
                                                    .border(
                                                        1.dp,
                                                        if (active) Color(0xFFF9D142) else Color(0xFF2E244E),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { 
                                                        selectedMockImage = imgId
                                                        userUploadedImageName = null
                                                    }
                                                    .padding(12.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("Calm, Bright, Sitar", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                    
                                    Button(
                                        onClick = {
                                            userUploadedImageName = "SurMaya_Image_Canvas.jpg"
                                            selectedMockImage = null
                                            Toast.makeText(context, "Uploaded 'SurMaya_Image_Canvas.jpg'! AI identified serene dawn color palettes.", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E143F)),
                                        border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                                        modifier = Modifier.fillMaxWidth().testTag("upload_image_btn")
                                    ) {
                                        Icon(Icons.Filled.Image, contentDescription = "Upload", tint = Color(0xFF9F75FF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (userUploadedImageName != null) "Change Image ($userUploadedImageName)" else "Upload Custom Image from Gallery",
                                            color = Color(0xFF9F75FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            "Video" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("AI Cinematic Scoring (Video to Song):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "AI tracks motion flow, scene transitions, and visual pace to generate synchronized cinematic background stems.",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 11.sp
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val videoOptions = listOf(
                                            "Taj Mahal Epic" to "🎬 Taj Mahal Aerial Scenery",
                                            "Kathak Jugalbandi" to "🎬 Kathak Dance Spins",
                                            "Bollywood Sunset" to "🎬 Sunset along Ghats"
                                        )
                                        videoOptions.forEach { (vidId, name) ->
                                            val active = selectedMockVideo == vidId
                                            Box(
                                                modifier = Modifier
                                                    .width(140.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (active) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color(0xFF140D2A))
                                                    .border(
                                                        1.dp,
                                                        if (active) Color(0xFFF9D142) else Color(0xFF2E244E),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { 
                                                        selectedMockVideo = vidId
                                                        userUploadedVideoName = null
                                                    }
                                                    .padding(12.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("Cinematic, Tabla, Fast", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                    
                                    Button(
                                        onClick = {
                                            userUploadedVideoName = "Vlog_Monsoon_Clip.mp4"
                                            selectedMockVideo = null
                                            Toast.makeText(context, "Uploaded 'Vlog_Monsoon_Clip.mp4'! AI identified classical cinematic pacing.", Toast.LENGTH_LONG).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E143F)),
                                        border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                                        modifier = Modifier.fillMaxWidth().testTag("upload_video_btn")
                                    ) {
                                        Icon(Icons.Filled.Movie, contentDescription = "Upload", tint = Color(0xFF9F75FF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (userUploadedVideoName != null) "Change Video ($userUploadedVideoName)" else "Upload Short Video clip (Max 30s)",
                                            color = Color(0xFF9F75FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            "Continue" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Select Song to Continue:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    
                                    if (allSongs.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1E143F))
                                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = "No songs found in history! Generate a song from Prompt/Lyrics first, then you can use Continue Mode.",
                                                color = Color(0xFFEFB8C8),
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else {
                                        Box {
                                            OutlinedButton(
                                                onClick = { trackDropdownExpanded = true },
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                                modifier = Modifier.fillMaxWidth().testTag("select_track_continue_btn")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = selectedTrackToModify?.title ?: "Choose a track to continue...",
                                                        color = if (selectedTrackToModify != null) Color.White else Color(0xFF9E93B3),
                                                        fontSize = 12.sp
                                                    )
                                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFFF9D142))
                                                }
                                            }
                                            
                                            DropdownMenu(
                                                expanded = trackDropdownExpanded,
                                                onDismissRequest = { trackDropdownExpanded = false }
                                            ) {
                                                allSongs.forEach { song ->
                                                    DropdownMenuItem(
                                                        text = { Text(song.title) },
                                                        onClick = {
                                                            selectedTrackToModify = song
                                                            trackDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        
                                        OutlinedTextField(
                                            value = continuationPrompt,
                                            onValueChange = { continuationPrompt = it },
                                            label = { Text("What should happen in the next section? (e.g., transition to high tempo beats)", color = Color(0xFF9E93B3)) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = Color(0xFFF9D142),
                                                unfocusedBorderColor = Color(0xFF2E244E),
                                                focusedContainerColor = Color(0xFF140D2A),
                                                unfocusedContainerColor = Color(0xFF140D2A)
                                            ),
                                            minLines = 2,
                                            modifier = Modifier.fillMaxWidth().testTag("continue_prompt_field")
                                        )
                                    }
                                }
                            }
                            "Extend" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Select Song to Extend:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    
                                    if (allSongs.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1E143F))
                                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = "No songs found in history! Generate a song from Prompt/Lyrics first, then you can use Extend Mode.",
                                                color = Color(0xFFEFB8C8),
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else {
                                        Box {
                                            OutlinedButton(
                                                onClick = { trackDropdownExpanded = true },
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                                modifier = Modifier.fillMaxWidth().testTag("select_track_extend_btn")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = selectedTrackToModify?.title ?: "Choose a track to extend...",
                                                        color = if (selectedTrackToModify != null) Color.White else Color(0xFF9E93B3),
                                                        fontSize = 12.sp
                                                    )
                                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFFF9D142))
                                                }
                                            }
                                            
                                            DropdownMenu(
                                                expanded = trackDropdownExpanded,
                                                onDismissRequest = { trackDropdownExpanded = false }
                                            ) {
                                                allSongs.forEach { song ->
                                                    DropdownMenuItem(
                                                        text = { Text(song.title) },
                                                        onClick = {
                                                            selectedTrackToModify = song
                                                            trackDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Text("Extend Duration By:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val durations = listOf("+30 Sec", "+1 Min", "+2 Min")
                                            durations.forEach { dur ->
                                                val active = selectedExtensionDuration == dur
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (active) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color(0xFF140D2A))
                                                        .border(
                                                            1.dp, 
                                                            if (active) Color(0xFFF9D142) else Color(0xFF2E244E), 
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable { selectedExtensionDuration = dur }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(dur, color = if (active) Color(0xFFF9D142) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        
                                        Text("Section Style to Append:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val sections = listOf("Vocal Verse", "Chorus Loop", "Alap Solo")
                                            sections.forEach { sec ->
                                                val active = selectedExtensionSection == sec
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (active) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color(0xFF140D2A))
                                                        .border(
                                                            1.dp, 
                                                            if (active) Color(0xFFF9D142) else Color(0xFF2E244E), 
                                                            RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable { selectedExtensionSection = sec }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(sec, color = if (active) Color(0xFFF9D142) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "Remix" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Select Song to Remix:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    
                                    if (allSongs.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1E143F))
                                                .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = "No songs found in history! Generate a song from Prompt/Lyrics first, then you can use Remix Mode.",
                                                color = Color(0xFFEFB8C8),
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else {
                                        Box {
                                            OutlinedButton(
                                                onClick = { trackDropdownExpanded = true },
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                                modifier = Modifier.fillMaxWidth().testTag("select_track_remix_btn")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = selectedTrackToModify?.title ?: "Choose a track to remix...",
                                                        color = if (selectedTrackToModify != null) Color.White else Color(0xFF9E93B3),
                                                        fontSize = 12.sp
                                                    )
                                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFFF9D142))
                                                }
                                            }
                                            
                                            DropdownMenu(
                                                expanded = trackDropdownExpanded,
                                                onDismissRequest = { trackDropdownExpanded = false }
                                            ) {
                                                allSongs.forEach { song ->
                                                    DropdownMenuItem(
                                                        text = { Text(song.title) },
                                                        onClick = {
                                                            selectedTrackToModify = song
                                                            trackDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1E143F))
                                                .border(1.dp, Color(0xFF9F75FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = "⚡ AI Remix Guide: Choose the new language, genre, voice, and tempo below. The AI will completely morph the original raga structure of '${selectedTrackToModify?.title ?: "the selected song"}' into your customized style!",
                                                color = Color(0xFF9F75FF),
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Basic Settings Dropdowns Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Language Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { langExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedLanguage, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Icon(Icons.Filled.ArrowDropDown, "Lang", tint = Color(0xFFF9D142), modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                                    languages.forEach { lang ->
                                        DropdownMenuItem(
                                            text = { Text(lang) },
                                            onClick = {
                                                selectedLanguage = lang
                                                langExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Genre Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { genreExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedGenre, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Icon(Icons.Filled.ArrowDropDown, "Genre", tint = Color(0xFFF9D142), modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(expanded = genreExpanded, onDismissRequest = { genreExpanded = false }) {
                                    genres.forEach { gen ->
                                        DropdownMenuItem(
                                            text = { Text(gen) },
                                            onClick = {
                                                selectedGenre = gen
                                                genreExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Mood & Tempo Dropdowns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { moodExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedMood, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Icon(Icons.Filled.ArrowDropDown, "Mood", tint = Color(0xFFF9D142), modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(expanded = moodExpanded, onDismissRequest = { moodExpanded = false }) {
                                    moods.forEach { md ->
                                        DropdownMenuItem(
                                            text = { Text(md) },
                                            onClick = {
                                                selectedMood = md
                                                moodExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Tempo presets
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF140D2A))
                                    .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(12.dp)),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Slow", "Med", "Fast").forEach { temp ->
                                    val act = (temp == "Slow" && selectedTempo == "Slow") ||
                                              (temp == "Med" && selectedTempo == "Medium") ||
                                              (temp == "Fast" && selectedTempo == "Fast")
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (act) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color.Transparent)
                                            .clickable { selectedTempo = if (temp == "Med") "Medium" else temp }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(temp, color = if (act) Color(0xFFF9D142) else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Singer Selection & Sliders Card (More options section from OCR)
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Voice Customization Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Choose Voice Name
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { voiceExpanded = true },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x33000000)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("$voiceName ($voiceGender)", color = Color.White, fontSize = 11.sp)
                                                Icon(Icons.Filled.VoiceOverOff, "Voice", tint = Color(0xFF9F75FF), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        DropdownMenu(expanded = voiceExpanded, onDismissRequest = { voiceExpanded = false }) {
                                            voices.forEach { (name, gender) ->
                                                DropdownMenuItem(
                                                    text = { Text("$name ($gender)") },
                                                    onClick = {
                                                        voiceName = name
                                                        voiceGender = gender
                                                        voiceExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Record / Upload Custom Voice button
                                    Button(
                                        onClick = { onNavigateToUpload() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF).copy(alpha = 0.2f)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.wrapContentWidth()
                                    ) {
                                        Icon(imageVector = Icons.Filled.Mic, contentDescription = "Mic", tint = Color(0xFF9F75FF), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Voice Clone", color = Color(0xFF9F75FF), fontSize = 11.sp)
                                    }
                                }

                                // Voice Match slider
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Voice Match Level", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        Text("${voiceMatchPercent.toInt()}%", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = voiceMatchPercent,
                                        onValueChange = { voiceMatchPercent = it },
                                        valueRange = 0f..100f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFFF9D142), activeTrackColor = Color(0xFFF9D142))
                                    )
                                }

                                // Style Influence slider
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Style Influence vs Originality", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        Text("${styleInfluencePercent.toInt()}%", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = styleInfluencePercent,
                                        onValueChange = { styleInfluencePercent = it },
                                        valueRange = 0f..100f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF9F75FF), activeTrackColor = Color(0xFF9F75FF))
                                    )
                                }
                            }
                        }

                        // AI Composer: Stems & Structural Elements Card
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("AI Composer Stems & Structure", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Configure composition elements to generate", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                    }
                                    
                                    // Compact Select All toggle
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF9F75FF).copy(alpha = 0.15f))
                                            .clickable {
                                                val anyOff = !composeMelody || !composeChords || !composeHarmony || !composeRhythm ||
                                                        !composeHookLine || !composeChorus || !composeBridge || !composeIntro || !composeOutro
                                                composeMelody = anyOff
                                                composeChords = anyOff
                                                composeHarmony = anyOff
                                                composeRhythm = anyOff
                                                composeHookLine = anyOff
                                                composeChorus = anyOff
                                                composeBridge = anyOff
                                                composeIntro = anyOff
                                                composeOutro = anyOff
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text("Toggle All", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Flow-like Row grid of 9 options
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val row1 = listOf(
                                        Triple("Melody", composeMelody, { composeMelody = !composeMelody }),
                                        Triple("Chords", composeChords, { composeChords = !composeChords }),
                                        Triple("Harmony", composeHarmony, { composeHarmony = !composeHarmony })
                                    )
                                    val row2 = listOf(
                                        Triple("Rhythm", composeRhythm, { composeRhythm = !composeRhythm }),
                                        Triple("Hook Line", composeHookLine, { composeHookLine = !composeHookLine }),
                                        Triple("Chorus", composeChorus, { composeChorus = !composeChorus })
                                    )
                                    val row3 = listOf(
                                        Triple("Bridge", composeBridge, { composeBridge = !composeBridge }),
                                        Triple("Intro", composeIntro, { composeIntro = !composeIntro }),
                                        Triple("Outro", composeOutro, { composeOutro = !composeOutro })
                                    )

                                    listOf(row1, row2, row3).forEach { rowItems ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            rowItems.forEach { (label, isSelected, toggle) ->
                                                val activeColor = Color(0xFFF9D142)
                                                val inactiveColor = Color(0xFF2E244E)
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSelected) Color(0xFF9F75FF).copy(alpha = 0.15f) else Color(0x22000000))
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) activeColor else inactiveColor,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable { toggle() }
                                                        .padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSelected) Icons.Filled.Check else Icons.Filled.Add,
                                                            contentDescription = label,
                                                            tint = if (isSelected) activeColor else Color(0xFF9E93B3),
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = label,
                                                            color = if (isSelected) Color.White else Color(0xFF9E93B3),
                                                            fontSize = 10.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Project Selector & Optional Title Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = songTitle,
                                onValueChange = { songTitle = it },
                                label = { Text("Song Title (Optional)", color = Color(0xFF9E93B3), fontSize = 11.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFF9D142),
                                    unfocusedBorderColor = Color(0xFF2E244E)
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )

                            // Save to Project/Folder Dropdown Selector
                            Box(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                                OutlinedButton(
                                    onClick = { projectExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(selectedProjectName, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Icon(Icons.Filled.Folder, "Project", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp))
                                    }
                                }
                                DropdownMenu(expanded = projectExpanded, onDismissRequest = { projectExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("No Folder") },
                                        onClick = {
                                            selectedProjectId = null
                                            selectedProjectName = "No Folder"
                                            projectExpanded = false
                                        }
                                    )
                                    projects.forEach { proj ->
                                        DropdownMenuItem(
                                            text = { Text(proj.name) },
                                            onClick = {
                                                selectedProjectId = proj.id
                                                selectedProjectName = proj.name
                                                projectExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Compile creation request button (deducts 5 credits)
                        GlowingButton(
                            text = "Compose Song (Cost: 5 Credits)",
                            onClick = {
                                val finalTitle = if (songTitle.isNotBlank()) songTitle else {
                                    when (selectedCreationMode) {
                                        "Prompt" -> if (songPrompt.length > 15) songPrompt.take(15) + "..." else "Prompt Composition"
                                        "Lyrics" -> "Lyrics Composition"
                                        "Story" -> "Story Ballad"
                                        "Emotion" -> "Rasa - $selectedEmotion"
                                        "Image" -> "Visual Score (${selectedMockImage ?: userUploadedImageName ?: "Custom Image"})"
                                        "Video" -> "Cinematic Score (${selectedMockVideo ?: userUploadedVideoName ?: "Custom Video"})"
                                        "Continue" -> "Cont. of ${selectedTrackToModify?.title ?: "Track"}"
                                        "Extend" -> "Ext. of ${selectedTrackToModify?.title ?: "Track"}"
                                        "Remix" -> "Remix of ${selectedTrackToModify?.title ?: "Track"}"
                                        else -> "SurMaya AI Song"
                                    }
                                }

                                val compositionStemsList = mutableListOf<String>()
                                if (composeMelody) compositionStemsList.add("Melody")
                                if (composeChords) compositionStemsList.add("Chords")
                                if (composeHarmony) compositionStemsList.add("Harmony")
                                if (composeRhythm) compositionStemsList.add("Rhythm")
                                if (composeHookLine) compositionStemsList.add("Hook Line")
                                if (composeChorus) compositionStemsList.add("Chorus")
                                if (composeBridge) compositionStemsList.add("Bridge")
                                if (composeIntro) compositionStemsList.add("Intro")
                                if (composeOutro) compositionStemsList.add("Outro")
                                val compositionStemsStr = if (compositionStemsList.isNotEmpty()) {
                                    " [AI Composer Settings - Generate: ${compositionStemsList.joinToString(", ")}]"
                                } else " [AI Composer Settings - Default]"

                                val basePrompt = when (selectedCreationMode) {
                                    "Prompt" -> songPrompt
                                    "Lyrics" -> "Custom Lyrics Melody: ${songLyrics.take(30)}..."
                                    "Story" -> "Based on Story: $songStory"
                                    "Emotion" -> "Rasa/Emotion: $selectedEmotion. Custom Feeling: $customEmotion"
                                    "Image" -> "Generated from Image: ${selectedMockImage ?: userUploadedImageName ?: "Uploaded Image"}"
                                    "Video" -> "Cinematic audio score synchronized to video: ${selectedMockVideo ?: userUploadedVideoName ?: "Uploaded Video"}"
                                    "Continue" -> "Continuation segment of '${selectedTrackToModify?.title}'. Details: $continuationPrompt"
                                    "Extend" -> "Extension of '${selectedTrackToModify?.title}' by $selectedExtensionDuration. Target section: $selectedExtensionSection"
                                    "Remix" -> "Remix of '${selectedTrackToModify?.title}' in $selectedGenre style"
                                    else -> "SurMaya prompt"
                                }
                                val finalPrompt = basePrompt + compositionStemsStr

                                val finalLyrics = if (selectedCreationMode == "Lyrics") songLyrics else "[Instrumental Raga Accent]"

                                // Validation
                                when (selectedCreationMode) {
                                    "Prompt" -> {
                                        if (songPrompt.isBlank()) {
                                            Toast.makeText(context, "Please enter a song prompt description", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                    "Lyrics" -> {
                                        if (songLyrics.isBlank()) {
                                            Toast.makeText(context, "Please write or paste your song lyrics", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                    "Story" -> {
                                        if (songStory.isBlank()) {
                                            Toast.makeText(context, "Please write or paste a story narrative", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                    "Image" -> {
                                        if (selectedMockImage == null && userUploadedImageName == null) {
                                            Toast.makeText(context, "Please select a mock image or upload an image", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                    "Video" -> {
                                        if (selectedMockVideo == null && userUploadedVideoName == null) {
                                            Toast.makeText(context, "Please select a mock video or upload a video clip", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                    "Continue" -> {
                                        if (selectedTrackToModify == null) {
                                            Toast.makeText(context, "Please select a song from your history to continue", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                        if (continuationPrompt.isBlank()) {
                                            Toast.makeText(context, "Please enter prompt details for continuation", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                    "Extend" -> {
                                        if (selectedTrackToModify == null) {
                                            Toast.makeText(context, "Please select a song from your history to extend", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                    "Remix" -> {
                                        if (selectedTrackToModify == null) {
                                            Toast.makeText(context, "Please select a song from your history to remix", Toast.LENGTH_SHORT).show()
                                            return@GlowingButton
                                        }
                                    }
                                }

                                musicViewModel.generateSong(
                                    title = finalTitle,
                                    prompt = finalPrompt,
                                    lyrics = finalLyrics,
                                    language = selectedLanguage,
                                    genre = selectedGenre,
                                    mood = selectedMood,
                                    style = selectedGenre,
                                    tempo = selectedTempo,
                                    duration = selectedDuration,
                                    voiceName = voiceName,
                                    voiceGender = voiceGender,
                                    voiceMatchPercent = voiceMatchPercent.toInt(),
                                    weirdness = weirdnessPercent.toInt(),
                                    styleInfluence = styleInfluencePercent.toInt(),
                                    projectId = selectedProjectId,
                                    uploadedAudioPath = mockUploadAudioPath
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_song_submit_button")
                        )

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            is com.example.ui.viewmodel.SongState.Loading -> {
                // Glowing Wave Generation Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stars,
                        contentDescription = "Computing",
                        tint = Color(0xFFF9D142),
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF9F75FF).copy(alpha = 0.15f), RoundedCornerShape(36.dp))
                            .padding(16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "SurMaya AI",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = state.status,
                        color = Color(0xFFF9D142),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Live Waveform visualizer bobs up and down representation
                    WaveformVisualizer(
                        waves = generationWaves,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        activeColor = Color(0xFF9F75FF),
                        inactiveColor = Color(0xFFF9D142)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    LinearProgressIndicator(
                        progress = { state.progress / 100f },
                        color = Color(0xFFF9D142),
                        trackColor = Color(0x1F9F75FF),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Estimated computation: 5 seconds remaining...",
                        color = Color(0xFF9E93B3),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Cancel computation button
                    OutlinedButton(
                        onClick = { musicViewModel.resetGenerationStates() },
                        border = BorderStroke(1.dp, Color(0xFFEFB8C8)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("cancel_generation_button")
                    ) {
                        Text("Cancel Composition", color = Color(0xFFEFB8C8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            is com.example.ui.viewmodel.SongState.Success -> {
                val createdTrack = state.song
                // Composition Complete / Result Presenter
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFFF9D142),
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Composition Complete!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "A brand-new high fidelity raga was successfully generated.",
                        color = Color(0xFF9E93B3),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // Results Card
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = createdTrack.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "${createdTrack.genre} • ${createdTrack.singerVoice} • ${createdTrack.language}",
                                color = Color(0xFFF9D142),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                // Play created track button
                                IconButton(
                                    onClick = { musicViewModel.togglePlayback(createdTrack) },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF9F75FF))
                                ) {
                                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.Black)
                                }

                                // Favorite Toggle
                                IconButton(onClick = { musicViewModel.toggleFavorite(createdTrack) }) {
                                    Icon(
                                        imageVector = if (createdTrack.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Fav",
                                        tint = Color(0xFFF9D142)
                                    )
                                }

                                // Share dialog simulation
                                IconButton(onClick = { Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show() }) {
                                    Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GlowingButton(
                        text = "Generate Similar Song",
                        onClick = {
                            musicViewModel.resetGenerationStates()
                            songPrompt = "Variation of ${createdTrack.title}: ${createdTrack.prompt}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { musicViewModel.resetGenerationStates() },
                        border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Composer", color = Color(0xFF9F75FF))
                    }
                }
            }

            is com.example.ui.viewmodel.SongState.Error -> {
                // Error presentation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Filled.Error, contentDescription = "Error", tint = Color(0xFFEFB8C8), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Composition Failed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(state.message, color = Color(0xFFEFB8C8), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))

                    GlowingButton(
                        text = "Dismiss",
                        onClick = { musicViewModel.resetGenerationStates() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
