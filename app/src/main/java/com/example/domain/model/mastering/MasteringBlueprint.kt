package com.example.domain.model.mastering

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MasteringBlueprint(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "genreStyle") val genreStyle: String, // "Bollywood", "Hollywood", "EDM", "Pop", "Classical", "Devotional", "Folk", "Rock", "Cinematic"
    @Json(name = "targetLoudnessLufs") val targetLoudnessLufs: Float,
    @Json(name = "multibandBands") val multibandBands: List<MultibandBand> = emptyList(),
    @Json(name = "stereoSettings") val stereoSettings: StereoMetrics,
    @Json(name = "exciterSettings") val exciterSettings: HarmonicExciterSettings,
    @Json(name = "limiterSettings") val limiterSettings: TruePeakLimiterSettings,
    @Json(name = "ditherSettings") val ditherSettings: DitherSettings,
    @Json(name = "selectedStreamingTargets") val selectedStreamingTargets: List<String> = emptyList()
)
