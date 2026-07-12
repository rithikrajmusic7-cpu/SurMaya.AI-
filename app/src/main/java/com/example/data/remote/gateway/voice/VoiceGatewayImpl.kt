package com.example.data.remote.gateway.voice

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class VoiceGatewayImpl private constructor(private val context: Context) : VoiceGateway {

    companion object {
        @Volatile
        private var INSTANCE: VoiceGatewayImpl? = null

        fun getInstance(context: Context): VoiceGatewayImpl {
            return INSTANCE ?: synchronized(this) {
                val instance = VoiceGatewayImpl(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    private val providerManager = VoiceProviderManager.getInstance(context)
    private val recordingModule = VoiceRecordingModuleImpl(context)
    private val uploadModule = VoiceUploadModuleImpl()
    private val verificationEngine = VoiceVerificationEngineImpl()
    private val consentManager = VoiceConsentManagerImpl(context)
    private val modelManager = VoiceModelManagerImpl(context)
    private val jobQueue = VoiceJobQueue()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override suspend fun startCloningJob(
        voiceName: String,
        audioFile: File,
        consentSignature: String
    ): Result<VoiceJob> {
        Log.i("VoiceGateway", "Starting voice cloning job registration for: $voiceName")

        // 1. Enforce active user digital consent checking
        val isConsentActive = consentManager.verifyConsent(voiceName)
        if (!isConsentActive && consentSignature.isBlank()) {
            return Result.failure(VoiceException.ConsentMissing())
        }

        if (!isConsentActive) {
            val consentText = "I hereby confirm that I possess full ownership and authorized commercial rights over the uploaded vocal characteristics."
            consentManager.recordConsent(voiceName, consentText, consentSignature)
        }

        // 2. Perform deep structural audio validation
        val qualityResult = uploadModule.validateAudioFile(audioFile)
        val report = qualityResult.getOrElse {
            return Result.failure(VoiceException.AudioQualityRejected(listOf("Failed reading audio content.")))
        }

        if (!report.isAccepted) {
            return Result.failure(VoiceException.AudioQualityRejected(report.issuesDetected))
        }

        // 3. Match signatures against standard verification reference if present
        val testVerificationFile = File(context.cacheDir, "verification_ref.wav")
        if (testVerificationFile.exists()) {
            val verification = verificationEngine.verifyVoiceSignature(testVerificationFile, audioFile).getOrNull()
            if (verification != null && verification.status == VerificationStatus.REJECTED) {
                return Result.failure(VoiceException.VerificationFailed("Spectral characteristics do not match verification baseline."))
            }
        }

        // 4. Enqueue active modeling job
        val jobId = "vjob_" + UUID.randomUUID().toString().take(12)
        val initialJob = VoiceJob(
            jobId = jobId,
            voiceName = voiceName,
            providerName = providerManager.getPrimaryProvider().providerId,
            status = VoiceJobStatus.QUEUED,
            progress = 0.0f,
            estimatedSecondsRemaining = 45,
            targetVoiceModelId = null
        )
        jobQueue.enqueueJob(initialJob)

        // 5. Fire off asynchronous training routine matching WorkManager loops
        coroutineScope.launch {
            try {
                jobQueue.updateJobStatus(jobId, VoiceJobStatus.RUNNING, 0.1f, 40)
                delay(3000)

                jobQueue.updateJobStatus(jobId, VoiceJobStatus.VERIFYING, 0.4f, 25)
                delay(2000)

                val provider = providerManager.getPrimaryProvider()
                val remoteModelResult = provider.trainModel(voiceName, audioFile)
                val modelId = remoteModelResult.getOrThrow()

                // Save cloned metadata inside system
                val newModel = VoiceModel(
                    id = modelId,
                    name = voiceName,
                    description = "Custom cloned vocal model of $voiceName trained from recorded audio segment.",
                    providerId = provider.providerId,
                    isTrained = true,
                    fileSizeBytes = audioFile.length()
                )
                modelManager.saveModel(newModel)

                jobQueue.updateJobStatus(jobId, VoiceJobStatus.COMPLETED, 1.0f, 0, modelId = modelId)
                Log.i("VoiceGateway", "Vocal model training successfully finished: $modelId")
            } catch (e: Exception) {
                Log.e("VoiceGateway", "Asynchronous voice model generation failed", e)
                jobQueue.updateJobStatus(jobId, VoiceJobStatus.FAILED, 0.0f, 0, error = e.message ?: "Server error")
            }
        }

        return Result.success(initialJob)
    }

    override suspend fun getJobStatus(jobId: String): Result<VoiceJob> {
        val job = jobQueue.getJob(jobId) ?: return Result.failure(IllegalArgumentException("Job not found."))
        return Result.success(job)
    }

    override suspend fun synthesizeVoice(
        text: String,
        voiceModelId: String,
        pitchOffset: Float
    ): Result<VoiceSynthesisResponse> {
        Log.i("VoiceGateway", "Synthesizing voice lines with model: $voiceModelId (Pitch Offset: $pitchOffset)")
        
        // Lookup standard voice model details
        val savedModel = modelManager.getModel(voiceModelId).getOrNull()
        val provider = if (savedModel != null) {
            if (savedModel.providerId == "elevenlabs_voice") providerManager.getPrimaryProvider() else providerManager.getFallbackProvider()
        } else {
            providerManager.getPrimaryProvider()
        }

        return try {
            val response = provider.synthesize(text, voiceModelId, pitchOffset)
            response
        } catch (throwable: Throwable) {
            Log.w("VoiceGateway", "Primary synthesis failed, attempting backup recovery routing.", throwable)
            try {
                providerManager.getFallbackProvider().synthesize(text, voiceModelId, pitchOffset)
            } catch (fallbackThrowable: Throwable) {
                Result.failure(VoiceErrorMapper.mapException(fallbackThrowable))
            }
        }
    }

    override fun observeJobs(): Flow<List<VoiceJob>> {
        return jobQueue.jobs.map { it.values.toList() }
    }

    override fun observeModels(): Flow<List<VoiceModel>> {
        // Simple Flow wrapper over persistent list
        return MutableStateFlow(modelsCacheList()).map { it }
    }

    private fun modelsCacheList(): List<VoiceModel> {
        return try {
            // Read from managers directly
            kotlinx.coroutines.runBlocking { modelManager.getAllModels() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Helper exposed getters for screens
    fun getRecordingModule(): VoiceRecordingModule = recordingModule
    fun getConsentManager(): VoiceConsentManager = consentManager
    fun getModelManager(): VoiceModelManager = modelManager
}
