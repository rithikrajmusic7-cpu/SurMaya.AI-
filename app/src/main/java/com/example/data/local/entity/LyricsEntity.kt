package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_lyrics")
data class LyricsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val prompt: String,
    val content: String,
    val language: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)
