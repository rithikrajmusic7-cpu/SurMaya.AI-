package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.navigation.NavGraph
import com.example.ui.theme.SurMayaTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModel.Factory(application)
    }

    private val musicViewModel: MusicViewModel by viewModels {
        MusicViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SurMayaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF09041A) // Force Deep Space Dark
                ) {
                    NavGraph(
                        authViewModel = authViewModel,
                        musicViewModel = musicViewModel
                    )
                }
            }
        }
    }
}
