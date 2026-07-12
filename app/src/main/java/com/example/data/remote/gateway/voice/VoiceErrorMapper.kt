package com.example.data.remote.gateway.voice

import com.example.data.remote.gateway.SurMayaException
import java.io.IOException

sealed class VoiceException(message: String, val errCode: String, cause: Throwable? = null) : Exception(message, cause) {
    class PermissionDenied : VoiceException("Microphone permission has been denied by the system.", "MIC_PERM_01")
    class RecordingFailed(details: String) : VoiceException("Recording hardware failure: $details", "REC_HW_02")
    class AudioQualityRejected(val issues: List<String>) : VoiceException("Audio failed threshold constraints: ${issues.joinToString(", ")}", "QUALITY_REJ_03")
    class VerificationFailed(message: String) : VoiceException("Voice biometric verification rejected: $message", "BIOMETRIC_FAIL_04")
    class ConsentMissing : VoiceException("Active digital ownership consent verification failed.", "CONSENT_MISS_05")
    class TrainingFailed(details: String) : VoiceException("Voice model synthesis training failed: $details", "TRAINING_ERR_06")
    class ModelNotFound(modelId: String) : VoiceException("Voice model not found or archived: $modelId", "MODEL_MISS_07")
}

object VoiceErrorMapper {
    fun mapException(throwable: Throwable): Throwable {
        return when (throwable) {
            is VoiceException -> throwable
            is java.lang.SecurityException -> VoiceException.PermissionDenied()
            is IOException -> SurMayaException.NetworkFailure(throwable)
            else -> throwable
        }
    }
}
