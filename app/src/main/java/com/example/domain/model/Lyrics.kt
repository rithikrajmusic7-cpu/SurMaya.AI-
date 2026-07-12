package com.example.domain.model

data class Lyrics(
    val id: String,
    val title: String,
    val prompt: String,
    val content: String,
    val language: String,
    val createdTimestamp: Long
)
