package com.example.domain.mastering

import com.example.domain.model.mastering.StreamingTarget

interface IStreamingOptimizer {
    fun optimizeForPlatform(platformName: String): StreamingTarget
}
