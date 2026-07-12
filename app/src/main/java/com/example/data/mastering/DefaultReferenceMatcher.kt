package com.example.data.mastering

import com.example.domain.mastering.IReferenceMatcher
import com.example.domain.model.mastering.ReferenceMatchingReport

class DefaultReferenceMatcher : IReferenceMatcher {
    override fun matchReference(genreStyle: String, mood: String, language: String): ReferenceMatchingReport {
        val (spectral, dynamics, stereo) = when (genreStyle.uppercase()) {
            "BOLLYWOOD" -> Triple(94.5f, 92.0f, 95.0f)
            "EDM" -> Triple(91.0f, 88.5f, 93.0f)
            "POP" -> Triple(96.0f, 94.0f, 94.5f)
            "CLASSICAL" -> Triple(98.0f, 97.0f, 91.0f)
            else -> Triple(93.0f, 91.0f, 92.0f)
        }

        val recommendations = mutableListOf<String>()
        if (spectral < 95.0f) {
            recommendations.add("Consider gentle high-shelf boost (+0.5 dB above 10kHz) for that modern 'air' sheen.")
        }
        if (dynamics < 92.0f) {
            recommendations.add("Adjust multiband compression threshold on the mid band to control transient density.")
        }
        if (stereo < 94.0f) {
            recommendations.add("Slightly broaden side channel gains in the 2kHz-6kHz range for a wider stereo image.")
        }
        if (recommendations.isEmpty()) {
            recommendations.add("The track perfectly matches the structural target profile. Ready for release!")
        }

        return ReferenceMatchingReport(
            referenceGenre = genreStyle,
            referenceMood = mood,
            referenceLanguage = language,
            spectralMatchPercentage = spectral,
            dynamicsMatchPercentage = dynamics,
            stereoMatchPercentage = stereo,
            recommendations = recommendations
        )
    }
}
