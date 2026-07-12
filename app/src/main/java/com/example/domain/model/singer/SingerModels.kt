package com.example.domain.model.singer

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhonemeSegment(
    @Json(name = "text") val text: String,             // Original lyric segment (e.g., "Ma")
    @Json(name = "ipaPhoneme") val ipaPhoneme: String,  // International Phonetic Alphabet representation (e.g., "maː")
    @Json(name = "startTimeMs") val startTimeMs: Long,
    @Json(name = "durationMs") val durationMs: Long,
    @Json(name = "pitch") val pitch: Int,               // MIDI Pitch value
    @Json(name = "vowelSustain") val vowelSustain: Float // Ratio of duration spent sustaining the vowel vs. consonant transition
)

@JsonClass(generateAdapter = true)
data class PhonemeSequence(
    @Json(name = "language") val language: String,      // "Hindi", "Odia", "English", "Sanskrit"
    @Json(name = "segments") val segments: List<PhonemeSegment> = emptyList()
)

@JsonClass(generateAdapter = true)
data class VocalOrnamentation(
    @Json(name = "timeMs") val timeMs: Long,
    @Json(name = "type") val type: String,               // "Meend" (Slide), "Murki" (Grace notes), "Gamak" (Oscillation), "KanSwar" (Touch note), "Andolan" (Sustained wave), "Khatka"
    @Json(name = "intensity") val intensity: Float,     // 0f to 1f
    @Json(name = "pitchRange") val pitchRange: Float,   // Frequency deviation range in semitones (e.g. 2.0f)
    @Json(name = "durationMs") val durationMs: Int
)

@JsonClass(generateAdapter = true)
data class BreathMarker(
    @Json(name = "timeMs") val timeMs: Long,
    @Json(name = "durationMs") val durationMs: Int,
    @Json(name = "breathIntensity") val breathIntensity: Float, // 0f to 1f (quiet to deep inhalation)
    @Json(name = "lungVolumeLeft") val lungVolumeLeft: Float     // Percentage of lung capacity left before the breath
)

@JsonClass(generateAdapter = true)
data class VocalPhrase(
    @Json(name = "phraseId") val phraseId: String,
    @Json(name = "text") val text: String,
    @Json(name = "startTimeMs") val startTimeMs: Long,
    @Json(name = "durationMs") val durationMs: Long,
    @Json(name = "phonemes") val phonemes: List<PhonemeSegment> = emptyList(),
    @Json(name = "ornaments") val ornaments: List<VocalOrnamentation> = emptyList(),
    @Json(name = "breaths") val breaths: List<BreathMarker> = emptyList(),
    @Json(name = "vocalRegister") val vocalRegister: String // "Chest", "Head", "Falsetto", "Mixed"
)

@JsonClass(generateAdapter = true)
data class VoiceIdentity(
    @Json(name = "voiceId") val voiceId: String,
    @Json(name = "name") val name: String,
    @Json(name = "gender") val gender: String,          // "Male", "Female", "Kids", "Custom"
    @Json(name = "description") val description: String,
    @Json(name = "nativeLanguage") val nativeLanguage: String,
    @Json(name = "minPitch") val minPitch: Int,         // MIDI floor limit
    @Json(name = "maxPitch") val maxPitch: Int,         // MIDI ceiling limit
    @Json(name = "supportedEmotions") val supportedEmotions: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SingerConfiguration(
    @Json(name = "voiceId") val voiceId: String,
    @Json(name = "style") val style: String = "Traditional", // "Bollywood", "Classical", "Rap", "Devotional"
    @Json(name = "pitchOffset") val pitchOffset: Int = 0,    // Semitones
    @Json(name = "emotion") val emotion: String = "Romantic", // "Romantic", "Sad", "Devotional", "Happy", "Energetic"
    @Json(name = "vibratoDepth") val vibratoDepth: Float = 50f, // 0 to 100
    @Json(name = "breathiness") val breathiness: Float = 20f,   // 0 to 100
    @Json(name = "power") val power: Float = 75f,               // 0 to 100
    @Json(name = "softness") val softness: Float = 30f,         // 0 to 100
    @Json(name = "pronunciation") val pronunciation: String = "Standard Hindi"
)

@JsonClass(generateAdapter = true)
data class VocalSynthesisValidation(
    @Json(name = "isValid") val isValid: Boolean,
    @Json(name = "limitingFactors") val limitingFactors: List<String> = emptyList(),
    @Json(name = "warnings") val warnings: List<String> = emptyList(),
    @Json(name = "suggestedFix") val suggestedFix: String? = null
)

@JsonClass(generateAdapter = true)
data class VocalSynthesisResult(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "voiceIdentity") val voiceIdentity: VoiceIdentity,
    @Json(name = "config") val config: SingerConfiguration,
    @Json(name = "phrases") val phrases: List<VocalPhrase> = emptyList(),
    @Json(name = "validation") val validation: VocalSynthesisValidation,
    @Json(name = "offlineFallbackStatus") val offlineFallbackStatus: String, // "Offline Ready", "API Key Required", "Cloud Only"
    @Json(name = "summaryAuditReport") val summaryAuditReport: String
)
