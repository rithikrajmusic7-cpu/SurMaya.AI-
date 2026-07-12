package com.example.domain.model.mastering

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoudnessMetrics(
    @Json(name = "integratedLufs") val integratedLufs: Float,
    @Json(name = "shortTermLufs") val shortTermLufs: Float,
    @Json(name = "momentaryLufs") val momentaryLufs: Float,
    @Json(name = "dynamicRangeDb") val dynamicRangeDb: Float,
    @Json(name = "peakDb") val peakDb: Float,
    @Json(name = "truePeakDb") val truePeakDb: Float,
    @Json(name = "rmsDb") val rmsDb: Float,
    @Json(name = "crestFactor") val crestFactor: Float,
    @Json(name = "headroomDb") val headroomDb: Float
)

@JsonClass(generateAdapter = true)
data class MultibandBand(
    @Json(name = "bandName") val bandName: String, // "Low", "Low Mid", "Mid", "High Mid", "Air"
    @Json(name = "crossoverStartHz") val crossoverStartHz: Float,
    @Json(name = "crossoverEndHz") val crossoverEndHz: Float,
    @Json(name = "thresholdDb") val thresholdDb: Float,
    @Json(name = "ratio") val ratio: Float,
    @Json(name = "attackMs") val attackMs: Float,
    @Json(name = "releaseMs") val releaseMs: Float,
    @Json(name = "kneeDb") val kneeDb: Float,
    @Json(name = "gainReductionDb") val gainReductionDb: Float
)

@JsonClass(generateAdapter = true)
data class StereoMetrics(
    @Json(name = "midSideBalance") val midSideBalance: Float, // -1.0 (Mid only) to 1.0 (Side only)
    @Json(name = "stereoWidth") val stereoWidth: Float, // 0.0 (Mono) to 2.0 (Ultra wide)
    @Json(name = "monoCompatibility") val monoCompatibility: Float, // 0 to 100%
    @Json(name = "correlation") val correlation: Float, // -1.0 to 1.0
    @Json(name = "phaseDetected") val phaseDetected: Boolean, // False means normal, True means phase issue
    @Json(name = "centerFocus") val centerFocus: Float, // 0 to 100%
    @Json(name = "bassMonoizerEnabled") val bassMonoizerEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class HarmonicExciterSettings(
    @Json(name = "mode") val mode: String, // "Tube", "Tape", "Analog", "Warm", "Digital Clean", "Vintage"
    @Json(name = "saturationAmount") val saturationAmount: Float, // 0 to 100%
    @Json(name = "oddHarmonics") val oddHarmonics: Float, // 0 to 100%
    @Json(name = "evenHarmonics") val evenHarmonics: Float // 0 to 100%
)

@JsonClass(generateAdapter = true)
data class TruePeakLimiterSettings(
    @Json(name = "ceilingDb") val ceilingDb: Float,
    @Json(name = "releaseMs") val releaseMs: Float,
    @Json(name = "oversampling") val oversampling: String, // "None", "2x", "4x", "8x"
    @Json(name = "lookAheadMs") val lookAheadMs: Float,
    @Json(name = "ispDetected") val ispDetected: Boolean
)

@JsonClass(generateAdapter = true)
data class DitherSettings(
    @Json(name = "bitDepth") val bitDepth: String, // "16 Bit", "24 Bit", "32 Float"
    @Json(name = "noiseShaping") val noiseShaping: String, // "None", "Low", "Medium", "High"
    @Json(name = "exportQuality") val exportQuality: String // "Standard", "High", "Ultra High Studio"
)

@JsonClass(generateAdapter = true)
data class ReferenceMatchingReport(
    @Json(name = "referenceGenre") val referenceGenre: String, // "Bollywood", "Hollywood", "EDM", "Pop", etc.
    @Json(name = "referenceMood") val referenceMood: String,
    @Json(name = "referenceLanguage") val referenceLanguage: String,
    @Json(name = "spectralMatchPercentage") val spectralMatchPercentage: Float, // 0 to 100
    @Json(name = "dynamicsMatchPercentage") val dynamicsMatchPercentage: Float, // 0 to 100
    @Json(name = "stereoMatchPercentage") val stereoMatchPercentage: Float, // 0 to 100
    @Json(name = "recommendations") val recommendations: List<String> = emptyList()
)
