package com.example.data.remote.gateway

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ApiCredentialManager
import com.example.data.local.DeveloperPrefsManager
import java.io.File
import java.util.UUID

// ==========================================
// Central AI Gateway Module (Orchestrator)
// ==========================================

class AIGateway private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: AIGateway? = null

        fun getInstance(context: Context): AIGateway {
            return INSTANCE ?: synchronized(this) {
                val instance = AIGateway(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val providerManager = ProviderManager.getInstance(context)
    private val jobManager = JobManager.getInstance(context)
    private val downloadManager = DownloadManager.getInstance(context)

    /**
     * Orchestrates Lyrics generation using primary and fallback providers.
     */
    suspend fun generateLyrics(
        prompt: String,
        genre: String,
        mood: String
    ): Result<LyricsResponse> {
        val config = providerManager.getProviderConfig(ProviderManager.Capability.LYRICS)
        Log.i("AIGateway", "Orchestrating Lyrics. Primary: ${config.primaryProviderId}, Fallback: ${config.fallbackProviderId}")

        // In a production app, we would look up the concrete registered implementations of LyricsProvider.
        // We'll perform the execution with dynamic backup routing:
        return try {
            val response = executeLyricsWithProvider(config.primaryProviderId, prompt, genre, mood)
            Result.success(response)
        } catch (throwable: Throwable) {
            Log.w("AIGateway", "Primary Lyrics provider failed. Activating fallback: ${config.fallbackProviderId}", throwable)
            try {
                val response = executeLyricsWithProvider(config.fallbackProviderId, prompt, genre, mood)
                Result.success(response)
            } catch (fallbackThrowable: Throwable) {
                val finalError = ErrorMapper.mapException(fallbackThrowable, config.fallbackProviderId)
                Log.e("AIGateway", "All Lyrics providers failed.", finalError)
                Result.failure(finalError)
            }
        }
    }

    /**
     * Orchestrates dynamic Music/Song generation with robust error boundaries.
     */
    suspend fun generateMusicTrack(
        prompt: String,
        lyrics: String,
        mood: String,
        genre: String,
        tempo: String,
        durationSec: Int,
        singerType: String,
        instrumentStyle: String,
        outputFormat: String
    ): Result<MusicJobResponse> {
        val config = providerManager.getProviderConfig(ProviderManager.Capability.MUSIC_GENERATION)
        Log.i("AIGateway", "Orchestrating Music generation. Primary: ${config.primaryProviderId}, Fallback: ${config.fallbackProviderId}")

        val jobId = jobManager.createTrackGenerationJob(config.primaryProviderId, prompt)
        
        return try {
            val response = executeMusicWithProvider(
                config.primaryProviderId,
                prompt, lyrics, mood, genre, tempo, durationSec, singerType, instrumentStyle, outputFormat
            )
            jobManager.updateJob(jobId, response.copy(jobId = jobId, status = JobStatus.COMPLETED, progress = 1.0f))
            Result.success(response)
        } catch (throwable: Throwable) {
            Log.w("AIGateway", "Primary Music Engine failed. Activating fallback: ${config.fallbackProviderId}", throwable)
            try {
                val response = executeMusicWithProvider(
                    config.fallbackProviderId,
                    prompt, lyrics, mood, genre, tempo, durationSec, singerType, instrumentStyle, outputFormat
                )
                jobManager.updateJob(jobId, response.copy(jobId = jobId, status = JobStatus.COMPLETED, progress = 1.0f))
                Result.success(response)
            } catch (fallbackThrowable: Throwable) {
                val finalError = ErrorMapper.mapException(fallbackThrowable, config.fallbackProviderId)
                jobManager.updateJob(jobId, MusicJobResponse(
                    jobId = jobId,
                    providerName = config.fallbackProviderId,
                    status = JobStatus.FAILED,
                    progress = 0.0f,
                    estimatedSecondsRemaining = 0,
                    audioUrl = null,
                    lyrics = lyrics,
                    errors = listOf(finalError.message ?: "Unknown Error")
                ))
                Log.e("AIGateway", "All Music Engines failed.", finalError)
                Result.failure(finalError)
            }
        }
    }

    // ==========================================
    // Mock/Dummy Provider Call Routing
    // ==========================================

    private suspend fun executeLyricsWithProvider(
        providerId: String,
        prompt: String,
        genre: String,
        mood: String
    ): LyricsResponse {
        if (providerId == "local_offline") {
            return LyricsResponse(
                title = "Offline Ghazal: $genre",
                content = "Verse 1:\nDil se teri yaadon ka chiraag jalta hai...\n\nChorus:\nSeparation is silent like the wind...",
                detectedLanguage = "Urdu/Hindi",
                sectionCount = 3
            )
        }

        // Simulating standard API credentials loading & invocation
        val devPrefs = DeveloperPrefsManager.getInstance(context)
        val apiManager = ApiCredentialManager.getInstance(context)
        
        val key = if (devPrefs.isDeveloperModeEnabled && devPrefs.customGeminiApiKey.isNotBlank()) {
            devPrefs.customGeminiApiKey
        } else {
            val savedKey = apiManager.geminiApiKey
            if (savedKey.isNotBlank()) savedKey else BuildConfig.GEMINI_API_KEY
        }

        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            // Emulate fallback trigger if key is empty
            throw SurMayaException.InvalidApiKey()
        }

        return LyricsResponse(
            title = "SurMaya AI Melodies",
            content = "Verse 1:\nCreative lines matching prompt '$prompt'\nStyled with $mood mood in $genre style.\n\nChorus:\nThis is a beautiful composition...",
            detectedLanguage = "English/Hindi Blend",
            sectionCount = 4
        )
    }

    private suspend fun executeMusicWithProvider(
        providerId: String,
        prompt: String,
        lyrics: String,
        mood: String,
        genre: String,
        tempo: String,
        durationSec: Int,
        singerType: String,
        instrumentStyle: String,
        outputFormat: String
    ): MusicJobResponse {
        if (providerId == "local_sine_synth") {
            // Under fallback, we construct mock track details
            return MusicJobResponse(
                jobId = "job_fallback_" + UUID.randomUUID().hashCode(),
                providerName = "Local Synth Backplane",
                status = JobStatus.COMPLETED,
                progress = 1.0f,
                estimatedSecondsRemaining = 0,
                audioUrl = "local_synthesized_asset.wav",
                lyrics = lyrics
            )
        }

        // Ensure key check before calling API
        val devPrefs = DeveloperPrefsManager.getInstance(context)
        val apiManager = ApiCredentialManager.getInstance(context)
        val savedKey = apiManager.geminiApiKey
        val key = if (savedKey.isNotBlank()) savedKey else BuildConfig.GEMINI_API_KEY

        if (key.isBlank() || key == "MY_GEMINI_API_KEY") {
            throw SurMayaException.InvalidApiKey()
        }

        return MusicJobResponse(
            jobId = "job_real_" + UUID.randomUUID().hashCode(),
            providerName = "SurMaya AI Cloud Pipeline",
            status = JobStatus.COMPLETED,
            progress = 1.0f,
            estimatedSecondsRemaining = 0,
            audioUrl = "https://example.com/assets/music/generated_track.mp3",
            lyrics = lyrics
        )
    }

    // ==========================================
    // AIRE v2.0 Native Runtime Execution Methods
    // ==========================================

    private var enginePtr = 0L

    fun initEngine(sampleRate: Int = 48000, bufferSize: Int = 512): Boolean {
        if (enginePtr != 0L) {
            AIREJniBridge.releaseEngine(enginePtr)
        }
        enginePtr = AIREJniBridge.initEngine(sampleRate, bufferSize)
        Log.i("AIGateway", "AIRE Native Runtime engine initialized: pointer=$enginePtr")
        return enginePtr != 0L
    }

    fun startPlayback() {
        if (enginePtr != 0L) {
            AIREJniBridge.startPlayback(enginePtr)
            Log.i("AIGateway", "AIRE Playback started on native thread.")
        }
    }

    fun pausePlayback() {
        if (enginePtr != 0L) {
            AIREJniBridge.pausePlayback(enginePtr)
            Log.i("AIGateway", "AIRE Playback paused on native thread.")
        }
    }

    fun applyProjectQuality(quality: String) {
        if (enginePtr != 0L) {
            AIREJniBridge.setQualityProfile(enginePtr, quality)
            Log.i("AIGateway", "AIRE Quality Profile applied: $quality")
        }
    }

    fun applySongStyle(style: String) {
        if (enginePtr != 0L) {
            AIREJniBridge.setMusicStyleProfile(enginePtr, style)
            Log.i("AIGateway", "AIRE Song Style applied: $style")
        }
    }

    fun configureVocalTrack(trackId: String, emotion: String, vibrato: Float, breath: Float) {
        if (enginePtr != 0L) {
            AIREJniBridge.configureSingerExpression(enginePtr, trackId, emotion, vibrato, breath)
            Log.i("AIGateway", "AIRE Singer parameters set for $trackId [Emotion: $emotion, Vibrato: $vibrato]")
        }
    }

    fun triggerNoteOn(note: Int, velocity: Int, instrument: String) {
        if (enginePtr != 0L) {
            AIREJniBridge.noteOn(enginePtr, note, velocity, instrument)
        }
    }

    fun triggerNoteOff(note: Int) {
        if (enginePtr != 0L) {
            AIREJniBridge.noteOff(enginePtr, note)
        }
    }

    fun applyInstrumentPreset(presetName: String) {
        if (enginePtr != 0L) {
            AIREJniBridge.setInstrumentPreset(enginePtr, presetName)
        }
    }

    fun setChannelVolume(channelIndex: Int, faderDb: Float) {
        if (enginePtr != 0L) {
            AIREJniBridge.setChannelVolume(enginePtr, channelIndex, faderDb)
        }
    }

    fun setChannelPan(channelIndex: Int, pan: Float) {
        if (enginePtr != 0L) {
            AIREJniBridge.setChannelPan(enginePtr, channelIndex, pan)
        }
    }

    fun setChannelEQ(channelIndex: Int, gainDb: Float) {
        if (enginePtr != 0L) {
            AIREJniBridge.setChannelEQ(enginePtr, channelIndex, gainDb)
        }
    }

    fun setChannelAuxSends(channelIndex: Int, reverbDb: Float, delayDb: Float) {
        if (enginePtr != 0L) {
            AIREJniBridge.setChannelAuxSends(enginePtr, channelIndex, reverbDb, delayDb)
        }
    }

    fun setMasterFader(masterDb: Float) {
        if (enginePtr != 0L) {
            AIREJniBridge.setMasterFader(enginePtr, masterDb)
        }
    }

    fun getTruePeakMeters(outPeaks: FloatArray) {
        if (enginePtr != 0L) {
            AIREJniBridge.getTruePeakMeters(enginePtr, outPeaks)
        }
    }

    fun release() {
        if (enginePtr != 0L) {
            AIREJniBridge.releaseEngine(enginePtr)
            Log.i("AIGateway", "AIRE Native Runtime released.")
            enginePtr = 0L
        }
    }

    fun getEnginePointer(): Long = enginePtr
}

