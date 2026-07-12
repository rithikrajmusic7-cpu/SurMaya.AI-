package com.example.ui.screens.diagnostics

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.qa.QADefect
import com.example.domain.model.qa.QAQualityReport
import com.example.ui.viewmodel.QAViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioQualityValidationScreen(
    qaViewModel: QAViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reports by qaViewModel.reports.collectAsState()
    val defects by qaViewModel.defects.collectAsState()
    val selectedReport by qaViewModel.selectedReport.collectAsState()
    val isHarnessRunning by qaViewModel.isHarnessRunning.collectAsState()
    val harnessStatusMessage by qaViewModel.harnessStatusMessage.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Dashboard, 1 = Defects
    var showAddDefectDialog by remember { mutableStateOf(false) }
    var showManualValidateDialog by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF00E5FF) // Cyan
    val goldAccent = Color(0xFFF9D142) // Gold
    val bgDark = Color(0xFF09041A) // Dark deep purple/black
    val cardBg = Color(0xFF1E143D) // Card dark violet

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Audio Quality Validation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("qa_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { qaViewModel.clearAllQAData() },
                        modifier = Modifier.testTag("qa_clear_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Clear All Data",
                            tint = Color(0xFFFF5252)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        },
        containerColor = bgDark,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Harness Progress banner
            harnessStatusMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E244E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("harness_status_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (isHarnessRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = primaryColor
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Done",
                                tint = Color(0xFF39FF14),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = msg,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Tab selectors (Dashboard & Defects)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = bgDark,
                contentColor = primaryColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = primaryColor
                    )
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Quality Dashboard", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    selectedContentColor = primaryColor,
                    unselectedContentColor = Color.White.copy(alpha = 0.5f)
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Defect Tracking (${defects.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    selectedContentColor = primaryColor,
                    unselectedContentColor = Color.White.copy(alpha = 0.5f)
                )
            }

            if (activeTab == 0) {
                if (selectedReport == null) {
                    // Dashboard Home
                    DashboardHomeView(
                        reports = reports,
                        defects = defects,
                        isHarnessRunning = isHarnessRunning,
                        onRunHarness = { count -> qaViewModel.runTestHarnessBatch(count) },
                        onSelectReport = { report -> qaViewModel.selectReport(report) },
                        onTriggerManualValidate = { showManualValidateDialog = true },
                        primaryColor = primaryColor,
                        goldAccent = goldAccent,
                        cardBg = cardBg,
                        qaViewModel = qaViewModel
                    )
                } else {
                    // Report Detail View
                    ReportDetailView(
                        report = selectedReport!!,
                        defects = defects,
                        onClose = { qaViewModel.selectReport(null) },
                        onCreateDefectFromReport = {
                            qaViewModel.createManualDefect(
                                module = "Mastering",
                                severity = "Major",
                                description = "Validation manual defect on report: ${selectedReport!!.title}",
                                reproductionSteps = "Inspect track validation details",
                                expectedResult = "Score >= 90.0",
                                actualResult = "Score is ${selectedReport!!.overallScore}",
                                songId = selectedReport!!.songId
                            )
                        },
                        primaryColor = primaryColor,
                        goldAccent = goldAccent,
                        cardBg = cardBg,
                        qaViewModel = qaViewModel
                    )
                }
            } else {
                // Defects Tracker View
                DefectsTrackerView(
                    defects = defects,
                    onUpdateStatus = { id, status -> qaViewModel.updateDefectStatus(id, status) },
                    onDeleteDefect = { id -> qaViewModel.deleteDefect(id) },
                    onTriggerAddDefect = { showAddDefectDialog = true },
                    primaryColor = primaryColor,
                    cardBg = cardBg
                )
            }
        }
    }

    // Manual validation dialog
    if (showManualValidateDialog) {
        var title by remember { mutableStateOf("") }
        var genre by remember { mutableStateOf("Romantic") }
        var language by remember { mutableStateOf("Hindi") }
        var voice by remember { mutableStateOf("Shrija") }

        val genresList = listOf("Romantic", "Sad", "Devotional", "Odia", "Hindi", "Folk", "Classical", "Pop")
        val voicesList = listOf("Ajit", "Shrija", "Pandit G", "Shanti")
        val languagesList = listOf("Hindi", "Odia", "Sanskrit", "Bengali")

        AlertDialog(
            onDismissRequest = { showManualValidateDialog = false },
            containerColor = cardBg,
            title = { Text("Validate Generated Audio", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Song Title", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = primaryColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_song_title_input")
                    )

                    // Simple select lists
                    Text("Genre:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        genresList.take(4).forEach { g ->
                            FilterChip(
                                selected = genre == g,
                                onClick = { genre = g },
                                label = { Text(g, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        genresList.takeLast(4).forEach { g ->
                            FilterChip(
                                selected = genre == g,
                                onClick = { genre = g },
                                label = { Text(g, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Text("Singer Voice:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        voicesList.forEach { v ->
                            FilterChip(
                                selected = voice == v,
                                onClick = { voice = v },
                                label = { Text(v, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Text("Language:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        languagesList.forEach { l ->
                            FilterChip(
                                selected = language == l,
                                onClick = { language = l },
                                label = { Text(l, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            qaViewModel.validateNewSong(title, genre, language, voice)
                            showManualValidateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.testTag("manual_validate_confirm_button")
                ) {
                    Text("Analyze & Log", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualValidateDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    // Add defect manual dialog
    if (showAddDefectDialog) {
        var module by remember { mutableStateOf("Melody") }
        var severity by remember { mutableStateOf("Major") }
        var description by remember { mutableStateOf("") }
        var reproSteps by remember { mutableStateOf("") }
        var expectedResult by remember { mutableStateOf("") }
        var actualResult by remember { mutableStateOf("") }

        val modulesList = listOf("Melody", "Rhythm", "Vocals", "Mixing", "Mastering")
        val severitiesList = listOf("Critical", "Major", "Minor")

        AlertDialog(
            onDismissRequest = { showAddDefectDialog = false },
            containerColor = cardBg,
            title = { Text("Log QA Defect Ticket", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text("Module Layer:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            modulesList.forEach { m ->
                                FilterChip(
                                    selected = module == m,
                                    onClick = { module = m },
                                    label = { Text(m, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Text("Severity Rating:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            severitiesList.forEach { s ->
                                FilterChip(
                                    selected = severity == s,
                                    onClick = { severity = s },
                                    label = { Text(s, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when(s) {
                                            "Critical" -> Color(0xFFFF5252)
                                            "Major" -> Color(0xFFF9D142)
                                            else -> Color(0xFF00E5FF)
                                        },
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Short Description", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("defect_description_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = reproSteps,
                            onValueChange = { reproSteps = it },
                            label = { Text("Reproduction Steps", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("defect_repro_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = expectedResult,
                            onValueChange = { expectedResult = it },
                            label = { Text("Expected Outcome", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("defect_expected_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = actualResult,
                            onValueChange = { actualResult = it },
                            label = { Text("Actual Anomalies Observed", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("defect_actual_input")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (description.isNotBlank()) {
                            qaViewModel.createManualDefect(
                                module = module,
                                severity = severity,
                                description = description,
                                reproductionSteps = reproSteps,
                                expectedResult = expectedResult,
                                actualResult = actualResult
                            )
                            showAddDefectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.testTag("manual_defect_confirm_button")
                ) {
                    Text("Save Ticket", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDefectDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

@Composable
fun DashboardHomeView(
    reports: List<QAQualityReport>,
    defects: List<QADefect>,
    isHarnessRunning: Boolean,
    onRunHarness: (Int) -> Unit,
    onSelectReport: (QAQualityReport) -> Unit,
    onTriggerManualValidate: () -> Unit,
    primaryColor: Color,
    goldAccent: Color,
    cardBg: Color,
    qaViewModel: QAViewModel
) {
    val totalValidations = reports.size
    val averageScore = if (reports.isEmpty()) 0f else reports.map { it.overallScore }.average().toFloat()
    val passingCount = reports.count { it.validationResult == "Pass" }
    val passRate = if (reports.isEmpty()) 0f else (passingCount.toFloat() / totalValidations) * 100f
    val openDefects = defects.count { it.resolutionStatus != "Resolved" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // High-level QA Statistics Row
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Total Verified",
                    value = "$totalValidations",
                    icon = Icons.Filled.List,
                    color = primaryColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Avg Quality Score",
                    value = "${String.format("%.1f", averageScore)}%",
                    icon = Icons.Filled.GraphicEq,
                    color = goldAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatCard(
                    title = "Pass Rate",
                    value = "${String.format("%.1f", passRate)}%",
                    icon = Icons.Filled.Percent,
                    color = Color(0xFF39FF14),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Open Defects",
                    value = "$openDefects",
                    icon = Icons.Filled.BugReport,
                    color = Color(0xFFFF5252),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "QA Execution Suite",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Configure and launch the automated audio analysis system.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onTriggerManualValidate,
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, primaryColor),
                            enabled = !isHarnessRunning,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("validate_single_song_button")
                        ) {
                            Icon(imageVector = Icons.Filled.PlaylistPlay, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analyze Track", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onRunHarness(12) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !isHarnessRunning,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("launch_test_harness_button")
                        ) {
                            if (isHarnessRunning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(imageVector = Icons.Filled.Speed, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bulk Validate", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Real Audio Campaign Card (Milestone 3.1B)
        item {
            val isCampaignRunning by qaViewModel.isCampaignRunning.collectAsState()
            val campaignProgress by qaViewModel.campaignProgress.collectAsState()
            val campaignResult by qaViewModel.campaignResult.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().testTag("real_audio_campaign_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Campaign, contentDescription = null, tint = goldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Real Audio Validation Campaign",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Scan and validate real tracks residing in the local SurMaya music database. Auto-logs metrics and files defect tickets for sub-standard audio assets.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    
                    if (campaignResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = campaignResult ?: "",
                                fontSize = 10.sp,
                                color = goldAccent,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    if (isCampaignRunning) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = campaignProgress,
                                color = primaryColor,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${(campaignProgress * 100).toInt()}%",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { qaViewModel.injectDemoSongsForCampaign() },
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            enabled = !isCampaignRunning,
                            modifier = Modifier.weight(1f).testTag("inject_demo_songs_button")
                        ) {
                            Icon(imageVector = Icons.Filled.QueueMusic, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inject Demo Tracks", color = Color.White, fontSize = 10.sp)
                        }

                        Button(
                            onClick = { qaViewModel.runRealAudioCampaign() },
                            colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                            enabled = !isCampaignRunning,
                            modifier = Modifier.weight(1f).testTag("run_real_campaign_button")
                        ) {
                            if (isCampaignRunning) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.Black)
                            } else {
                                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Run Campaign", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // End-to-End Workflow Simulator Card (Milestone 3.2)
        item {
            val isWorkflowRunning by qaViewModel.isWorkflowRunning.collectAsState()
            val workflowStep by qaViewModel.workflowStep.collectAsState()
            val workflowLogs by qaViewModel.workflowLogs.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().testTag("e2e_workflow_simulator_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Memory, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "E2E System Workflow Simulator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Execute the complete, 6-stage SurMaya production pipeline simulation. Follows lyrics generation, chord layout, vocals synthesis, studio mix, mastering limiters, and final QA verification.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    if (isWorkflowRunning || workflowLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Step indicator
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            for (step in 1..6) {
                                val stepColor = when {
                                    workflowStep > step -> Color(0xFF39FF14) // Completed: Green
                                    workflowStep == step -> primaryColor // Active: Cyan
                                    else -> Color.White.copy(alpha = 0.2f) // Upcoming: Grey
                                }
                                val stepIcon = when (step) {
                                    1 -> "📄"
                                    2 -> "🎹"
                                    3 -> "🎤"
                                    4 -> "🎚️"
                                    5 -> "📀"
                                    else -> "🛡️"
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(stepColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                            .border(BorderStroke(1.dp, stepColor), RoundedCornerShape(16.dp))
                                    ) {
                                        Text(stepIcon, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when(step) {
                                            1 -> "Lyrics"
                                            2 -> "Arrange"
                                            3 -> "Vocal"
                                            4 -> "Mix"
                                            5 -> "Master"
                                            else -> "QA"
                                        },
                                        fontSize = 8.sp,
                                        color = stepColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (step < 6) {
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height(1.dp)
                                            .align(Alignment.CenterVertically)
                                            .background(if (workflowStep > step) Color(0xFF39FF14) else Color.White.copy(alpha = 0.15f))
                                    )
                                }
                            }
                        }

                        // Terminal logs box
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(workflowLogs.reversed()) { log ->
                                    val isHeader = log.startsWith("Initializing") || log.contains("Stage")
                                    val logColor = if (isHeader) primaryColor else Color.White.copy(alpha = 0.8f)
                                    Text(
                                        text = "> $log",
                                        fontSize = 9.5.sp,
                                        color = logColor,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var simTitle by remember { mutableStateOf("") }
                    val simGenre = "Romantic"
                    val simVoice = "Shrija"

                    if (!isWorkflowRunning) {
                        OutlinedTextField(
                            value = simTitle,
                            onValueChange = { simTitle = it },
                            label = { Text("Simulation Track Name (e.g. Sajna Re, Sufi Humsafar)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("sim_track_title_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            val title = if (simTitle.isNotBlank()) simTitle else "Simulated Master #${(100..999).random()}"
                            qaViewModel.startEndToEndSimulation(title, simGenre, simVoice)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !isWorkflowRunning,
                        modifier = Modifier.fillMaxWidth().testTag("run_e2e_simulation_button")
                    ) {
                        if (isWorkflowRunning) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing Pipeline...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Filled.Memory, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Complete 6-Stage E2E Production", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // AIRE v2.0 Live Performance Monitor Card (Milestone 3A.1)
        item {
            val isEngineRunning by qaViewModel.performanceMonitor.isEngineRunning.collectAsState()
            val telemetry by qaViewModel.performanceMonitor.telemetry.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            DisposableEffect(Unit) {
                onDispose {
                    qaViewModel.performanceMonitor.stopMonitoring()
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().testTag("aire_performance_monitor_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Filled.GraphicEq, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "AIRE v2.0 Native Runtime",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                "Real-time Execution Core (C++ / AAudio)",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                        
                        // Start/Stop Switch Button
                        Button(
                            onClick = {
                                if (isEngineRunning) {
                                    qaViewModel.performanceMonitor.stopMonitoring()
                                } else {
                                    qaViewModel.performanceMonitor.startMonitoring(coroutineScope)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEngineRunning) Color(0xFFFF5252) else Color(0xFF39FF14)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(28.dp).testTag("aire_toggle_button")
                        ) {
                            Text(
                                text = if (isEngineRunning) "OFF" else "ON",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isEngineRunning) {
                        Text(
                            text = "Native real-time synthesis engine is offline. Turn on to monitor live raga waveform rendering, latency controls, and dynamic DSP memory layouts.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    } else {
                        // Telemetry Grid
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                TelemetryMetricBox(
                                    label = "CPU Load",
                                    value = "${String.format("%.1f", telemetry?.cpuLoadPercent ?: 0f)}%",
                                    color = if ((telemetry?.cpuLoadPercent ?: 0f) > 85f) Color(0xFFFF5252) else primaryColor,
                                    modifier = Modifier.weight(1f)
                                )
                                TelemetryMetricBox(
                                    label = "Active Voices",
                                    value = "${telemetry?.activeVoices ?: 0} / 32",
                                    color = goldAccent,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                TelemetryMetricBox(
                                    label = "JNI Latency",
                                    value = "${telemetry?.latencyMs ?: 0} ms",
                                    color = Color(0xFF39FF14),
                                    modifier = Modifier.weight(1f)
                                )
                                TelemetryMetricBox(
                                    label = "Render Time",
                                    value = "${telemetry?.renderTimeUs ?: 0} μs",
                                    color = Color(0xFFE040FB),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                TelemetryMetricBox(
                                    label = "Buffer Underruns",
                                    value = "${telemetry?.xruns ?: 0} XRUNs",
                                    color = if ((telemetry?.xruns ?: 0) > 0) Color(0xFFFF5252) else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.weight(1f)
                                )
                                TelemetryMetricBox(
                                    label = "DSP Memory",
                                    value = "${String.format("%.1f", (telemetry?.memoryUsageKb ?: 0).toFloat() / 1024f)} MB",
                                    color = Color(0xFF00E5FF),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Dynamic Interactive controls for Style & Quality
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(6.dp))

                            var selectedStyle by remember { mutableStateOf("Odia_Classical") }
                            var selectedQuality by remember { mutableStateOf("Studio") }
                            var thermalThrottle by remember { mutableStateOf(false) }
                            var batterySaver by remember { mutableStateOf(false) }

                            Text("Render Genre Style Preset:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Odia_Classical", "Bollywood", "Bhajan").forEach { style ->
                                    FilterChip(
                                        selected = selectedStyle == style,
                                        onClick = {
                                            selectedStyle = style
                                            qaViewModel.performanceMonitor.setStyle(style)
                                        },
                                        label = { Text(style.replace("_", " "), fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = primaryColor,
                                            selectedLabelColor = Color.Black
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Project Quality Profile:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Draft", "Studio", "Ultra").forEach { qual ->
                                    FilterChip(
                                        selected = selectedQuality == qual,
                                        onClick = {
                                            selectedQuality = qual
                                            qaViewModel.performanceMonitor.setQuality(qual)
                                        },
                                        label = { Text(qual, fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = goldAccent,
                                            selectedLabelColor = Color.Black
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Checkbox constraints
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = thermalThrottle,
                                        onCheckedChange = {
                                            thermalThrottle = it
                                            qaViewModel.performanceMonitor.updateSchedulerConstraints(thermalThrottle, batterySaver)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF5252))
                                    )
                                    Text("Thermal Throttle", color = Color.White, fontSize = 10.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = batterySaver,
                                        onCheckedChange = {
                                            batterySaver = it
                                            qaViewModel.performanceMonitor.updateSchedulerConstraints(thermalThrottle, batterySaver)
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFF9D142))
                                    )
                                    Text("Battery Saver", color = Color.White, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            var activeInstrument by remember { mutableStateOf("Sitar") }
                            var activePreset by remember { mutableStateOf("Concert_Grand") }

                            LaunchedEffect(activePreset) {
                                qaViewModel.performanceMonitor.applyInstrumentPreset(activePreset)
                            }

                            Text("AIRE Developer Console — Swara Instrument Play", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Raga Instrument:", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            ) {
                                listOf("Sitar", "Santoor", "Bansuri_Flute", "Raga_Violin", "Tabla").forEach { inst ->
                                    FilterChip(
                                        selected = activeInstrument == inst,
                                        onClick = { activeInstrument = inst },
                                        label = { Text(inst.replace("_", " "), fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = primaryColor,
                                            selectedLabelColor = Color.Black
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Character Personality (ADSR):", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                            ) {
                                listOf("Concert_Grand", "Soft_Piano", "Vintage_Upright", "Film_Noir_Steinway", "Bright_Studio_Grand").forEach { preset ->
                                    FilterChip(
                                        selected = activePreset == preset,
                                        onClick = { activePreset = preset },
                                        label = { Text(preset.replace("_", " "), fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = goldAccent,
                                            selectedLabelColor = Color.Black
                                        ),
                                        modifier = Modifier.height(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Interactive Sargam Keypad (Tap and Hold to Bow/Pluck):", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Shuddha Swaras (Sa, Re, Ga, Ma, Pa, Dha, Ni, Sa')
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(
                                        "Sa" to 60,
                                        "Re" to 62,
                                        "Ga" to 64,
                                        "Ma" to 65,
                                        "Pa" to 67,
                                        "Dha" to 69,
                                        "Ni" to 71,
                                        "Sa'" to 72
                                    ).forEach { (swara, pitch) ->
                                        val interactionSource = remember { MutableInteractionSource() }
                                        val isPressed by interactionSource.collectIsPressedAsState()
                                        
                                        LaunchedEffect(isPressed) {
                                            if (isPressed) {
                                                qaViewModel.performanceMonitor.triggerNoteOn(pitch, 110, activeInstrument)
                                            } else {
                                                qaViewModel.performanceMonitor.triggerNoteOff(pitch)
                                            }
                                        }

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(56.dp)
                                                .background(
                                                    color = if (isPressed) Color(0xFF39FF14) else Color(0xFF322A5E),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isPressed) Color(0xFF39FF14) else Color.White.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = androidx.compose.foundation.LocalIndication.current,
                                                    onClick = {}
                                                )
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(swara, color = if (isPressed) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text(pitch.toString(), color = if (isPressed) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f), fontSize = 7.sp)
                                            }
                                        }
                                    }
                                }

                                // Komal and Teevra Swaras (Komal Re, Komal Ga, Teevra Ma, Komal Dha, Komal Ni)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                ) {
                                    listOf(
                                        "re" to 61,
                                        "ga" to 63,
                                        "ma'" to 66,
                                        "dha" to 68,
                                        "ni" to 70
                                    ).forEach { (swara, pitch) ->
                                        val interactionSource = remember { MutableInteractionSource() }
                                        val isPressed by interactionSource.collectIsPressedAsState()
                                        
                                        LaunchedEffect(isPressed) {
                                            if (isPressed) {
                                                qaViewModel.performanceMonitor.triggerNoteOn(pitch, 110, activeInstrument)
                                            } else {
                                                qaViewModel.performanceMonitor.triggerNoteOff(pitch)
                                            }
                                        }

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(48.dp)
                                                .background(
                                                    color = if (isPressed) Color(0xFFFF5252) else Color(0xFF1B1133),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isPressed) Color(0xFFFF5252) else Color.White.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = androidx.compose.foundation.LocalIndication.current,
                                                    onClick = {}
                                                )
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(swara, color = if (isPressed) Color.Black else Color(0xFFD1C4E9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text(pitch.toString(), color = if (isPressed) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.2f), fontSize = 7.sp)
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

        // List of Validated Tracks Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Verification History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (reports.isNotEmpty()) {
                    Text(
                        text = "${reports.size} tracks logged",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (reports.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No Audio Quality Records Yet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Launch the Bulk Validation suite to evaluate tracks\nacross Romantic, Sad, Devotional, and Classical genres.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        } else {
            items(reports) { report ->
                ReportItemCard(
                    report = report,
                    onClick = { onSelectReport(report) },
                    cardBg = cardBg,
                    primaryColor = primaryColor
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E143D)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ReportItemCard(
    report: QAQualityReport,
    onClick: () -> Unit,
    cardBg: Color,
    primaryColor: Color
) {
    val resultColor = when (report.validationResult) {
        "Pass" -> Color(0xFF39FF14)
        "Warning" -> Color(0xFFF9D142)
        else -> Color(0xFFFF5252)
    }

    val df = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateStr = df.format(Date(report.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("report_item_${report.songId}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(report.genre, fontSize = 9.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Voice: ${report.voiceUsed}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                    Text("Lang: ${report.language}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                    Text(dateStr, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                }
            }

            // Quality score indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.1f", report.overallScore)}%",
                    fontSize = 15.sp,
                    color = resultColor,
                    fontWeight = FontWeight.Black
                )
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(resultColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        report.validationResult,
                        fontSize = 8.sp,
                        color = resultColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ReportDetailView(
    report: QAQualityReport,
    defects: List<QADefect>,
    onClose: () -> Unit,
    onCreateDefectFromReport: () -> Unit,
    primaryColor: Color,
    goldAccent: Color,
    cardBg: Color,
    qaViewModel: QAViewModel
) {
    val resultColor = when (report.validationResult) {
        "Pass" -> Color(0xFF39FF14)
        "Warning" -> Color(0xFFF9D142)
        else -> Color(0xFFFF5252)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("report_detail_panel")
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("report_detail_close_button")) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                }
                Text("Report Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onCreateDefectFromReport,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    modifier = Modifier.testTag("file_defect_from_report_button")
                ) {
                    Icon(imageVector = Icons.Filled.BugReport, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File Defect", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(report.title, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Genre: ${report.genre}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("Voice: ${report.voiceUsed}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("Lang: ${report.language}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("OVERALL PRODUCTION SCORE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                            Text("${String.format("%.1f", report.overallScore)}%", fontSize = 32.sp, color = resultColor, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .background(resultColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, resultColor), RoundedCornerShape(6.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                report.validationResult.uppercase(),
                                fontSize = 14.sp,
                                color = resultColor,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // --- Mastering Optimization Suite (Milestone 3.1C) ---
        item {
            val isOptimized = report.overallScore >= 95.0f && report.warnings.firstOrNull()?.contains("Optimal") == true

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, if (isOptimized) Color(0xFF39FF14).copy(alpha = 0.3f) else primaryColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().testTag("mastering_optimization_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOptimized) Icons.Filled.OfflineBolt else Icons.Filled.Autorenew,
                            contentDescription = null,
                            tint = if (isOptimized) Color(0xFF39FF14) else goldAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOptimized) "Optimized ⚡" else "SurMaya Auto-Mastering Optimizer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isOptimized) {
                            "Mastering optimization profile successfully applied! Real-time dynamic EQ side-chaining, brickwall limiting, and sargam harmonic stabilization are actively managing audio stream fidelity."
                        } else {
                            "Detected acoustic abnormalities, dynamic headroom deficits, or loudness clipping. Trigger the automated signal correction engine to apply a professional mastering profile and boost overall quality."
                        },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isOptimized) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF39FF14).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .border(BorderStroke(1.dp, Color(0xFF39FF14).copy(alpha = 0.2f)), RoundedCornerShape(4.dp))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color(0xFF39FF14), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("All streaming compliance targets actively satisfied.", color = Color(0xFF39FF14), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = { qaViewModel.optimizeReportSong(report.songId) },
                            colors = ButtonDefaults.buttonColors(containerColor = goldAccent),
                            modifier = Modifier.fillMaxWidth().testTag("optimize_track_details_button")
                        ) {
                            Icon(imageVector = Icons.Filled.FlashOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply Mastering Optimization Profile", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Scores breakdown
        item {
            Text("Module Verification Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    ScoreBar(label = "Melody Quality", score = report.melodyScore, primaryColor = primaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Pitch Accuracy: ${String.format("%.1f", report.melodyPitchAccuracy)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Note Stability: ${String.format("%.1f", report.melodyNoteStability)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Intonation: ${String.format("%.1f", report.melodyIntonationConsistency)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    val computedRhythmScore = (report.rhythmTimingAccuracy + report.rhythmBeatSync + report.rhythmGrooveConsistency) / 3f
                    ScoreBar(label = "Rhythm & Sync", score = computedRhythmScore, primaryColor = primaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Timing Accuracy: ${String.format("%.1f", report.rhythmTimingAccuracy)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Beat Synchronization: ${String.format("%.1f", report.rhythmBeatSync)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Groove: ${String.format("%.1f", report.rhythmGrooveConsistency)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    ScoreBar(label = "Vocals Synthesis", score = report.vocalScore, primaryColor = primaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Naturalness: ${String.format("%.1f", report.vocalNaturalness)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Pronunciation: ${String.format("%.1f", report.vocalPronunciationClarity)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Breath Gate: ${String.format("%.1f", report.vocalBreathPlacement)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    val computedInstrumentScore = (report.instrumentBalance + report.instrumentStereoImaging + report.instrumentFrequencyMasking + report.instrumentDynamicConsistency) / 4f
                    ScoreBar(label = "Instrument Performance", score = computedInstrumentScore, primaryColor = primaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Balance: ${String.format("%.1f", report.instrumentBalance)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Stereo Image: ${String.format("%.1f", report.instrumentStereoImaging)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Freq Masking: ${String.format("%.1f", report.instrumentFrequencyMasking)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    ScoreBar(label = "Mixing & EQ", score = report.mixingScore, primaryColor = primaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Gain Stage: ${String.format("%.1f", report.mixingGainStaging)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("EQ Balance: ${String.format("%.1f", report.mixingEqBalance)}%", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Headroom: ${String.format("%.1f", report.mixingHeadroom)} dB", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    ScoreBar(label = "Mastering & Loudness", score = report.masteringScore, primaryColor = primaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("LUFS: ${String.format("%.1f", report.masteringLufs)} dB", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("True Peak: ${String.format("%.2f", report.masteringTruePeak)} dB", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                        Text("Dynamic Range: ${String.format("%.1f", report.masteringDynamicRange)} DR", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Streaming standard validation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF150F2B)),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Streaming Profile Status", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        Text(report.masteringStreamingProfile, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Warnings and Suggestions
        item {
            Text("Automated Insights & Suggested Adjustments", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Warnings Detected", fontSize = 11.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    report.warnings.forEach { warn ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(warn, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Suggested Engineering Fixes", fontSize = 11.sp, color = Color(0xFF39FF14), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    report.suggestions.forEach { sug ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(imageVector = Icons.Filled.Lightbulb, contentDescription = null, tint = Color(0xFF39FF14), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sug, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBar(label: String, score: Float, primaryColor: Color) {
    val barColor = when {
        score >= 90f -> Color(0xFF39FF14)
        score >= 80f -> Color(0xFFF9D142)
        else -> Color(0xFFFF5252)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("${String.format("%.1f", score)}%", fontSize = 11.sp, color = barColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            color = barColor,
            trackColor = Color.White.copy(alpha = 0.08f),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun DefectsTrackerView(
    defects: List<QADefect>,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteDefect: (String) -> Unit,
    onTriggerAddDefect: () -> Unit,
    primaryColor: Color,
    cardBg: Color
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Engineering Defect Register",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onTriggerAddDefect,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier.testTag("log_manual_defect_button")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("File Ticket", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (defects.isEmpty()) {
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.BugReport,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No Quality Defects Found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "All active track metrics are within optimal standards.\nAny warnings during runs will automatically compile here.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        } else {
            items(defects) { defect ->
                DefectCard(
                    defect = defect,
                    onUpdateStatus = onUpdateStatus,
                    onDelete = onDeleteDefect,
                    cardBg = cardBg
                )
            }
        }
    }
}

@Composable
fun DefectCard(
    defect: QADefect,
    onUpdateStatus: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    cardBg: Color
) {
    val severityColor = when (defect.severity) {
        "Critical" -> Color(0xFFFF5252)
        "Major" -> Color(0xFFF9D142)
        else -> Color(0xFF00E5FF)
    }

    val statusColor = when (defect.resolutionStatus) {
        "Open" -> Color(0xFFFF5252)
        "In Progress" -> Color(0xFFF9D142)
        else -> Color(0xFF39FF14)
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("defect_card_${defect.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(severityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(defect.severity.uppercase(), fontSize = 8.sp, color = severityColor, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = defect.id,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .clickable {
                            val nextStatus = when (defect.resolutionStatus) {
                                "Open" -> "In Progress"
                                "In Progress" -> "Resolved"
                                else -> "Open"
                            }
                            onUpdateStatus(defect.id, nextStatus)
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        defect.resolutionStatus.uppercase(),
                        fontSize = 9.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = defect.description,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Layer: ${defect.module}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                Text("Time: ${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(defect.timestamp))}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Text("Reproduction Steps:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                Text(defect.reproductionSteps, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(8.dp))

                Text("Expected Result:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                Text(defect.expectedResult, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(8.dp))

                Text("Actual Findings:", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                Text(defect.actualResult, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        onClick = { onDelete(defect.id) },
                        modifier = Modifier.testTag("delete_defect_${defect.id}")
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252).copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun TelemetryMetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.15f)), RoundedCornerShape(6.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
