package com.example.data.remote.gateway.lyrics

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LyricProject(
    val id: String,
    val title: String,
    val currentLyrics: String,
    val language: String,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val versions: List<LyricVersion> = emptyList(),
    val chatHistory: List<LyricChatMessage> = emptyList(),
    val lastAutoSaved: Long = System.currentTimeMillis(),
    val prompt: String = "",
    val genre: String = "Bollywood",
    val mood: String = "Romantic",
    val story: String = "",
    val songStructure: String = "",
    val artistStyle: String = ""
)

@JsonClass(generateAdapter = true)
data class LyricVersion(
    val id: String,
    val label: String, // e.g., "v1 - Initial", "v2 - More Emotional"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val author: String = "AI", // "AI" or "User"
    val versionNumber: Int = 1,
    val editSummary: String = "",
    val selectedSection: String? = null,
    val language: String = "Hindi",
    val genre: String = "Bollywood",
    val mood: String = "Romantic"
)

@JsonClass(generateAdapter = true)
data class LyricChatMessage(
    val id: String,
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val selectedTextSegment: String? = null
)

@JsonClass(generateAdapter = true)
data class LyricsGenerationParams(
    val prompt: String,
    val genres: List<String> = emptyList(),
    val moods: List<String> = emptyList(),
    val targetAudience: String = "",
    val rhymeScheme: String = "",
    val artistStyle: String = "",
    val additionalNotes: String = ""
)

@JsonClass(generateAdapter = true)
data class QualityScoreReport(
    val originalityScore: Float, // 0 to 100
    val singabilityScore: Float,
    val rhymeScore: Float,
    val emotionScore: Float,
    val commercialAppealScore: Float,
    val storytellingScore: Float,
    val structureScore: Float,
    val readabilityIndex: Float,
    val issuesAndSuggestions: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class SmartSuggestions(
    val suggestedTitles: List<String> = emptyList(),
    val potentialHooks: List<String> = emptyList(),
    val alternativeRhymes: List<Map<String, String>> = emptyList(), // e.g., "dil" -> "mil, jhil, khil"
    val harmonyTips: List<String> = emptyList(),
    val callAndResponseLines: List<String> = emptyList()
)
