package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassShape = RoundedCornerShape(cornerRadius)
    val cardModifier = if (testTag != null) {
        modifier.testTag(testTag)
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier
            .clip(glassShape)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x40FFD700), // Muted Gold Translucent
                        Color(0x10FFFFFF)  // Muted White Translucent
                    )
                ),
                shape = glassShape
            ),
        color = Color(0x7F140D2A), // Half-transparent dark card
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
