package com.example.domain.model.mastering

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MasteringValidation(
    @Json(name = "isClippingRisk") val isClippingRisk: Boolean,
    @Json(name = "isPhaseIncompatible") val isPhaseIncompatible: Boolean,
    @Json(name = "isOvercompressed") val isOvercompressed: Boolean,
    @Json(name = "warnings") val warnings: List<String> = emptyList(),
    @Json(name = "suggestions") val suggestions: List<String> = emptyList()
)
