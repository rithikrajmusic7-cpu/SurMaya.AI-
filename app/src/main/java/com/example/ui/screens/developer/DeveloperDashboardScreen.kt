package com.example.ui.screens.developer

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.DeveloperSessionManager
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardScreen(
    authViewModel: AuthViewModel,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToApiConfig: () -> Unit,
    onNavigateToAudioQuality: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionManager = remember { DeveloperSessionManager.getInstance(context) }
    var sessionCheckTrigger by remember { mutableStateOf(0) }

    // Session status poll
    LaunchedEffect(sessionCheckTrigger) {
        while (true) {
            delay(5000) // Poll session status every 5s
            if (!sessionManager.isDeveloperAuthenticated()) {
                Toast.makeText(context, "Developer session expired.", Toast.LENGTH_LONG).show()
                onLogout()
                break
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DEVELOPER WORKSPACE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFF9D142)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF09041A),
                    titleContentColor = Color.White
                ),
                modifier = Modifier.testTag("developer_dashboard_appbar")
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF09041A),
                            Color(0xFF0C071F)
                        )
                    )
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Secure Session Health Banner
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "session_health_banner"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = "Security Active",
                                tint = Color(0xFF39FF14),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECURE DEV SESSION ACTIVE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF142F1B), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SSL/AES-256",
                                color = Color(0xFF39FF14),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Divider(color = Color(0x1AFFFFFF), thickness = 0.5.dp)

                    Text(
                        text = "Device Signature ID: ${sessionManager.getDeviceUuid()}",
                        color = Color(0xFF9E93B3),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Auto-timeout active: 15m inactivity threshold. Reading/writing config updates extends the session.",
                        color = Color(0xFF9E93B3),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Text(
                text = "SYSTEM CONTROLS & DIAGNOSTICS",
                color = Color(0xFF9E93B3),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Diagnostics Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Configure Secure API Keys
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToApiConfig() }
                        .testTag("nav_api_config_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130F26)),
                    border = BorderStroke(1.dp, Color(0xFF3E3556))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1A1F3B), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Keys",
                                tint = Color(0xFF39FF14)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Configure API Credentials",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage secure keystore for Gemini & Suno tokens",
                                color = Color(0xFF9E93B3),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "Go",
                            tint = Color(0xFF9E93B3)
                        )
                    }
                }

                // 2. Open Network Diagnostics
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDiagnostics() }
                        .testTag("nav_network_diagnostics_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130F26)),
                    border = BorderStroke(1.dp, Color(0xFF3E3556))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1A1F3B), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WifiFind,
                                contentDescription = "Diagnostics",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Network Diagnostics Console",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Analyze socket connection latency & host responsiveness",
                                color = Color(0xFF9E93B3),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "Go",
                            tint = Color(0xFF9E93B3)
                        )
                    }
                }

                // 3. Audio Quality Validation Dashboard
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAudioQuality() }
                        .testTag("nav_audio_quality_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130F26)),
                    border = BorderStroke(1.dp, Color(0xFF3E3556))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF1A1F3B), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Hearing,
                                contentDescription = "Hearing / Audio",
                                tint = Color(0xFFF9D142)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Audio Quality Validation",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Inspect real-time synthesis waves, rates, and bit depth",
                                color = Color(0xFF9E93B3),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowRight,
                            contentDescription = "Go",
                            tint = Color(0xFF9E93B3)
                        )
                    }
                }
            }

            Text(
                text = "UTILITIES & EXPIRATION TESTING",
                color = Color(0xFF9E93B3),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Utilities Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Validation & Lifetime Controls",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Simulate 31-day expiration limits to verify grace-period handling and automatic termination triggers.",
                        color = Color(0xFF9E93B3),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Button(
                        onClick = {
                            authViewModel.simulateExpiration()
                            Toast.makeText(context, "31-Day Expiration Simulated!", Toast.LENGTH_LONG).show()
                            onLogout() // Instantly logs out as session is expired
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Simulate Expiry",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIMULATE 31-DAY CLIENT EXPIRATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logout & Destroy Session
            Button(
                onClick = {
                    sessionManager.clearSession()
                    authViewModel.refreshDevModeState()
                    Toast.makeText(context, "Developer Session Terminated Safely", Toast.LENGTH_SHORT).show()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A152E)),
                border = BorderStroke(1.5.dp, Color(0xFFFF5252)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("terminate_session_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.LockPerson,
                    contentDescription = "Terminate Key",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("TERMINATE WORKSPACE SESSION", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
