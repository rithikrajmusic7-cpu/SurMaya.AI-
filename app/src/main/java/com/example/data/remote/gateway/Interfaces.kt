package com.example.data.remote.gateway

import java.io.File
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Core Unified Provider Interfaces
// ==========================================

interface LyricsProvider {
    suspend fun generateLyrics(prompt: String, genre: String, mood: String): Result<LyricsResponse>
}

interface MusicProvider {
    suspend fun generateMusic(
        prompt: String,
        lyrics: String,
        mood: String,
        genre: String,
        tempo: String,
        durationSec: Int,
        singerType: String,
        instrumentStyle: String,
        outputFormat: String
    ): Result<MusicJobResponse>
}

interface VoiceProvider {
    suspend fun synthesizeVoice(
        text: String,
        voiceStyle: String, // e.g. Male, Female, Choir, Classical, Rap
        pitchOffset: Float
    ): Result<VoiceResponse>
}

interface VoiceCloneProvider {
    suspend fun cloneVoice(audioSample: File, targetName: String): Result<VoiceModelResponse>
}

interface InstrumentProvider {
    suspend fun generateInstrumental(prompt: String, style: String, durationSec: Int): Result<MusicJobResponse>
}

interface MoodProvider {
    suspend fun analyzeMood(prompt: String): Result<MoodAnalysisResponse>
}

interface ComposerProvider {
    suspend fun composeMelody(lyrics: String, style: String): Result<CompositionResponse>
}

interface MixProvider {
    suspend fun mixTracks(backingTrack: File, vocals: File): Result<ProcessingResponse>
}

interface MasteringProvider {
    suspend fun masterTrack(inputAudio: File, targetLoudness: Float): Result<ProcessingResponse>
}

// ==========================================
// 2. Unified Data Models & Contracts
// ==========================================

data class LyricsResponse(
    val title: String,
    val content: String,
    val detectedLanguage: String,
    val sectionCount: Int
)

data class MusicJobResponse(
    val jobId: String,
    val providerName: String,
    val status: JobStatus,
    val progress: Float,
    val estimatedSecondsRemaining: Int,
    val audioUrl: String?,
    val lyrics: String?,
    val errors: List<String> = emptyList()
)

data class VoiceResponse(
    val audioUrl: String,
    val detectedGender: String,
    val sampleRate: Int
)

data class VoiceModelResponse(
    val voiceId: String,
    val modelUrl: String,
    val isTrained: Boolean
)

data class MoodAnalysisResponse(
    val primaryMood: String,
    val energyLevel: Float, // 0.0 to 1.0
    val suggestedBpm: Int,
    val recommendedGenre: String
)

data class CompositionResponse(
    val midiData: ByteArray?,
    val chordProgression: String,
    val durationSeconds: Int
)

data class ProcessingResponse(
    val outputAudioUrl: String,
    val sampleRate: Int,
    val durationMs: Long
)

enum class JobStatus {
    QUEUED,
    RUNNING,
    RETRYING,
    COMPLETED,
    FAILED,
    CANCELLED
}
