package com.example.domain.model.melody

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MelodyProject(
    val id: String,
    val title: String,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val lyrics: String = "",
    val chords: String = "",
    val prompt: String = "",
    val emotion: String = "Expressive",
    val genre: String = "Bollywood",
    val mood: String = "Romantic",
    val scale: String = "C Major",
    val raga: String = "Yaman",
    val tempo: Int = 90,
    val vocalStyle: String = "Duet",
    val sectionType: String = "Chorus",
    val currentMelodyJson: String? = null // Serialized GeneratedMelodyPlan JSON
)

@JsonClass(generateAdapter = true)
data class GeneratedMelodyPlan(
    val id: String,
    val title: String,
    val genre: String,
    val emotion: String,
    val raga: String,
    val scale: String,
    val bpm: Int,
    val taal: String,
    val pipelineConfidence: Float,
    val promptInsight: String,
    
    // Core Melody Engine Outputs
    val motif: Motif,
    val phrases: List<MelodyPhrase>,
    val variations: List<MelodyVariation>,
    val evaluation: MelodyEvaluation,
    
    // Indian Music Aesthetics
    val indianAesthetics: IndianAestheticDetails,
    
    // Export Layer
    val midiExportReady: Boolean = true,
    val musicXmlExportReady: Boolean = true,
    val wavGuideUrl: String = "",
    val noteSequence: List<PitchNote> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Motif(
    val id: String,
    val sargamContour: String, // e.g. "S R G P M G R"
    val durationBeats: Float,
    val hookStrength: Float, // 0.0 - 1.0
    val complexity: String, // Low, Medium, High
    val emotionalImpact: String
)

@JsonClass(generateAdapter = true)
data class MelodyPhrase(
    val id: String,
    val section: String, // Intro, Verse, Chorus, Bridge, Outro, Hook, Instrumental
    val sargamNotes: String, // e.g. "Pa Dha Ni Sa' Re' Sa'"
    val lyricAlignment: String,
    val dynamicRange: String, // Pianissimo to Fortissimo
    val breathingPoints: List<Float>, // Time markers in seconds
    val syncopationStrength: Float, // 0.0 - 1.0
    val noteSequence: List<PitchNote>
)

@JsonClass(generateAdapter = true)
data class PitchNote(
    val noteName: String, // e.g., "G4", "A4", "C5"
    val pitchHz: Float,
    val sargamEquivalent: String, // e.g. "Re", "Ga", "Ma"
    val startTimeSeconds: Float,
    val durationSeconds: Float,
    val velocity: Int, // 0 - 127
    val ornamentation: String = "None" // Meend, Murki, Gamak, None
)

@JsonClass(generateAdapter = true)
data class MelodyVariation(
    val id: String,
    val variationType: String, // e.g. "Classical Gamak Ornamented", "Sufi High Pitch Alap", "Modern Lo-Fi Chill"
    val noteCount: Int,
    val syncopationShift: Float,
    val complexityDelta: Float,
    val notes: List<PitchNote>
)

@JsonClass(generateAdapter = true)
data class MelodyEvaluation(
    val singabilityScore: Float, // 0.0 - 1.0
    val originalityScore: Float, // 0.0 - 1.0
    val musicalFlowScore: Float, // 0.0 - 1.0
    val repetitionScore: Float, // 0.0 - 1.0
    val emotionMatchScore: Float, // 0.0 - 1.0
    val genreMatchScore: Float, // 0.0 - 1.0
    val pitchStabilityScore: Float, // 0.0 - 1.0
    val hookQualityScore: Float, // 0.0 - 1.0
    val phraseBalanceScore: Float, // 0.0 - 1.0
    val humanLikenessScore: Float, // 0.0 - 1.0
    val averageScore: Float,
    val recommendations: List<String>
)

@JsonClass(generateAdapter = true)
data class IndianAestheticDetails(
    val ragaGrammar: String, // Aaroh & Avroh, Vadi & Samvadi notes
    val selectedOrnamentation: List<String>, // Meend (Slides), Murki (Trills), Gamak, Kan Swar
    val pakadSargam: String, // Key catchphrase of the Raga
    val layakariPattern: String, // Rhythmic subdivision matching the Taal
    val taalCompatibility: String, // e.g., "Keharwa Taal (8 Beats)", "Dadra (6 Beats)"
    val emotionalRiseAesthetics: String, // Description of how Alap ascends emotionally
    val ornamentReason: String, // Description explaining why these specific ornamentations are used
    val sargamReason: String // Description of how the melody aligns with classical ragas
)

enum class MelodyGenerationState {
    IDLE,
    PLANNING,
    GENERATING,
    EVALUATING,
    COMPLETE,
    ERROR
}
