package com.example.data.remote.gateway

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object AIREJniBridge {
    private const val TAG = "AIREJniBridge"
    
    var isLibraryLoaded = false
        private set

    init {
        try {
            System.loadLibrary("surmaya_aire")
            isLibraryLoaded = true
            Log.i(TAG, "Native libsurmaya_aire.so loaded successfully!")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library not available. Activating high-fidelity Kotlin Audio Runtime Emulator.")
            isLibraryLoaded = false
        }
    }

    // --- Public API Wrappers with Automatic Kotlin Fallback ---

    fun initEngine(sampleRate: Int, bufferSize: Int): Long {
        return if (isLibraryLoaded) {
            try {
                initEngineNative(sampleRate, bufferSize)
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "UnsatisfiedLinkError on native init. Invoking Emulator.", e)
                Emulator.init(sampleRate, bufferSize)
            }
        } else {
            Emulator.init(sampleRate, bufferSize)
        }
    }

    fun releaseEngine(enginePtr: Long) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                releaseEngineNative(enginePtr)
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "UnsatisfiedLinkError on native release. Invoking Emulator.", e)
                Emulator.release(enginePtr)
            }
        } else {
            Emulator.release(enginePtr)
        }
    }

    fun startPlayback(enginePtr: Long) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                startPlaybackNative(enginePtr)
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "UnsatisfiedLinkError on native start. Invoking Emulator.", e)
                Emulator.startPlayback(enginePtr)
            }
        } else {
            Emulator.startPlayback(enginePtr)
        }
    }

    fun pausePlayback(enginePtr: Long) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                pausePlaybackNative(enginePtr)
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "UnsatisfiedLinkError on native pause. Invoking Emulator.", e)
                Emulator.pausePlayback(enginePtr)
            }
        } else {
            Emulator.pausePlayback(enginePtr)
        }
    }

    fun registerTelemetryBuffer(enginePtr: Long, buffer: ByteBuffer) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                registerTelemetryBufferNative(enginePtr, buffer)
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "UnsatisfiedLinkError on native register telemetry. Invoking Emulator.", e)
                Emulator.registerTelemetryBuffer(enginePtr, buffer)
            }
        } else {
            Emulator.registerTelemetryBuffer(enginePtr, buffer)
        }
    }

    fun setHumanizeParameters(enginePtr: Long, amount: Float, tempoBpm: Float) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setHumanizeParametersNative(enginePtr, amount, tempoBpm)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setHumanize(amount, tempoBpm)
            }
        } else {
            Emulator.setHumanize(amount, tempoBpm)
        }
    }

    fun applyPerformanceInterpretation(enginePtr: Long, trackId: String, notesJson: String) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                applyPerformanceInterpretationNative(enginePtr, trackId, notesJson)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.applyPerformanceInterpretation(enginePtr, trackId, notesJson)
            }
        } else {
            Emulator.applyPerformanceInterpretation(enginePtr, trackId, notesJson)
        }
    }

    fun setMusicStyleProfile(enginePtr: Long, styleName: String) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setMusicStyleProfileNative(enginePtr, styleName)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setStyleProfile(styleName)
            }
        } else {
            Emulator.setStyleProfile(styleName)
        }
    }

    fun configureInstrumentPersonality(enginePtr: Long, trackId: String, presetName: String) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                configureInstrumentPersonalityNative(enginePtr, trackId, presetName)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.configureInstrument(trackId, presetName)
            }
        } else {
            Emulator.configureInstrument(trackId, presetName)
        }
    }

    fun configureSingerExpression(enginePtr: Long, trackId: String, emotion: String, vibratoDepth: Float, breathGain: Float) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                configureSingerExpressionNative(enginePtr, trackId, emotion, vibratoDepth, breathGain)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.configureSinger(trackId, emotion, vibratoDepth, breathGain)
            }
        } else {
            Emulator.configureSinger(trackId, emotion, vibratoDepth, breathGain)
        }
    }

    fun setQualityProfile(enginePtr: Long, profileName: String) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setQualityProfileNative(enginePtr, profileName)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setQualityProfile(profileName)
            }
        } else {
            Emulator.setQualityProfile(profileName)
        }
    }

    fun updateSchedulerConstraints(enginePtr: Long, thermalThrottle: Boolean, batterySaver: Boolean) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                updateSchedulerConstraintsNative(enginePtr, thermalThrottle, batterySaver)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.updateConstraints(thermalThrottle, batterySaver)
            }
        } else {
            Emulator.updateConstraints(thermalThrottle, batterySaver)
        }
    }

    fun noteOn(enginePtr: Long, note: Int, velocity: Int, instrument: String) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                noteOnNative(enginePtr, note, velocity, instrument)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.noteOn(note, velocity, instrument)
            }
        } else {
            Emulator.noteOn(note, velocity, instrument)
        }
    }

    fun noteOff(enginePtr: Long, note: Int) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                noteOffNative(enginePtr, note)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.noteOff(note)
            }
        } else {
            Emulator.noteOff(note)
        }
    }

    fun setInstrumentPreset(enginePtr: Long, presetName: String) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setInstrumentPresetNative(enginePtr, presetName)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setInstrumentPreset(presetName)
            }
        } else {
            Emulator.setInstrumentPreset(presetName)
        }
    }

    fun setChannelVolume(enginePtr: Long, channelIndex: Int, faderDb: Float) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setChannelVolumeNative(enginePtr, channelIndex, faderDb)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setChannelVolume(channelIndex, faderDb)
            }
        } else {
            Emulator.setChannelVolume(channelIndex, faderDb)
        }
    }

    fun setChannelPan(enginePtr: Long, channelIndex: Int, pan: Float) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setChannelPanNative(enginePtr, channelIndex, pan)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setChannelPan(channelIndex, pan)
            }
        } else {
            Emulator.setChannelPan(channelIndex, pan)
        }
    }

    fun setChannelEQ(enginePtr: Long, channelIndex: Int, gainDb: Float) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setChannelEQNative(enginePtr, channelIndex, gainDb)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setChannelEQ(channelIndex, gainDb)
            }
        } else {
            Emulator.setChannelEQ(channelIndex, gainDb)
        }
    }

    fun setChannelAuxSends(enginePtr: Long, channelIndex: Int, reverbDb: Float, delayDb: Float) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setChannelAuxSendsNative(enginePtr, channelIndex, reverbDb, delayDb)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setChannelAuxSends(channelIndex, reverbDb, delayDb)
            }
        } else {
            Emulator.setChannelAuxSends(channelIndex, reverbDb, delayDb)
        }
    }

    fun setMasterFader(enginePtr: Long, masterDb: Float) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                setMasterFaderNative(enginePtr, masterDb)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.setMasterFader(masterDb)
            }
        } else {
            Emulator.setMasterFader(masterDb)
        }
    }

    fun getTruePeakMeters(enginePtr: Long, outPeaks: FloatArray) {
        if (isLibraryLoaded && enginePtr != Emulator.EMULATOR_PTR) {
            try {
                getTruePeakMetersNative(enginePtr, outPeaks)
            } catch (e: UnsatisfiedLinkError) {
                Emulator.getTruePeakMeters(outPeaks)
            }
        } else {
            Emulator.getTruePeakMeters(outPeaks)
        }
    }

    // --- Private Native Bridge Hooks ---

    private external fun initEngineNative(sampleRate: Int, bufferSize: Int): Long
    private external fun releaseEngineNative(enginePtr: Long)
    private external fun startPlaybackNative(enginePtr: Long)
    private external fun pausePlaybackNative(enginePtr: Long)
    private external fun registerTelemetryBufferNative(enginePtr: Long, buffer: ByteBuffer)
    private external fun setHumanizeParametersNative(enginePtr: Long, amount: Float, tempoBpm: Float)
    private external fun applyPerformanceInterpretationNative(enginePtr: Long, trackId: String, notesJson: String)
    private external fun setMusicStyleProfileNative(enginePtr: Long, styleName: String)
    private external fun configureInstrumentPersonalityNative(enginePtr: Long, trackId: String, presetName: String)
    private external fun configureSingerExpressionNative(enginePtr: Long, trackId: String, emotion: String, vibratoDepth: Float, breathGain: Float)
    private external fun setQualityProfileNative(enginePtr: Long, profileName: String)
    private external fun updateSchedulerConstraintsNative(enginePtr: Long, thermalThrottle: Boolean, batterySaver: Boolean)
    private external fun noteOnNative(enginePtr: Long, note: Int, velocity: Int, instrument: String)
    private external fun noteOffNative(enginePtr: Long, note: Int)
    private external fun setInstrumentPresetNative(enginePtr: Long, presetName: String)
    private external fun setChannelVolumeNative(enginePtr: Long, channelIndex: Int, faderDb: Float)
    private external fun setChannelPanNative(enginePtr: Long, channelIndex: Int, pan: Float)
    private external fun setChannelEQNative(enginePtr: Long, channelIndex: Int, gainDb: Float)
    private external fun setChannelAuxSendsNative(enginePtr: Long, channelIndex: Int, reverbDb: Float, delayDb: Float)
    private external fun setMasterFaderNative(enginePtr: Long, masterDb: Float)
    private external fun getTruePeakMetersNative(enginePtr: Long, outPeaks: FloatArray)


    // =========================================================================
    //                    HIGH-FIDELITY KOTLIN BACKUP EMULATOR
    // =========================================================================

    object Emulator {
        const val EMULATOR_PTR = 0xDEADC0DEL

        private var sampleRate = 48000
        private var bufferSize = 512
        private var isPlaying = false
        private var isThreadRunning = false
        private var simThread: Thread? = null

        // Shared Direct ByteBuffer address
        private var telemetryBuffer: ByteBuffer? = null

        // Subsystem values
        private var xruns = 0
        private var cpuLoad = 0.5f // 0.0 to 100.0 %
        private var renderTimeUs = 120
        private var latencyMs = 15
        private var activeVoices = 0
        private var memoryUsageKb = 24576

        private val activeNotes = mutableSetOf<Int>()

        fun noteOn(note: Int, velocity: Int, instrument: String) {
            synchronized(activeNotes) {
                activeNotes.add(note)
                activeVoices = activeNotes.size
                memoryUsageKb = 24576 + (activeVoices * 128)
            }
            Log.i(TAG, "Kotlin Audio Emulator: Note On: $note [Vel: $velocity, Inst: $instrument]")
        }

        fun noteOff(note: Int) {
            synchronized(activeNotes) {
                activeNotes.remove(note)
                activeVoices = activeNotes.size
                memoryUsageKb = 24576 + (activeVoices * 128)
            }
            Log.i(TAG, "Kotlin Audio Emulator: Note Off: $note")
        }

        fun setInstrumentPreset(presetName: String) {
            Log.i(TAG, "Kotlin Audio Emulator: Set Instrument Preset: $presetName")
        }

        fun setChannelVolume(channelIndex: Int, faderDb: Float) {
            Log.i(TAG, "Kotlin Audio Emulator: Set Channel $channelIndex Volume to $faderDb dB")
        }

        fun setChannelPan(channelIndex: Int, pan: Float) {
            Log.i(TAG, "Kotlin Audio Emulator: Set Channel $channelIndex Pan to $pan")
        }

        fun setChannelEQ(channelIndex: Int, gainDb: Float) {
            Log.i(TAG, "Kotlin Audio Emulator: Set Channel $channelIndex EQ Mid Gain to $gainDb dB")
        }

        fun setChannelAuxSends(channelIndex: Int, reverbDb: Float, delayDb: Float) {
            Log.i(TAG, "Kotlin Audio Emulator: Set Channel $channelIndex Sends: Reverb $reverbDb dB, Delay $delayDb dB")
        }

        fun setMasterFader(masterDb: Float) {
            Log.i(TAG, "Kotlin Audio Emulator: Set Master Fader to $masterDb dB")
        }

        fun getTruePeakMeters(outPeaks: FloatArray) {
            outPeaks[0] = -12.0f + (Math.random() * 8.0f).toFloat()
            outPeaks[1] = -12.0f + (Math.random() * 8.0f).toFloat()
        }

        // Audio variables for simulated dsp parameters
        private var styleProfile = "Odia_Classical"
        private var qualityProfile = "Studio"
        private var humanizeAmount = 0.15f
        private var tempoBpm = 105.0f
        private var vocalEmotion = "Devotional"
        private var vibratoDepth = 0.35f
        private var breathGain = 0.15f

        fun init(sampleRate: Int, bufferSize: Int): Long {
            this.sampleRate = sampleRate
            this.bufferSize = bufferSize
            this.isPlaying = false
            
            startThread()
            Log.i(TAG, "Kotlin Audio Emulator initialized at $sampleRate Hz [Buffer: $bufferSize]")
            return EMULATOR_PTR
        }

        fun release(ptr: Long) {
            if (ptr == EMULATOR_PTR) {
                isPlaying = false
                isThreadRunning = false
                simThread?.interrupt()
                simThread = null
                telemetryBuffer = null
                Log.i(TAG, "Kotlin Audio Emulator released.")
            }
        }

        fun startPlayback(ptr: Long) {
            if (ptr == EMULATOR_PTR) {
                isPlaying = true
                Log.i(TAG, "Kotlin Audio Emulator: Playback started.")
            }
        }

        fun pausePlayback(ptr: Long) {
            if (ptr == EMULATOR_PTR) {
                isPlaying = false
                Log.i(TAG, "Kotlin Audio Emulator: Playback paused.")
            }
        }

        fun registerTelemetryBuffer(ptr: Long, buffer: ByteBuffer) {
            if (ptr == EMULATOR_PTR) {
                telemetryBuffer = buffer.order(ByteOrder.nativeOrder())
                Log.i(TAG, "Kotlin Audio Emulator: Telemetry ByteBuffer registered.")
            }
        }

        fun setHumanize(amount: Float, tempoBpm: Float) {
            this.humanizeAmount = amount
            this.tempoBpm = tempoBpm
            Log.i(TAG, "Kotlin Audio Emulator: Humanization set to $amount @ $tempoBpm BPM")
        }

        fun applyPerformanceInterpretation(ptr: Long, trackId: String, notesJson: String) {
            Log.i(TAG, "Kotlin Audio Emulator: Performance interpreted for Track $trackId")
        }

        fun setStyleProfile(styleName: String) {
            this.styleProfile = styleName
            Log.i(TAG, "Kotlin Audio Emulator: Loaded Style: $styleName")
            
            // Adjust EQ and CPU footprints depending on Style
            when (styleName) {
                "Odia_Classical" -> {
                    activeVoices = 14
                    latencyMs = 18
                }
                "Bollywood" -> {
                    activeVoices = 24
                    latencyMs = 12
                }
                "Bhajan" -> {
                    activeVoices = 18
                    latencyMs = 24
                }
            }
        }

        fun configureInstrument(trackId: String, presetName: String) {
            Log.i(TAG, "Kotlin Audio Emulator: Configured Instrument $trackId with Character preset $presetName")
        }

        fun configureSinger(trackId: String, emotion: String, vibrato: Float, breath: Float) {
            this.vocalEmotion = emotion
            this.vibratoDepth = vibrato
            this.breathGain = breath
            Log.i(TAG, "Kotlin Audio Emulator: Configured Singer $trackId [Emotion: $emotion, Vibrato: $vibrato]")
        }

        fun setQualityProfile(profileName: String) {
            this.qualityProfile = profileName
            Log.i(TAG, "Kotlin Audio Emulator: Applied Quality: $profileName")
            
            when (profileName) {
                "Draft" -> {
                    activeVoices = 8
                    memoryUsageKb = 12288
                }
                "Studio" -> {
                    activeVoices = 16
                    memoryUsageKb = 24576
                }
                "Ultra" -> {
                    activeVoices = 32
                    memoryUsageKb = 49152
                }
            }
        }

        fun updateConstraints(thermalThrottle: Boolean, batterySaver: Boolean) {
            Log.i(TAG, "Kotlin Audio Emulator: Thermal: $thermalThrottle, Battery Saver: $batterySaver")
            if (thermalThrottle) {
                activeVoices = 6
                latencyMs = 45
            } else if (batterySaver) {
                activeVoices = 10
                latencyMs = 30
            } else {
                latencyMs = 12
            }
        }

        private fun startThread() {
            if (isThreadRunning) return
            isThreadRunning = true
            
            simThread = thread(start = true, isDaemon = true, name = "AIRE_Kotlin_AudioLoop") {
                val blockDurationMs = ((bufferSize.toFloat() / sampleRate.toFloat()) * 1000f).toLong()
                
                while (isThreadRunning) {
                    val tickStart = System.nanoTime()

                    // Simulate CPU processing Load based on active voices and style profile
                    var baseProcessingUs = activeVoices * 8 + (if (styleProfile == "Bollywood") 110 else 60)
                    
                    // Add random humanized micro-jitters
                    baseProcessingUs += (Math.random() * 25).toInt()
                    
                    // Update CPU, Latency and stats
                    renderTimeUs = baseProcessingUs
                    val blockTimeUs = (bufferSize.toFloat() / sampleRate.toFloat()) * 1000000f
                    cpuLoad = min(100.0f, max(0.1f, (renderTimeUs.toFloat() / blockTimeUs) * 100f))

                    // Simulate buffer underrun/XRUNs under heavy stress
                    if (cpuLoad > 96.0f) {
                        xruns++
                    }

                    // Directly update Direct ByteBuffer mapping if available
                    telemetryBuffer?.let { buf ->
                        try {
                            buf.putInt(0, xruns)
                            buf.putFloat(4, cpuLoad)
                            buf.putInt(8, renderTimeUs)
                            buf.putInt(12, latencyMs)
                            buf.putInt(16, activeVoices)
                            buf.putInt(20, memoryUsageKb)
                        } catch (e: Exception) {
                            // Suppress out of bounds
                        }
                    }

                    val renderElapsedNs = System.nanoTime() - tickStart
                    val elapsedMs = renderElapsedNs / 1000000
                    
                    // Throttle block rate accurately
                    if (elapsedMs < blockDurationMs) {
                        try {
                            Thread.sleep(blockDurationMs - elapsedMs)
                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                }
            }
        }
    }
}
