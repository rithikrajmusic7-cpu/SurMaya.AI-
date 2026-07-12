package com.example.data.remote.gateway.voice

import android.util.Log
import java.io.File
import java.io.FileInputStream

class VoiceUploadModuleImpl : VoiceUploadModule {

    override fun validateAudioFile(file: File): Result<AudioQualityReport> {
        if (!file.exists()) {
            return Result.failure(IllegalArgumentException("Voice file does not exist."))
        }

        val length = file.length()
        if (length < 1024) {
            return Result.success(AudioQualityReport(
                durationSeconds = 0f,
                isAccepted = false,
                noiseFloorDb = -120f,
                sampleRateHz = 0,
                bitsPerSample = 0,
                channelsCount = 0,
                issuesDetected = listOf("File too small or empty.")
            ))
        }

        return try {
            // Attempt standard WAV metadata header checking
            val fis = FileInputStream(file)
            val header = ByteArray(44)
            val bytesRead = fis.read(header)
            fis.close()

            var sampleRate = 44100
            var bitsPerSample = 16
            var channels = 1
            val issues = mutableListOf<String>()

            if (bytesRead >= 44 && header[0] == 'R'.toByte() && header[1] == 'I'.toByte() && header[2] == 'F'.toByte() && header[3] == 'F'.toByte()) {
                // Read channels from WAV header
                channels = (header[22].toInt() and 0xff) or ((header[23].toInt() and 0xff) shl 8)
                // Read sample rate
                sampleRate = (header[24].toInt() and 0xff) or
                             ((header[25].toInt() and 0xff) shl 8) or
                             ((header[26].toInt() and 0xff) shl 16) or
                             ((header[27].toInt() and 0xff) shl 24)
                // Read bits per sample
                bitsPerSample = (header[34].toInt() and 0xff) or ((header[35].toInt() and 0xff) shl 8)
            } else {
                Log.d("VoiceUploadModule", "Non-WAV audio structure uploaded, relying on compressed file heuristics.")
            }

            // Estimate duration in seconds based on average file bitrate
            val bytesPerSec = sampleRate * channels * (bitsPerSample / 8)
            val duration = if (bytesPerSec > 0) length.toFloat() / bytesPerSec else 15f

            if (duration < 5.0f) {
                issues.add("Voice sample too short. Minimum requirement is 5 seconds of clear vocal activity.")
            }
            if (duration > 300.0f) {
                issues.add("Voice sample too long. Standard session limits are capped at 5 minutes.")
            }
            if (sampleRate < 16000) {
                issues.add("Sample rate ($sampleRate Hz) is below standard biometric fidelity (minimum 16kHz).")
            }

            val isAccepted = issues.isEmpty()
            val report = AudioQualityReport(
                durationSeconds = duration,
                isAccepted = isAccepted,
                noiseFloorDb = -58.5f, // Estimated average
                sampleRateHz = sampleRate,
                bitsPerSample = bitsPerSample,
                channelsCount = channels,
                issuesDetected = issues
            )

            Log.i("VoiceUploadModule", "Audio Validation Report completed. Accepted: $isAccepted")
            Result.success(report)
        } catch (e: Exception) {
            Log.e("VoiceUploadModule", "Error performing audio signal check", e)
            Result.failure(e)
        }
    }
}
