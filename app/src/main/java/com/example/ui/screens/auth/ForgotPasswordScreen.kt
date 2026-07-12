package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }

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
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
                    .clickable { onNavigateBack() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFF9D142)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Sign In", color = Color(0xFF9E93B3), fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Reset Password",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "We will send instructions to your email",
                color = Color(0xFF9E93B3),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Registered Email",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                            .testTag("reset_email_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    GlowingButton(
                        text = "Send Instructions",
                        onClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                            } else {
                                authViewModel.forgotPassword(email) { result ->
                                    result.fold(
                                        onSuccess = {
                                            Toast.makeText(context, "Instructions sent to $email!", Toast.LENGTH_SHORT).show()
                                            onNavigateBack()
                                        },
                                        onFailure = {
                                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "send_reset_button"
                    )
                }
            }
        }
    }
}
