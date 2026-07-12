package com.example.domain.model.chord

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChordProject(
    val id: String,
    val title: String,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val melodyProjectId: String? = null,
    val lyrics: String = "",
    val prompt: String = "",
    val genre: String = "Bollywood",
    val emotion: String = "Expressive",
    val mood: String = "Romantic",
    val scale: String = "C Major",
    val raga: String = "Yaman",
    val bpm: Int = 90,
    val chordComplexity: String = "Medium", // Low, Medium, High
    val currentProgressionJson: String? = null // Serialized GeneratedChordProgression JSON
)

@JsonClass(generateAdapter = true)
data class ArrangementMetadata(
    val recommendedBassMovement: String = "Root-Fifth alternation",
    val suggestedRhythmicPattern: String = "4/4 quarter note pulses",
    val instrumentEmphasis: String = "Acoustic Piano, Warm Pad",
    val dynamicIntensity: String = "Medium-Soft"
)

@JsonClass(generateAdapter = true)
data class ModulationInfo(
    val targetScale: String = "",
    val pivotChords: List<String> = emptyList(),
    val modalShiftType: String = "None",
    val ragaTransitionPath: String = "None"
)

@JsonClass(generateAdapter = true)
data class GeneratedChordProgression(
    val id: String,
    val title: String,
    val genre: String,
    val emotion: String,
    val scale: String,
    val bpm: Int,
    val pipelineConfidence: Float,
    val explanationInsight: String,
    
    // Core Chord Engine Outputs
    val chords: List<ChordSegment>,
    val harmonyProfile: HarmonyProfile,
    val evaluation: ChordEvaluation,
    
    // Export Status
    val midiExportReady: Boolean = true,
    val musicXmlExportReady: Boolean = true,
    val jsonExportReady: Boolean = true,

    // Enterprise v1.1 Advanced features
    val tokenEngineStages: List<String> = emptyList(), // Stages compiled by token engine
    val reharmonizationStyle: String = "Original",
    val modulationInfo: ModulationInfo? = null
)

@JsonClass(generateAdapter = true)
data class ChordSegment(
    val id: String,
    val chordName: String, // e.g. "Cmaj7", "Dmin9", "G7"
    val romanNumeral: String, // e.g. "Imaj7", "ii9", "V7"
    val startTimeBeats: Float,
    val durationBeats: Float,
    val midiNotes: List<Int>, // e.g. [60, 64, 67, 71] for Cmaj7
    val pitchHz: List<Float>, // Frequencies for DSP synthesis
    val noteNames: List<String>, // e.g. ["C4", "E4", "G4", "B4"]
    val guitarFingering: String = "", // e.g. "x32000" or similar
    val pianoKeys: List<Int> = emptyList(), // Index on keyboard: 0-23
    val functionType: String = "Tonic", // Tonic, Subdominant, Dominant, Modal, Drone
    val ornamentation: String = "None", // Arpeggio, Dynamic Accent, None

    // Voice Leading & Functional details
    val voiceLeadingNotes: List<String> = emptyList(), // Soprano, Alto, Tenor, Bass
    val bassMovementNote: String = "",
    val chordFunction: String = ""
)

@JsonClass(generateAdapter = true)
data class HarmonyProfile(
    val id: String,
    val name: String, // "Extended Jazz Harmony", "Indian Drone Harmony", "Neo Soul Harmony", "Film Score"
    val voiceLeadingType: String, // e.g. "Smooth Voicings", "Open Position", "Standard Root Position"
    val modalInterchangeEnabled: Boolean = false,
    val secondaryDominantsEnabled: Boolean = false,
    val droneSwaras: List<String> = emptyList(), // e.g. ["Sa", "Pa"] for drone
    val description: String = "",
    val arrangementMetadata: ArrangementMetadata? = null
)

@JsonClass(generateAdapter = true)
data class ChordEvaluation(
    val harmonyQualityScore: Float, // 0.0 - 1.0
    val melodyCompatibilityScore: Float, // 0.0 - 1.0
    val voiceLeadingScore: Float, // 0.0 - 1.0
    val cadenceStrengthScore: Float, // 0.0 - 1.0
    val genreMatchScore: Float, // 0.0 - 1.0
    val emotionMatchScore: Float, // 0.0 - 1.0
    val originalityScore: Float, // 0.0 - 1.0
    val humanLikenessScore: Float, // 0.0 - 1.0
    val averageScore: Float,
    val criticalValidationCheck: String = "PASSED", // PASSED, WARNING, FAILED
    val pitchCollisionsDetected: Boolean = false,
    val recommendations: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChordTemplate(
    val id: String,
    val name: String, // e.g. "Pop Standard", "Jazz ii-V-I", "Hindustani Yaman Drone", "Epic Cinematic"
    val description: String,
    val chords: List<String>, // List of chord names or Roman numerals
    val genre: String
)

@JsonClass(generateAdapter = true)
data class ChordHistory(
    val id: String,
    val projectId: String,
    val timestamp: Long,
    val description: String,
    val chordProgressionJson: String
)

enum class ChordGenerationState {
    IDLE,
    PLANNING,
    GENERATING,
    EVALUATING,
    COMPLETE,
    ERROR
}
