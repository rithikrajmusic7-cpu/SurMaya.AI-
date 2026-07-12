package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    waves: List<Float>,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFFF9D142),
    inactiveColor: Color = Color(0xFF9F75FF)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(vertical = 4.dp)
    ) {
        val width = size.width
        val height = size.height
        val barCount = waves.size
        val gap = 6f
        val barWidth = (width - (gap * (barCount - 1))) / barCount

        val gradient = Brush.verticalGradient(
            colors = listOf(activeColor, inactiveColor),
            startY = 0f,
            endY = height
        )

        for (i in 0 until barCount) {
            val waveHeight = waves[i] * height
            val x = i * (barWidth + gap)
            val y = (height - waveHeight) / 2 // Centered vertically

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, waveHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
