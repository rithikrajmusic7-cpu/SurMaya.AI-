package com.example.domain.model.mixing

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TrackAnalysis(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "trackName") val trackName: String,
    @Json(name = "trackType") val trackType: String, // "Vocal", "Melody", "Chord", "Bass", "Drum", "Master"
    @Json(name = "rmsLoudnessDb") val rmsLoudnessDb: Float, // e.g. -18.0f
    @Json(name = "peakLoudnessDb") val peakLoudnessDb: Float, // e.g. -2.1f
    @Json(name = "dynamicRange") val dynamicRange: Float, // difference between peak and RMS
    @Json(name = "stereoWidth") val stereoWidth: Float, // 0.0 (mono) to 1.0 (wide stereo)
    @Json(name = "lowFreqEnergy") val lowFreqEnergy: Float, // 0 to 100 metric
    @Json(name = "midFreqEnergy") val midFreqEnergy: Float, // 0 to 100 metric
    @Json(name = "highFreqEnergy") val highFreqEnergy: Float, // 0 to 100 metric
    @Json(name = "transientCrispness") val transientCrispness: Float // 0 to 100 metric
)

@JsonClass(generateAdapter = true)
data class GainStaging(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "recommendedTrimDb") val recommendedTrimDb: Float, // Gain trim before channel strip
    @Json(name = "targetFaderLevelDb") val targetFaderLevelDb: Float, // Fader level
    @Json(name = "headroomDb") val headroomDb: Float, // Remaining headroom
    @Json(name = "compressionThresholdDb") val compressionThresholdDb: Float, // Suggested threshold
    @Json(name = "compressionRatio") val compressionRatio: Float, // e.g. 3.0f (representing 3:1 ratio)
    @Json(name = "makeupGainDb") val makeupGainDb: Float // Suggested make-up gain
)

@JsonClass(generateAdapter = true)
data class EQBandRecommendation(
    @Json(name = "bandName") val bandName: String, // "LowShelf", "LowMid", "HighMid", "HighShelf"
    @Json(name = "frequencyHz") val frequencyHz: Float, // Target frequency
    @Json(name = "gainDb") val gainDb: Float, // Boost/Cut amount in dB
    @Json(name = "qFactor") val qFactor: Float, // Bandwidth Q factor
    @Json(name = "purpose") val purpose: String // e.g. "Mud cleanup", "Vocal air", "Bass weight"
)

@JsonClass(generateAdapter = true)
data class EQIntelligence(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "bands") val bands: List<EQBandRecommendation> = emptyList(),
    @Json(name = "lowCutHz") val lowCutHz: Float, // High pass filter frequency
    @Json(name = "highCutHz") val highCutHz: Float, // Low pass filter frequency
    @Json(name = "explainableReason") val explainableReason: String
)

@JsonClass(generateAdapter = true)
data class SpatialMixing(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "pan") val pan: Float, // -1.0 (hard left) to 1.0 (hard right), 0.0 is center
    @Json(name = "stereoSpread") val stereoSpread: Float, // 0.0 to 100.0%
    @Json(name = "reverbSendDb") val reverbSendDb: Float, // Reverb aux send level
    @Json(name = "delaySendDb") val delaySendDb: Float // Delay aux send level
)

@JsonClass(generateAdapter = true)
data class CompressorIntelligence(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "thresholdDb") val thresholdDb: Float,
    @Json(name = "ratio") val ratio: Float,
    @Json(name = "attackMs") val attackMs: Float,
    @Json(name = "releaseMs") val releaseMs: Float,
    @Json(name = "kneeDb") val kneeDb: Float,
    @Json(name = "makeupGainDb") val makeupGainDb: Float,
    @Json(name = "lookaheadMs") val lookaheadMs: Float,
    @Json(name = "sidechainSource") val sidechainSource: String = "None",
    @Json(name = "isParallelEnabled") val isParallelEnabled: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DeEsserEngine(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "frequencyHz") val frequencyHz: Float,
    @Json(name = "thresholdDb") val thresholdDb: Float,
    @Json(name = "reductionDb") val reductionDb: Float,
    @Json(name = "detectedSibilants") val detectedSibilants: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NoiseIntelligence(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "humLevelDb") val humLevelDb: Float,
    @Json(name = "hissLevelDb") val hissLevelDb: Float,
    @Json(name = "roomNoiseDb") val roomNoiseDb: Float,
    @Json(name = "detectedNoises") val detectedNoises: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PhaseIntelligence(
    @Json(name = "correlation") val correlation: Float, // -1.0 to 1.0
    @Json(name = "monoCompatibility") val monoCompatibility: Float, // 0 to 100%
    @Json(name = "isStereoCollapseRisk") val isStereoCollapseRisk: Boolean,
    @Json(name = "warningMsg") val warningMsg: String? = null
)

@JsonClass(generateAdapter = true)
data class FrequencyConflict(
    @Json(name = "trackIdA") val trackIdA: String,
    @Json(name = "trackIdB") val trackIdB: String,
    @Json(name = "clashingFreqHz") val clashingFreqHz: Float,
    @Json(name = "maskingSeverity") val maskingSeverity: Float, // 0 to 100
    @Json(name = "suggestion") val suggestion: String
)

@JsonClass(generateAdapter = true)
data class LoudnessIntelligence(
    @Json(name = "integratedLufs") val integratedLufs: Float,
    @Json(name = "rmsDb") val rmsDb: Float,
    @Json(name = "peakDb") val peakDb: Float,
    @Json(name = "truePeakDb") val truePeakDb: Float,
    @Json(name = "dynamicRangeDb") val dynamicRangeDb: Float,
    @Json(name = "streamingReadiness") val streamingReadiness: String // e.g. "YouTube Compliant, Spotify Standard"
)

@JsonClass(generateAdapter = true)
data class MixingBlueprint(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "targetLoudnessLufs") val targetLoudnessLufs: Float, // e.g. -14.0f LUFS
    @Json(name = "genreStyle") val genreStyle: String, // "Bollywood", "Classical", "Pop", "EDM", "Ghazal"
    @Json(name = "gainStagingMap") val gainStagingMap: Map<String, GainStaging> = emptyMap(),
    @Json(name = "eqIntelligenceMap") val eqIntelligenceMap: Map<String, EQIntelligence> = emptyMap(),
    @Json(name = "spatialMixingMap") val spatialMixingMap: Map<String, SpatialMixing> = emptyMap(),
    @Json(name = "masterLimiterCeilingDb") val masterLimiterCeilingDb: Float = -1.0f,
    @Json(name = "masterLimiterThresholdDb") val masterLimiterThresholdDb: Float = -4.0f
)

@JsonClass(generateAdapter = true)
data class ReferenceMixComparison(
    @Json(name = "referenceName") val referenceName: String,
    @Json(name = "loudnessMatchDiffDb") val loudnessMatchDiffDb: Float,
    @Json(name = "spectralMatchPercentage") val spectralMatchPercentage: Float, // 0 to 100
    @Json(name = "dynamicMatchPercentage") val dynamicMatchPercentage: Float, // 0 to 100
    @Json(name = "recommends") val recommends: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MixingValidation(
    @Json(name = "isClippingRisk") val isClippingRisk: Boolean,
    @Json(name = "phaseIssuesDetected") val phaseIssuesDetected: Boolean,
    @Json(name = "warnings") val warnings: List<String> = emptyList(),
    @Json(name = "suggestedFix") val suggestedFix: String? = null
)

@JsonClass(generateAdapter = true)
data class MixingSynthesisResult(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "blueprint") val blueprint: MixingBlueprint,
    @Json(name = "trackAnalyses") val trackAnalyses: List<TrackAnalysis> = emptyList(),
    @Json(name = "referenceComparison") val referenceComparison: ReferenceMixComparison,
    @Json(name = "validation") val validation: MixingValidation,
    @Json(name = "explainableMixReport") val explainableMixReport: String,
    @Json(name = "compressors") val compressors: List<CompressorIntelligence> = emptyList(),
    @Json(name = "deEsserReport") val deEsserReport: List<DeEsserEngine> = emptyList(),
    @Json(name = "noiseReport") val noiseReport: List<NoiseIntelligence> = emptyList(),
    @Json(name = "phaseReport") val phaseReport: PhaseIntelligence? = null,
    @Json(name = "frequencyConflicts") val frequencyConflicts: List<FrequencyConflict> = emptyList(),
    @Json(name = "loudness") val loudness: LoudnessIntelligence? = null
)
