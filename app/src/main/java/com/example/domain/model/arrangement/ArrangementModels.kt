package com.example.domain.model.arrangement

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArrangementProject(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "createdTimestamp") val createdTimestamp: Long,
    @Json(name = "updatedTimestamp") val updatedTimestamp: Long,
    @Json(name = "lyricsProjectId") val lyricsProjectId: String?,
    @Json(name = "melodyProjectId") val melodyProjectId: String?,
    @Json(name = "chordProjectId") val chordProjectId: String?,
    @Json(name = "lyrics") val lyrics: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "genre") val genre: String,
    @Json(name = "mood") val mood: String,
    @Json(name = "emotion") val emotion: String,
    @Json(name = "bpm") val bpm: Int,
    @Json(name = "key") val key: String,
    @Json(name = "scale") val scale: String,
    @Json(name = "raga") val raga: String,
    @Json(name = "songDurationSeconds") val songDurationSeconds: Int,
    @Json(name = "singerType") val singerType: String,
    @Json(name = "language") val language: String,
    @Json(name = "targetAudience") val targetAudience: String,
    @Json(name = "songStructureType") val songStructureType: String,
    @Json(name = "sections") val sections: List<ArrangementSection> = emptyList(),
    @Json(name = "tracks") val tracks: List<InstrumentTrack> = emptyList(),
    @Json(name = "masterAutomation") val masterAutomation: List<AutomationLane> = emptyList(),
    @Json(name = "transitions") val transitions: List<ArrangementTransition> = emptyList(),
    @Json(name = "counterMelodies") val counterMelodies: List<CounterMelody> = emptyList(),
    @Json(name = "evaluation") val evaluation: ArrangementEvaluation? = null
)

@JsonClass(generateAdapter = true)
data class ArrangementSection(
    @Json(name = "id") val id: String,
    @Json(name = "projectId") val projectId: String,
    @Json(name = "sectionName") val sectionName: String,
    @Json(name = "durationSeconds") val durationSeconds: Int,
    @Json(name = "bars") val bars: Int,
    @Json(name = "energyLevel") val energyLevel: Int,
    @Json(name = "instruments") val instruments: List<String>,
    @Json(name = "melodyUsage") val melodyUsage: String,
    @Json(name = "harmonyUsage") val harmonyUsage: String,
    @Json(name = "rhythmPattern") val rhythmPattern: String,
    @Json(name = "dynamics") val dynamics: String,
    @Json(name = "automation") val automation: String,
    @Json(name = "fx") val fx: String,
    @Json(name = "transitions") val transitions: String,
    @Json(name = "mood") val mood: String,
    @Json(name = "intensity") val intensity: String,
    @Json(name = "sequenceIndex") val sequenceIndex: Int
)

@JsonClass(generateAdapter = true)
data class InstrumentTrack(
    @Json(name = "id") val id: String,
    @Json(name = "projectId") val projectId: String,
    @Json(name = "instrumentName") val instrumentName: String,
    @Json(name = "trackColorHex") val trackColorHex: String,
    @Json(name = "isMuted") val isMuted: Boolean = false,
    @Json(name = "isSoloed") val isSoloed: Boolean = false,
    @Json(name = "isLocked") val isLocked: Boolean = false,
    @Json(name = "rhythmPattern") val rhythmPattern: String = "Standard 4/4 Pattern",
    @Json(name = "notes") val notes: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AutomationPoint(
    @Json(name = "timeSeconds") val timeSeconds: Float,
    @Json(name = "value") val value: Float
)

@JsonClass(generateAdapter = true)
data class AutomationLane(
    @Json(name = "id") val id: String,
    @Json(name = "projectId") val projectId: String,
    @Json(name = "trackId") val trackId: String?,
    @Json(name = "parameterName") val parameterName: String,
    @Json(name = "points") val points: List<AutomationPoint> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArrangementTransition(
    @Json(name = "id") val id: String,
    @Json(name = "projectId") val projectId: String,
    @Json(name = "fromSectionId") val fromSectionId: String,
    @Json(name = "toSectionId") val toSectionId: String,
    @Json(name = "transitionType") val transitionType: String,
    @Json(name = "bars") val bars: Float,
    @Json(name = "fxUsage") val fxUsage: String
)

@JsonClass(generateAdapter = true)
data class CounterMelody(
    @Json(name = "id") val id: String,
    @Json(name = "projectId") val projectId: String,
    @Json(name = "sectionId") val sectionId: String,
    @Json(name = "instrumentName") val instrumentName: String,
    @Json(name = "notes") val notes: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArrangementHistory(
    @Json(name = "id") val id: String,
    @Json(name = "projectId") val projectId: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "description") val description: String,
    @Json(name = "stateJson") val stateJson: String
)

@JsonClass(generateAdapter = true)
data class ArrangementTemplate(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String,
    @Json(name = "genre") val genre: String,
    @Json(name = "structureType") val structureType: String,
    @Json(name = "sections") val sections: List<ArrangementSection> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArrangementEvaluation(
    @Json(name = "id") val id: String,
    @Json(name = "projectId") val projectId: String,
    @Json(name = "overallQualityScore") val overallQualityScore: Int,
    @Json(name = "energyFlowScore") val energyFlowScore: Int,
    @Json(name = "sectionBalanceScore") val sectionBalanceScore: Int,
    @Json(name = "instrumentBalanceScore") val instrumentBalanceScore: Int,
    @Json(name = "genreMatchScore") val genreMatchScore: Int,
    @Json(name = "emotionMatchScore") val emotionMatchScore: Int,
    @Json(name = "transitionQualityScore") val transitionQualityScore: Int,
    @Json(name = "professionalScore") val professionalScore: Int,
    @Json(name = "humanLikenessScore") val humanLikenessScore: Int,
    @Json(name = "commercialReadinessScore") val commercialReadinessScore: Int,
    @Json(name = "detailedFeedback") val detailedFeedback: String
)
