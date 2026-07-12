# 🚀 SurMaya AI - Performance Benchmarks

**Document Reference**: SRMY-QA-BENCH-LAB-04  
**Status**: ACTIVE  
**Last Updated**: July 11, 2026  
**Audience**: Systems Architects & Performance Analysts  

---

## 🏛️ 1. Overview & Objectives

To maintain our brand identity as a **Professional Offline AI Music Studio**, **SurMaya AI** must execute complex DSP, audio mixing, and export pipelines smoothly on resource-constrained physical devices. 

This document establishes the official performance benchmarks, latency standards, and stability thresholds that the application must maintain during **Internal Device Acceptance Testing**.

---

## ⏱️ 2. Offline Export Rendering Speed-up Factor

This benchmark measures how fast the engine renders a full audio project relative to its real-time duration.

$$\text{Speed-up Factor} = \frac{\text{Project Duration (seconds)}}{\text{Export Render Time (seconds)}}$$

### Baseline Test Scenario
* **Audio Length**: 5-Minute Stereo Project (PCM 44.1kHz / 24-bit)
* **Track Configuration**: 8-Track Timeline with EQ, Pan, and Limiter processing.

### Expected Performance Thresholds

| Output File Format | Tier-1 (Low-End) | Tier-2 (Mid-Range) | Tier-3 (Flagship) |
| :--- | :--- | :--- | :--- |
| **WAV (24-bit Lossless)** | $8.0\times$ ($\le 37.5$s) | $18.0\times$ ($\le 16.6$s) | $32.0\times$ ($\le 9.4$s) |
| **MP3 (320kbps CBR)** | $6.0\times$ ($\le 50.0$s) | $15.0\times$ ($\le 20.0$s) | $26.0\times$ ($\le 11.5$s) |

---

## ⚙️ 3. Real-Time Audio Callback Timing

To prevent real-time audio glitches, pops, and dropouts (**XRUNs**), the audio rendering thread must process its buffer well within the physical deadline.

### Buffer Configuration
* **Buffer Size**: 512 samples @ 44.1kHz  
* **Hard Processing Deadline**: **$11.6\text{ ms}$**

### Maximum Allowed Processing Times (Average / Peak)

| Active Tracks | Tier-1 (Low-End) | Tier-2 (Mid-Range) | Tier-3 (Flagship) |
| :--- | :--- | :--- | :--- |
| **4 Stereo Tracks** | $1.8\text{ ms}$ / $3.2\text{ ms}$ | $0.6\text{ ms}$ / $1.2\text{ ms}$ | $0.2\text{ ms}$ / $0.4\text{ ms}$ |
| **8 Stereo Tracks** | $3.5\text{ ms}$ / $6.0\text{ ms}$ | $1.2\text{ ms}$ / $2.5\text{ ms}$ | $0.4\text{ ms}$ / $1.0\text{ ms}$ |
| **16 Stereo Tracks**| $7.0\text{ ms}$ / $10.0\text{ ms}$| $2.5\text{ ms}$ / $4.2\text{ ms}$ | $0.8\text{ ms}$ / $1.6\text{ ms}$ |

*All configurations must complete within the 11.6 ms window. If any peak timing exceeds 11.6 ms on a given tier, the track count limit for that hardware tier must be dynamically clamped to prevent CPU overload.*

---

## 💾 4. Memory Footprint & Leak Guidelines

Continuous memory allocations in JNI or PCM buffers can trigger out-of-memory (OOM) failures or garbage collection (GC) pauses that disrupt live playback.

* **Base RAM Footprint**: Should not exceed **250 MB** during inactive dashboard states.
* **Peak Studio Memory**: Up to **450 MB** when editing a heavy 16-track project containing raw PCM.
* **Leak Tolerance**: **0 MB** residual accumulation. After a project is closed, 100% of the pre-allocated PCM memory must be released, and the heap must return to base footprint within **5 seconds** of garbage collection.

---

## 🔋 5. Thermal & Battery Benchmarks

Professional recording sessions often last 30 to 60 minutes. The app must regulate its CPU usage to prevent device heating and battery drain:

* **Battery Draw Limit**: Less than **$2.5\%$** capacity drain per 30 minutes of continuous playback.
* **Thermal Throttling**: The app should maintain a balanced thread priority so that CPU temperature does not exceed **$43^\circ\text{C}$** during a standard 45-minute multi-track recording session.

---

## 📊 6. Performance Validation Procedure

Testers must monitor these thresholds using standard Android profiling tools:

1. Launch Android Studio Profiler (or standard system profiling tools).
2. Start a continuous 30-minute recording session inside the Studio Workspace.
3. Observe the JVM Heap, Native Memory, and CPU usage curves.
4. Verify that memory consumption stabilizes flatly without stair-stepping curves.

---
*Certified and Published by:*  
**Lead Systems Performance Architect, SurMaya QA Board**  
**Sri Itnaa Systems Division**  
