package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mastering_projects")
data class MasteringProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val genreStyle: String,
    val targetLoudnessLufs: Float,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val selectedPlatformsJson: String, // Serialized List<String>
    val lastSynthesisResultJson: String? // Serialized MasteringResult
)

@Entity(tableName = "mastering_history")
data class MasteringHistoryEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val timestamp: Long,
    val masteredLufs: Float,
    val masteredTruePeak: Float,
    val format: String,
    val ditherSettingsJson: String,
    val isReleased: Boolean
)

@Entity(tableName = "mastering_presets")
data class MasteringPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val genreStyle: String,
    val targetLoudness: Float,
    val description: String,
    val multibandSettingsJson: String,
    val limiterSettingsJson: String
)
