package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SurMayaColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = GlowingGold,
    tertiary = GlowingGold,
    background = DeepSpaceDark,
    surface = SolidCardDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = Color(0xFF1C1337),
    onSurfaceVariant = MutedText,
    outline = Color(0x22FFFFFF)
)

@Composable
fun SurMayaTheme(
    darkTheme: Boolean = true, // Force premium dark theme by default
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SurMayaColorScheme,
        typography = Typography,
        content = content
    )
}
