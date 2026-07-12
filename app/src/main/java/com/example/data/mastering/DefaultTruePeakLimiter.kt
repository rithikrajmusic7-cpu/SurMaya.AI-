package com.example.data.mastering

import com.example.domain.mastering.ITruePeakLimiter
import com.example.domain.model.mastering.TruePeakLimiterSettings

class DefaultTruePeakLimiter : ITruePeakLimiter {
    override fun processLimiting(targetTruePeakDb: Float, inputLoudnessLufs: Float): TruePeakLimiterSettings {
        // Higher volume targets require faster release and more oversampling
        val oversampling = if (inputLoudnessLufs > -12.0f) "4x" else "2x"
        val releaseMs = if (inputLoudnessLufs > -10.0f) 50.0f else 150.0f
        val lookAheadMs = 2.0f

        return TruePeakLimiterSettings(
            ceilingDb = targetTruePeakDb,
            releaseMs = releaseMs,
            oversampling = oversampling,
            lookAheadMs = lookAheadMs,
            ispDetected = false // No inter-sample peaks slipping past our smart limiter!
        )
    }
}
