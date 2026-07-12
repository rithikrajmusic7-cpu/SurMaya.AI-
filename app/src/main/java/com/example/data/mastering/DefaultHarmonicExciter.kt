package com.example.data.mastering

import com.example.domain.mastering.IHarmonicExciter
import com.example.domain.model.mastering.HarmonicExciterSettings

class DefaultHarmonicExciter : IHarmonicExciter {
    override fun exciteHarmonics(genreStyle: String, intensity: Float): HarmonicExciterSettings {
        val mode = when (genreStyle.uppercase()) {
            "BOLLYWOOD" -> "Warm"
            "EDM" -> "Tube"
            "POP" -> "Tape"
            "DEVOTIONAL" -> "Analog"
            "CLASSICAL" -> "Digital Clean"
            else -> "Vintage"
        }

        val saturationAmount = intensity * 100.0f
        val oddHarmonics = when (mode) {
            "Tube" -> 30.0f
            "Tape" -> 60.0f
            "Warm" -> 20.0f
            "Analog" -> 45.0f
            "Vintage" -> 50.0f
            else -> 10.0f
        }
        val evenHarmonics = when (mode) {
            "Tube" -> 70.0f
            "Tape" -> 40.0f
            "Warm" -> 80.0f
            "Analog" -> 55.0f
            "Vintage" -> 50.0f
            else -> 10.0f
        }

        return HarmonicExciterSettings(
            mode = mode,
            saturationAmount = saturationAmount,
            oddHarmonics = oddHarmonics,
            evenHarmonics = evenHarmonics
        )
    }
}
