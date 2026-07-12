# SurMaya AI: AIRE v2.0 Software Design Document (SDD)
**Classification**: Master Systems Architecture & AI Audio Runtime Blueprint
**Author**: Chief Technology Officer, Lead Audio Architect, and AI/DSP Systems Engineer
**Baseline Version**: AIRE v2.0 (Comprehensive AI Execution Runtime)

---

## Executive Summary
This document establishes the official software design specifications for the **Audio Intelligence Rendering Engine (AIRE) v2.0**. While AIRE v1.3 established the low-level DSP and JNI foundation, AIRE v2.0 elevates the system into a complete **SurMaya Music Execution Runtime**. It acts as the intelligent execution layer that translates raw AI musical decisions (from Lyrics, Composer, Melody, Chord, Arrangement, Instrument, Singer, and Mixing engines) into expressive, humanized, and studio-grade native audio completely on-device.

AIRE v2.0 bridges the semantic gap between high-level AI symbols and physical PCM buffers by introducing an interpretation layer, genre-style renderers, expressive performance models, and an adaptive native hardware scheduler.

---

## 1. Overall AIRE v2.0 Architecture

AIRE v2.0 is structured into four highly decoupled, specialized layers designed to maximize CPU efficiency, minimize latency, and preserve battery life on Android:

```
+-----------------------------------------------------------------------------------+
|                        1. COGNITIVE INTERPRETATION LAYER (Kotlin)                  |
|   +-----------------------+    +-------------------------+    +----------------+  |
|   |  Song Blueprint (.json) |-->|   Performance Interpreter|-->| Style Renderer  |  |
|   +-----------------------+    +-------------------------+    +----------------+  |
+--------------------------------------------|--------------------------------------+
                                             v
+-----------------------------------------------------------------------------------+
|                           2. PROJECT & ASSET PLANE (Kotlin)                       |
|   +-----------------------+    +-------------------------+    +----------------+  |
|   |   .surmaya Packager   |    |  Project Asset Manager  |    | Quality Manager|  |
|   +-----------------------+    +-------------------------+    +----------------+  |
+--------------------------------------------|--------------------------------------+
                                             | (Direct JNI Telemetry / Events Queue)
                                             v
+-----------------------------------------------------------------------------------+
|                        3. NATIVE AUDIO PROCESSOR (C++17)                          |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  |                          EXPRESSIVE VOICE RENDERER                          |  |
|  |  +-------------------------+  +-------------------------+  +-------------+  |  |
|  |  | Vocal Expression Node   |  | Instrument Personality  |  | Neural Core |  |  |
|  |  | (Emotion/Vibrato/Breath)|  | (Concert/Vintage Presets)  (Reserved)  |  |  |
|  |  +-------------------------+  +-------------------------+  +-------------+  |  |
|  +---------------------------------------|-------------------------------------+  |
|                                          v                                        |
|  +-----------------------------------------------------------------------------+  |
|  |                             AUDIO GRAPH EVALUATOR                           |  |
|  |     [Topological DAG Solver] ----> [Lock-Free Node Swapping Interface]      |  |
|  +---------------------------------------|-------------------------------------+  |
|                                          v                                        |
|  +-----------------------------------------------------------------------------+  |
|  |                            STUDIO ROUTING & MIXER                           |  |
|  |  [Channel Strip 1-16] ──> [Aux FX Sends] ──> [Master Bus] ──> [True-Peak]   |  |
|  +---------------------------------------|-------------------------------------+  |
+------------------------------------------|----------------------------------------+
                                           v
+-----------------------------------------------------------------------------------+
|                           4. NATIVE PLATFORM DRIVERS                              |
|   +---------------------------------------+   +--------------------------------+  |
|   |       AAudio Low-Latency Engine       |   |      Offline File Export       |  |
|   |   - Real-time SCHED_FIFO Priority     |   |   - Multi-threaded Encoders    |  |
|   |   - Power/Thermal-Aware Scheduler     |   |   - WAV, MP3, FLAC, AAC        |  |
|   +---------------------------------------+   +--------------------------------+  |
+-----------------------------------------------------------------------------------+
```

---

## 2. Module-by-Module Design (Expanded v2.0 Subsystems)

AIRE v2.0 introduces several new modules that wrap around the v1.3 core:

### 2.1 Song Blueprint Manager & Validator
* **Responsibilities**: Parses, validates, and optimizes the raw generated **Song Blueprint JSON** before rendering begins.
* **Internal Functions**:
  * Runs static structural analysis on note intervals and track parameters.
  * Adjusts polyphony density to avoid hardware performance cliffs.
  * Prunes empty tracks or redundant automation tracks.

### 2.2 Performance Interpretation Layer (PIL)
* **Responsibilities**: Translates static, robotic MIDI notes into expressive, humanized micro-timings, velocity variations, and performance articulations.
* **Internal Functions**:
  * **Humanization**: Injects microscopic, organic timing deviations (within a micro-second window) based on the target tempo.
  * **Dynamics Mapping**: Scales note velocities dynamically to establish realistic performance accents (e.g., strong/weak beat relationships).
  * **Articulation Selection**: Maps note transitions to legato, staccato, or portamento performance techniques in the synthesizer.

### 2.3 Instrument Personality Layer (IPL)
* **Responsibilities**: Customizes the sonic color, timber, and spatial character of loaded virtual instruments based on AI design metadata.
* **Internal Functions**:
  * Modulates SoundFont envelopes dynamically (ADSR parameters).
  * Adjusts biquad filter sweeps and resonance settings to produce presets like **Soft Piano**, **Concert Grand**, **Film Noir Steinway**, or **Vintage upright**.

### 2.4 Advanced Singer Rendering Layer (ASRL)
* **Responsibilities**: Synthesizes singing voices with natural vocal modulations (vibrato, dynamic swells, and breath markers).
* **Internal Functions**:
  * **Vibrato LFO**: Modulates voice pitch continuously using low-frequency oscillators with dynamic frequency and depth.
  * **Breath Injector**: Integrates non-periodic breath white-noise bursts at phrasing gaps or notes marked with physical pauses.
  * **Emotion Envelope**: Modulates high-frequency saturation and gain ratios to match distinct emotional states (e.g., Joyful, Sorrowful, Aggressive).

### 2.5 Music Style Renderer (MSR)
* **Responsibilities**: Adapts MIDI parameters and DSP parameters to follow cultural/genre aesthetics.
* **Internal Functions**:
  * **Bollywood style**: Adds warm dynamic compression, mid-frequency focus, and lush reverb decays.
  * **Odia traditional style**: Shapes custom scales (Ragas) and implements microtonal pitch-bend structures on regional flute or string nodes.
  * **Bhajan style**: Adds natural room spatializations and acoustic clarity to percussion elements.

### 2.6 Project Asset Manager (PAM)
* **Responsibilities**: Handles the localized loading, staging, and lifecycle of SoundFonts, vocal models, custom wave files, and lyrics assets.
* **Internal Functions**:
  * Implements reference counting on cache entries.
  * Pre-allocates fixed memory partitions for track audio caches to guarantee zero memory fragmentation during runs.

### 2.7 AI Render Scheduler (ARS)
* **Responsibilities**: Adapts rendering priority, block sizes, and thread allocations dynamically depending on device capability, temperature, and battery life.
* **Internal Functions**:
  * **Thermal Guardian**: Reduces active voice limit or forces lower-quality filters if the native Android thermal manager reports throttling.
  * **Battery Aware Mode**: Limits background thread count for export tasks when the device is in low power mode.

### 2.8 Audio Quality Manager (AQM)
* **Responsibilities**: Governs global processing criteria depending on the target rendering profile (Draft, Studio, Cinema, Ultra).
* **Internal Functions**:
  * **Draft Mode**: Disables lookahead limiting, reduces sample rates to 32kHz, and disables expensive reverb nodes for immediate, light-weight preview.
  * **Ultra Mode**: Activates 4x oversampling in the mastering chain, uses high-order FIR interpolation filters, and utilizes high-bitrate multi-channel summation.

### 2.9 Project Format (.surmaya Packager)
* **Responsibilities**: Packs and unpacks a consolidated project file holding all state data and assets.
* **Internal Structure**: Single zip-compressed project container with structured metadata and nested audio/MIDI assets.

### 2.10 Future-Ready AI Layer
* **Responsibilities**: Reserves structural pipeline routes for neural rendering engine nodes (DDSP, neural physical modeling, wavetable synthesis).

---

## 3. Data Flow Diagrams & Schemas

### End-to-End AIRE v2.0 Data Flow

```
                                  [ Song Blueprint JSON ]
                                             │
                                             v
                              [ Song Blueprint Manager ]
                                (Validate and Optimize)
                                             │
                                             v
                            [ Performance Interpretation ]
                           (Timing / Velocity / Articulate)
                                             │
                    ┌────────────────────────┴────────────────────────┐
                    ▼                                                 ▼
        [ Music Style Renderer ]                         [ Instrument Personality ]
         (Bollywood / Odia / Bhajan)                       (Concert / Vintage Tone)
                    │                                                 │
                    ▼                                                 ▼
         [ Vocal Expression Node ]                        [ SoundFont Voice Engines ]
        (Emotion / Vibrato / Breath)                      (32-Voice Envelope Shapes)
                    │                                                 │
                    └────────────────────────┬────────────────────────┘
                                             │ (Summing Matrix)
                                             v
                                   [ Mixer Channel Strip ]
                                    (Parametric EQ / Panning)
                                             │
                                             v
                                  [ Audio Graph Evaluator ]
                                   (Topological DAG Run)
                                             │
                                             v
                                       [ Master Bus ]
                                 (Oversampling / Limiter)
                                             │
                    ┌────────────────────────┴────────────────────────┐
                    ▼                                                 ▼
         [ AAudio Playback Driver ]                       [ Offline Export Pipeline ]
         (SCHED_FIFO / Low-Latency)                       (Background Service Engine)
```

---

## 4. Technical Data Schemas

### 4.1 SurMaya Native Project File Structure (`.surmaya`)
A `.surmaya` project file is a unified ZIP archive containing:
```
/
├── project.json                   # Consolidated tracks, automation, and session metadata
├── assets/
│   ├── vocals/
│   │   └── lead_model.bin         # Cached singer-inference vocal parameters
│   └── instruments/
│       └── grand_piano.sf2        # Embedded SoundFont sample instruments
├── lyrics/
│   └── raw_lyrics.txt             # Original text block
└── render_cache/
    └── preview.pcm                # Cached pre-rendered session block
```

### 4.2 Comprehensive Project State Schema (`project.json`)
```json
{
  "project_version": "2.0",
  "project_id": "sm_2026_09a7d",
  "meta": {
    "title": "Bande Utkala Janani",
    "artist": "SurMaya AI",
    "created_at": 1783584000
  },
  "global_settings": {
    "target_sample_rate": 48000,
    "quality_profile": "Studio",
    "style": "Odia_Classical"
  },
  "tempo_map": [
    { "tick": 0, "bpm": 108.0, "time_signature": "4/4" }
  ],
  "tracks": [
    {
      "track_id": "singer_lead",
      "name": "Lead Singer",
      "type": "Vocal",
      "vocal_model_id": "odia_female_singer_01",
      "emotion_state": "Devotional",
      "vibrato_depth": 0.35,
      "channel_settings": {
        "volume": 0.85,
        "pan": 0.5,
        "eq_low_gain": 0.0,
        "eq_mid_gain": 2.0,
        "eq_high_gain": 1.0
      },
      "phrases": [
        {
          "tick": 0,
          "lyrics": "Bande Utkala Janani",
          "notes": [
            { "tick": 0, "duration": 480, "note": 65, "velocity": 95, "phonemes": "ban" },
            { "tick": 480, "duration": 960, "note": 67, "velocity": 100, "phonemes": "de" }
          ]
        }
      ]
    },
    {
      "track_id": "harmonium_backing",
      "name": "Harmonium",
      "type": "Instrument",
      "instrument_preset": "Warm_Harmonium",
      "soundfont_asset": "harmonium_classical.sf2",
      "channel_settings": {
        "volume": 0.65,
        "pan": 0.3,
        "eq_low_gain": -1.5,
        "eq_mid_gain": 0.5,
        "eq_high_gain": -2.0
      },
      "notes": [
        { "tick": 0, "duration": 1920, "note": 48, "velocity": 75 },
        { "tick": 0, "duration": 1920, "note": 52, "velocity": 70 }
      ]
    }
  ]
}
```

---

## 5. Native C++ Software Interfaces (v2.0 Subsystems)

The following C++ structures establish the code-level API configurations for the AIRE v2.0 interpreter modules:

### 5.1 Performance Interpreter Definitions
```cpp
#pragma once
#include <vector>
#include <cstdint>

struct MIDINote {
    uint64_t tick;
    uint32_t duration;
    uint8_t noteNumber;
    uint8_t velocity;
    float microTimingOffsetSec; // Shift applied by humanizer
};

class PerformanceInterpreter {
public:
    // Applies micro-timing adjustments and velocity scaling based on style constraints
    static void HumanizeNotes(std::vector<MIDINote>& notes, float humanizeAmount, float tempoBpm) {
        if (humanizeAmount <= 0.0f) return;
        
        // Seeded random timing shifts (bounds: -12ms to +12ms)
        float maxShiftSec = 0.012f * humanizeAmount;
        for (auto& note : notes) {
            float shift = ((float)rand() / (float)RAND_MAX) * 2.0f - 1.0f;
            note.microTimingOffsetSec = shift * maxShiftSec;
            
            // Adjust velocity to introduce accent variance
            int velVariance = static_cast<int>(((rand() % 15) - 7) * humanizeAmount);
            note.velocity = static_cast<uint8_t>(std::max(1, std::min(127, note.velocity + velVariance)));
        }
    }
};
```

### 5.2 Dynamic Singer Vibrato & Pitch Modulator
```cpp
#pragma once
#include <cmath>

class VocalVibratoLFO {
private:
    float mPhase = 0.0f;
    float mFrequencyHz = 6.2f; // Average natural human vibrato rate
    float mDepthSemitones = 0.3f;
    float mSampleRate = 48000.0f;

public:
    void Configure(float rateHz, float depthSemitones, float sampleRate) {
        mFrequencyHz = rateHz;
        mDepthSemitones = depthSemitones;
        mSampleRate = sampleRate;
    }

    // Calculates the dynamic pitch multiplication factor for the current sample
    inline float GetPitchFactorAndStep() {
        // Evaluate LFO
        float value = sinf(mPhase);
        
        // Calculate pitch bend factor from semitones
        float bendSemitones = value * mDepthSemitones;
        float pitchFactor = powf(2.0f, bendSemitones / 12.0f);
        
        // Update phase accumulator
        mPhase += (2.0f * M_PI * mFrequencyHz) / mSampleRate;
        if (mPhase >= 2.0f * M_PI) {
            mPhase -= 2.0f * M_PI;
        }
        
        return pitchFactor;
    }
};
```

---

## 6. JNI Bridge Specification (AIRE v2.0 Extensions)

### JNI Class: `com.example.data.remote.gateway.AIREJniBridge`

This updated specification includes the new v2.0 JNI methods for Style Renderer mapping, Singer emotion adjustments, and direct diagnostic logs:

```kotlin
package com.example.data.remote.gateway

import java.nio.ByteBuffer

object AIREJniBridge {
    init {
        System.loadLibrary("surmaya_aire")
    }

    // --- Core Lifecycle (v1.3) ---
    external fun initEngine(sampleRate: Int, bufferSize: Int): Long
    external fun releaseEngine(enginePtr: Long)
    external fun startPlayback(enginePtr: Long)
    external fun pausePlayback(enginePtr: Long)
    external fun registerTelemetryBuffer(enginePtr: Long, buffer: ByteBuffer)

    // --- Performance & Humanization (v2.0) ---
    external fun setHumanizeParameters(enginePtr: Long, amount: Float, tempoBpm: Float)
    external fun applyPerformanceInterpretation(enginePtr: Long, trackId: String, notesJson: String)

    // --- Style & Personality Controllers (v2.0) ---
    external fun setMusicStyleProfile(enginePtr: Long, styleName: String)
    external fun configureInstrumentPersonality(enginePtr: Long, trackId: String, presetName: String)

    // --- Vocal Expressions Interface (v2.0) ---
    external fun configureSingerExpression(
        enginePtr: Long, 
        trackId: String, 
        emotion: String, 
        vibratoDepth: Float, 
        breathGain: Float
    )

    // --- Quality & Energy Managers (v2.0) ---
    external fun setQualityProfile(enginePtr: Long, profileName: String)
    external fun updateSchedulerConstraints(enginePtr: Long, thermalThrottle: Boolean, batterySaver: Boolean)
}
```

### JNI C++ Implementation Bindings (`AIREJniBridge.cpp`)

```cpp
#include <jni.h>
#include <string>
#include <android/log.h>
#include "NativeAudioEngine.h"

#define LOG_TAG "AIRE_JNI_v2_0"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setHumanizeParameters(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jfloat amount, jfloat tempo_bpm) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->GetPerformanceInterpreter().SetHumanizeFactors(amount, tempo_bpm);
        LOGD("JNI: Humanization set to %.2f @ %.1f BPM", amount, tempo_bpm);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setMusicStyleProfile(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring style_name) {
    if (engine_ptr && style_name) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_style = env->GetStringUTFChars(style_name, nullptr);
        engine->GetMusicStyleRenderer().LoadStyleProfile(native_style);
        LOGD("JNI: Style loaded: %s", native_style);
        env->ReleaseStringUTFChars(style_name, native_style);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_configureSingerExpression(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring track_id, 
        jstring emotion, jfloat vibrato_depth, jfloat breath_gain) {
    if (engine_ptr && track_id && emotion) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_track = env->GetStringUTFChars(track_id, nullptr);
        const char* native_emotion = env->GetStringUTFChars(emotion, nullptr);
        
        engine->GetVocalManager().ConfigureVocalExpression(
            native_track, native_emotion, vibrato_depth, breath_gain
        );
        
        LOGD("JNI: Configure vocal expression for Track %s [Emotion: %s, Vibrato: %.2f]", 
             native_track, native_emotion, vibrato_depth);
             
        env->ReleaseStringUTFChars(track_id, native_track);
        env->ReleaseStringUTFChars(emotion, native_emotion);
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setQualityProfile(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jstring profile_name) {
    if (engine_ptr && profile_name) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        const char* native_profile = env->GetStringUTFChars(profile_name, nullptr);
        engine->GetQualityManager().ApplyQualityProfile(native_profile);
        LOGD("JNI: Quality Profile applied: %s", native_profile);
        env->ReleaseStringUTFChars(profile_name, native_profile);
    }
}

}
```

---

## 7. Kotlin Architecture & Orchestrator Implementation

The Kotlin domain orchestrates the native lifecycle. The **`AIGateway`** acts as the high-level manager:

```kotlin
package com.example.data.remote.gateway

import android.content.Context
import android.os.PowerManager
import com.example.core.audio.TimelineCoordinator
import com.example.core.audio.AudioTelemetry
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AIGateway private constructor(context: Context) {
    private var nativeEnginePtr: Long = 0
    private var telemetryBuffer: ByteBuffer? = null
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    companion object {
        @Volatile
        private var instance: AIGateway? = null

        fun getInstance(context: Context): AIGateway {
            return instance ?: synchronized(this) {
                instance ?: AIGateway(context.applicationContext).also { instance = it }
            }
        }
    }

    fun initEngine(sampleRate: Int, bufferSize: Int) {
        if (nativeEnginePtr == 0L) {
            nativeEnginePtr = AIREJniBridge.initEngine(sampleRate, bufferSize)
            
            // Allocate a direct 128-byte array for VU meter values and progress updates
            telemetryBuffer = ByteBuffer.allocateDirect(128)
                .order(ByteOrder.nativeOrder())
                
            telemetryBuffer?.let {
                AIREJniBridge.registerTelemetryBuffer(nativeEnginePtr, it)
            }
        }
    }

    fun startPlayback() {
        if (nativeEnginePtr != 0L) {
            // Assess energy state before rendering audio blocks
            val inSaverMode = powerManager.isPowerSaveMode
            AIREJniBridge.updateSchedulerConstraints(nativeEnginePtr, false, inSaverMode)
            AIREJniBridge.startPlayback(nativeEnginePtr)
        }
    }

    fun pausePlayback() {
        if (nativeEnginePtr != 0L) {
            AIREJniBridge.pausePlayback(nativeEnginePtr)
        }
    }

    fun applyProjectQuality(quality: String) {
        if (nativeEnginePtr != 0L) {
            AIREJniBridge.setQualityProfile(nativeEnginePtr, quality)
        }
    }

    fun applySongStyle(style: String) {
        if (nativeEnginePtr != 0L) {
            AIREJniBridge.setMusicStyleProfile(nativeEnginePtr, style)
        }
    }

    fun configureVocalTrack(trackId: String, emotion: String, vibrato: Float, breath: Float) {
        if (nativeEnginePtr != 0L) {
            AIREJniBridge.configureSingerExpression(nativeEnginePtr, trackId, emotion, vibrato, breath)
        }
    }

    fun release() {
        if (nativeEnginePtr != 0L) {
            AIREJniBridge.releaseEngine(nativeEnginePtr)
            nativeEnginePtr = 0L
            telemetryBuffer = null
        }
    }
}
```

---

## 8. Threading & OS Priority Strategy (v2.0 Adaptations)

AIRE v2.0 introduces an adaptive threading topology designed to maintain extreme stability across various Android hardware configurations:

```
  Android OS Thermal Monitor (OS Event Callback)
                      │
                      ▼ (Thermal Level: High / Throttled)
         [ AIRE Scheduler Controller ]
                      │
         ┌────────────┴────────────┐
         ▼                         ▼
   (Reduce Voices)          (Disable Aux FX)
   - Max 32 -> 16           - Peak limiting ONLY
```

### Scheduling Priorities (Linux/Android Native)
* **Real-time Callback Thread**: Bound via standard JNI call `pthread_setschedparam` to `SCHED_FIFO` with dynamic priority matching.
* **Interpretation Background Thread**: Runs asynchronously using Kotlin Coroutines dispatcher `Dispatchers.Default` for parsing and optimizing project data before pushing to native ring buffers.
* **Asset Loading Task**: Dispatched via a dedicated single-threaded Executor to prevent blocking the JVM main thread during file read operations.

---

## 9. Folder & Package Layout (AIRE v2.0 Complete Structure)

```
/
├── app/
│   └── src/
│       ├── main/
│       │   ├── cpp/
│       │   │   ├── core/
│       │   │   │   ├── NativeAudioEngine.h        # Root engine lifecycle coordinator
│       │   │   │   ├── AudioGraphEvaluator.h      # Topological DAG analyzer
│       │   │   │   ├── LockFreeSPSCQueue.h        # Atomic synchronization structure
│       │   │   │   └── ProjectFormat.h            # .surmaya format file reader/writer
│       │   │   ├── dsp/
│       │   │   │   ├── BiquadFilter.h             # Parametric EQ filters
│       │   │   │   ├── Compressor.h               # Dynamics compressor
│       │   │   │   └── MasteringLimiter.h         # True-Peak lookahead limiter
│       │   │   ├── interpreter/
│       │   │   │   ├── PerformanceInterpreter.h   # Humanization and timings modifiers
│       │   │   │   ├── InstrumentPersonality.h    # SoundFont character modifier
│       │   │   │   ├── SingerExpressionEngine.h   # Vibrato, dynamic swell, emotion modifier
│       │   │   │   └── StyleRenderer.h            # Genre profiles (Odia, Bollywood, Bhajan)
│       │   │   ├── jni/
│       │   │   │   └── AIREJniBridge.cpp          # Multi-method bindings file
│       │   │   └── CMakeLists.txt                 # Compilation instructions
│       │   └── java/com/example/
│       │       ├── core/audio/
│       │       │   ├── QualityManager.kt          # Governs draft, studio, ultra parameters
│       │       │   └── SurmayaProjectPackager.kt  # Reads/writes .surmaya project files
│       │       └── data/remote/gateway/
│       │           ├── AIGateway.kt               # Central coordinator
│       │           └── AIREJniBridge.kt           # JNI bindings declarations
```

---

## 10. AIRE v2.0 Implementation Roadmap & Exit Criteria

```
+---------------------------------------------------------------------------------+
|                              AIRE v2.0 ROADMAP                                  |
|                                                                                 |
|  [3A.1: Low-level Engine] ────> [3A.2: Synthesis core] ────> [3A.3: DSP Mixer]  |
|                                                                                 │
|  [3A.6: Style & Plugins]  <──── [3A.5: Expression layers] <─ [3A.4: WAV Exporter]
+---------------------------------------------------------------------------------+
```

### Milestone 3A.1: Native Audio Runtime (Low-Level Boot)
* **Goal**: Build AAudio/Oboe bridge, topological audio graph, and atomic SPSC event queues.
* **Exit Criteria**: Audio callback successfully boots without XRuns. Zero heap allocations occur in the real-time thread during active callbacks.

### Milestone 3A.2: Synthesis & Instrument Foundations
* **Goal**: SoundFont (.sf2/.sf3) loading, sample interpolation, and the primary voice allocator.
* **Exit Criteria**: Clean 32-voice piano/strings playback without audible clicks or sample misalignments.

### Milestone 3A.3: DSP Mixer & Routing Engine
* **Goal**: Inline parametric EQ, dynamics compressor, auxiliary send loops, and true-peak mastering limiter.
* **Exit Criteria**: Summed output registers no digital clipping under maximum voice load; SNR maintains optimal studio targets ($\ge 110\text{ dB}$).

### Milestone 3A.4: Non-Real-Time Offline Export Pipeline
* **Goal**: Background block-by-block rendering loop, multi-format encoding wrappers (WAV, MP3, FLAC, AAC).
* **Exit Criteria**: 3-minute composition renders offline in under 15 seconds with $\text{PSNR} \ge 120\text{ dB}$ against golden standards.

### Milestone 3A.5: Performance Interpretation & Vocal Expression Layers
* **Goal**: Integrates PIL (Humanizer), IPL (Personality Modifiers), and ASRL (Vibrato LFO, Breath triggers).
* **Exit Criteria**: Static MIDI streams exhibit measurable micro-timing adjustments and natural dynamic volume/pitch shifts during active playback.

### Milestone 3A.6: Style Renderer, Asset Packager, & Platform Integrations
* **Goal**: Style profiles (Bollywood, Odia traditional, Bhajan), `.surmaya` packaging, dynamic thermal/power scheduler.
* **Exit Criteria**: Audio engine dynamically scales voice pool in response to OS thermal alerts; consolidated projects load and save successfully.
