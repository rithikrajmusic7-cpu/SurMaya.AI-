package com.example.ui.screens.settings
 
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Speed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.AuthViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.GeminiDiagnostics
import com.example.data.remote.ApiTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToApiConfig: () -> Unit = {},
    onNavigateToAudioQuality: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var isPrivateProfileEnabled by remember { mutableStateOf(false) }
    var appVersion by remember { mutableStateOf("1.0.0 (MVP)") }
    var versionTapCount by remember { mutableStateOf(0) }

    val devPrefs = remember { com.example.data.local.DeveloperPrefsManager.getInstance(context) }
    var customGeminiKey by remember { mutableStateOf(devPrefs.customGeminiApiKey) }
    var customSunoKey by remember { mutableStateOf(devPrefs.customSunoApiKey) }
    var customMusicGenKey by remember { mutableStateOf(devPrefs.customMusicGenApiKey) }
    var customApiEndpoint by remember { mutableStateOf(devPrefs.customApiEndpoint) }
    var generationModel by remember { mutableStateOf(devPrefs.generationModel) }
    var generationTemperature by remember { mutableStateOf(devPrefs.generationTemperature) }
    var forceRealApiRequests by remember { mutableStateOf(devPrefs.forceRealApiRequests) }
    var systemInstructionOverride by remember { mutableStateOf(devPrefs.systemInstructionOverride) }

    var showGeminiKey by remember { mutableStateOf(false) }
    var showSunoKey by remember { mutableStateOf(false) }
    var showMusicGenKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Application Settings", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("settings_screen_scaffold")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text("Preferences", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)

            // Notifications switch
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Notifications, contentDescription = "Notify", tint = Color(0xFF9F75FF))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Push Notifications", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Notify when AI rendering completes", color = Color(0xFF9E93B3), fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { isNotificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFF9D142),
                            checkedTrackColor = Color(0xFF9F75FF)
                        )
                    )
                }
            }

            // Privacy settings
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Security, contentDescription = "Secure", tint = Color(0xFF9F75FF))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Private Studio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Only you can view and download songs", color = Color(0xFF9E93B3), fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = isPrivateProfileEnabled,
                        onCheckedChange = { isPrivateProfileEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFF9D142),
                            checkedTrackColor = Color(0xFF9F75FF)
                        )
                    )
                }
            }

            // Developer Access Controls Section
            var showPasswordDialog by remember { mutableStateOf(false) }
            var isExtendAction by remember { mutableStateOf(false) }
            var passwordText by remember { mutableStateOf("") }
            var passwordError by remember { mutableStateOf(false) }
            
            val isDevModeActive by authViewModel.isDevMode.collectAsStateWithLifecycle()
            val devDaysRemaining by authViewModel.devDaysRemaining.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                authViewModel.refreshDevModeState()
            }

            if (isDevModeActive) {
                Text("Developer Access", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp, 
                            if (isDevModeActive) Color(0xFFF9D142).copy(alpha = 0.5f) else Color(0xFF2E244E), 
                            RoundedCornerShape(16.dp)
                        )
                        .testTag("developer_mode_card")
                ) {
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
                                    imageVector = Icons.Filled.DeveloperMode, 
                                    contentDescription = "Dev Mode", 
                                    tint = if (isDevModeActive) Color(0xFFF9D142) else Color(0xFF9E93B3),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Developer Mode", 
                                        color = Color.White, 
                                        fontSize = 14.sp, 
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Grants Premier status for feature validation", 
                                        color = Color(0xFF9E93B3), 
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = isDevModeActive,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        val daysLeft = authViewModel.devDaysRemaining.value
                                        if (daysLeft <= 0L) {
                                            isExtendAction = false
                                            showPasswordDialog = true
                                            passwordText = ""
                                            passwordError = false
                                        } else {
                                            authViewModel.setDevMode(true)
                                            Toast.makeText(context, "Developer Mode Activated: Premier Status Granted", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        authViewModel.setDevMode(false)
                                        Toast.makeText(context, "Developer Mode Deactivated", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFF9D142),
                                    checkedTrackColor = Color(0xFF9F75FF)
                                ),
                                modifier = Modifier.testTag("developer_mode_switch")
                            )
                        }

                        if (isDevModeActive) {
                        Divider(color = Color(0xFF2E244E), thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Validation Period Active",
                                    color = Color(0xFF39FF14),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$devDaysRemaining days remaining of 30-day limit",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = {
                                    isExtendAction = true
                                    showPasswordDialog = true
                                    passwordText = ""
                                    passwordError = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1738)),
                                border = BorderStroke(1.dp, Color(0xFFF9D142)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp).testTag("extend_dev_mode_button")
                            ) {
                                Text("Extend 30 Days", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = {
                                authViewModel.simulateExpiration()
                                Toast.makeText(context, "Simulated 31-day expiration! Dev Mode has expired.", Toast.LENGTH_LONG).show()
                            },
                            border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            modifier = Modifier.fillMaxWidth().testTag("simulate_dev_expiry_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Speed, contentDescription = "Simulate", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate 31-Day Expiration (Testing)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Diagnostics Console Navigate Button
                        Button(
                            onClick = onNavigateToDiagnostics,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.fillMaxWidth().testTag("network_diagnostics_nav_button")
                        ) {
                            Icon(imageVector = Icons.Filled.BugReport, contentDescription = "Diagnostics", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Network Diagnostics Console", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secure API Key Configuration Navigate Button
                        Button(
                            onClick = onNavigateToApiConfig,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39FF14)),
                            modifier = Modifier.fillMaxWidth().testTag("secure_api_credentials_nav_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Security, contentDescription = "Security", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Configure Secure API Keys (Keystore)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Audio Quality Validation Navigation Button
                        Button(
                            onClick = onNavigateToAudioQuality,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                            modifier = Modifier.fillMaxWidth().testTag("audio_quality_validation_nav_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Tune, contentDescription = "Audio Quality", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Audio Quality Validation Dashboard", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFF2E244E), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Developer Keys & Music Engines",
                            color = Color(0xFFF9D142),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Gemini API Key Input
                        OutlinedTextField(
                            value = customGeminiKey,
                            onValueChange = {
                                customGeminiKey = it
                                devPrefs.customGeminiApiKey = it
                            },
                            label = { Text("Custom Gemini API Key", color = Color(0xFF9E93B3), fontSize = 12.sp) },
                            placeholder = { Text("Enter your Gemini API Key", color = Color(0xFF9E93B3).copy(alpha = 0.5f), fontSize = 12.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Key, contentDescription = "Key", tint = Color(0xFFF9D142), modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                                    Icon(
                                        imageVector = if (showGeminiKey) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = Color(0xFF9E93B3),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF2E244E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0C071F),
                                unfocusedContainerColor = Color(0xFF0C071F)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("custom_gemini_key_input")
                        )

                        // Suno AI API Key Input
                        OutlinedTextField(
                            value = customSunoKey,
                            onValueChange = {
                                customSunoKey = it
                                devPrefs.customSunoApiKey = it
                            },
                            label = { Text("Custom Suno AI API Key", color = Color(0xFF9E93B3), fontSize = 12.sp) },
                            placeholder = { Text("Enter your Suno AI Key (Optional)", color = Color(0xFF9E93B3).copy(alpha = 0.5f), fontSize = 12.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Build, contentDescription = "Suno", tint = Color(0xFF9F75FF), modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { showSunoKey = !showSunoKey }) {
                                    Icon(
                                        imageVector = if (showSunoKey) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = Color(0xFF9E93B3),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showSunoKey) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF2E244E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0C071F),
                                unfocusedContainerColor = Color(0xFF0C071F)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("custom_suno_key_input")
                        )

                        // Music Gen API Key Input
                        OutlinedTextField(
                            value = customMusicGenKey,
                            onValueChange = {
                                customMusicGenKey = it
                                devPrefs.customMusicGenApiKey = it
                            },
                            label = { Text("Music Generation API Key", color = Color(0xFF9E93B3), fontSize = 12.sp) },
                            placeholder = { Text("Enter custom Music Gen key", color = Color(0xFF9E93B3).copy(alpha = 0.5f), fontSize = 12.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.BugReport, contentDescription = "MusicGen", tint = Color(0xFF39FF14), modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { showMusicGenKey = !showMusicGenKey }) {
                                    Icon(
                                        imageVector = if (showMusicGenKey) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = "Toggle Visibility",
                                        tint = Color(0xFF9E93B3),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            visualTransformation = if (showMusicGenKey) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF2E244E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0C071F),
                                unfocusedContainerColor = Color(0xFF0C071F)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("custom_music_gen_key_input")
                        )

                        // Custom API Endpoint
                        OutlinedTextField(
                            value = customApiEndpoint,
                            onValueChange = {
                                customApiEndpoint = it
                                devPrefs.customApiEndpoint = it
                            },
                            label = { Text("Custom API Endpoint / Proxy (Optional)", color = Color(0xFF9E93B3), fontSize = 12.sp) },
                            placeholder = { Text("https://generativelanguage.googleapis.com/", color = Color(0xFF9E93B3).copy(alpha = 0.5f), fontSize = 12.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Http, contentDescription = "Endpoint", tint = Color(0xFF9E93B3), modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF2E244E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0C071F),
                                unfocusedContainerColor = Color(0xFF0C071F)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("custom_api_endpoint_input")
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = Color(0xFF2E244E), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Advanced Parameters Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Tune, contentDescription = "Tune", tint = Color(0xFFF9D142), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Advanced Audio Configurations", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Model Selector TextField
                        OutlinedTextField(
                            value = generationModel,
                            onValueChange = {
                                generationModel = it
                                devPrefs.generationModel = it
                            },
                            label = { Text("Selected Gemini Model", color = Color(0xFF9E93B3), fontSize = 12.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Model", tint = Color(0xFF9E93B3), modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF2E244E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0C071F),
                                unfocusedContainerColor = Color(0xFF0C071F)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("generation_model_input")
                        )

                        // Force Real API Requests Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Force Real API Requests", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Disables silent fallbacks. Throws and displays real raw error logs if keys fail.",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = forceRealApiRequests,
                                onCheckedChange = {
                                    forceRealApiRequests = it
                                    devPrefs.forceRealApiRequests = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFF9D142),
                                    checkedTrackColor = Color(0xFF9F75FF)
                                ),
                                modifier = Modifier.testTag("force_real_api_requests_switch")
                            )
                        }

                        // Temperature Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Generation Temperature", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = String.format(Locale.US, "%.2f", generationTemperature),
                                    color = Color(0xFFF9D142),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Slider(
                                value = generationTemperature,
                                onValueChange = {
                                    generationTemperature = it
                                    devPrefs.generationTemperature = it
                                },
                                valueRange = 0.0f..2.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFF9D142),
                                    activeTrackColor = Color(0xFF9F75FF),
                                    inactiveTrackColor = Color(0xFF2E244E)
                                ),
                                modifier = Modifier.testTag("generation_temp_slider")
                            )
                        }

                        // Custom System Instructions override
                        OutlinedTextField(
                            value = systemInstructionOverride,
                            onValueChange = {
                                systemInstructionOverride = it
                                devPrefs.systemInstructionOverride = it
                            },
                            label = { Text("Custom System Instructions Prompt", color = Color(0xFF9E93B3), fontSize = 12.sp) },
                            placeholder = { Text("Tweak style directives sent to the generation endpoint...", color = Color(0xFF9E93B3).copy(alpha = 0.5f), fontSize = 12.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Filled.TextFormat, contentDescription = "Instructions", tint = Color(0xFF9E93B3), modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFF9D142),
                                unfocusedBorderColor = Color(0xFF2E244E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0C071F),
                                unfocusedContainerColor = Color(0xFF0C071F)
                            ),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("custom_instructions_input")
                        )

                        // Reset Configuration Button
                        OutlinedButton(
                            onClick = {
                                customGeminiKey = ""
                                customSunoKey = ""
                                customMusicGenKey = ""
                                customApiEndpoint = ""
                                generationModel = "gemini-2.5-flash-preview-tts"
                                generationTemperature = 1.0f
                                forceRealApiRequests = false
                                systemInstructionOverride = "Generate a high fidelity musical piece or song. Standard Indian music theme, clean production."
                                
                                devPrefs.customGeminiApiKey = ""
                                devPrefs.customSunoApiKey = ""
                                devPrefs.customMusicGenApiKey = ""
                                devPrefs.customApiEndpoint = ""
                                devPrefs.generationModel = "gemini-2.5-flash-preview-tts"
                                devPrefs.generationTemperature = 1.0f
                                devPrefs.forceRealApiRequests = false
                                devPrefs.systemInstructionOverride = "Generate a high fidelity musical piece or song. Standard Indian music theme, clean production."
                                
                                Toast.makeText(context, "Developer configurations reset to defaults!", Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, Color(0xFF9F75FF).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9F75FF)),
                            modifier = Modifier.fillMaxWidth().testTag("reset_developer_configs_button")
                        ) {
                            Text("Reset Developer Configs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val daysLeft = authViewModel.devDaysRemaining.value
                        if (daysLeft <= 0L) {
                            Divider(color = Color(0xFF2E244E), thickness = 1.dp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Validation Period Expired",
                                        color = Color(0xFFFF5252),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Enter password to renew/extend next 30 days.",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 11.sp
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        isExtendAction = false
                                        showPasswordDialog = true
                                        passwordText = ""
                                        passwordError = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("reset_dev_mode_button")
                                ) {
                                    Text("Reset Mode", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            }

            if (showPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { showPasswordDialog = false },
                    title = {
                        Text(
                            text = if (isExtendAction) "Extend Developer Access" else "Reset Developer Mode",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "To validate access for another 30 days, please enter the developer password.",
                                color = Color(0xFF9E93B3),
                                fontSize = 13.sp
                            )
                            
                            OutlinedTextField(
                                value = passwordText,
                                onValueChange = { 
                                    passwordText = it
                                    passwordError = false
                                },
                                label = { Text("Developer Password", color = Color(0xFF9E93B3)) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                isError = passwordError,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF9D142),
                                    unfocusedBorderColor = Color(0xFF2E244E),
                                    focusedLabelColor = Color(0xFFF9D142),
                                    cursorColor = Color(0xFFF9D142),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("dev_password_input")
                            )
                            
                            if (passwordError) {
                                Text(
                                    text = "Incorrect password. Please try again.",
                                    color = Color(0xFFFF5252),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val success = authViewModel.extendDevMode(passwordText)
                                if (success) {
                                    showPasswordDialog = false
                                    Toast.makeText(context, "Developer Mode validated and extended for 30 days!", Toast.LENGTH_LONG).show()
                                } else {
                                    passwordError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.testTag("dialog_submit_button")
                        ) {
                            Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPasswordDialog = false },
                            modifier = Modifier.testTag("dialog_cancel_button")
                        ) {
                            Text("Cancel", color = Color(0xFF9E93B3))
                        }
                    },
                    containerColor = Color(0xFF140D2A),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // AI API Diagnostics Section
            val apiLogs by GeminiDiagnostics.logs.collectAsStateWithLifecycle(initialValue = emptyList())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI API Diagnostics", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (apiLogs.isNotEmpty()) {
                    TextButton(
                        onClick = { GeminiDiagnostics.clearLogs() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252)),
                        modifier = Modifier.testTag("clear_api_logs_button")
                    ) {
                        Text("Clear Logs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                if (apiLogs.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "No Logs",
                            tint = Color(0xFF9E93B3),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No API transactions captured yet.",
                            color = Color(0xFF9E93B3),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Generate a song, a voice preview, or play a track to capture real-time AI network logs.",
                            color = Color(0xFF9E93B3).copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Captured ${apiLogs.size} API calls (showing up to 5 newest):",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        apiLogs.take(5).forEach { log ->
                            var isExpanded by remember { mutableStateOf(false) }
                            val timeStr = remember(log.timestamp) {
                                SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF140D2A)),
                                border = BorderStroke(1.dp, if (log.isSuccess) Color(0xFF39FF14).copy(alpha = 0.3f) else Color(0xFFFF5252).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().testTag("log_item_${log.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "[${log.method}] ${log.url.substringAfter("googleapis.com/")}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "HTTP ${log.responseCode}",
                                            color = if (log.isSuccess) Color(0xFF39FF14) else Color(0xFFFF5252),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Time: $timeStr",
                                            color = Color(0xFF9E93B3),
                                            fontSize = 10.sp
                                        )
                                        TextButton(
                                            onClick = { isExpanded = !isExpanded },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(18.dp)
                                        ) {
                                            Text(
                                                text = if (isExpanded) "Hide Details" else "View Payload",
                                                fontSize = 10.sp,
                                                color = Color(0xFFF9D142)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Text(
                                        text = "Verification: ${log.verificationStatus}",
                                        color = if (log.isSuccess) Color(0xFF39FF14).copy(alpha = 0.9f) else Color(0xFFFF5252),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (isExpanded) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = Color(0xFF2E244E), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        if (!log.requestBody.isNullOrBlank()) {
                                            Text("Request Prompt/Body:", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF0C071F), RoundedCornerShape(4.dp))
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = log.requestBody ?: "",
                                                    color = Color(0xFFE0DBEC),
                                                    fontSize = 9.sp,
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                        
                                        Text("Response Payload Sample:", color = Color(0xFFF9D142), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF0C071F), RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = log.responseBodyString?.take(800) ?: "No payload body available",
                                                color = Color(0xFFE0DBEC),
                                                fontSize = 9.sp,
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Text("Build Specifications", color = Color(0xFFF9D142), fontSize = 13.sp, fontWeight = FontWeight.Bold)

            // Build specifications info card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Info, contentDescription = "Build", tint = Color(0xFFF9D142))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("SurMaya AI Specifications", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Divider(color = Color(0x1AFFFFFF))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("version_row"),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Application Version", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("1.0 (AIRE v2.0)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Platform Type", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("Professional Offline AI Music Studio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Organization", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("Sri Itnaa", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Founder", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("PRADEEP SINGH", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Target Architecture", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("ARM64 Android V10+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Core Synthesizer", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        Text("Indian Raga Procedural 44.1kHz", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
