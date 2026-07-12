package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlowingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    testTag: String? = null,
    colors: List<Color> = listOf(Color(0xFF9F75FF), Color(0xFFF9D142)) // Purple to Gold gradient
) {
    val shape = RoundedCornerShape(25.dp)
    val buttonModifier = if (testTag != null) {
        modifier.testTag(testTag)
    } else {
        modifier
    }

    Box(
        modifier = buttonModifier
            .shadow(if (enabled) 8.dp else 0.dp, shape)
            .clip(shape)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(colors)
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF332D47), Color(0xFF332D47)))
                }
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color(0xFF09041A) else Color(0xFF817799),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
