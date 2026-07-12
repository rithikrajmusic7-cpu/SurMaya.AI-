package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mix_projects")
data class MixProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val genreStyle: String,
    val targetLoudnessLufs: Float,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val trackNamesAndTypesJson: String, // Serialized List<Pair<String, String>>
    val lastSynthesisResultJson: String? // Serialized MixingSynthesisResult
)
