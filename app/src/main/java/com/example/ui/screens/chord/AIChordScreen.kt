package com.example.ui.screens.chord

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.chord.*
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.ChordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChordScreen(
    viewModel: ChordViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val allProjects by viewModel.allProjects.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val progression by viewModel.chordProgression.collectAsState()
    val generationState by viewModel.generationState.collectAsState()
    val activeChordIndex by viewModel.activePlayingChordIndex.collectAsState()
    val errorMsg by viewModel.error.collectAsState()
    val melodyProjects by viewModel.melodyProjects.collectAsState()
    val exportedTextResult by viewModel.exportedText.collectAsState()

    val favVoicing by viewModel.favVoicing.collectAsState()
    val favCadence by viewModel.favCadence.collectAsState()
    val favComplexity by viewModel.favComplexity.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedChordSegment by remember { mutableStateOf<ChordSegment?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormatSelected by remember { mutableStateOf("") }
    var isEditingChordName by remember { mutableStateOf<ChordSegment?>(null) }

    var editFavVoicing by remember { mutableStateOf("SATB Voicing") }
    var editFavCadence by remember { mutableStateOf("Perfect Authentic (V-I)") }
    var editFavComplexity by remember { mutableStateOf("Medium") }
    var targetModulationScale by remember { mutableStateOf("G Major") }
    var modalShiftType by remember { mutableStateOf("Dorian Modal Shift") }
    
    LaunchedEffect(favVoicing, favCadence, favComplexity) {
        editFavVoicing = favVoicing
        editFavCadence = favCadence
        editFavComplexity = favComplexity
    }

    // New Project State Variables
    var newTitle by remember { mutableStateOf("") }
    var newLyrics by remember { mutableStateOf("") }
    var newPrompt by remember { mutableStateOf("") }
    var selectedMelodyId by remember { mutableStateOf<String?>(null) }
    var newGenre by remember { mutableStateOf("Bollywood") }
    var newEmotion by remember { mutableStateOf("Expressive") }
    var newMood by remember { mutableStateOf("Romantic") }
    var newScale by remember { mutableStateOf("C Major") }
    var newRaga by remember { mutableStateOf("Yaman") }
    var newBpm by remember { mutableStateOf(90f) }
    var newComplexity by remember { mutableStateOf("Medium") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI CHORD GENERATOR",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "SurMaya Music OS • Polyphonic Harmony & DAW Studio",
                            fontSize = 9.sp,
                            color = Color(0xFFF9D142),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentProject != null) {
                                viewModel.stopChordPlayback()
                                viewModel.selectProject(currentProject!!.copy(id = "")) // Trigger project list reload back
                                viewModel.selectProject(currentProject!!.copy(currentProgressionJson = null)) // Reset active project visually
                                // To force return to the master project selector, we trigger selectProject with dummy, but view model expects null
                                @Suppress("UNCHECKED_CAST")
                                val dummy: ChordProject? = null
                                viewModel.selectProject(dummy as ChordProject)
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("chord_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (currentProject != null && progression != null) {
                        IconButton(
                            onClick = {
                                viewModel.playChordProgression()
                                Toast.makeText(context, "Playing Synth Chords...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("chord_play_btn")
                        ) {
                            Icon(
                                imageVector = if (activeChordIndex != -1) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = "Play Progression",
                                tint = if (activeChordIndex != -1) Color(0xFFF9D142) else Color.White
                            )
                        }
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("chord_export_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Export Progressions",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D0626),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF09041A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D0626), Color(0xFF050210))
                    )
                )
        ) {
            if (currentProject == null) {
                // --- PROJECT SELECTOR SCREEN ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Your Harmonic Workspaces",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (allProjects.isEmpty()) {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = "No Projects",
                                    tint = Color(0xFF9E93B3),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No projects created yet.",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create a chord workspace to auto-harmonize your melodies or lyrics.",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        allProjects.forEach { proj ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { viewModel.selectProject(proj) }
                                    .testTag("project_item_${proj.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = proj.title,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Genre: ${proj.genre} • Scale: ${proj.scale} • BPM: ${proj.bpm}",
                                            color = Color(0xFFF9D142),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (proj.prompt.isNotEmpty()) {
                                            Text(
                                                text = "Prompt: \"${proj.prompt}\"",
                                                color = Color(0xFF9E93B3),
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteProject(proj.id) },
                                        modifier = Modifier.testTag("delete_proj_${proj.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Delete Project",
                                            tint = Color(0xFFE57373)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    GlowingButton(
                        text = "Create Chord Workspace",
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("create_new_workspace_btn")
                    )
                }
            } else {
                // --- ACTIVE WORKSPACE SCREEN ---
                val activeProj = currentProject!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Title Bar Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0D0626))
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = activeProj.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${activeProj.genre} • ${activeProj.scale} • Raga ${activeProj.raga} • ${activeProj.bpm} BPM",
                                color = Color(0xFFF9D142),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2C1965))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeProj.chordComplexity,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (progression == null) {
                        // Empty Progression Generator Trigger State
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Analyze & Harmonize",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select your generative orchestration parameters to compile a chord sequence.",
                                color = Color(0xFF9E93B3),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            if (generationState != ChordGenerationState.IDLE) {
                                CircularProgressIndicator(
                                    color = Color(0xFFF9D142),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "AI PIPELINE: ${generationState.name}...",
                                    color = Color(0xFFF9D142),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            } else {
                                GlowingButton(
                                    text = "Generate Progressions",
                                    onClick = { viewModel.generateProgression() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("trigger_harmony_gen")
                                )
                            }
                        }
                    } else {
                        // PROGRESSION WORKSPACE TIMELINE & EDITING DECK
                        val prog = progression!!
                        
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Chord Timeline Flow",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Chord Progression Horizontal Scroll List
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                prog.chords.forEachIndexed { i, chord ->
                                    val isPlaying = i == activeChordIndex
                                    val isSelected = selectedChordSegment?.id == chord.id
                                    
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isPlaying -> Color(0xFFF9D142)
                                                isSelected -> Color(0xFF381E8C)
                                                else -> Color(0xFF161033)
                                            }
                                        ),
                                        modifier = Modifier
                                            .width(130.dp)
                                            .clickable { selectedChordSegment = chord }
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = Color(0xFFF9D142),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .testTag("chord_segment_card_$i")
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = chord.chordName,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (isPlaying) Color(0xFF09041A) else Color.White
                                            )
                                            Text(
                                                text = "Roman: ${chord.romanNumeral}",
                                                fontSize = 11.sp,
                                                color = if (isPlaying) Color(0xFF140D2A) else Color(0xFF9E93B3),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${chord.durationBeats} Beats",
                                                    fontSize = 10.sp,
                                                    color = if (isPlaying) Color(0xFF140D2A) else Color(0xFFF9D142),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isPlaying) Color(0xFF09041A) else Color(
                                                                0xFF2C1965
                                                            )
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = chord.functionType,
                                                        fontSize = 8.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // DYNAMIC DAW ACTION DECK
                            Spacer(modifier = Modifier.height(16.dp))
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "DAW Progression Editor Tools",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    // Row 1: Global Actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.transposeProgression(1); Toast.makeText(context, "Transposed +1 Semitone", Toast.LENGTH_SHORT).show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23184A)),
                                            modifier = Modifier.weight(1f).testTag("transpose_up_btn")
                                        ) {
                                            Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Trans +1", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.transposeProgression(-1); Toast.makeText(context, "Transposed -1 Semitone", Toast.LENGTH_SHORT).show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23184A)),
                                            modifier = Modifier.weight(1f).testTag("transpose_down_btn")
                                        ) {
                                            Icon(imageVector = Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Trans -1", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.quantizeProgression(); Toast.makeText(context, "Chords snapped to half-beat grid", Toast.LENGTH_SHORT).show() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23184A)),
                                            modifier = Modifier.weight(1f).testTag("quantize_btn")
                                        ) {
                                            Icon(imageVector = Icons.Filled.Grid4x4, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Quantize", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Row 2: Selected Chord Segment Specific Actions
                                    if (selectedChordSegment != null) {
                                        val seg = selectedChordSegment!!
                                        Text(
                                            text = "Selected Segment: ${seg.chordName} (${seg.romanNumeral})",
                                            color = Color(0xFFF9D142),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { isEditingChordName = seg },
                                                modifier = Modifier
                                                    .background(Color(0xFF381E8C), CircleShape)
                                                    .testTag("edit_chord_btn")
                                            ) {
                                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Chord", tint = Color.White)
                                            }

                                            IconButton(
                                                onClick = { viewModel.duplicateSegment(seg.id); Toast.makeText(context, "Chord duplicated", Toast.LENGTH_SHORT).show() },
                                                modifier = Modifier
                                                    .background(Color(0xFF381E8C), CircleShape)
                                                    .testTag("duplicate_chord_btn")
                                            ) {
                                                Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Duplicate Chord", tint = Color.White)
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteSegment(seg.id); selectedChordSegment = null; Toast.makeText(context, "Chord deleted", Toast.LENGTH_SHORT).show() },
                                                modifier = Modifier
                                                    .background(Color(0xFFE57373), CircleShape)
                                                    .testTag("delete_chord_btn")
                                            ) {
                                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete Chord", tint = Color.White)
                                            }
                                        }
                                        // Advanced 4-Part SATB Voice Leading Analysis
                                        if (seg.voiceLeadingNotes.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "SATB Four-Part Voice Leading",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                seg.voiceLeadingNotes.forEach { voice ->
                                                    val parts = voice.split(": ")
                                                    val label = parts.firstOrNull() ?: ""
                                                    val note = parts.getOrNull(1) ?: ""
                                                    Card(
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1440)),
                                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(8.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Text(text = label, color = Color(0xFFF9D142), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                            Text(text = note, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Recommended Bass Movement: [${seg.bassMovementNote}] • ${seg.chordFunction}",
                                                color = Color(0xFF9E93B3),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Select a chord from the timeline above to edit, duplicate, or transpose it individually.",
                                            color = Color(0xFF9E93B3),
                                            fontSize = 11.sp,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.addSegment("C"); Toast.makeText(context, "Appended C Major Chord", Toast.LENGTH_SHORT).show() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E8C)),
                                        modifier = Modifier.fillMaxWidth().testTag("add_chord_btn")
                                    ) {
                                        Icon(imageVector = Icons.Filled.AddCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Append Chord Segment", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // INTERACTIVE PIANO KEYBOARD PREVIEW
                            val activeChordNotes = selectedChordSegment?.pianoKeys ?: emptyList()
                            val activeChordName = selectedChordSegment?.chordName ?: ""
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Piano Keyboard Voicing${if (activeChordName.isNotEmpty()) " - $activeChordName" else ""}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // 2-Octave Piano Roll Rendering Canvas
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF23184A), RoundedCornerShape(8.dp))
                                    .testTag("piano_keyboard_roll")
                            ) {
                                val whiteKeyCount = 14
                                val keyWidth = size.width / whiteKeyCount
                                val keyHeight = size.height
                                val blackKeyHeight = keyHeight * 0.6f
                                val blackKeyWidth = keyWidth * 0.6f

                                // White key midi mappings (2 octaves starting from C4 (MIDI 60))
                                val whiteKeyMidiIndices = listOf(0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23)

                                // Draw White Keys
                                for (i in 0 until whiteKeyCount) {
                                    val midiVal = whiteKeyMidiIndices[i]
                                    val isActive = activeChordNotes.contains(midiVal)
                                    
                                    drawRect(
                                        color = if (isActive) Color(0xFFF9D142) else Color.White,
                                        topLeft = Offset(i * keyWidth, 0f),
                                        size = Size(keyWidth - 1f, keyHeight)
                                    )
                                }

                                // Black key offsets and midi mappings
                                val blackKeyOffsets = listOf(1, 3, 6, 8, 10, 13, 15, 18, 20, 22)
                                val blackKeyXMultipliers = listOf(0.7f, 1.7f, 3.7f, 4.7f, 5.7f, 7.7f, 8.7f, 10.7f, 11.7f, 12.7f)

                                // Draw Black Keys
                                for (i in blackKeyOffsets.indices) {
                                    val midiVal = blackKeyOffsets[i]
                                    val isActive = activeChordNotes.contains(midiVal)
                                    val xOffset = blackKeyXMultipliers[i] * keyWidth
                                    
                                    drawRect(
                                        color = if (isActive) Color(0xFF381E8C) else Color.Black,
                                        topLeft = Offset(xOffset, 0f),
                                        size = Size(blackKeyWidth, blackKeyHeight)
                                    )
                                }
                            }

                            // GUITAR FRETBOARD & FINGER TAB PREVIEW
                            if (selectedChordSegment != null && selectedChordSegment!!.guitarFingering.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(20.dp))
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = Color(0xFFF9D142),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = "Guitar Fretboard Fingering",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Chord fingering chart layout representation: ${selectedChordSegment!!.guitarFingering}",
                                                color = Color(0xFF9E93B3),
                                                fontSize = 10.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Notes voicing pitches: ${selectedChordSegment!!.noteNames.joinToString(", ")}",
                                                color = Color(0xFFF9D142),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // MUSIC THEORY EVALUATION DASHBOARD
                            val eval = prog.evaluation
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Harmony Validation Dashboard",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Total Average Score Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Professional Harmony Score",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Music theory metric evaluations",
                                                color = Color(0xFF9E93B3),
                                                fontSize = 10.sp
                                            )
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF381E8C))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${(eval.averageScore * 100).toInt()}%",
                                                color = Color(0xFFF9D142),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Evaluation parameters
                                    val metrics = listOf(
                                        "Harmony Quality" to eval.harmonyQualityScore,
                                        "Melody Compatibility" to eval.melodyCompatibilityScore,
                                        "Voice Leading Smoothness" to eval.voiceLeadingScore,
                                        "Cadence Strength" to eval.cadenceStrengthScore,
                                        "Genre & Emotion Alignment" to eval.genreMatchScore
                                    )

                                    metrics.forEach { (label, value) ->
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = label, color = Color.White, fontSize = 11.sp)
                                                Text(text = "${(value * 100).toInt()}%", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { value },
                                                color = Color(0xFFF9D142),
                                                trackColor = Color(0xFF23184A),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFF23184A))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Explain Why & Recommendations Panel
                                    Text(
                                        text = "AI Explain Why & Recommendations",
                                        color = Color(0xFFF9D142),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Text(
                                        text = prog.explanationInsight,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        style = androidx.compose.ui.text.TextStyle(lineHeight = 16.sp)
                                    )

                                    if (eval.recommendations.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        eval.recommendations.forEach { tip ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                                Text(text = "• ", color = Color(0xFFF9D142), fontSize = 11.sp)
                                                Text(text = tip, color = Color(0xFF9E93B3), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. ARRANGEMENT AWARENESS METADATA
                            prog.harmonyProfile.arrangementMetadata?.let { arrMeta ->
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Arrangement Engine Metadata",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "DOWNSTREAM DAW INTEGRATION METADATA",
                                            color = Color(0xFFF9D142),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            Text(text = "Bass Movement: ", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
                                            Text(text = arrMeta.recommendedBassMovement, color = Color.White, fontSize = 11.sp)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            Text(text = "Rhythmic Pattern: ", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
                                            Text(text = arrMeta.suggestedRhythmicPattern, color = Color.White, fontSize = 11.sp)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            Text(text = "Instrument Emphasis: ", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
                                            Text(text = arrMeta.instrumentEmphasis, color = Color.White, fontSize = 11.sp)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            Text(text = "Dynamic Intensity: ", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(130.dp))
                                            Text(text = arrMeta.dynamicIntensity, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // 2. REHARMONIZATION ENGINE
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Reharmonization Engine (v1.0.0)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "SELECT REHARMONIZATION PRESET STYLE",
                                        color = Color(0xFFF9D142),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    val reharmStyles = listOf("Simple", "Pop", "Jazz", "Cinematic", "Gospel", "Neo Soul", "Bollywood", "Classical")
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        reharmStyles.forEach { style ->
                                            val isCurrent = prog.reharmonizationStyle.equals(style, ignoreCase = true)
                                            FilterChip(
                                                selected = isCurrent,
                                                onClick = {
                                                    viewModel.reharmonize(style)
                                                    Toast.makeText(context, "Reharmonizing into $style style...", Toast.LENGTH_SHORT).show()
                                                },
                                                label = { Text(style, color = if (isCurrent) Color(0xFF0D0626) else Color.White) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFF9D142),
                                                    containerColor = Color(0xFF161033)
                                                ),
                                                modifier = Modifier.testTag("reharm_chip_$style")
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. INTELLIGENT MODULATION MANAGER
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Intelligent Modulation Manager",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "TRANSITION SCALE & COMPREHEND PIVOT CHORDS",
                                        color = Color(0xFFF9D142),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = targetModulationScale,
                                            onValueChange = { targetModulationScale = it },
                                            label = { Text("Target Scale", color = Color(0xFF9E93B3)) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = Color(0xFFF9D142),
                                                unfocusedBorderColor = Color(0xFF23184A)
                                            ),
                                            modifier = Modifier.weight(1f).testTag("modulation_scale_input")
                                        )
                                        
                                        OutlinedTextField(
                                            value = modalShiftType,
                                            onValueChange = { modalShiftType = it },
                                            label = { Text("Shift Type", color = Color(0xFF9E93B3)) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = Color(0xFFF9D142),
                                                unfocusedBorderColor = Color(0xFF23184A)
                                            ),
                                            modifier = Modifier.weight(1f).testTag("modulation_shift_input")
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    prog.modulationInfo?.let { mod ->
                                        if (mod.targetScale.isNotEmpty()) {
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161033)),
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(text = "Active Modulation Target: ${mod.targetScale}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text(text = "Resolved Pivot Chords: ${mod.pivotChords.joinToString(", ")}", color = Color(0xFFF9D142), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                    Text(text = "Modal Shift: ${mod.modalShiftType}", color = Color(0xFF9E93B3), fontSize = 10.sp)
                                                    if (mod.ragaTransitionPath != "None") {
                                                        Text(text = "Raga Transition: ${mod.ragaTransitionPath}", color = Color(0xFFF57C00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    Button(
                                        onClick = {
                                            viewModel.applyModulation(targetModulationScale, modalShiftType)
                                            Toast.makeText(context, "Modulating project to $targetModulationScale...", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381E8C)),
                                        modifier = Modifier.fillMaxWidth().testTag("modulate_action_btn")
                                    ) {
                                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Compile Intelligent Modulation", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 4. COMPOSER AI PROFILE MEMORY
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Composer AI Profile Memory",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "SAVE YOUR PREFERRED VOICE LEADINGS & COMPLEXITY TO AI MEMORY",
                                        color = Color(0xFFF9D142),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    OutlinedTextField(
                                        value = editFavVoicing,
                                        onValueChange = { editFavVoicing = it },
                                        label = { Text("Preferred Voicing Type (e.g. SATB, Drop-2, Jazz)", color = Color(0xFF9E93B3)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF23184A)
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("composer_voicing_input")
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedTextField(
                                        value = editFavCadence,
                                        onValueChange = { editFavCadence = it },
                                        label = { Text("Preferred Cadence Resolution (e.g. Authentic, Plagal)", color = Color(0xFF9E93B3)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF23184A)
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("composer_cadence_input")
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    OutlinedTextField(
                                        value = editFavComplexity,
                                        onValueChange = { editFavComplexity = it },
                                        label = { Text("Preferred Complexity Style", color = Color(0xFF9E93B3)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFFF9D142),
                                            unfocusedBorderColor = Color(0xFF23184A)
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("composer_complexity_input")
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Button(
                                        onClick = {
                                            viewModel.saveComposerMemory(editFavVoicing, editFavCadence, editFavComplexity)
                                            Toast.makeText(context, "Committed preference patterns to AI Composer Memory!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                                        modifier = Modifier.fillMaxWidth().testTag("save_composer_memory_btn")
                                    ) {
                                        Icon(imageVector = Icons.Filled.Save, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Commit Preferences to AI Memory", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // 5. HARMONIC AI TOKEN ENGINE PIPELINE
                            if (prog.tokenEngineStages.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Harmonic AI Token Engine Pipeline",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "TOKEN GENERATION LAYER FLOW (v1.1)",
                                            color = Color(0xFFF9D142),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        
                                        prog.tokenEngineStages.forEach { stage ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Done",
                                                    tint = Color(0xFF00C853),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = stage,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }
    }

    // --- CREATE DIALOG ---
    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("create_proj_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "New Chord Workspace",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Workspace Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF9E93B3),
                            focusedLabelColor = Color(0xFFF9D142),
                            unfocusedLabelColor = Color(0xFF9E93B3),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("proj_title_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Melody Selection Hook (Integrates directly with melody generator)
                    Text(
                        text = "Harmonize Existing Melody",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    var melodyDropdownExpanded by remember { mutableStateOf(false) }
                    val currentMelodyLabel = melodyProjects.find { it.id == selectedMelodyId }?.title ?: "Select Melody (Optional)"
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { melodyDropdownExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161033)),
                            modifier = Modifier.fillMaxWidth().testTag("melody_select_dropdown")
                        ) {
                            Text(text = currentMelodyLabel, color = Color.White, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = melodyDropdownExpanded,
                            onDismissRequest = { melodyDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Build from Scratch)") },
                                onClick = { selectedMelodyId = null; melodyDropdownExpanded = false }
                            )
                            melodyProjects.forEach { mel ->
                                DropdownMenuItem(
                                    text = { Text(mel.title) },
                                    onClick = { 
                                        selectedMelodyId = mel.id
                                        newTitle = "Harmony of ${mel.title}"
                                        newLyrics = mel.lyrics
                                        newGenre = mel.genre
                                        newEmotion = mel.emotion
                                        newMood = mel.mood
                                        newScale = mel.scale
                                        newRaga = mel.raga
                                        newBpm = mel.tempo.toFloat()
                                        melodyDropdownExpanded = false 
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPrompt,
                        onValueChange = { newPrompt = it },
                        label = { Text("Harmony Instructions Prompt") },
                        placeholder = { Text("e.g. Extended minor jazz feel, simple Yaman backing") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("proj_prompt_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newLyrics,
                        onValueChange = { newLyrics = it },
                        label = { Text("Song Lyrics Reference (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().height(80.dp).testTag("proj_lyrics_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Key details slider & dropdowns
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newGenre,
                            onValueChange = { newGenre = it },
                            label = { Text("Genre") },
                            modifier = Modifier.weight(1f).testTag("proj_genre_input")
                        )
                        OutlinedTextField(
                            value = newScale,
                            onValueChange = { newScale = it },
                            label = { Text("Scale") },
                            modifier = Modifier.weight(1f).testTag("proj_scale_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newRaga,
                            onValueChange = { newRaga = it },
                            label = { Text("Raga") },
                            modifier = Modifier.weight(1f).testTag("proj_raga_input")
                        )
                        OutlinedTextField(
                            value = newEmotion,
                            onValueChange = { newEmotion = it },
                            label = { Text("Emotion") },
                            modifier = Modifier.weight(1f).testTag("proj_emotion_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BPM Slider
                    Text(text = "Tempo (BPM): ${newBpm.toInt()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = newBpm,
                        onValueChange = { newBpm = it },
                        valueRange = 40f..220f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFF9D142),
                            activeTrackColor = Color(0xFFF9D142)
                        ),
                        modifier = Modifier.testTag("bpm_slider")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Complexity Choice
                    Text(text = "Chord Harmonization Complexity", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { choice ->
                            val isSelected = choice == newComplexity
                            Button(
                                onClick = { newComplexity = choice },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF381E8C) else Color(0xFF161033)
                                ),
                                modifier = Modifier.weight(1f).testTag("complexity_btn_$choice")
                            ) {
                                Text(text = choice, color = if (isSelected) Color(0xFFF9D142) else Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCreateDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    viewModel.createProject(
                                        title = newTitle,
                                        melodyProjectId = selectedMelodyId,
                                        lyrics = newLyrics,
                                        prompt = newPrompt,
                                        genre = newGenre,
                                        emotion = newEmotion,
                                        mood = newMood,
                                        scale = newScale,
                                        raga = newRaga,
                                        bpm = newBpm.toInt(),
                                        chordComplexity = newComplexity
                                    )
                                    showCreateDialog = false
                                } else {
                                    Toast.makeText(context, "Please write a workspace title", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.weight(1f).testTag("save_proj_btn")
                        ) {
                            Text("Create", color = Color(0xFF09041A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- EXPORT DIALOG ---
    if (showExportDialog && progression != null) {
        val prog = progression!!
        Dialog(onDismissRequest = { showExportDialog = false }) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("export_dialog_box")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Export Harmony Progression",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    listOf("MIDI", "MUSICXML", "JSON", "CHORD_CHARTS").forEach { format ->
                        Button(
                            onClick = {
                                viewModel.exportProgression(prog, format)
                                exportFormatSelected = format
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (exportFormatSelected == format) Color(0xFF381E8C) else Color(0xFF161033)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("export_format_$format")
                        ) {
                            Text(
                                text = "Export to $format",
                                color = if (exportFormatSelected == format) Color(0xFFF9D142) else Color.White
                            )
                        }
                    }

                    if (exportedTextResult.isNotEmpty() && !exportedTextResult.startsWith("Error:")) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Export Output Compiled successfully. Copy to use in professional DAWs.",
                            color = Color(0xFFF9D142),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(exportedTextResult))
                                Toast.makeText(context, "Copied $exportFormatSelected to Clipboard", Toast.LENGTH_SHORT).show()
                                showExportDialog = false
                                viewModel.clearExport()
                                exportFormatSelected = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.fillMaxWidth().testTag("copy_export_btn")
                        ) {
                            Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, tint = Color(0xFF09041A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy to Clipboard", color = Color(0xFF09041A), fontWeight = FontWeight.Bold)
                        }
                    } else if (exportedTextResult.startsWith("Error:")) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = exportedTextResult,
                            color = Color(0xFFE57373),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { 
                            showExportDialog = false
                            viewModel.clearExport()
                            exportFormatSelected = "" 
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // --- INDIVIDUAL CHORD SEGMENT EDIT DIALOG ---
    if (isEditingChordName != null) {
        val editingSeg = isEditingChordName!!
        var tempChordName by remember { mutableStateOf(editingSeg.chordName) }
        
        Dialog(onDismissRequest = { isEditingChordName = null }) {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("chord_edit_dialog_box")) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Reharmonize Chord Segment",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempChordName,
                        onValueChange = { tempChordName = it },
                        label = { Text("Chord Name (e.g. C, Am7, G7, Fmaj7)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("chord_name_edit_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditingChordName = null },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (tempChordName.isNotBlank()) {
                                    viewModel.updateSegmentChord(editingSeg.id, tempChordName)
                                    isEditingChordName = null
                                    selectedChordSegment = null
                                    Toast.makeText(context, "Chord updated", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.weight(1f).testTag("save_edited_chord_btn")
                        ) {
                            Text("Update", color = Color(0xFF09041A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
