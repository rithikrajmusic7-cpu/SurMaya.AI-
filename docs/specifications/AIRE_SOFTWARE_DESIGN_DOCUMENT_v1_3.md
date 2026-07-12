# SurMaya AI: AIRE v1.3 Software Design Document (SDD)
**Classification**: Lead Architect Technical Specification & Engineering Blueprint
**Author**: Chief Technology Officer, Lead Audio Architect, and DSP Engineer
**Baseline Version**: AIRE v1.3 (Architecture Frozen)

This Software Design Document (SDD) serves as the definitive engineering blueprint for the implementation of the **Audio Intelligence Rendering Engine (AIRE) v1.3** core. It outlines the end-to-end native audio rendering architecture for translating the SurMaya **Song Blueprint (JSON + MIDI + Performance Metadata)** into studio-grade PCM audio (WAV, MP3, FLAC, AAC) completely on-device.

---

## 1. Overall AIRE Architecture

The Audio Intelligence Rendering Engine (AIRE) v1.3 is divided into three major architectural planes: the **Kotlin/Compose Control & Presentation Plane**, the **JNI Bridge Serialization Plane**, and the **Native C++17 Audio Execution Plane**.

```
+-----------------------------------------------------------------------------------+
|                            KOTLIN UI & CONTROL LAYER                              |
|   +-----------------------+    +-------------------------+    +----------------+  |
|   |  Jetpack Compose UI   |--->|   MusicViewModel        |<---|  Project State |  |
|   |  (Timeline/Arranger)  |    |  (Audio Event Dispatch) |    |  (.surmaya)    |  |
|   +-----------------------+    +-------------------------+    +----------------+  |
+--------------------------------------------|--------------------------------------+
                                             | (JNI API Calls)
                                             v
+-----------------------------------------------------------------------------------+
|                                  JNI BRIDGE LAYER                                 |
|                      - IAiresJniBridge.cpp / AIREJniBridge.kt                     |
|                      - Event Serialization / Direct ByteBuffers                   |
+--------------------------------------------|--------------------------------------+
                                             |
                                             v
+-----------------------------------------------------------------------------------+
|                             NATIVE C++ AIRE ENGINE                                |
|                                                                                   |
|  +-----------------------------------------------------------------------------+  |
|  |                            AUDIO TIMELINE ENGINE                            |  |
|  |     [Tempo Map] ----> [Time Signature Map] ----> [Sample-Accurate Clock]    |  |
|  +---------------------------------------|-------------------------------------+  |
|                                          v                                        |
|  +-----------------------------------------------------------------------------+  |
|  |                              AUDIO GRAPH NODE                               |  |
|  |  +--------------------+  +----------------------+  +---------------------+  |  |
|  |  | Vocal Synth Node   |  | Instrument Sampler   |  | Procedural Wav Node |  |  |
|  |  +---------|----------+  +-----------|----------+  +----------|----------+  |  |
|  |            v                         v                        v             |  |
|  |     [Voice Manager]           [Polyphony Steal]         [Sample Cache]      |  |
|  +---------------------------------------|-------------------------------------+  |
|                                          v                                        |
|  +-----------------------------------------------------------------------------+  |
|  |                              MIXING & BUS STUDIO                            |  |
|  |  [Channel Strip 1]      [Channel Strip 2]      [Aux Sends]     [Busses 1-8] |  |
|  +---------------------------------------|-------------------------------------+  |
|                                          v                                        |
|  +-----------------------------------------------------------------------------+  |
|  |                               MASTER BUS & DSP                              |  |
|  |  - EQ (3-Band Parametric)      - Compressor (Lookahead)      - Brickwall    |  |
|  +---------------------------------------|-------------------------------------+  |
|                                          v                                        |
|  +-----------------------------------------------------------------------------+  |
|  |                             RENDERING PIPELINE                              |  |
|  |  +-------------------------------------+ +--------------------------------+  |  |
|  |  |        REAL-TIME PLAYBACK           | |       OFFLINE FILE EXPORT      |  |  |
|  |  |  - AAudio Low-Latency Client        | |  - Block-by-Block Processing   |  |  |
|  |  |  - High-Priority Audio Thread (2.9) | |  - Multi-threaded Encoding     |  |  |
|  |  +-------------------------------------+ +--------------------------------+  |  |
|  +-----------------------------------------------------------------------------+  |
+-----------------------------------------------------------------------------------+
```

### Architectural Plane Decoupling
1. **Control & Presentation Plane**: Manages state, handles asynchronous playback triggers, schedules background render tasks, and receives real-time telemetry (VU meters, sample play-head updates).
2. **Serialization Plane**: Employs JNI to transmit high-throughput event queues and telemetry buffers, minimizing JNI boundary crossings using shared JVM native buffers (`DirectByteBuffer`).
3. **Execution Plane**: An offline-first, highly optimized, non-blocking C++ DSP core that owns the audio graph and synthesizes buffers on demand.

---

## 2. Module-by-Module Design

AIRE is structured into modular subsystems, each with a single responsibility.

| Module | Core Responsibility |
| --- | --- |
| **AudioGraph** | Manages signal nodes in a Directed Acyclic Graph (DAG) and schedules buffer processing. |
| **Timeline Engine** | Maintains a sample-accurate clock, translating PPQN ticks into physical sample offsets. |
| **Tempo Map Engine** | Tracks musical tempo adjustments over time, enabling dynamic BPM ramps and changes. |
| **Sample Cache** | Manages high-performance loading and lifecycle of SoundFont and raw WAV files in RAM. |
| **Voice Manager** | Allocates and releases voices, managing polyphony stealing under heavy CPU loads. |
| **Vocal Render Engine** | Integrates AI-generated vocal phonemes and synthesizes high-fidelity singing voices. |
| **DSP Core** | Contains foundational biquad filters, dynamic range compressors, delays, and reverbs. |
| **Mixer Bus** | Handles channel summing, track routing, auxiliary sends, and stereophonic panning. |
| **Offline Renderer** | Runs block-by-block non-real-time audio synthesis to bypass platform hardware constraints. |
| **Audio Encoder** | Coordinates WAV, MP3 (LAME), FLAC, and AAC native encoders. |

---

## 3. Data Flow Diagrams & Data Schemas

### High-Level Data Flow: Song Blueprint to PCM Output
```
[Song Blueprint JSON]
         │
         ▼
[Timeline Event Queue (MIDI/Automation)]
         │
         ▼
[AudioGraph Renderer] <─── [Sample Cache (.sf2 / .wav)]
         │
         ▼
[Voice Summing / Vocal Synthesis]
         │
         ▼
[Mixer Channel Strips] ───> [Aux Sends (Reverb/Delay)]
         │
         ▼
[Master Bus Summing]
         │
         ▼
[DSP Mastering Chain (EQ -> Comp -> Brickwall Limiter)]
         │
         ▼
[Render Target: AAudio Buffer (Real-time) OR Multi-Format Encoder (Offline File)]
```

### Data Schema: JSON Song Blueprint Definition
```json
{
  "project_id": "sm_project_7302a",
  "tempo_map": [
    { "tick": 0, "bpm": 120.0, "time_signature": "4/4" },
    { "tick": 15360, "bpm": 135.0, "time_signature": "4/4" }
  ],
  "tracks": [
    {
      "track_id": "vocal_lead",
      "type": "vocal",
      "channel_strip": { "volume": 0.8, "pan": 0.5, "eq_low": -2.0, "eq_mid": 1.5, "eq_high": 0.0 },
      "automation": [
        { "tick": 0, "parameter": "pan", "value": 0.3 },
        { "tick": 1920, "parameter": "pan", "value": 0.7 }
      ],
      "events": [
        { "tick": 0, "duration": 480, "note": 60, "velocity": 100, "phonemes": "sa" },
        { "tick": 480, "duration": 480, "note": 62, "velocity": 100, "phonemes": "re" }
      ]
    },
    {
      "track_id": "backing_synth",
      "type": "instrument",
      "soundfont_path": "/assets/soundfonts/classic_piano.sf2",
      "channel_strip": { "volume": 0.7, "pan": 0.2, "eq_low": 2.0, "eq_mid": 0.0, "eq_high": -1.0 },
      "events": [
        { "tick": 0, "duration": 960, "note": 48, "velocity": 90, "phonemes": "" },
        { "tick": 960, "duration": 960, "note": 52, "velocity": 90, "phonemes": "" }
      ]
    }
  ]
}
```

---

## 4. Audio Graph Architecture & Evaluation Engine

The **AudioGraph** is a Directed Acyclic Graph (DAG) constructed from `AudioNode` structures.

```
                  +------------------------+
                  |  AudioGraph Evaluator  |
                  +------------------------+
                               |
                        (Traverses DAG)
                               v
            +-------------------------------------+
            |         Topological Sort            |
            |  Node 1 -> Node 2 -> Mixer -> Mast  |
            +-------------------------------------+
                               |
                   (Evaluates Block-by-Block)
                               v
                  +------------------------+
                  |      ProcessBlock      |
                  +------------------------+
```

### Node Interface Definition
```cpp
class AudioNode {
public:
    virtual ~AudioNode() = default;
    
    // Process input buffers and write to output buffers.
    // Floating-point arrays are flat-mapped, contiguous buffers.
    virtual void Process(const float** inputs, float** outputs, int numChannels, int numSamples) = 0;
    
    virtual void Reset() = 0;
    virtual bool IsActive() const = 0;
    virtual const char* GetNodeId() const = 0;
};
```

### Lock-Free Node Swapping
To add, remove, or modify processing connections in the graph without locking the real-time thread, AIRE utilizes a double-buffered layout managed by an atomic pointer.

```cpp
struct GraphTopology {
    std::vector<AudioNode*> sortedNodes;
    int nodeCount;
};

class AudioGraphEvaluator {
private:
    std::atomic<GraphTopology*> mActiveTopology;
    GraphTopology* mShadowTopology;

public:
    void ProcessBlock(float** outputBuffers, int numChannels, int numSamples) {
        GraphTopology* topology = mActiveTopology.load(std::memory_order_acquire);
        for (int i = 0; i < topology->nodeCount; ++i) {
            AudioNode* node = topology->sortedNodes[i];
            if (node->IsActive()) {
                node->Process(nullptr, outputBuffers, numChannels, numSamples);
            }
        }
    }

    void CommitTopologySwap(GraphTopology* newTopology) {
        GraphTopology* oldTopology = mActiveTopology.exchange(newTopology, std::memory_order_acq_rel);
        // Safely delete the old topology on a background cleanup thread
        ScheduleForDeferredDeletion(oldTopology);
    }
};
```

---

## 5. Rendering Pipeline

The rendering pipeline must adapt seamlessly between high-priority, real-time playback and maximum-speed offline file synthesis.

```
                                  +-------------------+
                                  | Playback Trigger  |
                                  +---------+---------+
                                            |
                                   Is Real-Time Playback?
                                  /                     \
                             [YES]                       [NO]
                             /                               \
                            v                                 v
               +-------------------------+       +-------------------------+
               |  AAudio/Oboe Engine     |       |  Offline Render Engine  |
               |  (SCHED_FIFO, Priority) |       |  (Worker Thread Pool)   |
               +------------+------------+       +------------+------------+
                            |                                 |
                            v                                 v
               +-------------------------+       +-------------------------+
               |  Low-Latency Audio CB   |       | Block-by-Block Export   |
               |  (numSamples = 192)     |       | (numSamples = 1024)     |
               +-------------------------+       +-------------------------+
```

### 1. Real-Time Playback Pipeline
* **Driver Target**: AAudio (falling back to OpenSL ES on Android 7.x and older devices).
* **Clock Sync**: Synchronized against the physical DAC. The AAudio thread pulls audio blocks from the `AudioGraph` on-demand using small, high-frequency buffer segments (e.g., 192 samples at 48kHz, providing a 4ms callback interval).
* **Scheduler Priority**: Thread set to `SCHED_FIFO` with a target priority of 99 using a direct native `pthread` call.

### 2. Offline Export Pipeline
* **Driver Target**: Bypasses the audio hardware completely. Runs sequentially inside a dedicated background thread pool.
* **Block Engine**: Pulls much larger chunk sizes (e.g., 1024 to 2048 samples) to minimize scheduling overhead and maximize cache efficiency.
* **Throttling**: Executes at maximum CPU capacity. Samples are pushed directly into native encoders (LAME, FDK AAC, FLAC) without waiting for real-time play-head intervals.

---

## 6. Threading Topology & Owner Rules

To guarantee glitch-free playback, AIRE divides thread execution into isolated pools with strict interaction boundaries.

```
  +-------------------+      (Push Events)      +--------------------+
  | JVM/Kotlin Thread | ──────────────────────> | Lock-Free Queue    |
  +---------+---------+                         +---------+----------+
            |                                             |
     (Starts/Stops)                                (Reads Events)
            |                                             |
            v                                             v
  +-------------------+                         +--------------------+
  | Offline Renderer  |                         |  AAudio Callback   |
  +-------------------+                         +--------------------+
```

### Thread Descriptions & Priorities
1. **JVM Control Thread**: Dispatches player transport (Play, Pause, Stop, Seek) to the native runtime via JNI. Priority: `THREAD_PRIORITY_DEFAULT`.
2. **Native Real-Time Thread**: Driven by the hardware callback. Priority: `SCHED_FIFO` (99).
3. **Native Offline Exporter Thread**: Bound to a low-priority background OS class to prevent starving the UI thread or blocking core services. Priority: `ANDROID_PRIORITY_BACKGROUND`.
4. **Asynchronous I/O Workers**: Background threads that read instrument sample files into RAM caches. Priority: `ANDROID_PRIORITY_BACKGROUND`.

### Real-Time Concurrency and Callback Constraints
```cpp
// Strictly FORBIDDEN inside the Real-Time Callback
void* p = malloc(size); // CRITICAL FAIL: Invokes dynamic kernel allocations
std::unique_lock<std::mutex> lock(mMutex); // CRITICAL FAIL: May lead to priority inversion
__android_log_print(ANDROID_LOG_INFO, "Tag", "Log"); // CRITICAL FAIL: Blocking write syscall
```
All system allocations, file accesses, and locking primitives must be strictly decoupled from the physical callback loop. The callback loop must interact only with lock-free data structures and pre-allocated memory buffers.

---

## 7. Buffering & Cache Alignment Strategy

AIRE structures its audio and event channels to perfectly align with modern ARM Cortex architecture caches.

```
       Cache Line Alignment (64-Bytes)
       +------------------------------------+
       | Float Buffer [0]  (Aligned to 64)  |
       +------------------------------------+
       | Float Buffer [1]                   |
       +------------------------------------+
       | Float Buffer [2]                   |
       +------------------------------------+
```

### Cache-Aligned Audio Memory Allocations
Standard memory allocators do not guarantee that starting pointers align perfectly with CPU cache lines. To prevent "cache line splitting" and optimize ARM NEON vector instructions, all internal audio blocks are aligned to 64-byte boundaries.

```cpp
#include <cstdlib>

inline float* AllocateAlignedAudioBuffer(size_t numSamples) {
    void* ptr = nullptr;
    // Align allocations to 64-byte boundaries (standard ARM L1/L2 cache line size)
    int result = posix_memalign(&ptr, 64, numSamples * sizeof(float));
    if (result != 0) return nullptr;
    return static_cast<float*>(ptr);
}

inline void FreeAlignedAudioBuffer(float* buffer) {
    free(buffer);
}
```

### Single-Producer Single-Consumer Lock-Free Queue
To pass events safely from the high-level JVM controller to the real-time processing thread, AIRE implements an array-based SPSC ring buffer.

```cpp
template <typename T, size_t Capacity>
class LockFreeSPSCQueue {
private:
    alignas(64) std::atomic<size_t> mWriteHead{0};
    alignas(64) std::atomic<size_t> mReadHead{0};
    T mRingBuffer[Capacity];

public:
    bool Push(const T& item) {
        const size_t writeIndex = mWriteHead.load(std::memory_order_relaxed);
        const size_t readIndex = mReadHead.load(std::memory_order_acquire);
        
        if ((writeIndex + 1) % Capacity == readIndex) {
            return false; // Queue is full
        }
        
        mRingBuffer[writeIndex] = item;
        mWriteHead.store((writeIndex + 1) % Capacity, std::memory_order_release);
        return true;
    }

    bool Pop(T& item) {
        const size_t readIndex = mReadHead.load(std::memory_order_relaxed);
        const size_t writeIndex = mWriteHead.load(std::memory_order_acquire);
        
        if (readIndex == writeIndex) {
            return false; // Queue is empty
        }
        
        item = mRingBuffer[readIndex];
        mReadHead.store((readIndex + 1) % Capacity, std::memory_order_release);
        return true;
    }
};
```

---

## 8. Native Performance Optimization Plan

### 1. Vectorization (ARM NEON SIMD)
AIRE leverages ARM NEON intrinsics to process four 32-bit floats simultaneously in a single processor cycle.

```cpp
#include <arm_neon.h>

void ApplyTrackGainSIMD(float* buffer, float gain, int numSamples) {
    int i = 0;
    float32x4_t gainVector = vdupq_n_f32(gain);
    
    // Process four samples per cycle
    for (; i <= numSamples - 4; i += 4) {
        float32x4_t pcmVector = vld1q_f32(&buffer[i]);
        float32x4_t processed = vmulq_f32(pcmVector, gainVector);
        vst1q_f32(&buffer[i], processed);
    }
    
    // Process remaining samples sequentially
    for (; i < numSamples; ++i) {
        buffer[i] *= gain;
    }
}
```

### 2. Manual Loop Unrolling
Critical rendering structures, such as envelope decay filters and linear interpolation routines, are manual loop-unrolled (processing groups of 4 or 8 segments consecutively) to minimize branch prediction misses.

### 3. Compiler Options (`CMakeLists.txt`)
AIRE compiles with the highest optimization tier flags specifically targeted at ARMv8-A architectures:
```cmake
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -Ofast -mfloat-abi=softfp -mfpu=neon-vfpv4 -funroll-loops -ffast-math")
```

---

## 9. Folder Structure

AIRE source files are strictly structured into dedicated native and Android execution subdirectories:

```
/
├── app/
│   └── src/
│       ├── main/
│       │   ├── cpp/
│       │   │   ├── core/               # Native Clock, Graph Evaluator, SPSC Queue
│       │   │   ├── dsp/                # Filters, Reverbs, Dynamic Processors
│       │   │   ├── encoders/           # WAV, MP3, FLAC, AAC Wrappers
│       │   │   ├── jni/                # JNI Registration, Signatures, Buffer Maps
│       │   │   ├── sampler/            # SoundFont Parser, Streaming Engines
│       │   │   └── CMakeLists.txt      # Root CMake Compilation Directives
│       │   └── java/com/example/
│       │       ├── data/remote/gateway/ # AIGateway and AIREJniBridge Definitions
│       │       ├── ui/viewmodel/       # MusicViewModel and State Managers
│       │       └── ui/screens/         # Jetpack Compose UI Screens
│       └── test/java/com/example/      # Test Suites and Performance Benchmarks
└── docs/
    └── specifications/                 # LLD, SRS, and SDD Spec Folders
```

---

## 10. Kotlin Package Structure

All Android-side classes follow a modular Clean Architecture package configuration:

```
com.example.surmaya
│
├── core
│   ├── audio
│   │   ├── TimelineCoordinator.kt      # Sample-accurate clock and PPQN calculators
│   │   ├── AudioTelemetry.kt           # VU telemetry data parsing models
│   │   └── ExtEncoderConfig.kt         # Bitrate and format configuration models
│   └── platform
│       └── AudioExportService.kt       # Background foreground service for export tasks
│
├── data
│   └── remote
│       └── gateway
│           ├── AIGateway.kt            # Core engine instance manager (Singleton)
│           └── AIREJniBridge.kt        # JNI Bridge external definitions
│
└── ui
    ├── viewmodel
    │   └── MusicViewModel.kt           # Exposes rendering triggers and project flows
    └── screens
        └── studio
            └── StudioScreen.kt         # DAW and Master Console UI
```

---

## 11. Kotlin Gradle Module Architecture

The `app/build.gradle.kts` configuration must integrate the external JNI source paths, specify targeting constraints for performance optimization, and compile the native core:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aistudio.surmaya.aire"
        minSdk = 26
        targetSdk = 34
        
        ndk {
            abiFilters.addAll(setOf("arm64-v8a"))
        }

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++17", "-O3", "-Ofast")
                arguments("-DANDROID_ARM_NEON=ON")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
}
```

---

## 12. Native C++ Module Structure & CMakeLists.txt

The native code uses CMake to compile the various subsystems into a single high-performance library: `libsurmaya_aire.so`.

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("surmaya_aire")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Optimization Compiler Flags
add_compile_options(-O3 -Ofast -mfloat-abi=softfp -mfpu=neon -funroll-loops -ffast-math)

include_directories(
    src/main/cpp/core
    src/main/cpp/dsp
    src/main/cpp/encoders
    src/main/cpp/sampler
    src/main/cpp/jni
)

file(GLOB_RECURSE SOURCE_FILES
    "src/main/cpp/core/*.cpp"
    "src/main/cpp/dsp/*.cpp"
    "src/main/cpp/encoders/*.cpp"
    "src/main/cpp/sampler/*.cpp"
    "src/main/cpp/jni/*.cpp"
)

add_library(surmaya_aire SHARED ${SOURCE_FILES})

# Link android logging and openSL/AAudio APIs
find_library(log-lib log)
find_library(aaudio-lib aaudio)
find_library(oboe-lib oboe)

target_link_libraries(surmaya_aire
    ${log-lib}
    ${aaudio-lib}
    ${oboe-lib}
)
```

---

## 13. JNI Bridge Design, Signatures & ByteBuffer Telemetry

Communication between Kotlin and C++ avoids expensive object mapping. Instead, track levels, processing progress, and telemetry parameters are updated directly via `java.nio.ByteBuffer` wrappers mapped in native memory.

```
+-------------------------------------------------------------------------+
|                              JVM MEMORY                                 |
|  +-------------------------------------------------------------------+  |
|  |  DirectByteBuffer (Allocated in Java)                              |  |
|  |  - Address: 0x7F02A000                                            |  |
|  |  - Mapped directly to native memory pointer                       |  |
|  +-------------------------------------------------------------------+  |
+------------------------------------+------------------------------------+
                                     |
                         No JNI Boundary Copy Overhead
                                     |
                                     v
+------------------------------------+------------------------------------+
|                             NATIVE C++                                  |
|  +-------------------------------------------------------------------+  |
|  |  env->GetDirectBufferAddress() -> 0x7F02A000                      |  |
|  |  - Updates elements in real-time (No copies, zero memory leaks)  |  |
|  +-------------------------------------------------------------------+  |
+-------------------------------------------------------------------------+
```

### JNI Header Method Definitions
```cpp
#include <jni.h>

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_initEngine(
    JNIEnv* env, jobject thiz, jint sample_rate, jint buffer_size);

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_releaseEngine(
    JNIEnv* env, jobject thiz, jlong engine_ptr);

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_startPlayback(
    JNIEnv* env, jobject thiz, jlong engine_ptr);

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_pausePlayback(
    JNIEnv* env, jobject thiz, jlong engine_ptr);

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_sendMIDIEvent(
    JNIEnv* env, jobject thiz, jlong engine_ptr, jint status, jint note, jint velocity);

JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_registerTelemetryBuffer(
    JNIEnv* env, jobject thiz, jlong engine_ptr, jobject byte_buffer);

}
```

### Direct Telemetry Sharing Implementation
```cpp
JNIEXPORT void JNICALL
Java_com_example_data_remote_gateway_AIREJniBridge_registerTelemetryBuffer(
    JNIEnv* env, jobject thiz, jlong engine_ptr, jobject byte_buffer) {
    if (!engine_ptr || !byte_buffer) return;
    
    // Extract direct address of Kotlin ByteBuffer
    void* bufferAddress = env->GetDirectBufferAddress(byte_buffer);
    jlong capacity = env->GetDirectBufferCapacity(byte_buffer);
    
    auto* engine = reinterpret_cast<NativeAudioEngine*>(engine_ptr);
    engine->SetSharedTelemetryBuffer(static_cast<float*>(bufferAddress), static_cast<size_t>(capacity / sizeof(float)));
}
```

---

## 14. Export Pipeline & Audio Encoder Envelope

AIRE packages raw floating-point streams directly into structural audio formats completely on-device, bypassing dynamic disk caches.

```
[Float PCM Buffer]
        │
        ├──> [WAV Exporter] ──────> [WAV File (16/24/32-bit)]
        │
        ├──> [LAME Native MP3] ───> [MP3 File (up to 320 kbps)]
        │
        └──> [FDK AAC Encoder] ───> [M4A/AAC File]
```

### Native MP3 Encoding Wrapper using LAME
```cpp
#include <lame/lame.h>

class MP3Encoder {
private:
    lame_global_flags* mGf = nullptr;

public:
    MP3Encoder(int sampleRate, int numChannels, int bitRateKbps) {
        mGf = lame_init();
        lame_set_in_samplerate(mGf, sampleRate);
        lame_set_num_channels(mGf, numChannels);
        lame_set_brate(mGf, bitRateKbps);
        lame_set_quality(mGf, 2); // 2 = High Quality
        lame_init_params(mGf);
    }

    ~MP3Encoder() {
        if (mGf) lame_close(mGf);
    }

    int EncodeBlock(const float* left, const float* right, int numSamples, unsigned char* mp3Buffer, int maxOutputSize) {
        return lame_encode_buffer_ieee_float(mGf, left, right, numSamples, mp3Buffer, maxOutputSize);
    }

    int Flush(unsigned char* mp3Buffer, int maxOutputSize) {
        return lame_encode_flush(mGf, mp3Buffer, maxOutputSize);
    }
};
```

---

## 15. Offline Block-by-Block PCM Rendering Workflow

To avoid glitching under massive track-summing loads, the offline renderer operates in non-real-time segments:

```cpp
void RenderProjectToPCM(NativeAudioEngine* engine, const char* outputPath, uint64_t totalDurationSamples) {
    FILE* file = fopen(outputPath, "wb");
    if (!file) return;

    // Structure a simple WAV file header
    WriteWavHeader(file, 48000, 2, totalDurationSamples);

    constexpr int blockSize = 1024;
    float* leftChannel = AllocateAlignedAudioBuffer(blockSize);
    float* rightChannel = AllocateAlignedAudioBuffer(blockSize);
    float* outStereo[2] = { leftChannel, rightChannel };

    uint64_t samplesRendered = 0;
    while (samplesRendered < totalDurationSamples) {
        int currentChunk = static_cast<int>(std::min(static_cast<uint64_t>(blockSize), totalDurationSamples - samplesRendered));
        
        // Render block offline without device drivers
        engine->ProcessOfflineBlock(outStereo, currentChunk);

        // Convert 32-bit float PCM buffers to 16-bit signed integer streams
        for (int i = 0; i < currentChunk; ++i) {
            int16_t lVal = static_cast<int16_t>(std::max(-32768.0f, std::min(32767.0f, outStereo[0][i] * 32767.0f)));
            int16_t rVal = static_cast<int16_t>(std::max(-32768.0f, std::min(32767.0f, outStereo[1][i] * 32767.0f)));
            fwrite(&lVal, sizeof(int16_t), 1, file);
            fwrite(&rVal, sizeof(int16_t), 1, file);
        }
        samplesRendered += currentChunk;
    }

    FreeAlignedAudioBuffer(leftChannel);
    FreeAlignedAudioBuffer(rightChannel);
    fclose(file);
}
```

---

## 16. DSP Effects Pipeline

AIRE integrates an inline processing stack for each channel strip.

### 1. 3-Band Parametric EQ (Direct-Form II Transposed Biquad Filters)
```cpp
class BiquadFilter {
private:
    float b0 = 1.0f, b1 = 0.0f, b2 = 0.0f, a1 = 0.0f, a2 = 0.0f;
    float w1 = 0.0f, w2 = 0.0f; // Delayed states

public:
    void SetPeakingEQ(float cutoffFreq, float sampleRate, float Q, float gainDb) {
        float A = powf(10.0f, gainDb / 40.0f);
        float omega = 2.0f * M_PI * cutoffFreq / sampleRate;
        float alpha = sinf(omega) / (2.0f * Q);
        float cosw = cosf(omega);

        float b0_raw = 1.0f + alpha * A;
        b0 = b0_raw / (1.0f + alpha / A);
        b1 = (-2.0f * cosw) / (1.0f + alpha / A);
        b2 = (1.0f - alpha * A) / (1.0f + alpha / A);
        a1 = (-2.0f * cosw) / (1.0f + alpha / A);
        a2 = (1.0f - alpha / A) / (1.0f + alpha / A);
    }

    inline float Process(float input) {
        float output = b0 * input + w1;
        w1 = b1 * input - a1 * output + w2;
        w2 = b2 * input - a2 * output;
        return output;
    }
};
```

### 2. Envelope-Detector Dynamic Compressor
```cpp
class Compressor {
private:
    float mThresholdDb = -12.0f;
    float mRatio = 4.0f;
    float mAttackTimeSec = 0.01f;
    float mReleaseTimeSec = 0.1f;
    float mEnvelopeDb = -96.0f;

public:
    float ProcessSample(float input) {
        float absVal = fabsf(input);
        float sampleDb = absVal > 1e-5f ? 20.0f * log10f(absVal) : -96.0f;
        
        // Attack/Release envelope tracking
        float theta = sampleDb > mEnvelopeDb ? mAttackTimeSec : mReleaseTimeSec;
        mEnvelopeDb = (1.0f - theta) * mEnvelopeDb + theta * sampleDb;
        
        float gainReductionDb = 0.0f;
        if (mEnvelopeDb > mThresholdDb) {
            gainReductionDb = (mThresholdDb - mEnvelopeDb) * (1.0f - 1.0f / mRatio);
        }
        
        float scaleFactor = powf(10.0f, gainReductionDb / 20.0f);
        return input * scaleFactor;
    }
};
```

---

## 17. Channel Strip & Mixer Bus Routing Architecture

Tracks are summed in a highly optimized structure to prevent high-amplitude clipping.

```
Track 1 Node ---> Volume/Pan Multipliers ---> Aux 1 Send Bus ────> Mixer Summed Bus
Track 2 Node ---> Volume/Pan Multipliers ---> Aux 2 Send Bus ────^
```

```cpp
class ChannelStrip {
public:
    float volume = 0.8f;
    float pan = 0.5f; // 0.0 = Hard Left, 1.0 = Hard Right
    float auxSend1 = 0.0f;
    float auxSend2 = 0.0f;
    BiquadFilter eqLow;
    BiquadFilter eqMid;
    BiquadFilter eqHigh;

    void ProcessChannel(float* inputBuffer, float* mainSumLeft, float* mainSumRight, 
                        float* auxSum1, float* auxSum2, int numSamples) {
        // Calculate stereo panning gains using constant power trigonometric curves
        float gainLeft = cosf(pan * M_PI_2) * volume;
        float gainRight = sinf(pan * M_PI_2) * volume;

        for (int i = 0; i < numSamples; ++i) {
            // Apply 3-Band Parametric Equalization
            float sample = inputBuffer[i];
            sample = eqLow.Process(sample);
            sample = eqMid.Process(sample);
            sample = eqHigh.Process(sample);

            // Sum to Aux send lines
            if (auxSum1) auxSum1[i] += sample * auxSend1;
            if (auxSum2) auxSum2[i] += sample * auxSend2;

            // Route and sum to Master bus channels
            mainSumLeft[i] += sample * gainLeft;
            mainSumRight[i] += sample * gainRight;
        }
    }
};
```

---

## 18. Master Bus & Mastering Architecture

The Master Bus acts as the final quality gate before DAC routing or offline storage encoding. It features a lookahead brickwall limiter with true-peak detection to guarantee distortion-free audio.

```
Master Summed Buffer
        │
        ▼
[Oversampling / True-Peak Interpolation (4x FIR Filter)]
        │
        ▼
[Lookahead Delay Line (5 ms Buffer)]
        │
        ▼
[Dynamic Peak Attenuator (Ceiling: -0.1 dBFS)]
        │
        ▼
Output Buffer
```

```cpp
class MasteringLimiter {
private:
    float mCeilingDb = -0.1f;
    float mCeilingLinear = powf(10.0f, -0.1f / 20.0f);
    int mLookaheadSamples = 240; // 5ms at 48kHz
    std::vector<float> mDelayBufferL;
    std::vector<float> mDelayBufferR;
    int mWriteIndex = 0;

public:
    MasteringLimiter() {
        mDelayBufferL.resize(mLookaheadSamples, 0.0f);
        mDelayBufferR.resize(mLookaheadSamples, 0.0f);
    }

    void ProcessMaster(float* masterL, float* masterR, int numSamples) {
        for (int i = 0; i < numSamples; ++i) {
            // Push incoming samples to lookahead delay buffers
            float rawL = masterL[i];
            float rawR = masterR[i];
            
            mDelayBufferL[mWriteIndex] = rawL;
            mDelayBufferR[mWriteIndex] = rawR;
            
            // Analyze the lookahead window to predict peaks
            float absoluteMax = std::max(fabsf(rawL), fabsf(rawR));
            float attenuation = 1.0f;
            
            if (absoluteMax > mCeilingLinear) {
                attenuation = mCeilingLinear / absoluteMax;
            }
            
            // Retrieve delayed samples and apply computed attenuation
            int readIndex = (mWriteIndex + 1) % mLookaheadSamples;
            masterL[i] = mDelayBufferL[readIndex] * attenuation;
            masterR[i] = mDelayBufferR[readIndex] * attenuation;
            
            mWriteIndex = (mWriteIndex + 1) % mLookaheadSamples;
        }
    }
};
```

---

## 19. Android-Specific Energy & Memory Optimization Strategies

1. **Avoid Garbage Collection Triggers**: No temporary objects are allocated inside Kotlin loop blocks. Frame telemetry arrays are recycled using a reusable Direct Byte Buffer.
2. **Audio Track Recycling**: The high-level audio playback tracker does not recreate instances of `AudioTrack` between consecutive track selections. It recycles a single, pre-initialized track.
3. **NEON Fused Multiply-Accumulate**: All gain multiplying operations are optimized with `vmlaq_f32` instructions to execute vector multiplication and addition in a single hardware step.
4. **Offline Export Thread Scheduling**: Background rendering is executed inside a foreground service using standard OS priorities (`Process.THREAD_PRIORITY_BACKGROUND`) to prevent freezing the UI and ensure the system does not terminate the app during massive processing tasks.

---

## 20. Comprehensive Roadmap

```
+---------------------------------------------------------------------------------+
|                                 AIRE ROADMAP                                    |
|                                                                                 |
|  [3A.1: Native Runtime] -> [3A.2: SoundFont Synth] -> [3A.3: Studio DSP FX]     |
|                                                                                 |
|  [3A.6: Plugin/SMAU]   <- [3A.5: Timeline / Automation]  <- [3A.4: Multi Enc]   |
+---------------------------------------------------------------------------------+
```

### Milestone 3A.1: Native Audio Runtime
* **Scope**: Build the Oboe/AAudio bridge, topological graph scheduler, and lock-free SPSC event loop.
* **Exit Criteria**: Audio callback successfully boots without XRuns under validation workloads. Zero heap allocations are made inside the callback thread.

### Milestone 3A.2: Synthesis Runtime
* **Scope**: Implement SoundFont (`.sf2` / `.sf3`) instrument sample loading, Hermite-spline interpolation, and voice allocation engines.
* **Exit Criteria**: Simultaneous 32-voice piano synthesis completes without audible dropouts on mid-range Android devices.

### Milestone 3A.3: DSP Production Engine
* **Scope**: Build 3-band parametric biquad EQs, Lookahead dynamic compressors, stereo feedback delays, and auxiliary sends.
* **Exit Criteria**: Real-time mixing summing passes the regression test suite with the signal-to-noise ratio remaining constant.

### Milestone 3A.4: Offline Export Pipeline
* **Scope**: Implement the offline block-by-block rendering loop. Integrate native LAME, FDK AAC, and FLAC library compilation pipelines.
* **Exit Criteria**: A standard 3-minute, 8-track blueprint exports to MP3 (320kbps) in under 12 seconds with $\text{PSNR} \ge 120\text{ dB}$ against the golden PCM reference.

### Milestone 3A.5: DAW Infrastructure
* **Scope**: Implement the sample-accurate timeline coordinator, PPQN tick trackers, tempo events mapping, and automation engines.
* **Exit Criteria**: Complex time-signature automation and sample-to-tick mappings complete with zero frame misalignment over 10-minute rendering blocks.

### Milestone 3A.6: Developer Platform
* **Scope**: Build the SMAU native plugin architecture, diagnostic instrumentation telemetry, and profiling benchmark suite.
* **Exit Criteria**: Custom C++ plugin extensions load dynamically and pass real-time stability benchmarks across all targeted Android architectures.
