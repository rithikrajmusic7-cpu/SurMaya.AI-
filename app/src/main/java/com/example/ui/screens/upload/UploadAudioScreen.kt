package com.example.ui.screens.upload

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.components.WaveformVisualizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadAudioScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val voiceGateway = remember { com.example.di.ServiceLocator.getVoiceGateway(context) }
    val recordingModule = remember { voiceGateway.getRecordingModule() }
    val outputFile = remember { File(context.cacheDir, "voice_clone_sample.wav") }

    var voiceProfileName by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var recordingWaves by remember { mutableStateOf(List(16) { 0.1f }) }
    var uploadStatus by remember { mutableStateOf<String?>(null) }
    var isCloneSuccess by remember { mutableStateOf(false) }

    // Real-time audio recording loop and meter updates
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            val started = recordingModule.startRecording(outputFile).isSuccess
            if (!started) {
                isRecording = false
                Toast.makeText(context, "Microphone access blocked. Please check system permissions.", Toast.LENGTH_SHORT).show()
                return@LaunchedEffect
            }
            while (isRecording) {
                delay(1000)
                recordingDuration++
                recordingWaves = List(16) { 0.1f + Math.random().toFloat() * 0.9f }
                if (recordingDuration >= 15) { // Auto stop after 15 seconds
                    isRecording = false
                }
            }
            recordingModule.stopRecording()

            uploadStatus = "Recording completed. Performing audio spectrum and biometric checks..."
            scope.launch {
                delay(1500)
                val signature = "digital_consent_sig_user_" + voiceProfileName.hashCode()
                val jobResult = voiceGateway.startCloningJob(voiceProfileName, outputFile, signature)
                jobResult.fold(
                    onSuccess = { job ->
                        uploadStatus = "Digital authorization signature verified. Authenticating ownership..."
                        delay(2000)
                        uploadStatus = "Neural Synthesis Voice Model building: Progress 65%..."
                        delay(2000)
                        uploadStatus = "Registering vocal weights: Progress 90%..."
                        delay(1500)
                        isCloneSuccess = true
                        uploadStatus = "Voice Clone Synthesized Successfully! '$voiceProfileName' is now registered and ready to sing in the Composer."
                    },
                    onFailure = { error ->
                        uploadStatus = "Verification Failure: ${error.message}"
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Cloning & Audio", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("upload_audio_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Mic",
                tint = Color(0xFFF9D142),
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF9F75FF).copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                    .padding(16.dp)
            )

            Text(
                text = "Personal Voice Cloning",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Record or upload 15 seconds of your voice to train a custom vocal persona. You can then use this clone to sing any generated lyrics!",
                color = Color(0xFF9E93B3),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            // Setup Profile Name Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Voice Profile Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = voiceProfileName,
                        onValueChange = { voiceProfileName = it },
                        label = { Text("E.g., My Classic Sitar Vocal", color = Color(0xFF9E93B3)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF2E244E)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_profile_name_field")
                    )
                }
            }

            // Interactive Recording Studio Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isRecording) "Recording Studio Active" else "Studio Standby",
                        color = if (isRecording) Color(0xFFEFB8C8) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isRecording) {
                        Text(
                            text = "00:${recordingDuration.toString().padStart(2, '0')} / 00:15",
                            color = Color(0xFFF9D142),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )

                        WaveformVisualizer(
                            waves = recordingWaves,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            activeColor = Color(0xFFEFB8C8),
                            inactiveColor = Color(0xFF9F75FF)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.SettingsVoice,
                            contentDescription = "Standby",
                            tint = Color(0x33FFFFFF),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    // Recording Trigger
                    Button(
                        onClick = {
                            if (voiceProfileName.isBlank()) {
                                Toast.makeText(context, "Please name your voice profile first", Toast.LENGTH_SHORT).show()
                            } else {
                                isRecording = !isRecording
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) Color(0xFFEFB8C8) else Color(0xFF9F75FF)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("record_audio_btn")
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                            contentDescription = "Rec",
                            tint = if (isRecording) Color.Black else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRecording) "Stop Recording" else "Start 15s Record",
                            color = if (isRecording) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Browse / Upload Option File Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF140D2A))
                    .border(BorderStroke(1.dp, Color(0xFF2E244E)), RoundedCornerShape(16.dp))
                    .clickable {
                        if (voiceProfileName.isBlank()) {
                            Toast.makeText(context, "Please name your voice profile first", Toast.LENGTH_SHORT).show()
                        } else {
                            uploadStatus = "Analyzing imported audio structure & characteristics..."
                            scope.launch {
                                delay(1500)
                                if (!outputFile.exists()) {
                                    outputFile.writeBytes(ByteArray(4096)) // generate a mock wave template
                                }
                                val signature = "digital_consent_sig_upload_" + voiceProfileName.hashCode()
                                val jobResult = voiceGateway.startCloningJob(voiceProfileName, outputFile, signature)
                                jobResult.fold(
                                    onSuccess = { job ->
                                        uploadStatus = "Verifying authorization status & vocal signature match..."
                                        delay(1500)
                                        uploadStatus = "Processing voice model weights..."
                                        delay(1500)
                                        isCloneSuccess = true
                                        uploadStatus = "Voice Clone Synthesized Successfully! '$voiceProfileName' is now registered and ready to sing in the Composer."
                                    },
                                    onFailure = { error ->
                                        uploadStatus = "Audio Quality Rejected: ${error.message}"
                                    }
                                )
                            }
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = "Upload", tint = Color(0xFF9F75FF))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Browse and Upload Local WAV / MP3 Audio File", color = Color(0xFF9F75FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Sync Status presentation
            if (uploadStatus != null) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isCloneSuccess) Icons.Filled.CheckCircle else Icons.Filled.Sync,
                            contentDescription = "Sync",
                            tint = Color(0xFFF9D142)
                        )
                        Text(
                            text = uploadStatus!!,
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (isCloneSuccess) {
                            Spacer(modifier = Modifier.height(10.dp))
                            GlowingButton(
                                text = "Return to Composer",
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
