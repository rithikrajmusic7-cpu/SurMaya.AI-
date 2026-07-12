package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "melody_projects")
data class MelodyProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val lyrics: String,
    val chords: String,
    val prompt: String,
    val emotion: String,
    val genre: String,
    val mood: String,
    val scale: String,
    val raga: String,
    val tempo: Int,
    val vocalStyle: String,
    val sectionType: String,
    val currentMelodyJson: String? // Serialized GeneratedMelodyPlan JSON
)
