package com.example.core.audio

import android.util.Log
import com.example.data.remote.gateway.AIGateway
import com.example.data.remote.gateway.AIREJniBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AIREPerformanceMonitor private constructor(private val gateway: AIGateway) {

    companion object {
        @Volatile
        private var INSTANCE: AIREPerformanceMonitor? = null

        fun getInstance(gateway: AIGateway): AIREPerformanceMonitor {
            return INSTANCE ?: synchronized(this) {
                val instance = AIREPerformanceMonitor(gateway)
                INSTANCE = instance
                instance
            }
        }
    }

    private val _telemetry = MutableStateFlow<AudioTelemetry?>(null)
    val telemetry: StateFlow<AudioTelemetry?> = _telemetry

    private val _isEngineRunning = MutableStateFlow(false)
    val isEngineRunning: StateFlow<Boolean> = _isEngineRunning

    private var monitorJob: Job? = null
    private var telemetryBuffer: ByteBuffer? = null

    fun startMonitoring(scope: CoroutineScope) {
        if (monitorJob != null) return

        try {
            // Ensure engine is initialized
            if (gateway.getEnginePointer() == 0L) {
                gateway.initEngine(48000, 512)
            }
            val ptr = gateway.getEnginePointer()
            if (ptr != 0L) {
                _isEngineRunning.value = true
                
                // Allocate 24 bytes direct buffer (6 variables * 4 bytes each)
                val buffer = ByteBuffer.allocateDirect(24).order(ByteOrder.nativeOrder())
                telemetryBuffer = buffer
                
                // Register buffer in native engine (either AAudio JNI or Kotlin Emulator)
                AIREJniBridge.registerTelemetryBuffer(ptr, buffer)
                
                // Start playback to initiate generation loops
                gateway.startPlayback()
                
                // Set default initial style & quality
                gateway.applySongStyle("Odia_Classical")
                gateway.applyProjectQuality("Studio")
                
                // Start telemetry background poller (100ms refresh rate)
                monitorJob = scope.launch(Dispatchers.Default) {
                    while (_isEngineRunning.value) {
                        val stats = AudioTelemetry.readFromBuffer(buffer)
                        _telemetry.value = stats
                        delay(100)
                    }
                }
                Log.i("AIREPerformanceMonitor", "Successfully initialized and started AIRE JNI monitor.")
            }
        } catch (e: Exception) {
            Log.e("AIREPerformanceMonitor", "Failed to launch telemetry monitor job", e)
        }
    }

    fun stopMonitoring() {
        _isEngineRunning.value = false
        monitorJob?.cancel()
        monitorJob = null
        
        gateway.pausePlayback()
        gateway.release()
        _telemetry.value = null
        telemetryBuffer = null
        Log.i("AIREPerformanceMonitor", "Stopped telemetry monitor, released AIRE engine pointer.")
    }

    fun setStyle(style: String) {
        gateway.applySongStyle(style)
    }

    fun setQuality(quality: String) {
        gateway.applyProjectQuality(quality)
    }

    fun triggerNoteOn(note: Int, velocity: Int, instrument: String) {
        gateway.triggerNoteOn(note, velocity, instrument)
    }

    fun triggerNoteOff(note: Int) {
        gateway.triggerNoteOff(note)
    }

    fun applyInstrumentPreset(presetName: String) {
        gateway.applyInstrumentPreset(presetName)
    }

    fun updateSchedulerConstraints(thermal: Boolean, battery: Boolean) {
        val ptr = gateway.getEnginePointer()
        if (ptr != 0L) {
            AIREJniBridge.updateSchedulerConstraints(ptr, thermal, battery)
        }
    }
}
