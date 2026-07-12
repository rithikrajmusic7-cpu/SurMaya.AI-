package com.example.data.remote.gateway.voice

import java.io.File
import kotlinx.coroutines.flow.Flow

// ==========================================
// 1. Core Unified Voice Engine Contracts
// ==========================================

interface VoiceGateway {
    suspend fun startCloningJob(
        voiceName: String,
        audioFile: File,
        consentSignature: String
    ): Result<VoiceJob>

    suspend fun getJobStatus(jobId: String): Result<VoiceJob>
    
    suspend fun synthesizeVoice(
        text: String,
        voiceModelId: String,
        pitchOffset: Float
    ): Result<VoiceSynthesisResponse>

    fun observeJobs(): Flow<List<VoiceJob>>
    fun observeModels(): Flow<List<VoiceModel>>
}

interface VoiceProvider {
    val providerId: String
    suspend fun trainModel(voiceName: String, audioSample: File): Result<String> // Returns remote model ID
    suspend fun synthesize(text: String, modelId: String, pitchOffset: Float): Result<VoiceSynthesisResponse>
    suspend fun checkTrainingStatus(remoteModelId: String): Result<VoiceJobStatus>
}

interface VoiceRecordingModule {
    fun startRecording(outputFile: File): Result<Unit>
    fun stopRecording(): Result<Unit>
    fun isRecording(): Boolean
    fun getRecordingDurationSec(): Int
    fun getInputLeveldB(): Float // For the level meter representation
    fun trimAudio(inputFile: File, outputFile: File, startMs: Long, endMs: Long): Result<File>
}

interface VoiceUploadModule {
    fun validateAudioFile(file: File): Result<AudioQualityReport>
}

interface VoiceVerificationEngine {
    suspend fun verifyVoiceSignature(
        sampleA: File,
        sampleB: File
    ): Result<VerificationResult>
}

interface VoiceConsentManager {
    suspend fun recordConsent(
        signerName: String,
        consentText: String,
        digitalSignature: String
    ): Result<VoiceConsent>
    
    suspend fun verifyConsent(signerName: String): Boolean
}

interface VoiceModelManager {
    suspend fun saveModel(model: VoiceModel): Result<Unit>
    suspend fun deleteModel(modelId: String): Result<Unit>
    suspend fun renameModel(modelId: String, newName: String): Result<Unit>
    suspend fun getModel(modelId: String): Result<VoiceModel?>
    suspend fun getAllModels(): List<VoiceModel>
    suspend fun toggleFavorite(modelId: String): Result<Unit>
}

// ==========================================
// 2. Data Models and Value Classes
// ==========================================

data class VoiceModel(
    val id: String,
    val name: String,
    val description: String,
    val providerId: String,
    val isFavorite: Boolean = false,
    val creationDate: Long = System.currentTimeMillis(),
    val language: String = "Hindi-English",
    val accent: String = "Indian Standard",
    val gender: String = "Neutral",
    val modelUrl: String? = null,
    val isTrained: Boolean = false,
    val fileSizeBytes: Long = 0
)

data class VoiceConsent(
    val id: String,
    val signerName: String,
    val consentText: String,
    val signatureHash: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isExpired: Boolean = false,
    val version: String = "v1.0"
)

data class VoiceJob(
    val jobId: String,
    val voiceName: String,
    val providerName: String,
    val status: VoiceJobStatus,
    val progress: Float, // 0.0 to 1.0
    val estimatedSecondsRemaining: Int,
    val targetVoiceModelId: String?,
    val errorDetails: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class VerificationResult(
    val status: VerificationStatus,
    val similarityPercentage: Float, // 0.0 to 100.0
    val confidencePercentage: Float, // 0.0 to 100.0
    val qualityScore: Float, // 0.0 to 100.0
    val verificationTimestamp: Long = System.currentTimeMillis()
)

data class AudioQualityReport(
    val durationSeconds: Float,
    val isAccepted: Boolean,
    val noiseFloorDb: Float,
    val sampleRateHz: Int,
    val bitsPerSample: Int,
    val channelsCount: Int,
    val issuesDetected: List<String>
)

data class VoiceSynthesisResponse(
    val audioUrl: String,
    val audioBytes: ByteArray?,
    val voiceModelId: String,
    val durationMs: Long
)

// ==========================================
// 3. Status and Taxonomy Enums
// ==========================================

enum class VoiceJobStatus {
    QUEUED,
    RUNNING,
    VERIFYING,
    COMPLETED,
    FAILED,
    CANCELLED,
    RETRYING
}

enum class VerificationStatus {
    VERIFIED,
    NEEDS_REVIEW,
    REJECTED
}

enum class AudioFormatType {
    WAV,
    FLAC,
    AAC,
    M4A,
    PCM
}
