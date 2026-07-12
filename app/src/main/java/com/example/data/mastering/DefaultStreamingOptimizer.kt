package com.example.data.mastering

import com.example.domain.mastering.IStreamingOptimizer
import com.example.domain.model.mastering.StreamingTarget

class DefaultStreamingOptimizer : IStreamingOptimizer {
    override fun optimizeForPlatform(platformName: String): StreamingTarget {
        return when (platformName.uppercase()) {
            "SPOTIFY" -> StreamingTarget.SPOTIFY
            "APPLE MUSIC" -> StreamingTarget.APPLE_MUSIC
            "YOUTUBE MUSIC" -> StreamingTarget.YOUTUBE_MUSIC
            "AMAZON MUSIC" -> StreamingTarget.AMAZON_MUSIC
            "JIOSAAVN" -> StreamingTarget.JIOSAAVN
            "GAANA" -> StreamingTarget.GAANA
            "WYNK" -> StreamingTarget.WYNK
            "INSTAGRAM/FB", "INSTAGRAM" -> StreamingTarget.INSTAGRAM
            "TIKTOK" -> StreamingTarget.TIKTOK
            "TV BROADCAST", "BROADCAST" -> StreamingTarget.BROADCAST
            "CD", "CD RED BOOK" -> StreamingTarget.CD_DA
            "CINEMA" -> StreamingTarget.CINEMA
            else -> StreamingTarget("Custom", -14.0f, -1.0f, "AAC/MP3", "WAV")
        }
    }
}
