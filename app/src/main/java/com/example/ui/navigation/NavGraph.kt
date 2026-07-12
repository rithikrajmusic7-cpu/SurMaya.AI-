package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.MusicPlayerController
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.create.CreateSongScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.ProjectLibraryScreen
import com.example.ui.screens.lyrics.LyricsGeneratorScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.upload.UploadAudioScreen
import com.example.ui.screens.singer.AISingerScreen
import com.example.ui.screens.instrument.AIInstrumentScreen
import com.example.ui.screens.composer.AIComposerScreen
import com.example.ui.screens.melody.AIMelodyScreen
import com.example.ui.screens.chord.AIChordScreen
import com.example.ui.screens.arrangement.AIArrangementScreen
import com.example.ui.screens.diagnostics.NetworkDiagnosticScreen
import com.example.ui.screens.diagnostics.ApiConfigurationScreen
import com.example.core.security.DeveloperSessionManager
import com.example.ui.screens.developer.DeveloperLoginScreen
import com.example.ui.screens.developer.DeveloperDashboardScreen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.SingerViewModel
import com.example.ui.viewmodel.InstrumentViewModel
import com.example.ui.viewmodel.ComposerViewModel
import com.example.ui.viewmodel.MelodyViewModel
import com.example.ui.viewmodel.ChordViewModel
import com.example.ui.viewmodel.ArrangementViewModel
import com.example.di.ServiceLocator
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.app.Application

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    musicViewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current.applicationContext as Application
    val singerViewModel: SingerViewModel = viewModel(
        factory = SingerViewModel.Factory(context)
    )
    val instrumentViewModel: InstrumentViewModel = viewModel(
        factory = InstrumentViewModel.Factory(context)
    )
    val composerViewModel: ComposerViewModel = viewModel(
        factory = ComposerViewModel.Factory(context)
    )
    val melodyViewModel: MelodyViewModel = viewModel(
        factory = MelodyViewModel.Factory(context)
    )
    val chordViewModel: ChordViewModel = viewModel(
        factory = ChordViewModel.Factory(context)
    )
    val arrangementViewModel: ArrangementViewModel = viewModel(
        factory = ArrangementViewModel.Factory(context, ServiceLocator.getArrangementRepository(context))
    )
    val mixingViewModel: com.example.ui.viewmodel.MixingViewModel = viewModel(
        factory = com.example.ui.viewmodel.MixingViewModel.Factory(context)
    )
    val masteringViewModel: com.example.ui.viewmodel.MasteringViewModel = viewModel(
        factory = com.example.ui.viewmodel.MasteringViewModel.Factory(context)
    )
    val qaViewModel: com.example.ui.viewmodel.QAViewModel = viewModel(
        factory = com.example.ui.viewmodel.QAViewModel.Factory(context)
    )
    val studioViewModel: com.example.ui.viewmodel.StudioViewModel = viewModel(
        factory = com.example.ui.viewmodel.StudioViewModel.Factory(context)
    )

    val currentUser by authViewModel.currentUser.collectAsState()
    val isUserLoggedIn = currentUser != null

    // Playback state managers
    val currentPlayingSong by musicViewModel.currentPlayingSong.collectAsState()
    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val playProgress by musicViewModel.playbackProgress.collectAsState()
    val playbackWaves by musicViewModel.visualizerWaves.collectAsState()

    // Determine if we should show bottom navigation bar & global player controller
    val nonTabbedRoutes = listOf(
        Routes.LOGIN,
        Routes.REGISTER,
        Routes.FORGOT_PASSWORD,
        Routes.DEVELOPER_LOGIN,
        Routes.DEVELOPER_DASHBOARD,
        Routes.SETTINGS,
        Routes.AUDIO_UPLOAD,
        Routes.NETWORK_DIAGNOSTIC,
        Routes.API_CONFIGURATION,
        Routes.AI_COMPOSER,
        Routes.AI_MELODY_GENERATOR,
        Routes.AI_CHORD_GENERATOR,
        Routes.AI_ARRANGEMENT_ENGINE,
        Routes.AI_MASTERING_STUDIO,
        Routes.PROFESSIONAL_STUDIO
    )
    val showNavigationBars = currentRoute != null && !nonTabbedRoutes.contains(currentRoute)

    Scaffold(
        bottomBar = {
            if (showNavigationBars) {
                Column {
                    // Floating OS-level music controller overlay (bobs up only when song loaded)
                    AnimatedVisibility(visible = currentPlayingSong != null) {
                        MusicPlayerController(
                            currentSong = currentPlayingSong,
                            isPlaying = isPlaying,
                            progress = playProgress,
                            waves = playbackWaves,
                            onTogglePlay = { currentPlayingSong?.let { musicViewModel.togglePlayback(it) } },
                            onStop = { musicViewModel.stopPlayback() }
                        )
                    }

                    // Main App bottom navigation bar
                    NavigationBar(
                        containerColor = Color(0xFF140D2A),
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("app_bottom_nav_bar")
                    ) {
                        // Home tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.HOME,
                            onClick = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF09041A),
                                selectedTextColor = Color(0xFFF9D142),
                                indicatorColor = Color(0xFFF9D142),
                                unselectedIconColor = Color(0xFF9E93B3),
                                unselectedTextColor = Color(0xFF9E93B3)
                            )
                        )

                        // Create Song tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.CREATE_SONG,
                            onClick = {
                                navController.navigate(Routes.CREATE_SONG) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = Icons.Filled.Audiotrack, contentDescription = "Compose") },
                            label = { Text("Compose", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF09041A),
                                selectedTextColor = Color(0xFFF9D142),
                                indicatorColor = Color(0xFFF9D142),
                                unselectedIconColor = Color(0xFF9E93B3),
                                unselectedTextColor = Color(0xFF9E93B3)
                            ),
                            modifier = Modifier.testTag("bottom_nav_create_btn")
                        )

                        // AI Singer tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.AI_SINGER,
                            onClick = {
                                navController.navigate(Routes.AI_SINGER) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = Icons.Filled.RecordVoiceOver, contentDescription = "Singer") },
                            label = { Text("Singer", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF09041A),
                                selectedTextColor = Color(0xFFF9D142),
                                indicatorColor = Color(0xFFF9D142),
                                unselectedIconColor = Color(0xFF9E93B3),
                                unselectedTextColor = Color(0xFF9E93B3)
                            )
                        )

                        // Lyrics tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.LYRICS_GENERATOR,
                            onClick = {
                                navController.navigate(Routes.LYRICS_GENERATOR) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = Icons.Filled.Lyrics, contentDescription = "Lyrics") },
                            label = { Text("Lyrics", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF09041A),
                                selectedTextColor = Color(0xFFF9D142),
                                indicatorColor = Color(0xFFF9D142),
                                unselectedIconColor = Color(0xFF9E93B3),
                                unselectedTextColor = Color(0xFF9E93B3)
                            )
                        )

                        // Library tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.LIBRARY,
                            onClick = {
                                navController.navigate(Routes.LIBRARY) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = Icons.Filled.Folder, contentDescription = "Library") },
                            label = { Text("Library", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF09041A),
                                selectedTextColor = Color(0xFFF9D142),
                                indicatorColor = Color(0xFFF9D142),
                                unselectedIconColor = Color(0xFF9E93B3),
                                unselectedTextColor = Color(0xFF9E93B3)
                            )
                        )

                        // Profile tab
                        NavigationBarItem(
                            selected = currentRoute == Routes.PROFILE,
                            onClick = {
                                navController.navigate(Routes.PROFILE) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF09041A),
                                selectedTextColor = Color(0xFFF9D142),
                                indicatorColor = Color(0xFFF9D142),
                                unselectedIconColor = Color(0xFF9E93B3),
                                unselectedTextColor = Color(0xFF9E93B3)
                            )
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF09041A),
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isUserLoggedIn) Routes.HOME else Routes.LOGIN,
            modifier = Modifier.padding(paddingValues)
        ) {
            // LOGIN SCREEN
            composable(Routes.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                    onNavigateToDeveloperLogin = { navController.navigate(Routes.DEVELOPER_LOGIN) }
                )
            }

            // REGISTER SCREEN
            composable(Routes.REGISTER) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
                )
            }

            // FORGOT PASSWORD SCREEN
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // HOME DASHBOARD
            composable(Routes.HOME) {
                HomeScreen(
                    authViewModel = authViewModel,
                    musicViewModel = musicViewModel,
                    onNavigateToCreateSong = { preStyle ->
                        navController.navigate(Routes.CREATE_SONG)
                    },
                    onNavigateToLyrics = { navController.navigate(Routes.LYRICS_GENERATOR) },
                    onNavigateToLibrary = { navController.navigate(Routes.LIBRARY) },
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateToSinger = { navController.navigate(Routes.AI_SINGER) },
                    onNavigateToInstrument = { navController.navigate(Routes.AI_INSTRUMENT) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToComposer = { navController.navigate(Routes.AI_COMPOSER) },
                    onNavigateToMelodyGenerator = { navController.navigate(Routes.AI_MELODY_GENERATOR) },
                    onNavigateToChordGenerator = { navController.navigate(Routes.AI_CHORD_GENERATOR) },
                    onNavigateToArrangement = { navController.navigate(Routes.AI_ARRANGEMENT_ENGINE) },
                    onNavigateToMixing = { navController.navigate(Routes.AI_MIXER_STUDIO) },
                    onNavigateToMastering = { navController.navigate(Routes.AI_MASTERING_STUDIO) },
                    onNavigateToProfessionalStudio = { navController.navigate(Routes.PROFESSIONAL_STUDIO) },
                    onLogoutSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // COMPOSE SONG SCREEN
            composable(Routes.CREATE_SONG) {
                CreateSongScreen(
                    musicViewModel = musicViewModel,
                    initialStyle = null,
                    onNavigateToUpload = { navController.navigate(Routes.AUDIO_UPLOAD) }
                )
            }

            // LYRICS GENERATOR SCREEN
            composable(Routes.LYRICS_GENERATOR) {
                LyricsGeneratorScreen(musicViewModel = musicViewModel)
            }

            // PROJECT LIBRARY SCREEN
            composable(Routes.LIBRARY) {
                ProjectLibraryScreen(musicViewModel = musicViewModel)
            }

            // USER PROFILE SCREEN
            composable(Routes.PROFILE) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onLogoutSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // AUDIO UPLOAD SCREEN
            composable(Routes.AUDIO_UPLOAD) {
                UploadAudioScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // APPLICATION SETTINGS SCREEN
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToDiagnostics = { navController.navigate(Routes.NETWORK_DIAGNOSTIC) },
                    onNavigateToApiConfig = { navController.navigate(Routes.API_CONFIGURATION) },
                    onNavigateToAudioQuality = { navController.navigate(Routes.AUDIO_QUALITY_VALIDATION) }
                )
            }

            // DEVELOPER LOGIN SCREEN
            composable(Routes.DEVELOPER_LOGIN) {
                DeveloperLoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.DEVELOPER_DASHBOARD) {
                            popUpTo(Routes.DEVELOPER_LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // DEVELOPER DASHBOARD SCREEN
            composable(Routes.DEVELOPER_DASHBOARD) {
                val devSession = DeveloperSessionManager.getInstance(LocalContext.current)
                if (!devSession.isDeveloperAuthenticated()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    DeveloperDashboardScreen(
                        authViewModel = authViewModel,
                        onNavigateToDiagnostics = { navController.navigate(Routes.NETWORK_DIAGNOSTIC) },
                        onNavigateToApiConfig = { navController.navigate(Routes.API_CONFIGURATION) },
                        onNavigateToAudioQuality = { navController.navigate(Routes.AUDIO_QUALITY_VALIDATION) },
                        onLogout = {
                            devSession.clearSession()
                            authViewModel.refreshDevModeState()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // NETWORK DIAGNOSTICS SCREEN
            composable(Routes.NETWORK_DIAGNOSTIC) {
                val devSession = DeveloperSessionManager.getInstance(LocalContext.current)
                if (!devSession.isDeveloperAuthenticated()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    NetworkDiagnosticScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }

            // SECURE API CONFIGURATION SCREEN
            composable(Routes.API_CONFIGURATION) {
                val devSession = DeveloperSessionManager.getInstance(LocalContext.current)
                if (!devSession.isDeveloperAuthenticated()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    ApiConfigurationScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }

            // AUDIO QUALITY VALIDATION SCREEN
            composable(Routes.AUDIO_QUALITY_VALIDATION) {
                val devSession = DeveloperSessionManager.getInstance(LocalContext.current)
                if (!devSession.isDeveloperAuthenticated()) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                } else {
                    com.example.ui.screens.diagnostics.AudioQualityValidationScreen(
                        qaViewModel = qaViewModel,
                        onNavigateBack = { navController.navigateUp() }
                    )
                }
            }

            // AI SINGER STUDIO SCREEN
            composable(Routes.AI_SINGER) {
                AISingerScreen(
                    singerViewModel = singerViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // AI INSTRUMENT STUDIO SCREEN
            composable(Routes.AI_INSTRUMENT) {
                AIInstrumentScreen(
                    instrumentViewModel = instrumentViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // AI COMPOSER OPERATING SYSTEM
            composable(Routes.AI_COMPOSER) {
                AIComposerScreen(
                    viewModel = composerViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // AI MELODY GENERATOR WORKSPACE
            composable(Routes.AI_MELODY_GENERATOR) {
                AIMelodyScreen(
                    viewModel = melodyViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // AI CHORD GENERATOR WORKSPACE
            composable(Routes.AI_CHORD_GENERATOR) {
                AIChordScreen(
                    viewModel = chordViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // AI ARRANGEMENT WORKSPACE
            composable(Routes.AI_ARRANGEMENT_ENGINE) {
                AIArrangementScreen(
                    viewModel = arrangementViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // AI MIXING WORKSPACE
            composable(Routes.AI_MIXER_STUDIO) {
                com.example.ui.screens.mixing.AIMixingScreen(
                    viewModel = mixingViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // AI MASTERING STUDIO WORKSPACE
            composable(Routes.AI_MASTERING_STUDIO) {
                com.example.ui.screens.mastering.AIMasteringScreen(
                    viewModel = masteringViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }

            // PROFESSIONAL ON-DEVICE STUDIO WORKSPACE RUNTIME
            composable(Routes.PROFESSIONAL_STUDIO) {
                com.example.ui.screens.studio.ProfessionalStudioScreen(
                    studioViewModel = studioViewModel,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}
