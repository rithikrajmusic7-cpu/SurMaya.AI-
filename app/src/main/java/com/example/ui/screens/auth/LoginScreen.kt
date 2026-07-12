package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToDeveloperLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var logoTapCount by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF09041A),
                        Color(0xFF140D2A)
                    )
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Brand Logo & Header
            Image(
                painter = painterResource(id = com.example.R.drawable.img_surmaya_icon_1783113555260),
                contentDescription = "SurMaya AI Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, Color(0xFFF9D142), RoundedCornerShape(20.dp))
                    .clickable {
                        logoTapCount++
                        if (logoTapCount >= 7) {
                            logoTapCount = 0
                            onNavigateToDeveloperLogin()
                        }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SurMaya AI",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Text(
                text = "Professional Offline AI Music Studio",
                color = Color(0xFFF9D142),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Glassmorphic Login Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "login_card"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign In",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address", color = Color(0xFF9E93B3)) },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email", tint = Color(0xFF9F75FF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF3E3556),
                            focusedContainerColor = Color(0x33000000),
                            unfocusedContainerColor = Color(0x1F000000)
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password", color = Color(0xFF9E93B3)) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password", tint = Color(0xFF9F75FF)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF3E3556),
                            focusedContainerColor = Color(0x33000000),
                            unfocusedContainerColor = Color(0x1F000000)
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFF9D142),
                                    uncheckedColor = Color(0xFF3E3556)
                                )
                            )
                            Text("Remember Me", color = Color(0xFF9E93B3), fontSize = 12.sp)
                        }

                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFFF9D142),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onNavigateToForgotPassword() }
                                .testTag("forgot_password_link")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign In Button
                    GlowingButton(
                        text = "Sign In",
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                            } else {
                                authViewModel.login(email) { result ->
                                    result.fold(
                                        onSuccess = {
                                            Toast.makeText(context, "Welcome back, ${it.displayName}!", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        },
                                        onFailure = {
                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_button"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Login Button
                    OutlinedButton(
                        onClick = {
                            authViewModel.loginWithGoogle { result ->
                                result.fold(
                                    onSuccess = {
                                        Toast.makeText(context, "Welcome, ${it.displayName}!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    },
                                    onFailure = {
                                        Toast.makeText(context, "Google sign in failed", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF3E3556)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button")
                    ) {
                        Text("Continue with Google", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guest mode
                    Text(
                        text = "Continue as Guest",
                        color = Color(0xFF9F75FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                authViewModel.guestLogin { result ->
                                    result.fold(
                                        onSuccess = {
                                            Toast.makeText(context, "Logged in as guest", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        },
                                        onFailure = {
                                            Toast.makeText(context, "Guest login failed", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                            .padding(8.dp)
                            .testTag("guest_login_button")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account? ", color = Color(0xFF9E93B3), fontSize = 14.sp)
                Text(
                    text = "Sign Up",
                    color = Color(0xFFF9D142),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToRegister() }
                        .testTag("register_link")
                )
            }
        }
    }
}
