package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chord_projects")
data class ChordProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val melodyProjectId: String?,
    val lyrics: String,
    val prompt: String,
    val genre: String,
    val emotion: String,
    val mood: String,
    val scale: String,
    val raga: String,
    val bpm: Int,
    val chordComplexity: String,
    val currentProgressionJson: String? // Serialized GeneratedChordProgression JSON
)

@Entity(tableName = "chord_history")
data class ChordHistoryEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val timestamp: Long,
    val description: String,
    val chordProgressionJson: String
)

@Entity(tableName = "chord_templates")
data class ChordTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val chordsJson: String, // List<String> serialized as JSON
    val genre: String
)
