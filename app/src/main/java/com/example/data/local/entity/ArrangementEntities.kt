package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "arrangement_projects")
data class ArrangementProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val lyricsProjectId: String?,
    val melodyProjectId: String?,
    val chordProjectId: String?,
    val lyrics: String,
    val prompt: String,
    val genre: String,
    val mood: String,
    val emotion: String,
    val bpm: Int,
    val key: String,
    val scale: String,
    val raga: String,
    val songDurationSeconds: Int,
    val singerType: String,
    val language: String,
    val targetAudience: String,
    val songStructureType: String, // e.g., "Bollywood", "Indian Pop", "EDM", etc.
    val fullArrangementJson: String? // Serialized complete JSON blueprint
)

@Entity(tableName = "arrangement_sections")
data class ArrangementSectionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sectionName: String, // e.g., Intro, Verse, Pre-Chorus, Chorus, Bridge, Outro
    val durationSeconds: Int,
    val bars: Int,
    val energyLevel: Int, // 1 - 10
    val instrumentsJson: String, // List of active instruments
    val melodyUsage: String,
    val harmonyUsage: String,
    val rhythmPattern: String,
    val dynamics: String,
    val automation: String,
    val fx: String,
    val transitions: String,
    val mood: String,
    val intensity: String,
    val sequenceIndex: Int // Ordered visual timeline index
)

@Entity(tableName = "instrument_tracks")
data class InstrumentTrackEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val instrumentName: String, // e.g., Tabla, Mridangam, Sitar, Flute, Piano, Strings
    val trackColorHex: String,
    val isMuted: Boolean = false,
    val isSoloed: Boolean = false,
    val isLocked: Boolean = false,
    val rhythmPatternJson: String? = null,
    val notesJson: String? = null
)

@Entity(tableName = "automation_lanes")
data class AutomationLaneEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val trackId: String?, // Null indicates global master automation
    val parameterName: String, // Volume, Filter Cutoff, Reverb Send
    val pointsJson: String // List of points (time, value) serialized
)

@Entity(tableName = "arrangement_transitions")
data class ArrangementTransitionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val fromSectionId: String,
    val toSectionId: String,
    val transitionType: String, // Snare Roll, Cymbal Swell, Sound Effect, Riser
    val bars: Float,
    val fxUsage: String
)

@Entity(tableName = "counter_melodies")
data class CounterMelodyEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sectionId: String,
    val instrumentName: String,
    val notesJson: String
)

@Entity(tableName = "arrangement_history")
data class ArrangementHistoryEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val timestamp: Long,
    val description: String,
    val arrangementStateJson: String
)

@Entity(tableName = "arrangement_templates")
data class ArrangementTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val genre: String,
    val structureType: String,
    val sectionsJson: String // List of sections inside this template
)

@Entity(tableName = "arrangement_evaluations")
data class ArrangementEvaluationEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val overallQualityScore: Int,
    val energyFlowScore: Int,
    val sectionBalanceScore: Int,
    val instrumentBalanceScore: Int,
    val genreMatchScore: Int,
    val emotionMatchScore: Int,
    val transitionQualityScore: Int,
    val professionalScore: Int,
    val humanLikenessScore: Int,
    val commercialReadinessScore: Int,
    val detailedFeedbackJson: String
)

@Entity(tableName = "arrangement_cache")
data class ArrangementCacheEntity(
    @PrimaryKey val id: String,
    val promptHash: String,
    val resultJson: String,
    val timestamp: Long
)
