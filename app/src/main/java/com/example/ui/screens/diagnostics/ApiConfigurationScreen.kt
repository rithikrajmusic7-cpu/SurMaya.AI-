package com.example.ui.screens.diagnostics

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ApiCredentialManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiConfigurationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val credentialManager = remember { ApiCredentialManager.getInstance(context) }

    // State matching input fields
    var geminiKeyInput by remember { mutableStateOf(credentialManager.geminiApiKey) }
    var sunoKeyInput by remember { mutableStateOf(credentialManager.sunoApiKey) }
    var sunoAudioProcessingKeyInput by remember { mutableStateOf(credentialManager.sunoAudioProcessingApiKey) }
    var musicGenKeyInput by remember { mutableStateOf(credentialManager.musicGenApiKey) }
    var endpointInput by remember { mutableStateOf(credentialManager.apiEndpoint) }

    // Toggle states for visible keys
    var showGemini by remember { mutableStateOf(false) }
    var showSuno by remember { mutableStateOf(false) }
    var showSunoAudioProcessing by remember { mutableStateOf(false) }
    var showMusicGen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Secure API Credentials", 
                        fontWeight = FontWeight.Black, 
                        color = Color.White,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack, 
                        modifier = Modifier.testTag("api_config_back_button")
                    ) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("api_configuration_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Explanation Header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF160F30)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF9F75FF).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Security, 
                            contentDescription = "Shield", 
                            tint = Color(0xFF39FF14),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Hardware-Backed Security", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your API keys are encrypted locally using AES-256-GCM authenticated encryption. " +
                        "The cryptographic master keys are stored safely inside Android's hardware Keystore, " +
                        "preventing key extraction or physical/cold boot memory dumping attacks.",
                        color = Color(0xFF9E93B3),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Text(
                "Configured Credentials", 
                color = Color(0xFFF9D142), 
                fontSize = 13.sp, 
                fontWeight = FontWeight.Bold
            )

            // 1. Gemini Key Input Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Gemini API Key", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = geminiKeyInput,
                    onValueChange = { geminiKeyInput = it },
                    placeholder = { Text("AI Studio Gemini API Key", color = Color(0xFF9E93B3).copy(alpha = 0.4f), fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Key, contentDescription = "Key Icon", tint = Color(0xFFF9D142), modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { showGemini = !showGemini }) {
                            Icon(
                                imageVector = if (showGemini) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = Color(0xFF9E93B3),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (showGemini) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF9D142),
                        unfocusedBorderColor = Color(0xFF2E244E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0C071F),
                        unfocusedContainerColor = Color(0xFF0C071F)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("secure_gemini_key_field")
                )
            }

            // 2. Suno AI Key Input Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Suno AI API Key (Optional)", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = sunoKeyInput,
                    onValueChange = { sunoKeyInput = it },
                    placeholder = { Text("Third-party Suno authorization key", color = Color(0xFF9E93B3).copy(alpha = 0.4f), fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Filled.AudioFile, contentDescription = "Suno Icon", tint = Color(0xFF9F75FF), modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { showSuno = !showSuno }) {
                            Icon(
                                imageVector = if (showSuno) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = Color(0xFF9E93B3),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (showSuno) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF9D142),
                        unfocusedBorderColor = Color(0xFF2E244E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0C071F),
                        unfocusedContainerColor = Color(0xFF0C071F)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("secure_suno_key_field")
                )
            }

            // 2.5 Suno Audio Processing Key Input Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Suno Audio Processing API Key (Optional)", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = sunoAudioProcessingKeyInput,
                    onValueChange = { sunoAudioProcessingKeyInput = it },
                    placeholder = { Text("Third-party Suno audio processing key", color = Color(0xFF9E93B3).copy(alpha = 0.4f), fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Filled.AppSettingsAlt, contentDescription = "Suno Proc Icon", tint = Color(0xFF9F75FF), modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { showSunoAudioProcessing = !showSunoAudioProcessing }) {
                            Icon(
                                imageVector = if (showSunoAudioProcessing) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = Color(0xFF9E93B3),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (showSunoAudioProcessing) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF9D142),
                        unfocusedBorderColor = Color(0xFF2E244E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0C071F),
                        unfocusedContainerColor = Color(0xFF0C071F)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("secure_suno_audio_processing_key_field")
                )
            }

            // 3. MusicGen Studio Key Input Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("MusicGen API Key (Optional)", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = musicGenKeyInput,
                    onValueChange = { musicGenKeyInput = it },
                    placeholder = { Text("Synthesizer back-end authorization key", color = Color(0xFF9E93B3).copy(alpha = 0.4f), fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Filled.MusicNote, contentDescription = "MusicGen Icon", tint = Color(0xFF39FF14), modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { showMusicGen = !showMusicGen }) {
                            Icon(
                                imageVector = if (showMusicGen) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = Color(0xFF9E93B3),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (showMusicGen) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF9D142),
                        unfocusedBorderColor = Color(0xFF2E244E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0C071F),
                        unfocusedContainerColor = Color(0xFF0C071F)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("secure_music_gen_key_field")
                )
            }

            // 4. Custom Endpoint Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Custom Generative Endpoint Target (Optional)", color = Color(0xFF9E93B3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = endpointInput,
                    onValueChange = { endpointInput = it },
                    placeholder = { Text("https://generativelanguage.googleapis.com/", color = Color(0xFF9E93B3).copy(alpha = 0.4f), fontSize = 12.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Filled.SettingsEthernet, contentDescription = "Link Icon", tint = Color(0xFFF9D142), modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF9D142),
                        unfocusedBorderColor = Color(0xFF2E244E),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0C071F),
                        unfocusedContainerColor = Color(0xFF0C071F)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("secure_api_endpoint_field")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Encryption Summary Status Info Card
            val isConfigured = geminiKeyInput.isNotBlank() || sunoKeyInput.isNotBlank() || sunoAudioProcessingKeyInput.isNotBlank() || musicGenKeyInput.isNotBlank() || endpointInput.isNotBlank()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isConfigured) Color(0xFF1B3D14) else Color(0xFF251414)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = if (isConfigured) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        contentDescription = "Configured Status",
                        tint = if (isConfigured) Color(0xFF39FF14) else Color(0xFFFF5252),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isConfigured) "Custom Secure Credentials are Ready & Configured" else "No secure credentials modified in current session.",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Action Row: Save & Reset
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Save Button
                Button(
                    onClick = {
                        credentialManager.geminiApiKey = geminiKeyInput.trim()
                        credentialManager.sunoApiKey = sunoKeyInput.trim()
                        credentialManager.sunoAudioProcessingApiKey = sunoAudioProcessingKeyInput.trim()
                        credentialManager.musicGenApiKey = musicGenKeyInput.trim()
                        credentialManager.apiEndpoint = endpointInput.trim()

                        Toast.makeText(context, "API Credentials securely encrypted and updated!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                    modifier = Modifier.fillMaxWidth().testTag("save_secure_keys_button")
                ) {
                    Icon(imageVector = Icons.Filled.Lock, contentDescription = "Lock Icon", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Apply (AES Encrypted)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Clear / Wipe Button
                OutlinedButton(
                    onClick = {
                        geminiKeyInput = ""
                        sunoKeyInput = ""
                        sunoAudioProcessingKeyInput = ""
                        musicGenKeyInput = ""
                        endpointInput = ""

                        credentialManager.geminiApiKey = ""
                        credentialManager.sunoApiKey = ""
                        credentialManager.sunoAudioProcessingApiKey = ""
                        credentialManager.musicGenApiKey = ""
                        credentialManager.apiEndpoint = ""

                        Toast.makeText(context, "All secure credentials successfully wiped and cleared!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("wipe_secure_keys_button")
                ) {
                    Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = "Trash Icon", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wipe and Clear Secure Credentials", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
