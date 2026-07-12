package com.example.core.audio

import java.nio.ByteBuffer

data class AudioTelemetry(
    val xruns: Int,
    val cpuLoadPercent: Float,
    val renderTimeUs: Int,
    val latencyMs: Int,
    val activeVoices: Int,
    val memoryUsageKb: Int
) {
    companion object {
        fun readFromBuffer(buffer: ByteBuffer): AudioTelemetry {
            return try {
                // Keep synchronization safe by reading indexed positions
                AudioTelemetry(
                    xruns = buffer.getInt(0),
                    cpuLoadPercent = buffer.getFloat(4),
                    renderTimeUs = buffer.getInt(8),
                    latencyMs = buffer.getInt(12),
                    activeVoices = buffer.getInt(16),
                    memoryUsageKb = buffer.getInt(20)
                )
            } catch (e: Exception) {
                AudioTelemetry(0, 0f, 0, 0, 0, 0)
            }
        }
    }
}
