package com.example.ui.screens.studio

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.audio.export.ExportFormat
import com.example.core.audio.export.ExportStatus
import com.example.domain.service.StudioProjectState
import com.example.domain.service.StudioTrackState
import com.example.domain.service.StudioClipState
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.StudioViewModel
import java.io.File
import java.util.UUID
import kotlin.math.sin
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalStudioScreen(
    studioViewModel: StudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentProject by studioViewModel.currentProjectState.collectAsState()
    val recentProjects by studioViewModel.recentProjects.collectAsState()
    val isPlaying by studioViewModel.isPlaying.collectAsState()
    val bpm by studioViewModel.bpm.collectAsState()
    val timeSignature by studioViewModel.timeSignature.collectAsState()
    val loopEnabled by studioViewModel.loopEnabled.collectAsState()
    val playheadPositionSec by studioViewModel.playheadPositionSec.collectAsState()

    val undoCount by studioViewModel.undoStackSize.collectAsState()
    val redoCount by studioViewModel.redoStackSize.collectAsState()
    val exportStatus by studioViewModel.exportStatus.collectAsState()

    val activeTab by studioViewModel.activeStudioTab.collectAsState()
    val showCreateDialog by studioViewModel.showCreateProjectDialog.collectAsState()
    val showExportDialog by studioViewModel.showExportDialog.collectAsState()
    val logs by studioViewModel.operationLogs.collectAsState()

    // Create project inputs
    var newProjName by remember { mutableStateOf("") }
    var newProjDesc by remember { mutableStateOf("") }

    // Export selection inputs
    var selectedExportFormat by remember { mutableStateOf(ExportFormat.WAV_16) }
    var exportDurationSec by remember { mutableStateOf(15) }

    // Fluctuating mixing levels when playing
    val infiniteTransition = rememberInfiniteTransition(label = "peaks")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentProject?.name ?: "Professional Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (currentProject != null) "SurMaya AI (AIRE v2.0) Active" else "Select or Create a Studio Session",
                            fontSize = 10.sp,
                            color = if (currentProject != null) Color(0xFFF9D142) else Color(0xFF9E93B3),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentProject != null) {
                                studioViewModel.closeCurrentProject()
                            }
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("studio_back_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit Studio", tint = Color.White)
                    }
                },
                actions = {
                    if (currentProject != null) {
                        // Undo Button
                        IconButton(
                            onClick = { studioViewModel.undo() },
                            enabled = undoCount > 0,
                            modifier = Modifier.testTag("studio_undo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "Undo Action ($undoCount)",
                                tint = if (undoCount > 0) Color(0xFF9F75FF) else Color.Gray.copy(alpha = 0.5f)
                            )
                        }

                        // Redo Button
                        IconButton(
                            onClick = { studioViewModel.redo() },
                            enabled = redoCount > 0,
                            modifier = Modifier.testTag("studio_redo_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Redo,
                                contentDescription = "Redo Action ($redoCount)",
                                tint = if (redoCount > 0) Color(0xFF9F75FF) else Color.Gray.copy(alpha = 0.5f)
                            )
                        }

                        // Save Button
                        IconButton(
                            onClick = {
                                studioViewModel.saveCurrentProject()
                                Toast.makeText(context, "Project archive fully saved!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("studio_save_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Project Package",
                                tint = if (currentProject?.isDirty == true) Color(0xFFF9D142) else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("professional_studio_scaffold")
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentProject == null) {
                // Landing state: List of recent projects & option to create new
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF130932), Color(0xFF09041A))
                                    )
                                )
                                .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "On-Device Studio Runtime",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Compile multi-track stems, balance channel strips, edit tempos & export certified master deliverables offline.",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                GlowingButton(
                                    text = "Create New Project Session",
                                    onClick = { studioViewModel.showCreateProjectDialog.value = true },
                                    height = 40.dp,
                                    testTag = "create_new_studio_project_btn"
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Recent Studio Sessions",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    if (recentProjects.isEmpty()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = "Empty",
                                        tint = Color(0xFF9E93B3),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No recent project sessions found",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Click 'Create New' above to initialize your first .surmaya production",
                                        color = Color(0xFF9E93B3).copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        items(recentProjects) { project ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { studioViewModel.loadProjectById(project.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2E244E)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LibraryMusic,
                                                contentDescription = "Project Archive",
                                                tint = Color(0xFFF9D142)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = project.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = project.description.ifBlank { "No description added" },
                                                color = Color(0xFF9E93B3),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { studioViewModel.deleteProject(project.id) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Project Session",
                                            tint = Color(0xFFFF5252).copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Workspace view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 150.dp) // Leave space for transport controls bar
                ) {
                    // Navigation Workspace Tabs
                    TabRow(
                        selectedTabIndex = when (activeTab) {
                            "overview" -> 0
                            "timeline" -> 1
                            "mixer" -> 2
                            "assets" -> 3
                            "diagnostics" -> 4
                            else -> 0
                        },
                        containerColor = Color(0xFF140D2A),
                        contentColor = Color(0xFF9F75FF),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[
                                    when (activeTab) {
                                        "overview" -> 0
                                        "timeline" -> 1
                                        "mixer" -> 2
                                        "assets" -> 3
                                        "diagnostics" -> 4
                                        else -> 0
                                    }
                                ]),
                                color = Color(0xFFF9D142)
                            )
                        }
                    ) {
                        Tab(
                            selected = activeTab == "overview",
                            onClick = { studioViewModel.activeStudioTab.value = "overview" },
                            text = { Text("Overview", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Overview", modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = activeTab == "timeline",
                            onClick = { studioViewModel.activeStudioTab.value = "timeline" },
                            text = { Text("Timeline", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(imageVector = Icons.Default.Tune, contentDescription = "Multi-track Timeline", modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = activeTab == "mixer",
                            onClick = { studioViewModel.activeStudioTab.value = "mixer" },
                            text = { Text("Mixer", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(imageVector = Icons.Default.SettingsInputComponent, contentDescription = "Mixing Desk", modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = activeTab == "assets",
                            onClick = { studioViewModel.activeStudioTab.value = "assets" },
                            text = { Text("Assets", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Assets Browser", modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = activeTab == "diagnostics",
                            onClick = { studioViewModel.activeStudioTab.value = "diagnostics" },
                            text = { Text("Telemetry", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(imageVector = Icons.Default.DeveloperMode, contentDescription = "Diagnostics Logs", modifier = Modifier.size(16.dp)) }
                        )
                    }

                    // Active workspace layout
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (activeTab) {
                            "overview" -> {
                                OverviewWorkspace(
                                    projectState = currentProject!!,
                                    studioViewModel = studioViewModel
                                )
                            }
                            "timeline" -> {
                                TimelineWorkspace(
                                    projectState = currentProject!!,
                                    studioViewModel = studioViewModel,
                                    isPlaying = isPlaying,
                                    playheadSec = playheadPositionSec
                                )
                            }
                            "mixer" -> {
                                MixerWorkspace(
                                    projectState = currentProject!!,
                                    studioViewModel = studioViewModel,
                                    isPlaying = isPlaying,
                                    waveOffset = waveOffset
                                )
                            }
                            "assets" -> {
                                AssetsWorkspace(
                                    studioViewModel = studioViewModel
                                )
                            }
                            "diagnostics" -> {
                                DiagnosticsWorkspace(
                                    logs = logs
                                )
                            }
                        }
                    }
                }

                // BOTTOM TRANSPORT BAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFF140D2A))
                        .border(1.dp, Color(0xFF2E244E))
                        .navigationBarsPadding() // Keep safe from system bars
                ) {
                    TransportControlDeck(
                        isPlaying = isPlaying,
                        bpm = bpm,
                        timeSignature = timeSignature,
                        loopEnabled = loopEnabled,
                        playheadSec = playheadPositionSec,
                        onPlay = { studioViewModel.play() },
                        onPause = { studioViewModel.pause() },
                        onStop = { studioViewModel.stop() },
                        onSeek = { seconds -> studioViewModel.seekTo(seconds) },
                        onBpmChange = { newBpm -> studioViewModel.setBpm(newBpm) },
                        onTimeSigChange = { sig -> studioViewModel.setTimeSignature(sig) },
                        onToggleLoop = { studioViewModel.toggleLoop() },
                        onExportTrigger = { studioViewModel.showExportDialog.value = true }
                    )
                }
            }

            // CREATE NEW SESSION DIALOG
            if (showCreateDialog) {
                Dialog(onDismissRequest = { studioViewModel.showCreateProjectDialog.value = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E244E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("create_project_dialog_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "New Studio Session",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            OutlinedTextField(
                                value = newProjName,
                                onValueChange = { newProjName = it },
                                label = { Text("Session Name") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF9F75FF),
                                    unfocusedBorderColor = Color(0xFF2E244E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_project_name_input")
                            )

                            OutlinedTextField(
                                value = newProjDesc,
                                onValueChange = { newProjDesc = it },
                                label = { Text("Session Description / Style") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF9F75FF),
                                    unfocusedBorderColor = Color(0xFF2E244E),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .testTag("new_project_desc_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { studioViewModel.showCreateProjectDialog.value = false },
                                    modifier = Modifier.testTag("cancel_create_project_btn")
                                ) {
                                    Text("Cancel", color = Color(0xFFFF5252))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (newProjName.isNotBlank()) {
                                            studioViewModel.createNewProject(newProjName, newProjDesc)
                                            studioViewModel.showCreateProjectDialog.value = false
                                            newProjName = ""
                                            newProjDesc = ""
                                        } else {
                                            Toast.makeText(context, "Session name cannot be blank", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                                    modifier = Modifier.testTag("submit_create_project_btn")
                                ) {
                                    Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // OFFLINE BOUNCE EXPORT DIALOG
            if (showExportDialog) {
                Dialog(onDismissRequest = { studioViewModel.showExportDialog.value = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E244E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("export_project_dialog_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "High-Speed Offline Render",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            Text(
                                text = "AIRE Engine synthesizes and sums all enabled channel-strip audio to your target release format at up to 25x speed.",
                                fontSize = 10.sp,
                                color = Color(0xFF9E93B3),
                                lineHeight = 14.sp
                            )

                            HorizontalDivider(color = Color(0xFF2E244E))

                            // Format Picker
                            Text("Target Release Format", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF09041A))
                                    .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                var expanded by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = true },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedExportFormat.displayName, color = Color(0xFFF9D142), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color(0xFF140D2A))
                                ) {
                                    ExportFormat.values().forEach { fmt ->
                                        DropdownMenuItem(
                                            text = { Text(fmt.displayName, color = Color.White, fontSize = 11.sp) },
                                            onClick = {
                                                selectedExportFormat = fmt
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Duration slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bounce Duration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("${exportDurationSec}s", fontSize = 12.sp, color = Color(0xFF9F75FF), fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = exportDurationSec.toFloat(),
                                    onValueChange = { exportDurationSec = it.toInt() },
                                    valueRange = 5f..120f,
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = Color(0xFF9F75FF),
                                        thumbColor = Color(0xFFF9D142)
                                    )
                                )
                            }

                            // Dynamic exporting status renderer
                            when (val status = exportStatus) {
                                is ExportStatus.Idle -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { studioViewModel.showExportDialog.value = false }) {
                                            Text("Cancel", color = Color(0xFFFF5252))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { studioViewModel.triggerOfflineBounceExport(selectedExportFormat, exportDurationSec) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142))
                                        ) {
                                            Text("Bounce Stems", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                is ExportStatus.Rendering -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LinearProgressIndicator(
                                            progress = status.progress,
                                            color = Color(0xFF9F75FF),
                                            trackColor = Color(0xFF2E244E),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Rendering master... ${(status.progress * 100).toInt()}%", fontSize = 10.sp, color = Color.White)
                                            Text("Speed: ${String.format("%.1f", status.speedMultiplier)}x", fontSize = 10.sp, color = Color(0xFF2FD6AA))
                                        }
                                        TextButton(onClick = { studioViewModel.cancelExport() }) {
                                            Text("Cancel Render", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                is ExportStatus.Success -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF2FD6AA), modifier = Modifier.size(36.dp))
                                        Text("Render Complete successfully!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Master output: ${status.file.name} (${status.sizeKb} KB)", color = Color(0xFF9E93B3), fontSize = 10.sp, textAlign = TextAlign.Center)
                                        
                                        Button(
                                            onClick = {
                                                // Save exported metadata to project files list and close dialog
                                                studioViewModel.showExportDialog.value = false
                                                // Reset export state
                                                studioViewModel.cancelExport()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FD6AA)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Awesome", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                is ExportStatus.Failed -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Error, contentDescription = "Failed", tint = Color(0xFFFF5252), modifier = Modifier.size(36.dp))
                                        Text("Render Failed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(status.error, color = Color(0xFFFF5252), fontSize = 10.sp, textAlign = TextAlign.Center)
                                        
                                        Button(
                                            onClick = { studioViewModel.cancelExport() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------- SUB WORKSPACES COMODS -------------------

@Composable
fun OverviewWorkspace(
    projectState: StudioProjectState,
    studioViewModel: StudioViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("Active Session Overview", color = Color(0xFFF9D142), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(projectState.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (projectState.isDirty) Color(0xFFF9D142).copy(alpha = 0.2f) else Color(0xFF2FD6AA).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (projectState.isDirty) "Modified" else "Synced",
                                color = if (projectState.isDirty) Color(0xFFF9D142) else Color(0xFF2FD6AA),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = projectState.description.ifBlank { "Add structured descriptions to categorize Indian styles, raga selections, or master plan configurations." },
                        color = Color(0xFF9E93B3),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text("Multitrack Channels Layout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(projectState.tracks) { track ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (track.type) {
                                        "Vocal" -> Color(0xFF9F75FF).copy(alpha = 0.15f)
                                        "Melody" -> Color(0xFFF9D142).copy(alpha = 0.15f)
                                        "Percussion" -> Color(0xFF2FD6AA).copy(alpha = 0.15f)
                                        else -> Color(0xFFBA68C8).copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (track.type) {
                                    "Vocal" -> Icons.Default.RecordVoiceOver
                                    "Melody" -> Icons.Default.MusicNote
                                    "Percussion" -> Icons.Default.QueueMusic
                                    else -> Icons.Default.Audiotrack
                                },
                                contentDescription = track.type,
                                tint = when (track.type) {
                                    "Vocal" -> Color(0xFF9F75FF)
                                    "Melody" -> Color(0xFFF9D142)
                                    "Percussion" -> Color(0xFF2FD6AA)
                                    else -> Color(0xFFBA68C8)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(track.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "Volume: ${String.format("%.1f", track.volumeDb)} dB | Pan: ${if (track.pan < 0) "L" else if (track.pan > 0) "R" else "C"}",
                                color = Color(0xFF9E93B3),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (track.isMuted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFF5252).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("MUTED", color = Color(0xFFFF5252), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (track.isSoloed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF9D142).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SOLO", color = Color(0xFFF9D142), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MixerWorkspace(
    projectState: StudioProjectState,
    studioViewModel: StudioViewModel,
    isPlaying: Boolean,
    waveOffset: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Fader Strips & Spatial Pan Desk", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projectState.tracks) { track ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header info and Mute/Solo buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(track.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(track.type, color = Color(0xFF9E93B3), fontSize = 9.sp)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Mute Button
                                Button(
                                    onClick = { studioViewModel.toggleTrackMute(track.trackId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (track.isMuted) Color(0xFFFF5252) else Color(0xFF2E244E)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = "M",
                                        color = if (track.isMuted) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                // Solo Button
                                Button(
                                    onClick = { studioViewModel.toggleTrackSolo(track.trackId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (track.isSoloed) Color(0xFFF9D142) else Color(0xFF2E244E)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = "S",
                                        color = if (track.isSoloed) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Volumes Balance Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Volume Balance", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                Text("${String.format("%.1f", track.volumeDb)} dB", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = track.volumeDb,
                                onValueChange = { studioViewModel.updateTrackVolume(track.trackId, it) },
                                valueRange = -48f..12f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFF9F75FF),
                                    thumbColor = Color(0xFFF9D142)
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // Spatial Panning Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Spatial Pan Matrix", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                Text(
                                    text = if (track.pan < -0.05f) "L ${String.format("%.1f", -track.pan)}" else if (track.pan > 0.05f) "R ${String.format("%.1f", track.pan)}" else "Center",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = track.pan,
                                onValueChange = { studioViewModel.updateTrackPan(track.trackId, it) },
                                valueRange = -1f..1f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFF2FD6AA),
                                    thumbColor = Color(0xFF9F75FF)
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // Peak indicator levels moving when playing
                        AnimatedVisibility(visible = isPlaying && !track.isMuted) {
                            val seed = track.trackId.hashCode().toDouble()
                            val peakVal = (0.3f + 0.6f * sin(waveOffset * 0.1 + seed).toFloat().coerceIn(0f, 1f))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Signal Peak Monitoring", color = Color(0xFF9E93B3).copy(alpha = 0.6f), fontSize = 8.sp)
                                LinearProgressIndicator(
                                    progress = peakVal,
                                    color = if (peakVal > 0.85f) ColorxFFFF5252 else if (peakVal > 0.6f) Color(0xFFF9D142) else Color(0xFF2FD6AA),
                                    trackColor = Color(0xFF09041A),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

val ColorxFFFF5252 = Color(0xFFFF5252)

@Composable
fun AssetsWorkspace(
    studioViewModel: StudioViewModel
) {
    val context = LocalContext.current
    var assets by remember { mutableStateOf<List<File>>(emptyList()) }

    // Fetch assets list on creation
    LaunchedEffect(Unit) {
        assets = studioViewModel.getProjectAssets()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Active Session Asset Browser", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        if (assets.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, tint = Color(0xFF9E93B3), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No local stems or recording assets found", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Synthesize stems using Instruments or bounce a master to populate this folder archive.", color = Color(0xFF9E93B3).copy(alpha = 0.7f), fontSize = 9.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(assets) { file ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (file.name.endsWith(".wav") || file.name.endsWith(".mp3")) Icons.Default.AudioFile else Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = Color(0xFF9F75FF)
                                )
                                Column {
                                    Text(file.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${file.length() / 1024} KB | System File", color = Color(0xFF9E93B3), fontSize = 9.sp)
                                }
                            }

                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Asset file verified & ready for AI Mixer (AMIE)", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Asset Preview", tint = Color(0xFF2FD6AA))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticsWorkspace(
    logs: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AI Studio Telemetry Diagnostics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF09041A))
                .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = if (log.contains("Error", ignoreCase = true) || log.contains("failed", ignoreCase = true)) ColorxFFFF5252 else if (log.contains("Transport", ignoreCase = true)) Color(0xFFF9D142) else Color(0xFF2FD6AA),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// ------------------- TRANSPORT DECK COMPOSABLE -------------------

@Composable
fun TransportControlDeck(
    isPlaying: Boolean,
    bpm: Float,
    timeSignature: String,
    loopEnabled: Boolean,
    playheadSec: Double,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Double) -> Unit,
    onBpmChange: (Float) -> Unit,
    onTimeSigChange: (String) -> Unit,
    onToggleLoop: () -> Unit,
    onExportTrigger: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline Playhead slider scrub
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(playheadSec),
                color = Color(0xFFF9D142),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Slider(
                value = playheadSec.toFloat(),
                onValueChange = { onSeek(it.toDouble()) },
                valueRange = 0f..120f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFFF9D142),
                    thumbColor = Color(0xFF9F75FF)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            Text(
                text = "02:00",
                color = Color(0xFF9E93B3),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        // Control Buttons Deck
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block: BPM and Time signature
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tempo BPM
                var showBpmSlider by remember { mutableStateOf(false) }
                Box {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF09041A))
                            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                            .clickable { showBpmSlider = !showBpmSlider }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("BPM", fontSize = 8.sp, color = Color(0xFF9E93B3))
                        Text("${bpm.toInt()}", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (showBpmSlider) {
                        DropdownMenu(
                            expanded = showBpmSlider,
                            onDismissRequest = { showBpmSlider = false },
                            modifier = Modifier
                                .width(200.dp)
                                .background(Color(0xFF140D2A))
                                .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("Tempo BPM: ${bpm.toInt()}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = bpm,
                                onValueChange = onBpmChange,
                                valueRange = 40f..240f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFF9F75FF),
                                    thumbColor = Color(0xFFF9D142)
                                )
                            )
                        }
                    }
                }

                // Time Signature
                var showTimeSigMenu by remember { mutableStateOf(false) }
                Box {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF09041A))
                            .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                            .clickable { showTimeSigMenu = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("METER", fontSize = 8.sp, color = Color(0xFF9E93B3))
                        Text(timeSignature, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    DropdownMenu(
                        expanded = showTimeSigMenu,
                        onDismissRequest = { showTimeSigMenu = false },
                        modifier = Modifier.background(Color(0xFF140D2A))
                    ) {
                        val measures = listOf("4/4", "3/4", "7/8", "6/8", "Teental (16 Beats)", "Rupak (7 Beats)")
                        measures.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    onTimeSigChange(m)
                                    showTimeSigMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Middle block: Playback buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Loop toggle
                IconButton(
                    onClick = onToggleLoop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (loopEnabled) Color(0xFF9F75FF).copy(alpha = 0.2f) else Color.Transparent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Loop,
                        contentDescription = "Loop Playback",
                        tint = if (loopEnabled) Color(0xFF9F75FF) else Color.White
                    )
                }

                // Stop Button
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF09041A))
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop Track", tint = Color.White)
                }

                // Play / Pause Button
                FloatingActionButton(
                    onClick = { if (isPlaying) onPause() else onPlay() },
                    containerColor = Color(0xFFF9D142),
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }
            }

            // Right block: Export Master button
            Button(
                onClick = onExportTrigger,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FD6AA)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text("Export", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatTime(seconds: Double): String {
    val m = (seconds / 60).toInt()
    val s = (seconds % 60).toInt()
    return String.format("%02d:%02d", m, s)
}

@Composable
fun TimelineWorkspace(
    projectState: StudioProjectState,
    studioViewModel: StudioViewModel,
    isPlaying: Boolean,
    playheadSec: Double
) {
    val zoom by studioViewModel.zoomScale.collectAsState()
    val snap by studioViewModel.snapGrid.collectAsState()
    val rippleEnabled by studioViewModel.isRippleEditEnabled.collectAsState()

    val connectedDevices by studioViewModel.connectedInputDevices.collectAsState()
    val selectedDeviceName by studioViewModel.selectedInputDeviceName.collectAsState()
    val isRecordingActive by studioViewModel.isRecordingDirect.collectAsState()
    val liveInputDb by studioViewModel.inputLevelDb.collectAsState()
    val recordingTrackId by studioViewModel.recordingTrackId.collectAsState()

    val selectedSource by studioViewModel.selectedInputSource.collectAsState()
    val selectedChannel by studioViewModel.selectedChannelConfig.collectAsState()
    val selectedSampleRate by studioViewModel.selectedSampleRate.collectAsState()
    val selectedBitDepth by studioViewModel.selectedBitDepth.collectAsState()
    val isLiveMonitoringEnabled by studioViewModel.isLiveMonitoringEnabled.collectAsState()
    val recordingDurationSec by studioViewModel.recordingDurationSec.collectAsState()

    val context = LocalContext.current

    // Automatically scan devices on composition
    LaunchedEffect(Unit) {
        studioViewModel.scanAudioInputDevices()
    }

    var selectedTrackId by remember { mutableStateOf<String?>(null) }
    var selectedClipId by remember { mutableStateOf<String?>(null) }

    var showAddTrackDialog by remember { mutableStateOf(false) }
    var newTrackName by remember { mutableStateOf("") }
    var newTrackType by remember { mutableStateOf("Melody") }
    var newTrackFolder by remember { mutableStateOf("") }

    // Resolve selected clip
    val selectedClipState = remember(projectState, selectedTrackId, selectedClipId) {
        val track = projectState.tracks.find { it.trackId == selectedTrackId }
        track?.clips?.find { it.clipId == selectedClipId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- TIMELINE CONTROLS BAR ---
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zoom group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Zoom:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                    IconButton(
                        onClick = { studioViewModel.updateZoomScale(zoom - 2f) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Text("${zoom.toInt()}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { studioViewModel.updateZoomScale(zoom + 2f) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Snap group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Snap Grid:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                    var expandedSnap by remember { mutableStateOf(false) }
                    Box {
                        Text(
                            text = snap,
                            color = Color(0xFF2FD6AA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E244E))
                                .clickable { expandedSnap = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        DropdownMenu(
                            expanded = expandedSnap,
                            onDismissRequest = { expandedSnap = false },
                            modifier = Modifier.background(Color(0xFF140D2A))
                        ) {
                            listOf("Off", "Beat", "Bar", "2 Bars").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = Color.White, fontSize = 11.sp) },
                                    onClick = {
                                        studioViewModel.updateSnapGrid(option)
                                        expandedSnap = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Ripple edit group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Ripple Edit:", color = Color(0xFF9E93B3), fontSize = 11.sp)
                    Switch(
                        checked = rippleEnabled,
                        onCheckedChange = { studioViewModel.toggleRippleEdit() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFF9D142),
                            checkedTrackColor = Color(0xFF9F75FF)
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                }

                // Add Track Button
                Button(
                    onClick = { showAddTrackDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Track", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- HARDWARE CONFIGURATION PANEL (Ahuja & USB Audio Interface Integration) ---
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: Active Device and Level Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsInputComponent,
                            contentDescription = "USB Interface",
                            tint = if (selectedDeviceName.contains("USB")) Color(0xFF2FD6AA) else Color(0xFF9F75FF),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Column {
                            Text(
                                text = "ACTIVE AUDIO HARDWARE",
                                color = Color(0xFF9E93B3),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            
                            var showDeviceMenu by remember { mutableStateOf(false) }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF130932))
                                        .clickable { showDeviceMenu = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = selectedDeviceName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Input",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = showDeviceMenu,
                                    onDismissRequest = { showDeviceMenu = false },
                                    modifier = Modifier.background(Color(0xFF140D2A))
                                ) {
                                    connectedDevices.forEach { device ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    text = device,
                                                    color = if (device == selectedDeviceName) Color(0xFF2FD6AA) else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (device == selectedDeviceName) FontWeight.Bold else FontWeight.Normal
                                                ) 
                                            },
                                            onClick = {
                                                studioViewModel.selectedInputDeviceName.value = device
                                                if (device.contains("USB") || device.contains("⚡")) {
                                                    studioViewModel.selectedInputSource.value = "USB Audio Interface"
                                                } else {
                                                    studioViewModel.selectedInputSource.value = "Built-in Microphone"
                                                }
                                                showDeviceMenu = false
                                                studioViewModel.logOperation("Audio input hardware assigned to: $device")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Meter & Status Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                studioViewModel.scanAudioInputDevices()
                                Toast.makeText(context, "Scanning for USB & Ahuja interfaces...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan Audio Inputs",
                                tint = Color(0xFF2FD6AA),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isRecordingActive) "🔴 RECORDING LIVE" else "READY / MONITORING",
                                color = if (isRecordingActive) Color(0xFFFF5252) else Color(0xFF2FD6AA),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFF2E244E))
                                ) {
                                    val percentage = ((liveInputDb + 120f) / 120f).coerceIn(0f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(percentage)
                                            .background(
                                                if (liveInputDb > -15f) Color(0xFFFF5252) else Color(0xFF2FD6AA)
                                            )
                                    )
                                }
                                Text(
                                    text = "${liveInputDb.toInt()} dB",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Divider(color = Color(0x1AFFFFFF), thickness = 1.dp)

                // Row 2: Source selector, Channels, Sample Rate, Bit Depth, and Live Monitoring
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Source selection
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("SOURCE", color = Color(0xFF9E93B3), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF130932))
                                .padding(2.dp)
                        ) {
                            listOf("Mic", "USB Link").forEach { src ->
                                val mappedSrc = if (src == "Mic") "Built-in Microphone" else "USB Audio Interface"
                                val isSelected = selectedSource == mappedSrc
                                Box(
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSelected) Color(0xFF9F75FF) else Color.Transparent)
                                        .clickable { 
                                            studioViewModel.selectedInputSource.value = mappedSrc
                                            if (mappedSrc == "Built-in Microphone") {
                                                studioViewModel.selectedInputDeviceName.value = "Default Microphone"
                                            } else {
                                                val list = connectedDevices
                                                val firstUsb = list.firstOrNull { it.startsWith("⚡ USB:") }
                                                if (firstUsb != null) {
                                                    studioViewModel.selectedInputDeviceName.value = firstUsb
                                                } else {
                                                    Toast.makeText(context, "Please connect a USB interface first!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = src,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Channels dropdown
                    Column {
                        Text("CHANNELS", color = Color(0xFF9E93B3), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        var showChannelMenu by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF130932))
                                    .clickable { showChannelMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(selectedChannel, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                            DropdownMenu(
                                expanded = showChannelMenu,
                                onDismissRequest = { showChannelMenu = false },
                                modifier = Modifier.background(Color(0xFF140D2A))
                            ) {
                                listOf("Mono", "Stereo").forEach { ch ->
                                    DropdownMenuItem(
                                        text = { Text(ch, color = Color.White, fontSize = 10.sp) },
                                        onClick = {
                                            studioViewModel.selectedChannelConfig.value = ch
                                            showChannelMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Sample Rate dropdown
                    Column {
                        Text("SAMPLE RATE", color = Color(0xFF9E93B3), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        var showRateMenu by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF130932))
                                    .clickable { showRateMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("${selectedSampleRate} Hz", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                            DropdownMenu(
                                expanded = showRateMenu,
                                onDismissRequest = { showRateMenu = false },
                                modifier = Modifier.background(Color(0xFF140D2A))
                            ) {
                                listOf(44100, 48000).forEach { rate ->
                                    DropdownMenuItem(
                                        text = { Text("$rate Hz", color = Color.White, fontSize = 10.sp) },
                                        onClick = {
                                            studioViewModel.selectedSampleRate.value = rate
                                            showRateMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Bit Depth dropdown
                    Column {
                        Text("BIT DEPTH", color = Color(0xFF9E93B3), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        var showBitMenu by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF130932))
                                    .clickable { showBitMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("${selectedBitDepth}-bit", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White, modifier = Modifier.size(10.dp))
                            }
                            DropdownMenu(
                                expanded = showBitMenu,
                                onDismissRequest = { showBitMenu = false },
                                modifier = Modifier.background(Color(0xFF140D2A))
                            ) {
                                listOf(16, 24).forEach { depth ->
                                    DropdownMenuItem(
                                        text = { Text("$depth-bit", color = Color.White, fontSize = 10.sp) },
                                        onClick = {
                                            studioViewModel.selectedBitDepth.value = depth
                                            showBitMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Live monitoring
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MONITOR", color = Color(0xFF9E93B3), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isLiveMonitoringEnabled,
                            onCheckedChange = { studioViewModel.isLiveMonitoringEnabled.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF2FD6AA),
                                checkedTrackColor = Color(0xFF130932)
                            ),
                            modifier = Modifier.scale(0.6f)
                        )
                    }
                }

                // Row 3: Recording Active Info Panel (armed track, timer)
                if (isRecordingActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x15FF5252))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                            )
                            val armedTrackName = projectState.tracks.find { it.trackId == recordingTrackId }?.name ?: "Unknown Track"
                            Text(
                                text = "Recording into: $armedTrackName",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Timer: ${recordingDurationSec}s",
                            color = Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- TRACKS & TIMELINE GRID CANVAS ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF09041A))
                .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Timeline Ruler
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(Color(0xFF130932))
                        .border(1.dp, Color(0xFF2E244E))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(zoom) {
                                detectTapGestures { offset ->
                                    val time = (offset.x / zoom).toDouble()
                                    studioViewModel.seekTo(time.coerceAtLeast(0.0))
                                }
                            }
                    ) {
                        val rulerWidth = size.width
                        val secondInterval = 5f
                        var sec = 0f
                        while (sec * zoom < rulerWidth) {
                            val x = sec * zoom
                            drawLine(
                                color = Color(0xFF9E93B3).copy(alpha = 0.4f),
                                start = androidx.compose.ui.geometry.Offset(x, 10f),
                                end = androidx.compose.ui.geometry.Offset(x, size.height),
                                strokeWidth = 1f
                            )
                            sec += secondInterval
                        }
                    }

                    // Text labels overlay on ruler
                    Box(modifier = Modifier.fillMaxSize()) {
                        var labelSec = 0
                        while (labelSec * zoom < 2000f) {
                            val xOffset = (labelSec * zoom).dp
                            Text(
                                text = formatTime(labelSec.toDouble()),
                                color = Color(0xFF9E93B3).copy(alpha = 0.8f),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.offset(x = xOffset, y = 2.dp)
                            )
                            labelSec += 10
                        }
                    }

                    // Playhead Line Overlay
                    val playheadX = (playheadSec * zoom).toFloat()
                    Box(
                        modifier = Modifier
                            .offset(x = playheadX.dp)
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(Color(0xFFF9D142))
                    )
                }

                // Scrollable Tracks Rows
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val groupedTracks = projectState.tracks.groupBy { it.folderGroup }
                    
                    groupedTracks.forEach { (folder, tracks) ->
                        if (folder != null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E1740).copy(alpha = 0.6f))
                                        .border(1.dp, Color(0xFF2E244E))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = Color(0xFFF9D142),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = folder.uppercase(),
                                            color = Color(0xFFF9D142),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "(${tracks.size} tracks group)",
                                            color = Color(0xFF9E93B3),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }

                        items(tracks) { track ->
                            val isSelectedTrack = selectedTrackId == track.trackId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .background(if (isSelectedTrack) Color(0xFF211440).copy(alpha = 0.4f) else Color.Transparent)
                                    .border(1.dp, Color(0xFF2E244E)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Track Header Panel
                                Column(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .fillMaxHeight()
                                        .background(Color(0xFF140D2A))
                                        .border(1.dp, Color(0xFF2E244E))
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(track.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(track.type, color = Color(0xFF9E93B3), fontSize = 8.sp)
                                        }

                                        var showFolderMenu by remember { mutableStateOf(false) }
                                        Box {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = Color(0xFF9E93B3),
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { showFolderMenu = true }
                                            )
                                            DropdownMenu(
                                                expanded = showFolderMenu,
                                                onDismissRequest = { showFolderMenu = false },
                                                modifier = Modifier.background(Color(0xFF140D2A))
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Move to Melody Stems", color = Color.White, fontSize = 10.sp) },
                                                    onClick = {
                                                        studioViewModel.updateTrackFolderGroup(track.trackId, "Melody Stems")
                                                        showFolderMenu = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Move to Rhythm Sect", color = Color.White, fontSize = 10.sp) },
                                                    onClick = {
                                                        studioViewModel.updateTrackFolderGroup(track.trackId, "Rhythm Sect")
                                                        showFolderMenu = false
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Move out of folders", color = Color.White, fontSize = 10.sp) },
                                                    onClick = {
                                                        studioViewModel.updateTrackFolderGroup(track.trackId, null)
                                                        showFolderMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (track.isMuted) Color(0xFFFF5252) else Color(0xFF2E244E))
                                                .clickable { studioViewModel.toggleTrackMute(track.trackId) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("M", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (track.isSoloed) Color(0xFFF9D142) else Color(0xFF2E244E))
                                                .clickable { studioViewModel.toggleTrackSolo(track.trackId) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("S", color = if (track.isSoloed) Color.Black else Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        val isThisTrackRecording = isRecordingActive && recordingTrackId == track.trackId
                                        Box(
                                            modifier = Modifier
                                                .height(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isThisTrackRecording) Color(0xFFFF5252) else if (isRecordingActive) Color.Gray.copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.2f))
                                                .clickable {
                                                    if (isThisTrackRecording) {
                                                        studioViewModel.stopDirectRecording()
                                                    } else if (!isRecordingActive) {
                                                        studioViewModel.startDirectRecording(track.trackId)
                                                    } else {
                                                        Toast.makeText(context, "Another track is currently recording!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(horizontal = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isThisTrackRecording) Color.White else Color(0xFFFF5252))
                                                )
                                                Text(
                                                    text = if (isThisTrackRecording) "STOP" else "REC",
                                                    color = if (isThisTrackRecording) Color.White else Color(0xFFFF5252),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Box(
                                            modifier = Modifier
                                                .height(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF2FD6AA).copy(alpha = 0.2f))
                                                .clickable {
                                                    studioViewModel.createEmptyClipOnTrack(track.trackId, playheadSec, 15.0)
                                                }
                                                .padding(horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("Compile", color = Color(0xFF2FD6AA), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Timeline Track horizontal canvas scrollable
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val rowWidth = size.width
                                        val divisionPx = 10f * zoom
                                        var x = 0f
                                        while (x < rowWidth) {
                                            drawLine(
                                                color = Color(0xFF2E244E).copy(alpha = 0.3f),
                                                start = androidx.compose.ui.geometry.Offset(x, 0f),
                                                end = androidx.compose.ui.geometry.Offset(x, size.height),
                                                strokeWidth = 0.5f
                                            )
                                            x += divisionPx
                                        }
                                    }

                                    track.clips.forEach { clip ->
                                        val isSelectedClip = selectedTrackId == track.trackId && selectedClipId == clip.clipId
                                        val clipStartPx = (clip.startOffsetSec * zoom).dp
                                        val clipWidthPx = (clip.durationSec * zoom).dp

                                        Box(
                                            modifier = Modifier
                                                .offset(x = clipStartPx)
                                                .width(clipWidthPx)
                                                .fillMaxHeight()
                                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.parseHtml(clip.colorHex).copy(alpha = if (isSelectedClip) 0.85f else 0.5f))
                                                .border(
                                                    width = if (isSelectedClip) 2.dp else 1.dp,
                                                    color = if (isSelectedClip) Color(0xFFF9D142) else Color.parseHtml(clip.colorHex),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    selectedTrackId = track.trackId
                                                    selectedClipId = clip.clipId
                                                }
                                                .padding(6.dp)
                                        ) {
                                            Canvas(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .alpha(0.35f)
                                            ) {
                                                val peaksCount = (clip.durationSec * 2).toInt().coerceIn(10..200)
                                                val barWidth = size.width / peaksCount
                                                val randSeed = clip.clipId.hashCode().toDouble()
                                                
                                                for (i in 0 until peaksCount) {
                                                    val peakScale = sin(i * 0.15 + randSeed).toFloat().coerceIn(0.1f..0.9f)
                                                    val amplitude = size.height * peakScale * 0.7f
                                                    val barX = i * barWidth
                                                    val barY = (size.height - amplitude) / 2
                                                    
                                                    drawRect(
                                                        color = Color.White,
                                                        topLeft = androidx.compose.ui.geometry.Offset(barX, barY),
                                                        size = androidx.compose.ui.geometry.Size(barWidth * 0.6f, amplitude)
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                                Text(
                                                    text = clip.name,
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "${String.format("%.1f", clip.startOffsetSec)}s",
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        fontSize = 7.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                    Text(
                                                        text = "Len: ${String.format("%.1f", clip.durationSec)}s",
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        fontSize = 7.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    val lanePlayheadX = (playheadSec * zoom).toFloat()
                                    Box(
                                        modifier = Modifier
                                            .offset(x = lanePlayheadX.dp)
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(Color(0xFFF9D142).copy(alpha = 0.5f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SELECTED CLIP EDITING DRAWER ---
        AnimatedVisibility(visible = selectedClipState != null) {
            val clip = selectedClipState!!
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                                    .background(Color.parseHtml(clip.colorHex))
                            )
                            Text(
                                text = "Selected Region: ${clip.name}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Split Clip Button
                            Button(
                                onClick = {
                                    if (playheadSec > clip.startOffsetSec && playheadSec < clip.startOffsetSec + clip.durationSec) {
                                        studioViewModel.splitClip(selectedTrackId!!, clip.clipId, playheadSec)
                                        selectedClipId = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                enabled = playheadSec > clip.startOffsetSec && playheadSec < clip.startOffsetSec + clip.durationSec,
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCut, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Split at Playhead", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            // Delete Clip
                            IconButton(
                                onClick = {
                                    studioViewModel.deleteClip(selectedTrackId!!, clip.clipId)
                                    selectedClipId = null
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252).copy(alpha = 0.15f))
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Clip", tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Sliders for dragging and resizing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Start Position Offset", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                Text("${String.format("%.1f", clip.startOffsetSec)}s", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = clip.startOffsetSec.toFloat(),
                                onValueChange = { studioViewModel.moveClip(selectedTrackId!!, clip.clipId, it.toDouble()) },
                                valueRange = 0f..100f,
                                modifier = Modifier.height(24.dp),
                                colors = SliderDefaults.colors(activeTrackColor = Color(0xFFF9D142))
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Trim Region Length", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                Text("${String.format("%.1f", clip.durationSec)}s", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = clip.durationSec.toFloat(),
                                onValueChange = { studioViewModel.trimClip(selectedTrackId!!, clip.clipId, clip.startOffsetSec, it.toDouble()) },
                                valueRange = 0.5f..60f,
                                modifier = Modifier.height(24.dp),
                                colors = SliderDefaults.colors(activeTrackColor = Color(0xFF9F75FF))
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Add Custom Track Dialog ---
    if (showAddTrackDialog) {
        AlertDialog(
            onDismissRequest = { showAddTrackDialog = false },
            title = { Text("Add Track to Timeline Workspace", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newTrackName,
                        onValueChange = { newTrackName = it },
                        label = { Text("Track Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9F75FF),
                            unfocusedBorderColor = Color(0xFF2E244E),
                            focusedLabelColor = Color(0xFF9F75FF)
                        )
                    )

                    Column {
                        Text("Track Stem Category", color = Color(0xFF9E93B3), fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Melody", "Vocal", "Percussion", "Drone").forEach { cat ->
                                val selected = newTrackType == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (selected) Color(0xFF9F75FF) else Color(0xFF2E244E))
                                        .clickable { newTrackType = cat }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (selected) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newTrackFolder,
                        onValueChange = { newTrackFolder = it },
                        label = { Text("Folder Group (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF9F75FF),
                            unfocusedBorderColor = Color(0xFF2E244E)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTrackName.isNotBlank()) {
                            studioViewModel.addNewTrack(newTrackName, newTrackType, newTrackFolder)
                            newTrackName = ""
                            newTrackFolder = ""
                            showAddTrackDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2FD6AA))
                ) {
                    Text("Add", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTrackDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF140D2A)
        )
    }
}

fun Color.Companion.parseHtml(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color(0xFF9F75FF)
    }
}
