package com.example.data.repository

import com.example.domain.model.singer.*
import com.example.domain.repository.SingerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SingerRepositoryImpl(
    private val singerEngine: AISingerEngine = AISingerEngine()
) : SingerRepository {

    override fun getAvailableVoices(): Flow<List<VoiceIdentity>> {
        val list = listOf(
            singerEngine.getVoiceIdentity("ajit"),
            singerEngine.getVoiceIdentity("shrija"),
            singerEngine.getVoiceIdentity("pandit_g"),
            singerEngine.getVoiceIdentity("mc_shanti")
        )
        return flowOf(list)
    }

    override suspend fun getVoiceById(voiceId: String): VoiceIdentity? {
        val voice = singerEngine.getVoiceIdentity(voiceId)
        return if (voice.voiceId == voiceId) voice else null
    }

    override suspend fun synthesizeVocalPerformance(
        projectId: String,
        lyrics: String,
        config: SingerConfiguration,
        tempo: Float,
        keyMidi: Int
    ): Result<VocalSynthesisResult> {
        return try {
            val result = singerEngine.synthesizeVocals(
                projectId = projectId,
                lyrics = lyrics,
                config = config,
                tempo = tempo,
                keyMidi = keyMidi
            )
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
