package com.example.domain.repository

import com.example.domain.model.singer.SingerConfiguration
import com.example.domain.model.singer.VoiceIdentity
import com.example.domain.model.singer.VocalSynthesisResult
import kotlinx.coroutines.flow.Flow

interface SingerRepository {
    fun getAvailableVoices(): Flow<List<VoiceIdentity>>
    suspend fun getVoiceById(voiceId: String): VoiceIdentity?
    
    suspend fun synthesizeVocalPerformance(
        projectId: String,
        lyrics: String,
        config: SingerConfiguration,
        tempo: Float,
        keyMidi: Int
    ): Result<VocalSynthesisResult>
}
