package com.example.domain.repository

import java.io.File

interface MusicGenerationRepository {
    suspend fun generateMusic(prompt: String, durationSec: Int): Result<File>
    suspend fun generateVoiceSample(prompt: String, voiceName: String): Result<File>
    suspend fun streamMusic(prompt: String, onChunkReceived: (ByteArray) -> Unit): Result<Unit>
}
