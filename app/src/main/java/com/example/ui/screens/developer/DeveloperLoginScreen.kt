package com.example.ui.screens.developer

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.DeveloperSessionManager
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionManager = remember { DeveloperSessionManager.getInstance(context) }

    var devIdInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isPhaseOnePassed by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var deviceAuthorized by remember { mutableStateOf(sessionManager.isDeviceAuthorized()) }

    var showDebugOtpHelper by remember { mutableStateOf(false) }
    val currentMinute = remember { System.currentTimeMillis() / 1000 / 60 }
    val generatedOtp = remember(currentMinute) { sessionManager.getOtpForTime(currentMinute) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF02010A),
                        Color(0xFF0C071F),
                        Color(0xFF02010A)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Shield Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFF1E1035), RoundedCornerShape(16.dp))
                    .border(1.5.dp, Color(0xFFF9D142), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Secure Key Icon",
                    tint = Color(0xFFF9D142),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SYSTEM GATEWAY",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "INTERNAL ENGINEERING GROUP PORTAL",
                color = Color(0xFF9E93B3),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Glassmorphic Login Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "secure_developer_login_card"
            ) {
                AnimatedContent(
                    targetState = isPhaseOnePassed,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "LoginPhases"
                ) { phasePassed ->
                    if (!phasePassed) {
                        // PHASE 1: ID & Password Entry
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "PHASE 1: KEY DEPLOYMENT",
                                color = Color(0xFFF9D142),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // Developer ID Field
                            OutlinedTextField(
                                value = devIdInput,
                                onValueChange = { devIdInput = it },
                                label = { Text("Developer ID", color = Color(0xFF9E93B3)) },
                                leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = "ID Icon", tint = Color(0xFF9F75FF)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFF9D142),
                                    unfocusedBorderColor = Color(0xFF3E3556),
                                    focusedContainerColor = Color(0x33000000),
                                    unfocusedContainerColor = Color(0x1F000000)
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("developer_id_field")
                            )

                            // Password Field
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("System Password", color = Color(0xFF9E93B3)) },
                                leadingIcon = { Icon(Icons.Filled.VpnKey, contentDescription = "Password Icon", tint = Color(0xFF9F75FF)) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = Color(0xFF9E93B3)
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFF9D142),
                                    unfocusedBorderColor = Color(0xFF3E3556),
                                    focusedContainerColor = Color(0x33000000),
                                    unfocusedContainerColor = Color(0x1F000000)
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("developer_password_field")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Continue Button
                            GlowingButton(
                                text = "DECRYPT & PROCEED",
                                onClick = {
                                    if (sessionManager.validateCredentials(devIdInput, passwordInput)) {
                                        isPhaseOnePassed = true
                                    } else {
                                        Toast.makeText(context, "Invalid Developer Credentials.", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "decrypt_proceed_button"
                            )

                            OutlinedButton(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("TERMINATE CONNECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // PHASE 2: Device Authorization & 2FA verification
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "PHASE 2: SECURE CHALLENGE",
                                color = Color(0xFF39FF14),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            // 1. Device registry status card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (deviceAuthorized) Color(0xFF142F1B) else Color(0xFF2E1717)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (deviceAuthorized) Icons.Filled.VerifiedUser else Icons.Filled.GppBad,
                                        contentDescription = "Status",
                                        tint = if (deviceAuthorized) Color(0xFF39FF14) else Color(0xFFFF5252)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (deviceAuthorized) "Device Authorized" else "Unknown hardware signature",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "UUID: ${sessionManager.getDeviceUuid().take(16)}...",
                                            color = Color(0xFF9E93B3),
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (!deviceAuthorized) {
                                        Button(
                                            onClick = {
                                                sessionManager.authorizeDevice()
                                                deviceAuthorized = true
                                                Toast.makeText(context, "Device added to trusted registry", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Authorize", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 2. 2FA dynamic OTP input
                            Text(
                                text = "A dynamic 6-digit challenge has been computed based on system clock synchronization. Input authenticator token below:",
                                color = Color(0xFF9E93B3),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { if (it.length <= 6) otpInput = it },
                                label = { Text("Authenticator OTP", color = Color(0xFF9E93B3)) },
                                leadingIcon = { Icon(Icons.Filled.MobileFriendly, contentDescription = "OTP Icon", tint = Color(0xFF00E5FF)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF39FF14),
                                    unfocusedBorderColor = Color(0xFF3E3556),
                                    focusedContainerColor = Color(0x33000000),
                                    unfocusedContainerColor = Color(0x1F000000)
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("developer_otp_field")
                            )

                            // Debug OTP Helper section (for test environment)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Need offline passcode helper?",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = { showDebugOtpHelper = !showDebugOtpHelper },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (showDebugOtpHelper) "Hide" else "Generate",
                                            color = Color(0xFFF9D142),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                AnimatedVisibility(visible = showDebugOtpHelper) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1035)),
                                        border = BorderStroke(1.dp, Color(0xFF9F75FF).copy(alpha = 0.3f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "Dynamic Offline OTP:",
                                                color = Color(0xFF9E93B3),
                                                fontSize = 10.sp
                                            )
                                            Text(
                                                text = generatedOtp,
                                                color = Color(0xFF39FF14),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                letterSpacing = 2.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                            Text(
                                                text = "This simulates receipt of secure device push notification code under current system minute.",
                                                color = Color(0xFF9E93B3),
                                                fontSize = 9.sp,
                                                lineHeight = 12.sp,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Submit & Authenticate Button
                             GlowingButton(
                                text = "VERIFY & LOGIN",
                                onClick = {
                                    if (!deviceAuthorized) {
                                        Toast.makeText(context, "Hardware Authorization Required", Toast.LENGTH_SHORT).show()
                                        return@GlowingButton
                                    }

                                    if (sessionManager.verify2FA(otpInput)) {
                                        sessionManager.createSession()
                                        Toast.makeText(context, "ACCESS GRANTED. System initialized.", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    } else {
                                        Toast.makeText(context, "Invalid Developer Credentials.", Toast.LENGTH_SHORT).show()
                                        // Go back to login screen on failure
                                        isPhaseOnePassed = false
                                        otpInput = ""
                                        onNavigateBack()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "verify_login_button"
                            )

                            // Cancel Button
                            OutlinedButton(
                                onClick = {
                                    isPhaseOnePassed = false
                                    otpInput = ""
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9E93B3)),
                                border = BorderStroke(1.dp, Color(0xFF3E3556)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("BACK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
