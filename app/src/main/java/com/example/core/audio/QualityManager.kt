package com.example.core.audio

import com.example.data.remote.gateway.AIREJniBridge

class QualityManager {
    enum class QualityProfile(val value: String) {
        DRAFT("Draft"),
        STUDIO("Studio"),
        ULTRA("Ultra")
    }

    private var activeProfile = QualityProfile.STUDIO

    fun applyQualityProfile(enginePtr: Long, profile: QualityProfile) {
        activeProfile = profile
        
        // Pass to native JNI bridge
        AIREJniBridge.setQualityProfile(enginePtr, profile.value)
    }

    fun getActiveProfile(): QualityProfile = activeProfile

    // Get specific rendering parameter limits depending on current profile
    fun getMaxVoiceLimit(): Int {
        return when (activeProfile) {
            QualityProfile.DRAFT -> 8
            QualityProfile.STUDIO -> 16
            QualityProfile.ULTRA -> 32
        }
    }

    fun getOversamplingFactor(): Int {
        return when (activeProfile) {
            QualityProfile.DRAFT -> 1
            QualityProfile.STUDIO -> 2
            QualityProfile.ULTRA -> 4
        }
    }

    fun isLookaheadLimiterEnabled(): Boolean {
        return when (activeProfile) {
            QualityProfile.DRAFT -> false // Avoid CPU overhead for quick draft previews
            else -> true
        }
    }
}
