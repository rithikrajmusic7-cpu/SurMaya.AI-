package com.example.domain.model.performance

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PerformanceConfiguration(
    @Json(name = "tempo") val tempo: Float = 120f,
    @Json(name = "scale") val scale: String = "Yaman",
    @Json(name = "key") val key: String = "C#",
    @Json(name = "timeSignature") val timeSignature: String = "4/4",
    @Json(name = "groove") val groove: Float = 75f,
    @Json(name = "swing") val swing: Float = 15f,
    @Json(name = "dynamics") val dynamics: Float = 80f,
    @Json(name = "humanization") val humanization: Float = 85f,
    @Json(name = "complexity") val complexity: Float = 60f,
    @Json(name = "performanceStyle") val performanceStyle: String = "Expressive Bollywood",
    @Json(name = "performanceEnergy") val performanceEnergy: Float = 70f,
    @Json(name = "velocity") val velocity: Float = 85f,
    @Json(name = "stereoWidth") val stereoWidth: Float = 75f,
    @Json(name = "expression") val expression: Float = 80f
)

@JsonClass(generateAdapter = true)
data class InstrumentCapability(
    @Json(name = "instrumentId") val instrumentId: String,
    @Json(name = "name") val name: String,
    @Json(name = "range") val range: String, // e.g. "G3 - C6"
    @Json(name = "timbre") val timbre: String, // e.g. "Warm, nasal, resonant"
    @Json(name = "sustain") val sustain: String, // e.g. "Short", "Medium", "Infinite (Bowed)"
    @Json(name = "dynamics") val dynamics: String, // e.g. "Very Wide (Pianissimo to Fortissimo)"
    @Json(name = "supportedArticulations") val supportedArticulations: List<String>
)

@JsonClass(generateAdapter = true)
data class ArticulationEvent(
    @Json(name = "timeMs") val timeMs: Long,
    @Json(name = "type") val type: String, // Legato, Staccato, Marcato, Tremolo, Pizzicato, Meend, Murki, Gamak, Slides
    @Json(name = "intensity") val intensity: Float, // 0f to 1f
    @Json(name = "durationMs") val durationMs: Int
)

@JsonClass(generateAdapter = true)
data class ExpressionEnvelope(
    @Json(name = "cc1Modulation") val cc1Modulation: List<EnvelopePoint> = emptyList(),
    @Json(name = "cc11Expression") val cc11Expression: List<EnvelopePoint> = emptyList(),
    @Json(name = "vibrato") val vibrato: List<EnvelopePoint> = emptyList(),
    @Json(name = "pitchBend") val pitchBend: List<EnvelopePoint> = emptyList()
)

@JsonClass(generateAdapter = true)
data class EnvelopePoint(
    @Json(name = "timeMs") val timeMs: Long,
    @Json(name = "value") val value: Float // 0f to 1f or normalized bend
)

@JsonClass(generateAdapter = true)
data class HumanizationOffset(
    @Json(name = "timingOffsetMs") val timingOffsetMs: Int,
    @Json(name = "velocityOffset") val velocityOffset: Int,
    @Json(name = "microPitchOffsetCents") val microPitchOffsetCents: Float,
    @Json(name = "articulationJitter") val articulationJitter: Float
)

@JsonClass(generateAdapter = true)
data class PlayabilityCheck(
    @Json(name = "isValid") val isValid: Boolean,
    @Json(name = "limitingFactor") val limitingFactor: String?, // e.g. "Physical hand stretch", "Breath capacity limit"
    @Json(name = "warnings") val warnings: List<String> = emptyList(),
    @Json(name = "suggestedFix") val suggestedFix: String? = null
)

@JsonClass(generateAdapter = true)
data class SampleMapping(
    @Json(name = "format") val format: String, // "SFZ", "SoundFont", "Kontakt", "ONNX"
    @Json(name = "presetPath") val presetPath: String,
    @Json(name = "routingTarget") val routingTarget: String,
    @Json(name = "sampleRate") val sampleRate: Int = 44100
)

@JsonClass(generateAdapter = true)
data class PerformanceTrack(
    @Json(name = "trackId") val trackId: String,
    @Json(name = "instrumentId") val instrumentId: String,
    @Json(name = "capability") val capability: InstrumentCapability,
    @Json(name = "notes") val notes: List<PerformanceNote> = emptyList(),
    @Json(name = "articulations") val articulations: List<ArticulationEvent> = emptyList(),
    @Json(name = "expressionEnvelope") val expressionEnvelope: ExpressionEnvelope = ExpressionEnvelope(),
    @Json(name = "humanizationOffset") val humanizationOffset: HumanizationOffset = HumanizationOffset(0, 0, 0f, 0f),
    @Json(name = "playability") val playability: PlayabilityCheck = PlayabilityCheck(true, null),
    @Json(name = "routing") val routing: SampleMapping
)

@JsonClass(generateAdapter = true)
data class PerformanceNote(
    @Json(name = "pitch") val pitch: Int, // MIDI Number
    @Json(name = "startTimeMs") val startTimeMs: Long,
    @Json(name = "durationMs") val durationMs: Long,
    @Json(name = "velocity") val velocity: Int, // 0-127
    @Json(name = "articulation") val articulation: String = "Sustain"
)

@JsonClass(generateAdapter = true)
data class PerformanceIntelligenceResult(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "config") val config: PerformanceConfiguration,
    @Json(name = "tracks") val tracks: List<PerformanceTrack> = emptyList(),
    @Json(name = "compositeAuditReport") val compositeAuditReport: String
)
