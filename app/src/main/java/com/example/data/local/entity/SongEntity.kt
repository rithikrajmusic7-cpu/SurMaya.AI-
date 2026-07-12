package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val prompt: String,
    val lyrics: String,
    val language: String,
    val genre: String,
    val mood: String,
    val style: String,
    val tempo: String,
    val duration: String,
    val singerVoice: String,
    val audioUrl: String?,
    val projectId: String?,
    val isFavorite: Boolean = false,
    val isDraft: Boolean = false,
    val isDownloaded: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
