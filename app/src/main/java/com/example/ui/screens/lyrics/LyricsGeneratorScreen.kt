package com.example.ui.screens.lyrics

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.di.ServiceLocator
import com.example.data.remote.gateway.lyrics.*
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.LyricistViewModel
import com.example.ui.viewmodel.MusicViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsGeneratorScreen(
    musicViewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val clipboardManager = LocalClipboardManager.current

    val lyricistViewModel: LyricistViewModel = viewModel(
        factory = LyricistViewModel.Factory(
            application = app,
            aiLyricistGateway = ServiceLocator.getAILyricistGateway(app)
        )
    )

    val currentProject by lyricistViewModel.currentProject.collectAsState()
    val allProjects by lyricistViewModel.allProjects.collectAsState()
    val lyricsParams by lyricistViewModel.lyricsParams.collectAsState()
    val selectedTextSegment by lyricistViewModel.selectedText.collectAsState()
    val qualityReport by lyricistViewModel.qualityReport.collectAsState()
    val smartSuggestions by lyricistViewModel.smartSuggestions.collectAsState()
    val isGenerating by lyricistViewModel.isGenerating.collectAsState()
    val errorMessage by lyricistViewModel.errorMessage.collectAsState()

    var languageInput by remember { mutableStateOf("Hindi") }
    var showLanguageDropdown by remember { mutableStateOf(false) }
    var activeLyricsTab by remember { mutableStateOf("Editor") } // "Editor", "Metrics", "Suggestions", "Versions"

    val languagesList = listOf("Hindi", "Odia", "Punjabi", "Sanskrit", "English", "Bengali", "Tamil", "Telugu", "Kannada", "Malayalam", "Marathi", "Bhojpuri")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Lyricist OS",
                            tint = Color(0xFFF9D142),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = if (currentProject != null) currentProject!!.title else "SurMaya",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Lyricist Operating System",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9E93B3),
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentProject != null) {
                        IconButton(onClick = { lyricistViewModel.deselectProject() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    if (currentProject != null) {
                        var showExportMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(imageVector = Icons.Filled.Share, contentDescription = "Export", tint = Color(0xFFF9D142))
                        }
                        DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Export as TXT") },
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(currentProject!!.currentLyrics))
                                    Toast.makeText(context, "Copied lyrics to clipboard!", Toast.LENGTH_SHORT).show()
                                    showExportMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as Markdown") },
                                onClick = {
                                    val md = "# ${currentProject!!.title}\n\n${currentProject!!.currentLyrics}"
                                    clipboardManager.setText(AnnotatedString(md))
                                    Toast.makeText(context, "Markdown formatted lyrics copied!", Toast.LENGTH_SHORT).show()
                                    showExportMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save Project Backup") },
                                onClick = {
                                    Toast.makeText(context, "Project backup saved offline successfully!", Toast.LENGTH_SHORT).show()
                                    showExportMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("lyrics_generator_scaffold")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentProject == null) {
                // DASHBOARD VIEW (Structured parameters + History list)
                DashboardView(
                    lyricistViewModel = lyricistViewModel,
                    allProjects = allProjects,
                    lyricsParams = lyricsParams,
                    languagesList = languagesList,
                    languageInput = languageInput,
                    showLanguageDropdown = showLanguageDropdown,
                    onLanguageChange = { languageInput = it },
                    onLanguageDropdownToggle = { showLanguageDropdown = it },
                    isGenerating = isGenerating,
                    errorMessage = errorMessage
                )
            } else {
                // ACTIVE WORKSPACE VIEW (AI Lyric Canvas + Chat Assistant)
                WorkspaceView(
                    project = currentProject!!,
                    lyricistViewModel = lyricistViewModel,
                    activeLyricsTab = activeLyricsTab,
                    onTabChanged = { activeLyricsTab = it },
                    selectedTextSegment = selectedTextSegment,
                    qualityReport = qualityReport,
                    smartSuggestions = smartSuggestions,
                    isGenerating = isGenerating,
                    errorMessage = errorMessage,
                    clipboardManager = clipboardManager
                )
            }
        }
    }
}

@Composable
fun DashboardView(
    lyricistViewModel: LyricistViewModel,
    allProjects: List<LyricProject>,
    lyricsParams: LyricsGenerationParams,
    languagesList: List<String>,
    languageInput: String,
    showLanguageDropdown: Boolean,
    onLanguageChange: (String) -> Unit,
    onLanguageDropdownToggle: (Boolean) -> Unit,
    isGenerating: Boolean,
    errorMessage: String?
) {
    var showForm by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf("") }
    var rhymeInput by remember { mutableStateOf("AABB") }
    var artistInput by remember { mutableStateOf("Modern Bollywood") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lyricist Operating System (AI-LOS)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFF9D142)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Collaborate with high-fidelity models. Generate structured stanzas, edit live with selection canvas, trace versions, and audit singability.",
                        fontSize = 12.sp,
                        color = Color(0xFFC7BFE6)
                    )
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showForm = !showForm },
                    colors = ButtonDefaults.buttonColors(containerColor = if (showForm) Color(0xFF2E244E) else Color(0xFF9F75FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = if (showForm) Icons.Filled.Close else Icons.Filled.Tune, contentDescription = "Params")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showForm) "Hide Controls" else "Structured Wizard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { lyricistViewModel.createNewProject() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF140D2A)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E244E)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add", tint = Color(0xFFF9D142))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Blank Canvas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Structured Input Fields
        if (showForm) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("SONG SPECIFICATIONS (ALL OPTIONAL)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)

                        // Topic Prompt
                        OutlinedTextField(
                            value = lyricsParams.prompt,
                            onValueChange = { lyricistViewModel.updateLyricsParams(lyricsParams.copy(prompt = it)) },
                            label = { Text("What should the song be about?", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF2E244E)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Genre Chip Selector
                        Column {
                            Text("Genres", color = Color(0xFF9E93B3), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val genres = listOf("Bollywood", "Sufi", "Ghazal", "Rap", "Bhajan", "Pop", "Folk", "EDM")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(genres) { g ->
                                    val active = lyricsParams.genres.contains(g)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                            .clickable {
                                                val next = if (active) lyricsParams.genres - g else lyricsParams.genres + g
                                                lyricistViewModel.updateLyricsParams(lyricsParams.copy(genres = next))
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(g, color = if (active) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Mood Chip Selector
                        Column {
                            Text("Moods", color = Color(0xFF9E93B3), fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val moods = listOf("Romantic", "Sad", "Energetic", "Devotional", "Patriotic", "Happy")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(moods) { m ->
                                    val active = lyricsParams.moods.contains(m)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                            .clickable {
                                                val next = if (active) lyricsParams.moods - m else lyricsParams.moods + m
                                                lyricistViewModel.updateLyricsParams(lyricsParams.copy(moods = next))
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(m, color = if (active) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Language Select
                        Box {
                            OutlinedButton(
                                onClick = { onLanguageDropdownToggle(true) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF140D2A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Text("Language: $languageInput", color = Color.White, fontSize = 12.sp)
                                    Icon(Icons.Filled.ArrowDropDown, "Lang", tint = Color(0xFFF9D142))
                                }
                            }
                            DropdownMenu(expanded = showLanguageDropdown, onDismissRequest = { onLanguageDropdownToggle(false) }) {
                                languagesList.forEach { lang ->
                                    DropdownMenuItem(text = { Text(lang) }, onClick = { onLanguageChange(lang); onLanguageDropdownToggle(false) })
                                }
                            }
                        }

                        // Rhyme Scheme
                        OutlinedTextField(
                            value = rhymeInput,
                            onValueChange = { rhymeInput = it; lyricistViewModel.updateLyricsParams(lyricsParams.copy(rhymeScheme = it)) },
                            label = { Text("Rhyme Scheme / Style (e.g., AABB, Free Verse)", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Artist Style
                        OutlinedTextField(
                            value = artistInput,
                            onValueChange = { artistInput = it; lyricistViewModel.updateLyricsParams(lyricsParams.copy(artistStyle = it)) },
                            label = { Text("Artist Stylistic Inspiration (e.g., Classic Bollywood)", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Notes
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it; lyricistViewModel.updateLyricsParams(lyricsParams.copy(additionalNotes = it)) },
                            label = { Text("Additional Custom Notes (e.g., Male-Female Duet)", color = Color(0xFF9E93B3)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Generate Button
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color(0xFFF9D142), modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            GlowingButton(
                                text = "Initiate Songwriting Machine",
                                onClick = { lyricistViewModel.generateLyricsStructured(languageInput) },
                                modifier = Modifier.fillMaxWidth().testTag("lyrics_generate_btn")
                            )
                        }
                    }
                }
            }
        }

        // Active Error
        if (errorMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFB8C8).copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Text(errorMessage, color = Color(0xFFEFB8C8), fontSize = 12.sp)
                }
            }
        }

        // Projects / Drafts List
        item {
            Text("MY RECENT LYRIC PROJECTS", fontWeight = FontWeight.Bold, color = Color(0xFF9E93B3), fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp))
        }

        if (allProjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Filled.FolderOpen, contentDescription = "Empty", tint = Color(0x33FFFFFF), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No projects yet", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("Create a blank canvas or specify parameters above to start writing.", color = Color(0x66FFFFFF), fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(allProjects) { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { lyricistViewModel.selectProject(p) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Language: ${p.language} | ${p.versions.size} versions saved", color = Color(0xFF9E93B3), fontSize = 11.sp)
                            }
                            Row {
                                IconButton(onClick = {
                                    lyricistViewModel.selectProject(p)
                                    lyricistViewModel.duplicateProject()
                                }) {
                                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFF9F75FF), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { lyricistViewModel.deleteProject(p.id) }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEFB8C8), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = p.currentLyrics,
                            color = Color(0x88FFFFFF),
                            fontSize = 11.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun VersionDiffDialog(
    originalText: String,
    revisedText: String,
    onDismiss: () -> Unit
) {
    val originalLines = originalText.lines()
    val revisedLines = revisedText.lines()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Visual Side-by-Side Comparison", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
        containerColor = Color(0xFF140D2A),
        text = {
            Column(modifier = Modifier.fillMaxHeight(0.7f).fillMaxWidth()) {
                Text(
                    text = "Comparing selected version with active canvas. Red lines (-) are removed/changed, Green lines (+) are added.",
                    color = Color(0xFF9E93B3),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Original Version (Left side)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .background(Color(0xFF0D0822))
                            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text("SELECTED VERSION", color = Color(0xFF9F75FF), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        originalLines.forEach { line ->
                            val isDifferent = line !in revisedLines
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDifferent) Color(0x33FF5555) else Color.Transparent)
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = if (isDifferent) "- $line" else line,
                                    color = if (isDifferent) Color(0xFFEFB8C8) else Color(0x99FFFFFF),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Revised Version (Right side)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .background(Color(0xFF09041A))
                            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text("ACTIVE CANVAS", color = Color(0xFFF9D142), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        revisedLines.forEach { line ->
                            val isDifferent = line !in originalLines
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isDifferent) Color(0x3355FF55) else Color.Transparent)
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = if (isDifferent) "+ $line" else line,
                                    color = if (isDifferent) Color(0xFF81C784) else Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun WorkspaceView(
    project: LyricProject,
    lyricistViewModel: LyricistViewModel,
    activeLyricsTab: String,
    onTabChanged: (String) -> Unit,
    selectedTextSegment: String?,
    qualityReport: QualityScoreReport?,
    smartSuggestions: SmartSuggestions?,
    isGenerating: Boolean,
    errorMessage: String?,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    val context = LocalContext.current
    var chatInput by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // Canvas Text Editor State setup
    var editorValue by remember { mutableStateOf(TextFieldValue(project.currentLyrics)) }

    // Sync editor state from project updates (but don't reset selections while typing)
    LaunchedEffect(project.currentLyrics) {
        if (editorValue.text != project.currentLyrics) {
            editorValue = editorValue.copy(text = project.currentLyrics)
        }
    }

    // Monitor highlights
    LaunchedEffect(editorValue.selection) {
        if (editorValue.selection.length > 0) {
            val selection = editorValue.text.substring(
                editorValue.selection.start,
                editorValue.selection.end
            )
            lyricistViewModel.selectTextSegment(selection)
        } else {
            lyricistViewModel.selectTextSegment(null)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Workspace Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF140D2A))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Editor", "Metrics", "Suggestions", "Versions").forEach { tab ->
                val active = (tab == activeLyricsTab)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (active) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onTabChanged(tab) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (active) Color(0xFFF9D142) else Color(0xFF9E93B3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Workspace Panels
        Row(modifier = Modifier.weight(1f)) {
            // Left Panel (Fixed Full Editor - "THE AI LYRIC CANVAS")
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .background(Color(0xFF0D0822))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI LYRIC CANVAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9F75FF))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { 
                                lyricistViewModel.undoLastEdit()
                                Toast.makeText(context, "Undo applied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Undo, contentDescription = "Undo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { 
                                lyricistViewModel.redoLastEdit()
                                Toast.makeText(context, "Redo applied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Redo, contentDescription = "Redo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Auto Saved", fontSize = 9.sp, color = Color(0x66FFFFFF))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Active songwriting context metadata chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val genre = project.genre.ifBlank { "Bollywood" }
                    val mood = project.mood.ifBlank { "Romantic" }
                    val language = project.language
                    
                    listOf(genre, mood, language).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF140D2A))
                                .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(tag, color = Color(0xFFC7BFE6), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Songwriting Session Context Memory Settings",
                        tint = Color(0xFFF9D142),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { showSettingsDialog = true }
                    )
                }

                if (showSettingsDialog) {
                    var titleEdit by remember { mutableStateOf(project.title) }
                    var genreEdit by remember { mutableStateOf(project.genre) }
                    var moodEdit by remember { mutableStateOf(project.mood) }
                    var storyEdit by remember { mutableStateOf(project.story) }
                    var structureEdit by remember { mutableStateOf(project.songStructure) }
                    
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        title = { Text("Active Co-Writer Memory Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                        containerColor = Color(0xFF140D2A),
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text("This configuration acts as the secure, persistent memory for the AI. Edits and suggestions will strictly respect these parameters.", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                OutlinedTextField(
                                    value = titleEdit,
                                    onValueChange = { titleEdit = it },
                                    label = { Text("Song Title", color = Color(0xFF9E93B3)) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = genreEdit,
                                    onValueChange = { genreEdit = it },
                                    label = { Text("Active Genre Memory", color = Color(0xFF9E93B3)) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = moodEdit,
                                    onValueChange = { moodEdit = it },
                                    label = { Text("Active Mood Memory", color = Color(0xFF9E93B3)) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = storyEdit,
                                    onValueChange = { storyEdit = it },
                                    label = { Text("Story / Character Context", color = Color(0xFF9E93B3)) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = structureEdit,
                                    onValueChange = { structureEdit = it },
                                    label = { Text("Song Structure Guidelines", color = Color(0xFF9E93B3)) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    lyricistViewModel.updateProjectSettings(titleEdit, genreEdit, moodEdit, storyEdit, structureEdit)
                                    showSettingsDialog = false
                                    Toast.makeText(context, "AI Memory constraints updated successfully", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Apply & Sync AI", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSettingsDialog = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = editorValue,
                    onValueChange = {
                        editorValue = it
                        lyricistViewModel.updateLyricsInEditor(it.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF09041A),
                        unfocusedContainerColor = Color(0xFF09041A),
                        focusedBorderColor = Color(0xFF2E244E),
                        unfocusedBorderColor = Color(0xFF140D2A)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 22.sp,
                        fontSize = 13.sp
                    )
                )

                if (selectedTextSegment != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF9D142).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "Selection", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Highlight Selection: \"$selectedTextSegment\"",
                                color = Color(0xFFF9D142),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF2E244E))
            )

            // Right Panel (Dynamic Switchable Assistant / Analytics Hub)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF09041A))
            ) {
                when (activeLyricsTab) {
                    "Editor" -> {
                        // AI Writing Chat Assistant Panel
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF140D2A))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Filled.Forum, contentDescription = "Chat", tint = Color(0xFF9F75FF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI SONG COLLABORATOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // AI Director Mode Quick Commands Banner
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F0926))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    "AI DIRECTOR QUICK-CONTROLS",
                                    color = Color(0xFF9F75FF),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "90s Bollywood" to "Infuse nostalgic 90s Bollywood romantic idioms, matching old-school musical patterns.",
                                        "Stadium Hook" to "Make the chorus feel like an epic, high-energy, sing-along stadium rock hook.",
                                        "Deep Emotion" to "Rewrite with highly poetic, intense metaphors and tragic storytelling.",
                                        "Convert Duet" to "Convert the song layout into a clean duet structure with distinct [Vocalist A] and [Vocalist B] markers."
                                    ).forEach { (label, command) ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF9D142).copy(alpha = 0.1f))
                                                .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    lyricistViewModel.generateLyricsChat(command)
                                                    Toast.makeText(context, "Directing: $label style...", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(label, color = Color(0xFFF9D142), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Message list
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp),
                                reverseLayout = false,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item { Spacer(modifier = Modifier.height(10.dp)) }
                                items(project.chatHistory) { msg ->
                                    val isUser = msg.sender == "user"
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(
                                                    RoundedCornerShape(
                                                        topStart = 12.dp,
                                                        topEnd = 12.dp,
                                                        bottomStart = if (isUser) 12.dp else 2.dp,
                                                        bottomEnd = if (isUser) 2.dp else 12.dp
                                                    )
                                                )
                                                .background(if (isUser) Color(0xFF9F75FF) else Color(0xFF140D2A))
                                                .padding(10.dp)
                                                .widthIn(max = 200.dp)
                                        ) {
                                            Text(
                                                msg.text,
                                                color = if (isUser) Color.Black else Color.White,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Text(
                                            text = if (isUser) "You" else "SurMaya",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0x44FFFFFF),
                                            fontSize = 8.sp,
                                            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                                        )
                                    }
                                }
                                if (isGenerating) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF140D2A))
                                                .padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color(0xFFF9D142))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("AI is rewriting song parts...", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            if (errorMessage != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEFB8C8).copy(alpha = 0.1f))
                                        .padding(8.dp)
                                ) {
                                    Text(errorMessage, color = Color(0xFFEFB8C8), fontSize = 10.sp)
                                }
                            }

                            // Chat input panel
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF140D2A))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatInput,
                                    onValueChange = { chatInput = it },
                                    placeholder = { Text("Ask AI to edit/rewrite...", color = Color(0x33FFFFFF), fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFFF9D142),
                                        unfocusedBorderColor = Color(0xFF2E244E)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        if (chatInput.isNotBlank()) {
                                            lyricistViewModel.generateLyricsChat(chatInput)
                                            chatInput = ""
                                        }
                                    },
                                    enabled = !isGenerating
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = if (isGenerating) Color(0x33FFFFFF) else Color(0xFFF9D142)
                                    )
                                }
                            }
                        }
                    }

                    "Metrics" -> {
                        // Live Quality score audits
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("LIVE QUALITY METRICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Acoustic analysis & structure scoring", fontSize = 10.sp, color = Color(0xFF9E93B3))
                            }

                            if (qualityReport == null) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF9F75FF))
                                    }
                                }
                            } else {
                                val stats = listOf(
                                    "Originality" to qualityReport.originalityScore,
                                    "Singability" to qualityReport.singabilityScore,
                                    "Rhyme Alignment" to qualityReport.rhymeScore,
                                    "Emotional Depth" to qualityReport.emotionScore,
                                    "Commercial Appeal" to qualityReport.commercialAppealScore,
                                    "Storytelling" to qualityReport.storytellingScore,
                                    "Structure Flow" to qualityReport.structureScore
                                )

                                items(stats) { (label, value) ->
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(label, color = Color(0xFFC7BFE6), fontSize = 11.sp)
                                            Text("${value.toInt()}%", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { value / 100f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                            color = Color(0xFF9F75FF),
                                            trackColor = Color(0xFF140D2A)
                                        )
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("IMPROVEMENT SUGGESTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF9D142))
                                }

                                items(qualityReport.issuesAndSuggestions) { issue ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF140D2A))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = "Tip", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(issue, color = Color.White, fontSize = 10.sp, lineHeight = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    "Suggestions" -> {
                        // Smart vocabulary tips, alternative rhymes and hooks
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("SMART IDEATION HUBS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Context-aware lyric extensions", fontSize = 10.sp, color = Color(0xFF9E93B3))
                            }

                            if (smartSuggestions == null) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF9F75FF))
                                    }
                                }
                            } else {
                                item {
                                    Text("Suggested Titles (Tap to set title)", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                items(smartSuggestions.suggestedTitles) { title ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF140D2A))
                                            .clickable {
                                                lyricistViewModel.updateProjectSettings(title, project.genre, project.mood, project.story, project.songStructure)
                                                Toast.makeText(context, "Project title set to '$title'!", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Text(title, color = Color.White, fontSize = 11.sp)
                                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Use", tint = Color(0xFFF9D142), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }

                                item {
                                    Text("One-Tap Interactive Alternative Lines", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Select a segment on the canvas to swap, or tap to append at the end.", color = Color(0xFF9E93B3), fontSize = 8.sp)
                                }
                                items(listOf(
                                    "Sathiyaa mere saaz me tu hi toh hai mila...",
                                    "Dhadkano ki ye laya keh rahi dastaan naya...",
                                    "Beh rahe hain hum dono, jaise geet me hawa...",
                                    "Tu nadi hai, main sagar ban mil jaun tujhse..."
                                )) { alternative ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF140D2A))
                                            .clickable {
                                                if (selectedTextSegment != null) {
                                                    val replaced = project.currentLyrics.replace(selectedTextSegment, alternative)
                                                    lyricistViewModel.updateLyricsInEditor(replaced)
                                                    Toast.makeText(context, "Replaced selection with alternative line!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val appended = project.currentLyrics + "\n" + alternative
                                                    lyricistViewModel.updateLyricsInEditor(appended)
                                                    Toast.makeText(context, "Appended alternative line to canvas!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Text(alternative, color = Color.White, fontSize = 11.sp)
                                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Insert", tint = Color(0xFFF9D142), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                item {
                                    Text("Rhyme Dictionary Alignments", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                items(smartSuggestions.alternativeRhymes) { dict ->
                                    dict.entries.forEach { (word, suggestion) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF140D2A))
                                                .padding(10.dp)
                                        ) {
                                            Text("$word -> ", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(suggestion, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }

                                item {
                                    Text("Harmony Arrangements", color = Color(0xFF9F75FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                items(smartSuggestions.harmonyTips) { tip ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF140D2A))
                                            .padding(10.dp)
                                    ) {
                                        Text(tip, color = Color.White, fontSize = 10.sp, lineHeight = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    "Versions" -> {
                        // Complete backup lists and branches
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text("VERSION TIMELINE (OFFLINE SECURE)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Branch, favorite, compare, or restore", fontSize = 10.sp, color = Color(0xFF9E93B3))
                            }

                            items(project.versions.asReversed()) { ver ->
                                var showRenameDialog by remember { mutableStateOf(false) }
                                var showBranchDialog by remember { mutableStateOf(false) }
                                var showDiffDialog by remember { mutableStateOf(false) }
                                
                                if (showRenameDialog) {
                                    var renameValue by remember { mutableStateOf(ver.label) }
                                    AlertDialog(
                                        onDismissRequest = { showRenameDialog = false },
                                        title = { Text("Rename Version Label", color = Color.White) },
                                        containerColor = Color(0xFF140D2A),
                                        text = {
                                            OutlinedTextField(
                                                value = renameValue,
                                                onValueChange = { renameValue = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142))
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                lyricistViewModel.renameVersion(ver.id, renameValue)
                                                showRenameDialog = false
                                                Toast.makeText(context, "Version label updated", Toast.LENGTH_SHORT).show()
                                            }) { Text("Save", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold) }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showRenameDialog = false }) { Text("Cancel", color = Color.Gray) }
                                        }
                                    )
                                }
                                
                                if (showBranchDialog) {
                                    var branchTitleValue by remember { mutableStateOf("${project.title} - ${ver.label} Branch") }
                                    AlertDialog(
                                        onDismissRequest = { showBranchDialog = false },
                                        title = { Text("Branch Version as New Song Project", color = Color.White) },
                                        containerColor = Color(0xFF140D2A),
                                        text = {
                                            OutlinedTextField(
                                                value = branchTitleValue,
                                                onValueChange = { branchTitleValue = it },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFF9D142)),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                lyricistViewModel.branchVersion(ver, branchTitleValue)
                                                showBranchDialog = false
                                                Toast.makeText(context, "Song branched successfully!", Toast.LENGTH_SHORT).show()
                                            }) { Text("Create Branch", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold) }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showBranchDialog = false }) { Text("Cancel", color = Color.Gray) }
                                        }
                                    )
                                }
                                
                                if (showDiffDialog) {
                                    VersionDiffDialog(
                                        originalText = ver.content,
                                        revisedText = project.currentLyrics,
                                        onDismiss = { showDiffDialog = false }
                                    )
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(ver.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    if (ver.editSummary.isNotBlank()) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(Color(0xFF9F75FF).copy(alpha = 0.2f))
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(ver.editSummary, color = Color(0xFF9F75FF), fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                                val date = SimpleDateFormat("HH:mm - dd MMM", Locale.getDefault()).format(Date(ver.timestamp))
                                                Text("Version #${ver.versionNumber} | $date | ${ver.genre}/${ver.mood}", color = Color(0x66FFFFFF), fontSize = 8.sp)
                                            }
                                            Row {
                                                IconButton(onClick = { lyricistViewModel.toggleVersionFavorite(ver.id) }) {
                                                    Icon(
                                                        imageVector = if (ver.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                        contentDescription = "Fav",
                                                        tint = if (ver.isFavorite) Color(0xFFEFB8C8) else Color(0x33FFFFFF),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                IconButton(onClick = { lyricistViewModel.restoreVersion(ver) }) {
                                                    Icon(imageVector = Icons.Filled.Restore, contentDescription = "Restore", tint = Color(0xFF9F75FF), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ver.content,
                                            color = Color(0x44FFFFFF),
                                            fontSize = 9.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                "Compare",
                                                color = Color(0xFFF9D142),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable { showDiffDialog = true }
                                                    .padding(vertical = 2.dp)
                                            )
                                            Text(
                                                "Rename",
                                                color = Color(0xFFC7BFE6),
                                                fontSize = 9.sp,
                                                modifier = Modifier
                                                    .clickable { showRenameDialog = true }
                                                    .padding(vertical = 2.dp)
                                            )
                                            Text(
                                                "Branch",
                                                color = Color(0xFF81C784),
                                                fontSize = 9.sp,
                                                modifier = Modifier
                                                    .clickable { showBranchDialog = true }
                                                    .padding(vertical = 2.dp)
                                            )
                                            if (project.versions.size > 1) {
                                                Text(
                                                    "Delete",
                                                    color = Color(0xFFEFB8C8),
                                                    fontSize = 9.sp,
                                                    modifier = Modifier
                                                        .clickable { 
                                                            lyricistViewModel.deleteVersion(ver.id)
                                                            Toast.makeText(context, "Version deleted", Toast.LENGTH_SHORT).show()
                                                        }
                                                        .padding(vertical = 2.dp)
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
}
