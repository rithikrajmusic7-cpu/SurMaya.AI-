package com.example.domain.model.mastering

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamingTarget(
    @Json(name = "name") val name: String,
    @Json(name = "targetLufs") val targetLufs: Float,
    @Json(name = "targetTruePeak") val targetTruePeak: Float,
    @Json(name = "codecRecommendation") val codecRecommendation: String,
    @Json(name = "exportRecommendation") val exportRecommendation: String
) {
    companion object {
        val SPOTIFY = StreamingTarget("Spotify", -14.0f, -1.0f, "Ogg Vorbis (320 kbps)", "WAV 24-bit 44.1kHz")
        val APPLE_MUSIC = StreamingTarget("Apple Music", -16.0f, -1.0f, "AAC (256 kbps, ALAC)", "WAV 24-bit 96kHz (Hi-Res)")
        val YOUTUBE_MUSIC = StreamingTarget("YouTube Music", -14.0f, -1.0f, "AAC (128/256 kbps)", "WAV 24-bit 48kHz")
        val AMAZON_MUSIC = StreamingTarget("Amazon Music", -14.0f, -1.0f, "FLAC / HD Audio", "WAV 24-bit 44.1kHz")
        val JIOSAAVN = StreamingTarget("JioSaavn", -14.0f, -1.0f, "AAC (320 kbps)", "WAV 16-bit 44.1kHz")
        val GAANA = StreamingTarget("Gaana", -14.0f, -1.0f, "AAC / MP3", "WAV 16-bit 44.1kHz")
        val WYNK = StreamingTarget("Wynk", -14.0f, -1.0f, "AAC (320 kbps)", "WAV 16-bit 44.1kHz")
        val INSTAGRAM = StreamingTarget("Instagram/FB", -14.0f, -1.5f, "AAC (Stereo)", "MP4/AAC 16-bit 44.1kHz")
        val TIKTOK = StreamingTarget("TikTok", -13.0f, -1.5f, "AAC (Stereo)", "WAV 16-bit 44.1kHz")
        val BROADCAST = StreamingTarget("TV Broadcast (EBU R128)", -23.0f, -2.0f, "PCM/Stereo", "WAV 24-bit 48kHz")
        val CD_DA = StreamingTarget("CD Red Book", -9.0f, -0.1f, "PCM 16-bit Stereo", "WAV 16-bit 44.1kHz")
        val CINEMA = StreamingTarget("Cinema (SMPTE)", -27.0f, -1.0f, "Linear PCM 5.1/7.1", "WAV 24-bit 48kHz")

        val ALL_TARGETS = listOf(
            SPOTIFY, APPLE_MUSIC, YOUTUBE_MUSIC, AMAZON_MUSIC,
            JIOSAAVN, GAANA, WYNK, INSTAGRAM, TIKTOK,
            BROADCAST, CD_DA, CINEMA
        )
    }
}
