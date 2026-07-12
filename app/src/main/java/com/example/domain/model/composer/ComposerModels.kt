package com.example.domain.model.composer

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MasterCompositionPlan(
    val title: String,
    val genre: String,
    val mood: String,
    val storySummary: String,
    val musicalTheme: String,
    val tempoBpm: Int,
    val timeSignature: String,
    val suggestedScale: String,
    val suggestedKey: String,
    val suggestedTaal: String = "", // e.g. Keharwa, Dadra, Teental (Indian Classical/Folk)
    val vocalBlueprint: VocalBlueprint,
    val instrumentPalette: List<String>,
    val songStructure: List<SectionPlan>,
    val melodyGuidance: MelodyGuidance,
    val chordGuidance: ChordGuidance,
    val rhythmPlan: RhythmPlan,
    val arrangementBlueprint: ArrangementBlueprint,
    val mixingGuidance: MixingGuidance,
    val masteringGuidance: MasteringGuidance,
    val diagnostics: CompositionDiagnostics,
    
    // AI Composer Intelligence v2.0 Additions
    val detectedLyricalTheme: String = "",
    val detectedStoryArc: String = "",
    val detectedEraStyle: String = "",
    val tempoReason: String = "",
    val keyScaleReason: String = "",
    val vocalStyleReason: String = "",
    val instrumentsReason: String = "",
    val taalReason: String = "",
    val melodyReason: String = "",
    val chordReason: String = "",
    val arrangementReason: String = "",
    val directorModeResponse: String = ""
)

@JsonClass(generateAdapter = true)
data class SectionPlan(
    val sectionName: String, // Intro, Verse, Pre-Chorus, Chorus, Post-Chorus, Bridge, Outro, etc.
    val durationSec: Int,
    val energyLevel: Float, // 0.0 to 1.0 (for Energy Curve)
    val instrumentUsage: List<String>,
    val vocalDynamics: String, // e.g. "Whispering, building up", "Full power high belt"
    val transitionNote: String // Notes on how to transition into this section
)

@JsonClass(generateAdapter = true)
data class VocalBlueprint(
    val suggestedStyle: String, // e.g. Classical, Soft Romantic, Heroic, Folk, Ghazal, Duet
    val voiceType: String, // e.g. Male, Female, Duet, Choir, Rap
    val pitchOffset: Float = 0f,
    val sectionWiseDynamics: List<String> = emptyList(),
    val rangeRequired: String = "Medium" // Low, Medium, High, Extreme
)

@JsonClass(generateAdapter = true)
data class MelodyGuidance(
    val phraseDirection: String, // e.g. Ascending, Descending, Arch, Wave
    val range: String, // octave or vocal range description
    val contour: String, // e.g. Smooth, Jagged, Statuesque
    val complexity: String, // Low, Medium, High
    val hookStrategy: String, // How the catchphrase or title should be delivered
    val motifSuggestions: List<String> = emptyList(), // Pitch contour suggestions e.g. "S R G P D S'"
    val emotionalRise: String = "",
    val breathingPoints: String = ""
)

@JsonClass(generateAdapter = true)
data class ChordGuidance(
    val harmonyStrategy: String, // e.g. Modal, Diatonic, Jazz-influenced
    val cadenceSuggestions: List<String>, // e.g. "Plagal Cadence (IV-I) at verse end", "Perfect Authentic (V-I) on hook"
    val chordMood: String, // e.g. Bright, Somber, Suspended, Tense
    val tensionLevel: Float, // 0.0 to 1.0
    val resolutionPlan: String, // Directions for resolving harmonic tension
    val emotionalProgression: String = ""
)

@JsonClass(generateAdapter = true)
data class RhythmPlan(
    val grooveProfile: String, // e.g. Straight 16ths, Shuffled, Syncopated Bhangra
    val rhythmicComplexity: String, // Low, Medium, High
    val accentPattern: String, // e.g. "Downbeats 1 and 3 accented heavily"
    val sectionRhythmStrategy: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArrangementBlueprint(
    val layeringPlan: String, // Description of how instruments layer
    val buildUps: List<String> = emptyList(),
    val drops: List<String> = emptyList(),
    val transitions: List<String> = emptyList(),
    val sectionDensity: String, // e.g. "Sparse intro, massive dense chorus"
    val endingStrategy: String // Fade-out, Sudden Stinger, Orchestral Sustained chord, etc.
)

@JsonClass(generateAdapter = true)
data class MixingGuidance(
    val stereoWidth: String, // Narrow, Wide, Extreme
    val reverbStyle: String, // Hall, Plate, Spring, Chamber, Cathedral
    val delayStyle: String, // Ping-Pong, Slapback, Mono Tap
    val compressionCharacter: String, // Punchy, Optical, Warm Vintage, Invisible
    val dynamics: String, // highly dynamic vs heavily compressed
    val atmosphere: String // Ambient Space, Intimate dry cabin, Cosmic Cinematic
)

@JsonClass(generateAdapter = true)
data class MasteringGuidance(
    val targetLoudnessStrategy: String, // e.g. -14 LUFS for Streaming, -9 LUFS for Club Play
    val dynamicCharacter: String, // Transparent, Punchy, Warm Tube
    val streamingOptimizationNotes: String,
    val commercialReleaseNotes: String
)

@JsonClass(generateAdapter = true)
data class CompositionDiagnostics(
    val compositionQualityScore: Int, // 0 to 100
    val commercialAppeal: Int, // 0 to 100
    val cinematicScore: Int, // 0 to 100
    val originalityScore: Int, // 0 to 100
    val genreConfidence: Int, // 0 to 100
    val emotionConsistency: Int, // 0 to 100
    val singabilityCompatibility: Int, // 0 to 100
    val melodyReadiness: Int, // 0 to 100
    val arrangementReadiness: Int, // 0 to 100
    val warnings: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

data class ComposerProject(
    val id: String,
    val title: String,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val lyrics: String,
    val language: String,
    val genre: String,
    val mood: String,
    val filmSituation: String = "", // AI Director Context
    val era: String = "", // AI Director Context
    val productionScale: String = "", // AI Director Context
    val emotionalJourney: String = "", // AI Director Context
    val instrumentPreferences: String = "",
    val userNotes: String = "",
    val currentPlan: MasterCompositionPlan? = null
)

data class CompositionVersion(
    val id: String,
    val projectId: String,
    val versionNumber: Int,
    val label: String,
    val timestamp: Long,
    val plan: MasterCompositionPlan,
    val lyrics: String,
    val isFavorite: Boolean = false,
    val editSummary: String = ""
)
