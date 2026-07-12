package com.example.domain.model.mastering

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MasteringReport(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "whyEQWasApplied") val whyEQWasApplied: String,
    @Json(name = "whyCompressionWasApplied") val whyCompressionWasApplied: String,
    @Json(name = "whyStereoWidthChanged") val whyStereoWidthChanged: String,
    @Json(name = "whyLimiterActed") val whyLimiterActed: String,
    @Json(name = "whyLoudnessChanged") val whyLoudnessChanged: String,
    @Json(name = "whyHarmonicExcitationApplied") val whyHarmonicExcitationApplied: String,
    @Json(name = "overallSummary") val overallSummary: String
)
