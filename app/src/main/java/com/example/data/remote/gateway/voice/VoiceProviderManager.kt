package com.example.data.remote.gateway.voice

import android.content.Context
import android.util.Log
import java.io.File

class VoiceProviderManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: VoiceProviderManager? = null

        fun getInstance(context: Context): VoiceProviderManager {
            return INSTANCE ?: synchronized(this) {
                val instance = VoiceProviderManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val providers = mutableMapOf<String, VoiceProvider>()
    private var primaryProviderId = "elevenlabs_voice"
    private var fallbackProviderId = "local_synthesizer"

    init {
        // Register some dummy voice providers matching interfaces
        registerProvider(object : VoiceProvider {
            override val providerId: String = "elevenlabs_voice"
            override suspend fun trainModel(voiceName: String, audioSample: File): Result<String> {
                return Result.success("eleven_model_" + voiceName.hashCode())
            }
            override suspend fun synthesize(text: String, modelId: String, pitchOffset: Float): Result<VoiceSynthesisResponse> {
                return Result.success(VoiceSynthesisResponse(
                    audioUrl = "https://example.com/assets/voices/$modelId.wav",
                    audioBytes = ByteArray(0),
                    voiceModelId = modelId,
                    durationMs = 5000
                ))
            }
            override suspend fun checkTrainingStatus(remoteModelId: String): Result<VoiceJobStatus> {
                return Result.success(VoiceJobStatus.COMPLETED)
            }
        })

        registerProvider(object : VoiceProvider {
            override val providerId: String = "local_synthesizer"
            override suspend fun trainModel(voiceName: String, audioSample: File): Result<String> {
                return Result.success("local_model_" + voiceName.hashCode())
            }
            override suspend fun synthesize(text: String, modelId: String, pitchOffset: Float): Result<VoiceSynthesisResponse> {
                return Result.success(VoiceSynthesisResponse(
                    audioUrl = "local_synthesized_$modelId.wav",
                    audioBytes = null,
                    voiceModelId = modelId,
                    durationMs = 4500
                ))
            }
            override suspend fun checkTrainingStatus(remoteModelId: String): Result<VoiceJobStatus> {
                return Result.success(VoiceJobStatus.COMPLETED)
            }
        })
    }

    fun registerProvider(provider: VoiceProvider) {
        providers[provider.providerId] = provider
        Log.i("VoiceProviderManager", "Registered Voice Provider: ${provider.providerId}")
    }

    fun getPrimaryProvider(): VoiceProvider {
        return providers[primaryProviderId] ?: providers["local_synthesizer"]!!
    }

    fun getFallbackProvider(): VoiceProvider {
        return providers[fallbackProviderId] ?: providers["local_synthesizer"]!!
    }

    fun setProviders(primaryId: String, fallbackId: String) {
        primaryProviderId = primaryId
        fallbackProviderId = fallbackId
        Log.d("VoiceProviderManager", "Updated active Voice config: Primary=$primaryId, Fallback=$fallbackId")
    }
}
