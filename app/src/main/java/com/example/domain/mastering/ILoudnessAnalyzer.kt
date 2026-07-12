package com.example.domain.mastering

import com.example.domain.model.mastering.LoudnessMetrics

interface ILoudnessAnalyzer {
    fun analyzeLoudness(audioDataDummy: FloatArray, sampleRate: Int = 44100): LoudnessMetrics
}
