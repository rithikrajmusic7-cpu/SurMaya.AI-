package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "composer_projects")
data class ComposerProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdTimestamp: Long,
    val updatedTimestamp: Long,
    val lyrics: String,
    val language: String,
    val genre: String,
    val mood: String,
    val filmSituation: String,
    val era: String,
    val productionScale: String,
    val emotionalJourney: String,
    val instrumentPreferences: String,
    val userNotes: String,
    val currentPlanJson: String? // Serialized MasterCompositionPlan JSON
)

@Entity(tableName = "composition_versions")
data class CompositionVersionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val versionNumber: Int,
    val label: String,
    val timestamp: Long,
    val planJson: String, // Serialized MasterCompositionPlan JSON
    val lyrics: String,
    val isFavorite: Boolean,
    val editSummary: String
)
