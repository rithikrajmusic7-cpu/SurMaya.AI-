package com.example.ui.screens.auth

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF09041A), Color(0xFF140D2A))
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
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = "Logo",
                tint = Color(0xFFF9D142),
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF9F75FF).copy(alpha = 0.15f), shape = MaterialTheme.shapes.medium)
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Join SurMaya",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Unleash your Indian AI melodies",
                color = Color(0xFF9E93B3),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "register_card"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name Input
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name", color = Color(0xFF9E93B3)) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Name", tint = Color(0xFF9F75FF)) },
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
                            .testTag("display_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                            .testTag("register_email_input")
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
                            .testTag("register_password_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign Up Button
                    GlowingButton(
                        text = "Sign Up",
                        onClick = {
                            if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                            } else {
                                authViewModel.register(displayName, email) { result ->
                                    result.fold(
                                        onSuccess = {
                                            Toast.makeText(context, "Account created! Welcome, ${it.displayName}", Toast.LENGTH_SHORT).show()
                                            onRegisterSuccess()
                                        },
                                        onFailure = {
                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "register_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", color = Color(0xFF9E93B3), fontSize = 14.sp)
                Text(
                    text = "Sign In",
                    color = Color(0xFFF9D142),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .testTag("login_link")
                )
            }
        }
    }
}
