package com.example.ui.screens.composer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.composer.*
import com.example.ui.viewmodel.ComposerViewModel
import com.example.ui.viewmodel.PlanCompilationState
import com.example.ui.viewmodel.ExportFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIComposerScreen(
    viewModel: ComposerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProjects by viewModel.allProjects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    val compilationState by viewModel.compilationState.collectAsState()
    val versions by viewModel.projectVersions.collectAsState()
    val exportedContent by viewModel.exportedContent.collectAsState()

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Navigation state inside the screen (Vision Board Form vs compiled Blueprint View)
    var activeSubTab by remember { mutableStateOf("VisionBoard") } // "VisionBoard" or "CompositionPlan"

    // Dialog trigger states
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showSaveVersionDialog by remember { mutableStateOf(false) }
    var showBranchDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showProjectsListDialog by remember { mutableStateOf(false) }

    // Form inputs for current project editing
    var formLyrics by remember { mutableStateOf("") }
    var formSituation by remember { mutableStateOf("") }
    var formEra by remember { mutableStateOf("") }
    var formScale by remember { mutableStateOf("") }
    var formJourney by remember { mutableStateOf("") }
    var formInstruments by remember { mutableStateOf("") }
    var formNotes by remember { mutableStateOf("") }

    // Set form fields whenever project changes
    LaunchedEffect(selectedProject) {
        selectedProject?.let { proj ->
            formLyrics = proj.lyrics
            formSituation = proj.filmSituation
            formEra = proj.era
            formScale = proj.productionScale
            formJourney = proj.emotionalJourney
            formInstruments = proj.instrumentPreferences
            formNotes = proj.userNotes
            if (proj.currentPlan != null) {
                activeSubTab = "CompositionPlan"
            } else {
                activeSubTab = "VisionBoard"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09041A)) // Deep Space Dark Background
    ) {
        // Decorative background nebula
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2C1953).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = size.width * 0.6f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF160E35).copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.8f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER BAR
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Composer Operating System",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Milestone 4.2: Master Composition Planner",
                            color = Color(0xFF9E93B3),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Projects manager
                    Button(
                        onClick = { showProjectsListDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D153A)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = "Projects", tint = Color(0xFFF9D142), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = selectedProject?.title?.take(10) ?: "Select Project", color = Color.White, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showNewProjectDialog = true }) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "New Project", tint = Color(0xFFF9D142))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0927).copy(alpha = 0.8f))
            )

            if (selectedProject == null) {
                // EMPTY STATE - NO PROJECT SELECTED
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.widthIn(max = 400.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color(0xFF1E143F), CircleShape)
                                .border(1.dp, Color(0xFFF9D142), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = "Composer", tint = Color(0xFFF9D142), modifier = Modifier.size(36.dp))
                        }
                        Text(
                            text = "Welcome to AI-Composer Engine",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "orchestrate complete Master Composition Plans (MCP) including scales, Indian taals, timelines, and diagnostics directly from the AI Director's Vision Board.",
                            color = Color(0xFF9E93B3),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showNewProjectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Composition Project")
                        }
                        
                        if (allProjects.isNotEmpty()) {
                            Text("Or select an existing workspace:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                allProjects.forEach { proj ->
                                    Card(
                                        onClick = { viewModel.selectProject(proj) },
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                        border = BorderStroke(1.dp, Color(0xFF2E244E))
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(proj.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${proj.genre} | ${proj.mood}", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                            }
                                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Open", tint = Color(0xFFF9D142))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ACTIVE WORKSPACE INTERFACE
                val proj = selectedProject!!
                
                // Screen Tabs
                TabRow(
                    selectedTabIndex = if (activeSubTab == "VisionBoard") 0 else 1,
                    containerColor = Color(0xFF0F0927),
                    contentColor = Color(0xFFF9D142),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeSubTab == "VisionBoard") 0 else 1]),
                            color = Color(0xFFF9D142),
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = activeSubTab == "VisionBoard",
                        onClick = { activeSubTab = "VisionBoard" },
                        text = { Text("AI Director's Vision Board", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Vision") }
                    )
                    Tab(
                        selected = activeSubTab == "CompositionPlan",
                        onClick = { activeSubTab = "CompositionPlan" },
                        enabled = proj.currentPlan != null,
                        text = { Text("Master Composition Plan (MCP)", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Analytics, contentDescription = "Plan") }
                    )
                }

                // SUBTAB VIEWPORTS
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    if (activeSubTab == "VisionBoard") {
                        // 1. VISION BOARD CONFIGURATION FORM
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section: Creative Context
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.MovieFilter, contentDescription = "Director Context", tint = Color(0xFF9F75FF))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI Director's Creative Context", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    
                                    // Film Situation Textbox
                                    OutlinedTextField(
                                        value = formSituation,
                                        onValueChange = { formSituation = it },
                                        label = { Text("Film Situation / Dramatic Context") },
                                        placeholder = { Text("e.g., romantic rain sequence, dramatic war climax, nostalgic wedding") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF0C071C),
                                            unfocusedContainerColor = Color(0xFF0C071C)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Quick Presets situation Row
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        listOf("Romantic Rain", "Dramatic Climax", "Sacred Wedding", "Sufi Mehfil", "Desert Caravan", "War Anthem").forEach { preset ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF1E143F))
                                                    .clickable { formSituation = preset }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(preset, color = Color(0xFF9F75FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Era Dropdown Mock selectors (as simple select fields)
                                    OutlinedTextField(
                                        value = formEra,
                                        onValueChange = { formEra = it },
                                        label = { Text("Musical Era / Period Aesthetic") },
                                        placeholder = { Text("e.g. 90s Bollywood Golden Age, Modern Cinematic, Retro 70s Soul") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF0C071C),
                                            unfocusedContainerColor = Color(0xFF0C071C)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Production Scale Selectors
                                    Text("Production Scale & Instrumentation", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("Acoustic Indie", "Studio Orchestra", "Epic Stadium Anthem").forEach { scaleOpt ->
                                            val active = formScale.lowercase() == scaleOpt.lowercase()
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (active) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color(0xFF0C071C))
                                                    .border(1.dp, if (active) Color(0xFFF9D142) else Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                                    .clickable { formScale = scaleOpt }
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(scaleOpt, color = if (active) Color(0xFFF9D142) else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }

                                    // Emotional Journey Text
                                    OutlinedTextField(
                                        value = formJourney,
                                        onValueChange = { formJourney = it },
                                        label = { Text("Emotional Journey / Energy curve") },
                                        placeholder = { Text("e.g., calm -> hope -> extreme excitement -> peaceful triumph") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF0C071C),
                                            unfocusedContainerColor = Color(0xFF0C071C)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Section: Lyrics & Preferences
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Lyrics, contentDescription = "Lyrics Input", tint = Color(0xFFF9D142))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Lyrics Canvas & Instrument Guides", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    OutlinedTextField(
                                        value = formLyrics,
                                        onValueChange = { formLyrics = it },
                                        label = { Text("Song Lyrics") },
                                        minLines = 4,
                                        maxLines = 8,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF0C071C),
                                            unfocusedContainerColor = Color(0xFF0C071C)
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("vision_board_lyrics_input")
                                    )

                                    OutlinedTextField(
                                        value = formInstruments,
                                        onValueChange = { formInstruments = it },
                                        label = { Text("Custom Instrument Preferences") },
                                        placeholder = { Text("e.g. Bansuri, solo acoustic nylon guitar, heavy dhol drums") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF0C071C),
                                            unfocusedContainerColor = Color(0xFF0C071C)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = formNotes,
                                        onValueChange = { formNotes = it },
                                        label = { Text("Additional Creative Notes / Prompts") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF2E244E),
                                            focusedContainerColor = Color(0xFF0C071C),
                                            unfocusedContainerColor = Color(0xFF0C071C)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Save and Compile Controls
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.updateProjectDetails(
                                            lyrics = formLyrics,
                                            filmSituation = formSituation,
                                            era = formEra,
                                            productionScale = formScale,
                                            emotionalJourney = formJourney,
                                            instrumentPreferences = formInstruments,
                                            userNotes = formNotes
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E244E)),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save Draft")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Draft", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateProjectDetails(
                                            lyrics = formLyrics,
                                            filmSituation = formSituation,
                                            era = formEra,
                                            productionScale = formScale,
                                            emotionalJourney = formJourney,
                                            instrumentPreferences = formInstruments,
                                            userNotes = formNotes
                                        )
                                        viewModel.compileCompositionPlan()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color.Black),
                                    modifier = Modifier.weight(1.5f).height(48.dp).testTag("compile_plan_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Compile Plan")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compile Plan (MCP)", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    } else if (activeSubTab == "CompositionPlan") {
                        // 2. MASTER COMPOSITION PLAN BLUEPRINT VIEW
                        val plan = proj.currentPlan!!
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Section: Quick Core Metrics (Tempo, Scale, Taal)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Tempo
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TEMPO", color = Color(0xFF9E93B3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${plan.tempoBpm}", color = Color(0xFFF9D142), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("BPM", color = Color.White, fontSize = 10.sp)
                                    }
                                }

                                // Key/Scale
                                Card(
                                    modifier = Modifier.weight(1.2f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("KEY / SCALE", color = Color(0xFF9E93B3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(plan.suggestedKey, color = Color(0xFF9F75FF), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                                        Text(plan.suggestedScale.take(12), color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                                    }
                                }

                                // Indian Taal
                                Card(
                                    modifier = Modifier.weight(1.3f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("RHYTHM FRAMEWORK", color = Color(0xFF9E93B3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (plan.suggestedTaal.isNotBlank()) plan.suggestedTaal.substringBefore(" ").take(10) else "Keharwa",
                                            color = Color(0xFF2FD6AA),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (plan.suggestedTaal.isNotBlank()) plan.suggestedTaal.substringAfter(" ") else "Standard 4/4",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // EXPLAIN WHY (MOST IMPORTANT FEATURE): CORE METRICS RATIONALE
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0927)),
                                border = BorderStroke(1.dp, Color(0xFF9F75FF).copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Info, contentDescription = "Composer Rationale", tint = Color(0xFFF9D142), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🎼 EXPLAIN WHY: COMPOSER DECISION RATIONALE", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (plan.tempoReason.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text("⏱️ BPM Reason: ", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text(plan.tempoReason, color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                        if (plan.keyScaleReason.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text("🎵 Key Reason: ", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text(plan.keyScaleReason, color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                        if (plan.taalReason.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.Top) {
                                                Text("🥁 Rhythm Reason: ", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text(plan.taalReason, color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Theme, Lyrics, Story, Era & AI Director Mode Intelligence
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("THEMATIC BLUEPRINT", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text(plan.musicalTheme, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(plan.storySummary, color = Color(0xFF9E93B3), fontSize = 12.sp, lineHeight = 18.sp)
                                    
                                    Divider(color = Color(0xFF2E244E))
                                    
                                    // MUSIC INTELLIGENCE LAYER V2.0
                                    Text("🎨 AUTOMATED MUSIC INTELLIGENCE", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Detected Lyrical Theme
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E143F))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text("LYRIC THEME", color = Color(0xFF9E93B3), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = plan.detectedLyricalTheme.ifBlank { "Cinematic Love" },
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        
                                        // Detected Era / Bollywood Style
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1E143F))
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Text("ERA/STYLE", color = Color(0xFF9E93B3), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = plan.detectedEraStyle.ifBlank { "Modern Bollywood" },
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Story Arc Progression Arc (Story Intelligence)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("STORY ARC PROGRESSION (Story Intelligence):", color = Color(0xFF2FD6AA), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF0C071C))
                                                .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = plan.detectedStoryArc.ifBlank { "Beginning ➔ Conflict ➔ Emotion ➔ Resolution" },
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // AI DIRECTOR MODE ENHANCEMENT
                            if (plan.directorModeResponse.isNotBlank()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF330C2F)),
                                    border = BorderStroke(1.dp, Color(0xFFFF79C6)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.MovieFilter, contentDescription = "Director Active", tint = Color(0xFFFF79C6), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("🎬 ACTIVE AI DIRECTOR MODE ADAPTATION", color = Color(0xFFFF79C6), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                        }
                                        Text(plan.directorModeResponse, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                                        
                                        // Visual indicators of automated orchestration parameters
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val badges = listOf(
                                                "Dynamics: Amplified" to Color(0xFFFF79C6),
                                                "Orchestra: Large" to Color(0xFF8BE9FD),
                                                "Choir: Enabled" to Color(0xFF50FA7B),
                                                "Vocal Energy: High" to Color(0xFFFFB86C),
                                                "Percussion: Epic" to Color(0xFFBD93F9)
                                            )
                                            badges.forEach { (text, color) ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(color.copy(alpha = 0.15f))
                                                        .border(1.dp, color, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: ENERGY CURVE TIMELINE CHART
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("CHRONOLOGICAL ENERGY CURVE TIMELINE", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Custom drawings of line chart on Canvas representing energy curve of the sections!
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                    ) {
                                        val pointsCount = plan.songStructure.size
                                        if (pointsCount > 0) {
                                            val stepX = size.width / (pointsCount + 1)
                                            val stepY = size.height
                                             
                                            val linePath = Path()
                                            plan.songStructure.forEachIndexed { index, section ->
                                                val x = stepX * (index + 1)
                                                val y = stepY * (1f - section.energyLevel)
                                                
                                                if (index == 0) {
                                                    linePath.moveTo(x, y)
                                                } else {
                                                    linePath.lineTo(x, y)
                                                }
                                                
                                                // Draw points
                                                drawCircle(
                                                    color = Color(0xFFF9D142),
                                                    radius = 4.dp.toPx(),
                                                    center = Offset(x, y)
                                                )
                                            }
                                            
                                            // Draw the line
                                            drawPath(
                                                path = linePath,
                                                color = Color(0xFF9F75FF),
                                                style = Stroke(width = 2.dp.toPx())
                                            )
                                        }
                                    }
                                    
                                    // Labels for sections
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        plan.songStructure.forEach { sec ->
                                            Text(
                                                text = sec.sectionName.substringBefore(" ").take(8),
                                                color = Color(0xFF9E93B3),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            // Section: Song Structure Timeline list
                            Text("Chronological Song Structure & Transitions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            plan.songStructure.forEachIndexed { index, section ->
                                var expanded by remember { mutableStateOf(index == 0 || index == 2) } // Auto expand intro and chorus
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0927)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { expanded = !expanded }
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .background(Color(0xFF2E244E), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("${index + 1}", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(section.sectionName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("Duration: ${section.durationSec}s | Energy: ${"%.0f%%".format(section.energyLevel * 100)}", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                                }
                                            }
                                            Icon(
                                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = "Expand",
                                                tint = Color(0xFFF9D142)
                                             )
                                        }

                                        if (expanded) {
                                            Divider(color = Color(0xFF2E244E))
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Text("Instruments Usage (Instrument Intelligence):", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    section.instrumentUsage.forEach { inst ->
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                 .background(Color(0xFF1E143F))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(inst, color = Color.White, fontSize = 10.sp)
                                                        }
                                                    }
                                                }

                                                Text("Vocal Dynamics Plan:", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text(section.vocalDynamics, color = Color.White, fontSize = 12.sp)

                                                Text("Transition Guide:", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text(section.transitionNote, color = Color(0xFF9E93B3), fontSize = 12.sp, lineHeight = 16.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Section: Vocal and Chord Guidance
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("VOCAL BLUEPRINT", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("Style: ${plan.vocalBlueprint.suggestedStyle}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Type: ${plan.vocalBlueprint.voiceType}", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        Text("Range: ${plan.vocalBlueprint.rangeRequired}", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        
                                        if (plan.vocalStyleReason.isNotBlank()) {
                                            Divider(color = Color(0xFF2E244E), modifier = Modifier.padding(vertical = 4.dp))
                                            Text("💡 Vocal Reason:", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(plan.vocalStyleReason, color = Color.White, fontSize = 10.sp, lineHeight = 14.sp)
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("HARMONY & CHORDS", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("Strategy: ${plan.chordGuidance.harmonyStrategy}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Chord Mood: ${plan.chordGuidance.chordMood}", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        
                                        if (plan.chordGuidance.emotionalProgression.isNotBlank()) {
                                            Text("Progression: ${plan.chordGuidance.emotionalProgression}", color = Color(0xFF2FD6AA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text("Tension: ${"%.0f%%".format(plan.chordGuidance.tensionLevel * 100)}", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        
                                        if (plan.chordReason.isNotBlank()) {
                                            Divider(color = Color(0xFF2E244E), modifier = Modifier.padding(vertical = 4.dp))
                                            Text("💡 Chord Reason:", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(plan.chordReason, color = Color.White, fontSize = 10.sp, lineHeight = 14.sp)
                                        }
                                    }
                                }
                            }

                            // Section: MELODY BLUEPRINT (New v2.0 Blueprint Segment)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Brush, contentDescription = "Melody Blueprint", tint = Color(0xFF9F75FF), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🎼 MELODY BLUEPRINT GUIDANCE", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    
                                    Row {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Phrase Direction", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                            Text(plan.melodyGuidance.phraseDirection, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Phrase Contour", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                            Text(plan.melodyGuidance.contour, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    if (plan.melodyGuidance.emotionalRise.isNotBlank()) {
                                        Text("Emotional Rise:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        Text(plan.melodyGuidance.emotionalRise, color = Color.White, fontSize = 11.sp)
                                    }
                                    if (plan.melodyGuidance.breathingPoints.isNotBlank()) {
                                        Text("Breathing Points:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        Text(plan.melodyGuidance.breathingPoints, color = Color.White, fontSize = 11.sp)
                                    }
                                    
                                    if (plan.melodyReason.isNotBlank()) {
                                        Divider(color = Color(0xFF2E244E), modifier = Modifier.padding(vertical = 4.dp))
                                        Text("💡 Melody Rationale:", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(plan.melodyReason, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                                    }
                                }
                            }

                            // Section: Arrangement Rationale & Instrument recommendations
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("ARRANGEMENT BLUEPRINT RATIONALE", color = Color(0xFFF9D142), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    
                                    Text("Automated Instrument Recommendations (Instrument Intelligence):", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                    Text(plan.instrumentPalette.joinToString(", "), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    
                                    if (plan.instrumentsReason.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("⚙️ Instrument Intelligence Rationale:", color = Color(0xFF9F75FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(plan.instrumentsReason, color = Color.White, fontSize = 11.sp)
                                    }
                                    
                                    if (plan.arrangementReason.isNotBlank()) {
                                        Divider(color = Color(0xFF2E244E), modifier = Modifier.padding(vertical = 4.dp))
                                        Text("💡 Arrangement Rationale:", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(plan.arrangementReason, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                                    }
                                }
                            }

                            // Section: Engineering & Mixing & Mastering NOTES
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("ENGINEERING & PRODUCTION SPECS", color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    
                                    Row {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Mixing Atmosphere", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                            Text(plan.mixingGuidance.atmosphere, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Stereo Field", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                            Text(plan.mixingGuidance.stereoWidth, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Divider(color = Color(0xFF2E244E))
                                    
                                    Text("Reverb Environment:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                    Text(plan.mixingGuidance.reverbStyle, color = Color.White, fontSize = 12.sp)

                                    Text("Mastering Release Target:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                    Text("${plan.masteringGuidance.targetLoudnessStrategy} (${plan.masteringGuidance.dynamicCharacter})", color = Color.White, fontSize = 12.sp)
                                }
                            }

                            // Section: AI DIAGNOSTICS SCORECARD
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("AI DIAGNOSTIC METRICS SCORECARD", color = Color(0xFF2FD6AA), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    
                                    // Custom grid of diagnostic ratings
                                    val diag = plan.diagnostics
                                    val metrics = listOf(
                                        Triple("Quality Score", diag.compositionQualityScore, Color(0xFFF9D142)),
                                        Triple("Commercial Appeal", diag.commercialAppeal, Color(0xFF9F75FF)),
                                        Triple("Originality Score", diag.originalityScore, Color(0xFF2FD6AA)),
                                        Triple("Cinematic Score", diag.cinematicScore, Color(0xFFE056FD))
                                    )
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        metrics.forEach { (label, value, color) ->
                                            Column {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                    Text("$value/100", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = value / 100f,
                                                    color = color,
                                                    trackColor = Color(0xFF1D153A),
                                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                    }

                                    if (diag.warnings.isNotEmpty()) {
                                        Divider(color = Color(0xFF2E244E))
                                        Text("⚠️ Composition Warnings:", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        diag.warnings.forEach { wrn ->
                                            Text("- $wrn", color = Color(0xFFFF8A8A), fontSize = 12.sp, lineHeight = 16.sp)
                                        }
                                    }

                                    if (diag.recommendations.isNotEmpty()) {
                                        Divider(color = Color(0xFF2E244E))
                                        Text("💡 Expert Recommendations:", color = Color(0xFF2FD6AA), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        diag.recommendations.forEach { rec ->
                                            Text("- $rec", color = Color(0xFFA8FFE5), fontSize = 12.sp, lineHeight = 16.sp)
                                        }
                                    }
                                }
                            }

                            // Section: Versioning Controls & Export Panel
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, Color(0xFF2E244E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("VERSIONING & EXPORT UTILITIES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { showSaveVersionDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E244E)),
                                            modifier = Modifier.weight(1f).height(40.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Save, contentDescription = "Commit Version", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save Version", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { showExportDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D153A)),
                                            modifier = Modifier.weight(1f).height(40.dp).testTag("export_mcp_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = "Export Report", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Export MCP", fontSize = 11.sp)
                                        }
                                    }

                                    // Display Version History inside Project
                                    if (versions.isNotEmpty()) {
                                        Text("Version Timeline History:", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            versions.forEach { ver ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF0F0927))
                                                        .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(ver.label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                            if (ver.isFavorite) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite", tint = Color(0xFFF9D142), modifier = Modifier.size(12.dp))
                                                            }
                                                        }
                                                        Text(ver.editSummary, color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                    }
                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        IconButton(
                                                            onClick = { viewModel.restoreToVersion(ver) },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Restore, contentDescription = "Restore", tint = Color(0xFF2FD6AA), modifier = Modifier.size(16.dp))
                                                        }
                                                        IconButton(
                                                            onClick = { showBranchDialog = true }, // branch triggers the dialog
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.CallSplit, contentDescription = "Branch", tint = Color(0xFF9F75FF), modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // ==========================================
        // DIALOGS & OVERLAYS
        // ==========================================

        // 1. PROJECT CREATION DIALOG
        if (showNewProjectDialog) {
            var newTitle by remember { mutableStateOf("") }
            var newGenre by remember { mutableStateOf("Bollywood Romantic") }
            var newMood by remember { mutableStateOf("Emotional") }
            var newLyrics by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showNewProjectDialog = false },
                title = { Text("Create AI-Composer Workspace") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Project Title") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth().testTag("new_project_title_input")
                        )
                        OutlinedTextField(
                            value = newGenre,
                            onValueChange = { newGenre = it },
                            label = { Text("Target Music Genre") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newMood,
                            onValueChange = { newMood = it },
                            label = { Text("Dominant Mood (Rasa)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newLyrics,
                            onValueChange = { newLyrics = it },
                            label = { Text("Lyrics Stems (Optional)") },
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.createProject(
                                title = newTitle,
                                lyrics = newLyrics,
                                language = "English/Hindi Blend",
                                genre = newGenre,
                                mood = newMood,
                                filmSituation = "",
                                era = "Modern Cinematic",
                                productionScale = "Studio Orchestra",
                                emotionalJourney = "",
                                instrumentPreferences = "",
                                userNotes = ""
                            )
                            showNewProjectDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color.Black),
                        modifier = Modifier.testTag("confirm_create_project_btn")
                    ) {
                        Text("Initiate Workspace")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewProjectDialog = false }) {
                        Text("Dismiss")
                    }
                },
                containerColor = Color(0xFF140D2A)
            )
        }

        // 2. EXPORT REPORT DIALOG
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { 
                    viewModel.clearExportedContent()
                    showExportDialog = false 
                },
                title = { Text("Export Composition Blueprint") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Generate high-fidelity structured reports of the Master Composition Plan (MCP) to distribute downstream.", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                "JSON Code" to ExportFormat.JSON,
                                "Markdown" to ExportFormat.MARKDOWN,
                                "Printable Text" to ExportFormat.PRINT_PDF_TEXT
                            ).forEach { (label, format) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E143F))
                                        .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                        .clickable { viewModel.exportPlan(format) }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        if (exportedContent != null) {
                            Divider(color = Color(0xFF2E244E))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(Color(0xFF0C071C))
                                    .border(1.dp, Color(0xFF2E244E))
                                    .padding(8.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = exportedContent!!,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(exportedContent!!))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FD6AA), contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Blueprint to Clipboard")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { 
                        viewModel.clearExportedContent()
                        showExportDialog = false 
                    }) {
                        Text("Done")
                    }
                },
                containerColor = Color(0xFF140D2A)
            )
        }

        // 3. COMPILE LOADING OVERLAY (ACTIVE STATE)
        if (compilationState is PlanCompilationState.Compiling) {
            val state = compilationState as PlanCompilationState.Compiling
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFF9D142),
                        trackColor = Color(0xFF1D153A),
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "COMPILING BLUEPRINT",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = state.status,
                        color = Color(0xFF9E93B3),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "${state.progress}%",
                        color = Color(0xFFF9D142),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    LinearProgressIndicator(
                        progress = state.progress / 100f,
                        color = Color(0xFFF9D142),
                        trackColor = Color(0xFF1D153A),
                        modifier = Modifier.width(180.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // 4. SAVE NEW VERSION DIALOG
        if (showSaveVersionDialog) {
            var label by remember { mutableStateOf("") }
            var summary by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showSaveVersionDialog = false },
                title = { Text("Save Active Blueprint Version") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Record a static snapshot of the Master Composition Plan to compare or restore in the future.", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("Version Tag / Label") },
                            placeholder = { Text("e.g. Raga Bhairavi Classical Take") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = summary,
                            onValueChange = { summary = it },
                            label = { Text("Revision Summary / Log") },
                            placeholder = { Text("e.g. Added flute hooks & changed energy profile in verse 2") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveCustomVersion(label, summary)
                            showSaveVersionDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color.Black)
                    ) {
                        Text("Commit Snapshot")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveVersionDialog = false }) {
                        Text("Dismiss")
                    }
                },
                containerColor = Color(0xFF140D2A)
            )
        }

        // 5. BRANCH PROJECT DIALOG
        if (showBranchDialog) {
            var branchName by remember { mutableStateOf("") }
            val currentVer = versions.firstOrNull() // branch from latest recorded version
            
            AlertDialog(
                onDismissRequest = { showBranchDialog = false },
                title = { Text("Branch Composition Project") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Spawn a completely new separate workspace project based on this version state to compose alternative ideas.", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        OutlinedTextField(
                            value = branchName,
                            onValueChange = { branchName = it },
                            label = { Text("New Branch Name") },
                            placeholder = { Text("e.g. SurMaya Raga Folk Remix Workspace") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFF9D142), unfocusedBorderColor = Color(0xFF2E244E)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (currentVer != null) {
                                viewModel.branchProject(branchName, currentVer)
                            }
                            showBranchDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142), contentColor = Color.Black)
                    ) {
                        Text("Instantiate Branch")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBranchDialog = false }) {
                        Text("Dismiss")
                    }
                },
                containerColor = Color(0xFF140D2A)
            )
        }

        // 6. ALL PROJECTS SELECTOR DIALOG
        if (showProjectsListDialog) {
            AlertDialog(
                onDismissRequest = { showProjectsListDialog = false },
                title = { Text("Your Composer Projects") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (allProjects.isEmpty()) {
                            Text("No composition projects active. Create one above!", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        } else {
                            allProjects.forEach { proj ->
                                Card(
                                    onClick = {
                                        viewModel.selectProject(proj)
                                        showProjectsListDialog = false
                                    },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0927)),
                                    border = BorderStroke(1.dp, Color(0xFF2E244E))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(proj.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${proj.genre} | ${proj.mood}", color = Color(0xFF9E93B3), fontSize = 11.sp)
                                        }
                                        Row {
                                            IconButton(
                                                onClick = { viewModel.deleteProject(proj.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showProjectsListDialog = false }) {
                        Text("Close")
                    }
                },
                containerColor = Color(0xFF140D2A)
            )
        }
    }
}
