# AIRE v1.3: Low-Level Implementation Specifications & Coding Standards
**Classification**: Professional Systems Engineering & DSP Blueprint
**Target Platform**: Android NDK (ARM64-v8a with NEON SIMD)
**Project Milestone**: Phase 3A Pre-Implementation Guardrails

This document establishes the official **Software Requirements Specification (SRS)**, **Low-Level Design (LLD)**, **JNI/IDL Specification**, **Test & Benchmark Specifications**, and **Coding Standards & Safety Regulations** for the **Audio Intelligence Rendering Engine (AIRE) v1.3** core implementation. All engineering modules under the SurMaya platform must adhere strictly to the protocols, interfaces, and safety limits defined herein.

---

## 1. Software Requirements Specification (SRS)

### 1.1 Core Intent & Boundaries
The primary objective of AIRE v1.3 is to translate a structured **Song Blueprint JSON** into high-fidelity PCM audio completely on-device.
* **Functional Inclusions**: Local sample synthesis (SoundFonts), timeline tracking, multi-channel track summing, biquad EQ, dynamics compression, brickwall limiting, automation, and offline multi-threaded rendering.
* **Explicit Exclusions**: Real-time microphone input processing, active voice synthesis/cloning (this is offloaded to the Singer engine's inference delegates), and local MIDI recording.

### 1.2 Performance & Latency Targets
* **Real-Time Buffer Latency**: $\le 10\text{ ms}$ round-trip on Tier 1 (AAudio-compatible) devices.
* **Callback Execution Window**: The real-time render callback must complete within $60\%$ of the physical buffer duration to avoid XRuns (buffer underflows). For a buffer size of 192 samples at $48\text{ kHz}$ ($4.0\text{ ms}$ window), the callback must complete in $\le 2.4\text{ ms}$.
* **Offline Render Speed**: $\ge 15\times$ real-time speed for standard 4-minute, 16-track compositions.

### 1.3 Resource Bounds
* **Peak Native Memory Allocation**: $\le 128\text{ MB}$ RAM for system cache, including the preloaded attack phases (50KB attack cache) of active instruments.
* **Disk I/O**: Strict limit of $0$ reads on the real-time audio thread. All sample streaming must utilize asynchronous pre-fetching buffers.

---

## 2. Low-Level Design (LLD)

### 2.1 Native Object Lifecycle & Memory Ownership
The Android JVM controls the high-level lifetime of the engine via the `AIGateway` singleton, which holds a pointer to the native `NativeAudioEngine` coordinator.

```
                  +-------------------------+
                  |    Kotlin AIGateway     |
                  +-------------------------+
                               |
                       (Owns life cycle)
                               v
                  +-------------------------+
                  |  NativeAudioEngine (C++)|
                  +-------------------------+
                     /         |         \
                    /          |          \
      (Strong Ref) /  (Strong Ref)  (Strong Ref)
                  v            v            v
        [AudioGraph]    [Timeline]    [SampleCache]
             |
        (Manages)
             v
     [SMAUPlugin Nodes]
```

* **Shared Resource Ownership**: Shared sample pools (e.g., loaded wave data) are managed by a central, thread-safe `SampleCache` using `std::shared_ptr<SampleData>`.
* **Lock-Free Configuration Swapping**: When tracks are added or deleted, the `AudioGraph` replaces its active list of nodes using atomic double-buffering:
  ```cpp
  struct GraphState {
      std::vector<std::shared_ptr<AudioNode>> activeNodes;
  };
  std::atomic<GraphState*> mCurrentGraphState;
  ```

### 2.2 Core Component Collaboration & Sequence Diagram

```
[Oboe/AAudio Callback]             [AudioGraph]         [VoiceManager]       [MasterBus]
          |                              |                     |                  |
          |--- Process(numSamples) ----->|                     |                  |
          |                              |--- Render(voices) ->|                  |
          |                              |                     |-- (Read Sample) -|
          |                              |<-- Summed Voices ---|                  |
          |                              |                                        |
          |                              |--- ProcessEffects() ------------------>|
          |                              |<-- Master PCM Stereo ------------------|
          |<-- Write Buffers ------------|                                        |
```

---

## 3. Interface Definition Language (IDL) & JNI Specifications

All communication between the high-level Android Java/Kotlin layers and the native C++ engine uses highly optimized JNI entry points. To minimize garbage collection overhead, floating-point telemetry and multi-track peak data are passed using direct byte buffers (`java.nio.ByteBuffer`).

### 3.1 JNI Method Specifications

#### JNI Class: `com.example.data.remote.gateway.AIGateway`

```kotlin
package com.example.data.remote.gateway

import java.nio.ByteBuffer

object AIREJniBridge {
    init {
        System.loadLibrary("surmaya_aire")
    }

    // Engine Lifecycle & Global Setup
    external fun initEngine(sampleRate: Int, bufferSize: Int): Long
    external fun releaseEngine(enginePtr: Long)
    external fun startPlayback(enginePtr: Long)
    external fun pausePlayback(enginePtr: Long)

    // Timeline, Tempo & Automation
    external fun setTempoBpm(enginePtr: Long, bpm: Double)
    external fun seekToTick(enginePtr: Long, ticks: Long)
    external fun getTimelinePositionTicks(enginePtr: Long): Long
    external fun sendTimelineEvent(enginePtr: Long, tickOffset: Long, type: Int, note: Int, velocity: Int)

    // Direct Buffer Telemetry (Lock-free VU levels & progress)
    external fun registerTelemetryBuffer(enginePtr: Long, buffer: ByteBuffer)
}
```

### 3.2 C++ JNI Implementation Bindings

```cpp
#include <jni.h>
#include <android/log.h>
#include "NativeAudioEngine.h"

#define LOG_TAG "AIRE_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_initEngine(
        JNIEnv* env, jobject thiz, jint sample_rate, jint buffer_size) {
    auto* engine = new NativeAudioEngine(static_cast<double>(sample_rate), static_cast<uint32_t>(buffer_size));
    LOGI("AIRE Engine initialized at %d Hz, buffer: %d samples", sample_rate, buffer_size);
    return reinterpret_cast<jlong>(engine);
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_releaseEngine(
        JNIEnv* env, jobject thiz, jlong engine_ptr) {
    auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
    delete engine;
    LOGI("AIRE Engine successfully released from memory.");
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_startPlayback(
        JNIEnv* env, jobject thiz, jlong engine_ptr) {
    if (enginePtr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->StartPlayback();
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_pausePlayback(
        JNIEnv* env, jobject thiz, jlong engine_ptr) {
    if (enginePtr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->PausePlayback();
    }
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_setTempoBpm(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jdouble bpm) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        engine->SetTempo(bpm);
    }
}

JNIEXPORT jlong JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_getTimelinePositionTicks(
        JNIEnv* env, jobject thiz, jlong engine_ptr) {
    if (engine_ptr) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        return static_cast<jlong>(engine->GetCurrentTimelineTicks());
    }
    return 0;
}

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_registerTelemetryBuffer(
        JNIEnv* env, jobject thiz, jlong engine_ptr, jobject byte_buffer) {
    if (engine_ptr && byte_buffer) {
        auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
        float* buffer_address = static_cast<float*>(env->GetDirectBufferAddress(byte_buffer));
        jlong capacity = env->GetDirectBufferCapacity(byte_buffer);
        engine->RegisterTelemetryChannel(buffer_address, capacity / sizeof(float));
    }
}

}
```

---

## 4. Test & Benchmark Specification

AIRE v1.3 mandates comprehensive automated verification profiles to preserve bit-exact outputs and guarantee real-time safety.

### 4.1 Golden Audio Testing (Regression Guard)
* **Goal**: Guarantee that DSP nodes produce bit-exact, click-free outputs across revisions.
* **Methodology**: 
  1. A structured 8-bar MIDI blueprint is rendered offline to a raw floating-point PCM stream (`golden_reference.raw`).
  2. During continuous integration (CI) tests, the test harness compiles and runs the rendering core over the same blueprint.
  3. The resulting stream is compared sample-by-sample. The peak signal-to-noise ratio (PSNR) must satisfy:
     $$\text{PSNR} \ge 120\text{ dB}$$

### 4.2 Real-Time Callback Profiling (Stress Suite)
To measure execution times inside the real-time audio loop, high-precision clock intervals are measured directly in native C++.

```cpp
#include <chrono>

class RealTimePerformanceMonitor {
private:
    std::chrono::high_resolution_clock::time_point mStart;
    double mAccumulatedDurationUs = 0.0;
    uint32_t mSampleCount = 0;

public:
    inline void BeginBlock() {
        mStart = std::chrono::high_resolution_clock::now();
    }

    inline void EndBlock(double maxBlockDurationUs) {
        auto end = std::chrono::high_resolution_clock::now();
        double elapsedUs = std::chrono::duration<double, std::micro>(end - mStart).count();
        mAccumulatedDurationUs += elapsedUs;
        mSampleCount++;

        if (elapsedUs > maxBlockDurationUs) {
            __android_log_print(ANDROID_LOG_WARN, "AIRE_PERF", 
                "CRITICAL WARNING: Block time exceeded budget! Limit: %.2f us, Elapsed: %.2f us [XRUN RISK]", 
                maxBlockDurationUs, elapsedUs);
        }
    }
};
```

---

## 5. Coding Standards & Safety Regulations

The real-time audio rendering thread operates under extreme timing constraints. Writing safe real-time code requires strict discipline to prevent audio glitches (XRuns).

### 5.1 Real-Time Thread Constraints (The Golden Rules)
Any code executing within the path of `ProcessBlock()` must conform to the following:
* **NO Dynamic Heap Allocations**: No calls to `malloc`, `free`, `new`, or `delete`. All working memory must be allocated during engine initialization.
* **NO File or Network I/O**: No standard stream operations (`std::ifstream`), raw file descriptors (`write`, `read`), or network sockets.
* **NO Mutex Locks**: Avoid lock operations that may block the rendering thread. Use lock-free atomics (`std::atomic`) or single-producer single-consumer ring buffers (`std::experimental::ring_buffer`) for inter-thread synchronization.
* **NO System Calls**: No logging commands that invoke blocking disk operations (e.g., standard `__android_log_print` should be buffered or run on a separate logging worker thread).

### 5.2 CPU Optimization & Vectorization
* **NEON SIMD Vectorization**: Vector operations (such as volume scaling and stereo panning) must use ARM NEON intrinsics to process four 32-bit floats simultaneously in a single cycle.
  ```cpp
  #include <arm_neon.h>

  void GainBlockNEON(float* buffer, float gain, int size) {
      int i = 0;
      float32x4_t gainVector = vdupq_n_f32(gain);
      for (; i <= size - 4; i += 4) {
          float32x4_t pcmVector = vld1q_f32(&buffer[i]);
          float32x4_t processed = vmulq_f32(pcmVector, gainVector);
          vst1q_f32(&buffer[i], processed);
      }
      // Process remaining samples sequentially
      for (; i < size; ++i) {
          buffer[i] *= gain;
      }
  }
  ```
* **Loop Unrolling**: Critical inner loops (such as the envelope decay loops) should be unrolled or marked with modern preprocessor optimization directives (`#pragma unroll`) to maximize compiler optimizations.

---

## Conclusion

The **AIRE v1.3 Low-Level Specifications** establish a rigorous technical baseline for the SurMaya Audio Engineering team. By enforcing zero heap allocations within the real-time thread, defining structured JNI interfaces, and integrating automated golden audio regression suites, SurMaya ensures its native rendering engine achieves elite stability and studio-grade audio rendering performance.
