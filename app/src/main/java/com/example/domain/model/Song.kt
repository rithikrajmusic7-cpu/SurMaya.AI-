package com.example.domain.model

data class Song(
    val id: String,
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
    val isFavorite: Boolean,
    val isDraft: Boolean,
    val isDownloaded: Boolean,
    val createdTimestamp: Long
)
