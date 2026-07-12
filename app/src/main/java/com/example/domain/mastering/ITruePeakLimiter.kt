package com.example.domain.mastering

import com.example.domain.model.mastering.TruePeakLimiterSettings

interface ITruePeakLimiter {
    fun processLimiting(targetTruePeakDb: Float, inputLoudnessLufs: Float): TruePeakLimiterSettings
}
