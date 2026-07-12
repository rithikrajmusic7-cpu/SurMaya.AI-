package com.example.data.mastering

import com.example.domain.mastering.IStereoEnhancer
import com.example.domain.model.mastering.StereoMetrics

class DefaultStereoEnhancer : IStereoEnhancer {
    override fun enhanceStereo(genreStyle: String): StereoMetrics {
        val (width, centerFocus, correlation) = when (genreStyle.uppercase()) {
            "EDM" -> Triple(1.4f, 75.0f, 0.82f)
            "CINEMATIC" -> Triple(1.5f, 65.0f, 0.78f)
            "BOLLYWOOD" -> Triple(1.2f, 85.0f, 0.90f)
            "POP" -> Triple(1.3f, 80.0f, 0.85f)
            "DEVOTIONAL" -> Triple(1.1f, 90.0f, 0.93f)
            "CLASSICAL" -> Triple(1.15f, 70.0f, 0.88f)
            else -> Triple(1.25f, 80.0f, 0.86f)
        }

        return StereoMetrics(
            midSideBalance = if (width > 1.2f) 0.15f else 0.05f,
            stereoWidth = width,
            monoCompatibility = (100.0f * (correlation + 1.0f) / 2.0f).coerceIn(0.0f, 100.0f),
            correlation = correlation,
            phaseDetected = correlation < 0.2f,
            centerFocus = centerFocus,
            bassMonoizerEnabled = true // Bass monoizer should always be enabled below 120Hz for solid low-end!
        )
    }
}
