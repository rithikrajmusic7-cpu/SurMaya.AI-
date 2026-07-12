package com.example.data.mastering

import com.example.domain.mastering.IDitherEngine
import com.example.domain.model.mastering.DitherSettings

class DefaultDitherEngine : IDitherEngine {
    override fun processDither(bitDepth: String): DitherSettings {
        val noiseShaping = when (bitDepth) {
            "16 Bit" -> "High"
            "24 Bit" -> "Medium"
            else -> "None"
        }
        val exportQuality = when (bitDepth) {
            "32 Float" -> "Ultra High Studio"
            "24 Bit" -> "High"
            else -> "Standard"
        }

        return DitherSettings(
            bitDepth = bitDepth,
            noiseShaping = noiseShaping,
            exportQuality = exportQuality
        )
    }
}
