package com.example.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Song
import com.example.domain.model.Project
import com.example.domain.model.User
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingButton
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    musicViewModel: MusicViewModel,
    onNavigateToCreateSong: (String?) -> Unit, // Optional genre/style
    onNavigateToLyrics: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSinger: () -> Unit,
    onNavigateToInstrument: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    onNavigateToComposer: () -> Unit = {},
    onNavigateToMelodyGenerator: () -> Unit = {},
    onNavigateToChordGenerator: () -> Unit = {},
    onNavigateToArrangement: () -> Unit = {},
    onNavigateToMixing: () -> Unit = {},
    onNavigateToMastering: () -> Unit = {},
    onNavigateToProfessionalStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val allSongs by musicViewModel.allSongs.collectAsState()
    val projects by musicViewModel.projects.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val currentPlayingSong by musicViewModel.currentPlayingSong.collectAsState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFolderDesc by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    val trendingStyles = listOf(
        "Bollywood" to "ic_bollywood",
        "Indian Pop" to "ic_pop",
        "Odia" to "ic_odia",
        "Classical" to "ic_classical",
        "Bhajan" to "ic_bhajan",
        "Folk" to "ic_folk",
        "Romantic" to "ic_romantic"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("surmaya_ai_tab_header")
                        ) {
                            Image(
                                painter = painterResource(id = com.example.R.drawable.img_surmaya_icon_1783113555260),
                                contentDescription = "SurMaya Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "SurMaya AI",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = "Dropdown Menu",
                                        tint = Color(0xFFF9D142),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Professional Offline AI Music Studio",
                                    fontSize = 10.sp,
                                    color = Color(0xFFF9D142),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(Color(0xFF140D2A))
                                .border(1.dp, Color(0xFF2E244E), RoundedCornerShape(8.dp))
                        ) {
                            val userEmail = currentUser?.email ?: ""
                            val isGuest = userEmail == "guest_surmaya@aistudio.com" || userEmail.startsWith("guest_")

                            // User Info Header
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = currentUser?.displayName ?: "SurMaya Creator",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isGuest) "Guest Account" else userEmail,
                                            color = Color(0xFF9E93B3),
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )

                            HorizontalDivider(color = Color(0xFF2E244E), thickness = 1.dp)

                            // Settings
                            DropdownMenuItem(
                                text = { Text("Settings", color = Color.White, fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Settings,
                                        contentDescription = "Settings",
                                        tint = Color(0xFF9F75FF)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                },
                                modifier = Modifier.testTag("menu_settings_button")
                            )

                            // Login / Switch Account
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (isGuest) "Login / Sign In" else "Switch Account",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Login,
                                        contentDescription = "Login",
                                        tint = Color(0xFFF9D142)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    authViewModel.logout {
                                        onLogoutSuccess()
                                    }
                                },
                                modifier = Modifier.testTag("menu_login_button")
                            )

                            // Logout
                            DropdownMenuItem(
                                text = { Text("Logout", color = Color(0xFFFF5252), fontSize = 14.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ExitToApp,
                                        contentDescription = "Logout",
                                        tint = Color(0xFFFF5252)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    authViewModel.logout {
                                        onLogoutSuccess()
                                    }
                                },
                                modifier = Modifier.testTag("menu_logout_button")
                            )
                        }
                    }
                },
                actions = {
                    // Profile Icon with Credits Counter
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF140D2A))
                            .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .clickable { onNavigateToProfile() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Stars,
                                contentDescription = "Credits",
                                tint = Color(0xFFF9D142),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentUser?.creditsRemaining ?: 0} Credits",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF09041A))
            )
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.testTag("home_screen_scaffold")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Welcome Card (Visual Asset Decoration)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF130932))
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                ) {
                    // Background Image Banner
                    Image(
                        painter = painterResource(id = com.example.R.drawable.img_surmaya_logo_1783113574193),
                        contentDescription = "SurMaya AI Banner Background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val greeting = "Namaste, ${currentUser?.displayName ?: "Creator"}"
                            Text(
                                text = greeting,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Create high-fidelity Indian songs in seconds using Gemini & procedural audio synthesizers.",
                                color = Color(0xFFE5DFFF),
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlowingButton(
                                text = "Create New Song",
                                onClick = { onNavigateToCreateSong(null) },
                                height = 36.dp,
                                testTag = "create_song_btn"
                            )
                            OutlinedButton(
                                onClick = onNavigateToLyrics,
                                border = BorderStroke(1.dp, Color(0xFF9F75FF)),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Generate Lyrics", color = Color(0xFF9F75FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Credits Status & Subscription Card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val planName = currentUser?.subscriptionPlan?.replaceFirstChar { it.uppercase() } ?: "Free"
                            Text(
                                text = "$planName Account Plan",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (planName == "Free") "50 daily renewal credits" else "Monthly priority premium queue",
                                color = Color(0xFF9E93B3),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = onNavigateToProfile,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F75FF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Upgrade", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // AI Singer Studio Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF211648), Color(0xFF140D2A))
                            )
                        )
                        .border(1.dp, Color(0xFF9F75FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToSinger() }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF9D142).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RecordVoiceOver,
                                    contentDescription = "AI Singer Studio",
                                    tint = Color(0xFFF9D142),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Singer Studio",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Synthesize expressive virtual Indian vocals & stems",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Studio",
                            tint = Color(0xFFF9D142)
                        )
                    }
                }
            }

            // AI Mixing Studio (AMIE) Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E0B36), Color(0xFF0F071D))
                            )
                        )
                        .border(1.dp, Color(0xFFBA68C8).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToMixing() }
                        .padding(16.dp)
                        .testTag("home_enter_mixer_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFBA68C8).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SettingsInputComponent,
                                    contentDescription = "AI Mixing Studio",
                                    tint = Color(0xFFBA68C8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Mixing Studio (AMIE)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Perform automatic fader balancing, EQ, & streaming diagnostics",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Studio",
                            tint = Color(0xFFBA68C8)
                        )
                    }
                }
            }

            // AI Mastering Studio (AIME) Gold Luxury Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF262114), Color(0xFF0F0D08))
                            )
                        )
                        .border(1.dp, Color(0xFFE5C158).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToMastering() }
                        .padding(16.dp)
                        .testTag("home_enter_mastering_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5C158).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Album,
                                    contentDescription = "AI Mastering Studio",
                                    tint = Color(0xFFE5C158),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Mastering Studio (AIME)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Engage release intelligence, loudness normalization & global metadata",
                                    color = Color(0xFFE5C158).copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Mastering Studio",
                            tint = Color(0xFFE5C158)
                        )
                    }
                }
            }

            // On-Device Professional Studio Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF130932), Color(0xFF2C1E5C))
                            )
                        )
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToProfessionalStudio() }
                        .padding(16.dp)
                        .testTag("home_enter_professional_studio_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF9D142).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MusicNote,
                                    contentDescription = "On-Device Studio",
                                    tint = Color(0xFFF9D142),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "On-Device Studio Workspace",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Compile multi-track stems, balance mixing desks, & export master deliverables",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter On-Device Studio Workspace",
                            tint = Color(0xFFF9D142)
                        )
                    }
                }
            }

            // AI Instrument Studio Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF140D2A), Color(0xFF211648))
                            )
                        )
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToInstrument() }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF9F75FF).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Piano,
                                    contentDescription = "AI Instrument Studio",
                                    tint = Color(0xFF9F75FF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Instrument Studio",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Synthesize Tabla, Sitar, Dholak, Bansuri & Western stems",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Studio",
                            tint = Color(0xFF9F75FF)
                        )
                    }
                }
            }

            // AI Composer Operating System Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF140D2A), Color(0xFF132A24))
                            )
                        )
                        .border(1.dp, Color(0xFF2FD6AA).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToComposer() }
                        .padding(16.dp)
                        .testTag("home_enter_composer_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2FD6AA).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SettingsInputComponent,
                                    contentDescription = "AI Composer OS",
                                    tint = Color(0xFF2FD6AA),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Composer Operating System",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Orchestrate full master composition plans, scales, Ragas & Indian taals",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Operating System",
                            tint = Color(0xFF2FD6AA)
                        )
                    }
                }
            }

            // AI Melody Generator Operating System Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF140D2A), Color(0xFF2C1E06))
                            )
                        )
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToMelodyGenerator() }
                        .padding(16.dp)
                        .testTag("home_enter_melody_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF9D142).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "AI Melody Generator OS",
                                    tint = Color(0xFFF9D142),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Melody Generator OS",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Generate classical Raga & Sargam melodies with real-time piano roll & synth",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Melody System",
                            tint = Color(0xFFF9D142)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // AI Chord Generator Operating System Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF140D2A), Color(0xFF1B0F3F))
                            )
                        )
                        .border(1.dp, Color(0xFF9F75FF).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToChordGenerator() }
                        .padding(16.dp)
                        .testTag("home_enter_chord_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF9F75FF).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QueueMusic,
                                    contentDescription = "AI Chord Generator OS",
                                    tint = Color(0xFF9F75FF),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Chord Generator OS",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Generate professional polyphonic chord progressions & harmonic guides",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Chord System",
                            tint = Color(0xFF9F75FF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // AI Arrangement Workspace Operating System Banner Entry Point
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF140D2A), Color(0xFF0C1D26))
                            )
                        )
                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .clickable { onNavigateToArrangement() }
                        .padding(16.dp)
                        .testTag("home_enter_arrangement_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF9D142).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Dashboard,
                                    contentDescription = "AI Arrangement OS",
                                    tint = Color(0xFFF9D142),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "AI Arrangement Workspace OS",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Orchestrate dynamic tracks, section energy lines, and professional session plan blueprints",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Enter Arrangement System",
                            tint = Color(0xFFF9D142)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Trending Indian Styles Slider
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trending Indian Styles",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "See All", tint = Color(0xFF9E93B3))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(trendingStyles) { (styleName, _) ->
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF140D2A))
                                    .border(1.dp, Color(0xFF9F75FF).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToCreateSong(styleName) }
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Icon(
                                        imageVector = Icons.Filled.Audiotrack,
                                        contentDescription = styleName,
                                        tint = Color(0xFFF9D142),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = styleName,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Generate AI",
                                        color = Color(0xFF9E93B3),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Folders/Projects Header
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Project Folders",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showCreateFolderDialog = true },
                            modifier = Modifier.testTag("create_folder_icon")
                        ) {
                            Icon(imageVector = Icons.Filled.CreateNewFolder, contentDescription = "New Folder", tint = Color(0xFFF9D142))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (projects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF140D2A))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Filled.FolderOpen, contentDescription = "No folders", tint = Color(0x33FFFFFF), modifier = Modifier.size(36.dp))
                                Text("No folders yet", color = Color(0xFF9E93B3), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                                Text("Tap the + icon above to categorize songs.", color = Color(0x66FFFFFF), fontSize = 10.sp)
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(projects) { project ->
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF1C1337))
                                        .border(1.dp, Color(0xFFF9D142).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                        .clickable { onNavigateToLibrary() }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Icon(imageVector = Icons.Filled.Folder, contentDescription = "Folder", tint = Color(0xFFF9D142), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = project.name,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = project.description.ifBlank { "No description" },
                                            color = Color(0xFF9E93B3),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent AI Creations Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Creations",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View Library",
                        color = Color(0xFF9F75FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToLibrary() }
                    )
                }
            }

            if (allSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF140D2A))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Filled.QueueMusic, contentDescription = "Empty", tint = Color(0x33FFFFFF), modifier = Modifier.size(48.dp))
                            Text("No songs created yet", color = Color(0xFF9E93B3), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                            Text("Your synthesized AI ragas will show here.", color = Color(0x55FFFFFF), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            } else {
                items(allSongs.take(5)) { song ->
                    val isCurrent = currentPlayingSong?.id == song.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("song_item_card_${song.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) Color(0xFF1C1337) else Color(0xFF140D2A)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isCurrent) Color(0xFFF9D142).copy(alpha = 0.5f) else Color(0xFF2E244E)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Sitar Play Icon
                            IconButton(
                                onClick = { musicViewModel.togglePlayback(song) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        if (isCurrent && isPlaying) Color(0xFFF9D142) else Color(0xFF9F75FF).copy(alpha = 0.15f)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isCurrent && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = if (isCurrent && isPlaying) Color.Black else Color(0xFFF9D142)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${song.genre} • ${song.singerVoice}",
                                    color = Color(0xFF9E93B3),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(onClick = { musicViewModel.toggleFavorite(song) }) {
                                Icon(
                                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (song.isFavorite) Color(0xFFF9D142) else Color(0xFF9E93B3)
                                )
                            }
                        }
                    }
                }
            }

            // Safe bottom spacing for player controller overlay
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }

    // New Folder Dialog
    if (showCreateFolderDialog) {
        Dialog(onDismissRequest = { showCreateFolderDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF140D2A),
                border = BorderStroke(1.dp, Color(0xFFF9D142).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Create Project Folder", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Categorize your AI lyrics and song stems.", color = Color(0xFF9E93B3), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name", color = Color(0xFF9E93B3)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF3E3556)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("folder_name_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newFolderDesc,
                        onValueChange = { newFolderDesc = it },
                        label = { Text("Description (Optional)", color = Color(0xFF9E93B3)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF9D142),
                            unfocusedBorderColor = Color(0xFF3E3556)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showCreateFolderDialog = false },
                            border = BorderStroke(1.dp, Color(0xFF3E3556)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                if (newFolderName.isNotBlank()) {
                                    musicViewModel.createProjectFolder(newFolderName, newFolderDesc)
                                    Toast.makeText(context, "Folder created successfully!", Toast.LENGTH_SHORT).show()
                                    newFolderName = ""
                                    newFolderDesc = ""
                                    showCreateFolderDialog = false
                                } else {
                                    Toast.makeText(context, "Please enter a name", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9D142)),
                            modifier = Modifier.weight(1f).testTag("save_folder_btn")
                        ) {
                            Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
