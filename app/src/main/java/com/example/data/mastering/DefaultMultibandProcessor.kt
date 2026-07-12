package com.example.data.mastering

import com.example.domain.mastering.IMultibandProcessor
import com.example.domain.model.mastering.MultibandBand

class DefaultMultibandProcessor : IMultibandProcessor {
    override fun processMultibandDynamics(genreStyle: String, inputLoudnessDb: Float): List<MultibandBand> {
        // Adjust threshold based on target genre style and loudness
        val thresholdOffset = if (genreStyle.equals("EDM", ignoreCase = true) || genreStyle.equals("Pop", ignoreCase = true)) -2.0f else -0.5f
        
        return listOf(
            MultibandBand(
                bandName = "Low",
                crossoverStartHz = 20.0f,
                crossoverEndHz = 120.0f,
                thresholdDb = -18.0f + thresholdOffset,
                ratio = 2.5f,
                attackMs = 50.0f,
                releaseMs = 150.0f,
                kneeDb = 4.0f,
                gainReductionDb = 1.8f
            ),
            MultibandBand(
                bandName = "Low Mid",
                crossoverStartHz = 120.0f,
                crossoverEndHz = 500.0f,
                thresholdDb = -15.0f + thresholdOffset,
                ratio = 2.0f,
                attackMs = 35.0f,
                releaseMs = 100.0f,
                kneeDb = 5.0f,
                gainReductionDb = 1.2f
            ),
            MultibandBand(
                bandName = "Mid",
                crossoverStartHz = 500.0f,
                crossoverEndHz = 2000.0f,
                thresholdDb = -12.0f + thresholdOffset,
                ratio = 1.8f,
                attackMs = 25.0f,
                releaseMs = 80.0f,
                kneeDb = 6.0f,
                gainReductionDb = 1.0f
            ),
            MultibandBand(
                bandName = "High Mid",
                crossoverStartHz = 2000.0f,
                crossoverEndHz = 8000.0f,
                thresholdDb = -14.0f + thresholdOffset,
                ratio = 2.0f,
                attackMs = 20.0f,
                releaseMs = 60.0f,
                kneeDb = 4.0f,
                gainReductionDb = 1.4f
            ),
            MultibandBand(
                bandName = "Air",
                crossoverStartHz = 8000.0f,
                crossoverEndHz = 20000.0f,
                thresholdDb = -20.0f + thresholdOffset,
                ratio = 1.5f,
                attackMs = 15.0f,
                releaseMs = 50.0f,
                kneeDb = 8.0f,
                gainReductionDb = 0.8f
            )
        )
    }
}
