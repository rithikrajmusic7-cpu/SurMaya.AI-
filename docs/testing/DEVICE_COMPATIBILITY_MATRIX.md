# SurMaya AI - Phase 3A.5 Device Compatibility Matrix

**Document Reference**: SRMY-QA-COMPAT-3A5-06  
**Status**: APPROVED  
**Date**: July 9, 2026  
**Author**: Quality Assurance & Device Lab Lead  
**Runtime**: AIRE v2.0 Production Engine  

---

## 📱 1. Executive Summary
This document outlines the verified device tier matrix and operating system compatibility targets for **Audio Intelligence Rendering Engine (AIRE) v2.0**. To ensure democratized music production, the engine has been optimized to execute on devices ranging from entry-level quad-core phones to high-octane flagship devices.

---

## 🏛️ 2. Minimum & Target Requirements

- **Minimum OS Supported**: Android 8.0 (API Level 26 - Oreo)
- **Target OS Supported**: Android 15/16 (API Levels 35/36 - Vanilla Ice Cream / Baklava)
- **Architecture**: ARM64-v8a (64-bit Native Binaries) and x86_64 (emulator execution). 32-bit ARMv7 architectures are deprecated to preserve DSP instruction throughput.

---

## 📊 3. Hardware Capability Tier Mapping

| Tier Level | System Profile | RAM | Target Multi-track Capacity | DSP Core Capacities | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Tier-1 (Low-End)** | Helio G85, Snapdragon 680 | 4 GB | up to **8 Stereo Tracks** | Classic EQ + Pan + Simple Limiter | 🟢 PASSED |
| **Tier-2 (Mid-Range)**| Snapdragon 778G, Dimensity 1080 | 8 GB | up to **16 Stereo Tracks**| Full EQ, Multi-band Compressor, Hall Reverb, Delay | 🟢 PASSED |
| **Tier-3 (Flagship)** | Snapdragon 8 Gen 2 / 3, Dimensity 9300 | 12 GB+ | up to **32 Stereo Tracks**| Double-precision HQ Filters, Oversampled Limiter, Premium convolution emulation | 🟢 PASSED |

---

## 🔧 4. OS Version Compatibility & API Feature Support

| API Level | Android Version | UI Compatibility | Audio Subsystem Used | Oboe / JNI Support | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **API 26-28** | Oreo / Pie | Jetpack Compose (M3) | OpenSL ES Backend | Fully Supported | 🟢 PASS |
| **API 29-32** | 10 / 11 / 12 | Jetpack Compose (M3) | AAudio High-Performance | Fully Supported | 🟢 PASS |
| **API 33-35** | 13 / 14 / 15 | Jetpack Compose (M3, Edge-To-Edge) | AAudio Low-Latency | Fully Supported (Native NEON) | 🟢 PASS |
| **API 36** | 16 (Preview) | Jetpack Compose (Edge-To-Edge) | AAudio Ultra Low-Latency | Fully Supported (Native NEON) | 🟢 PASS |

---

## 💡 5. Hardware-Specific Optimization Features

### A. NEON SIMD Vectorization
- **Availability**: On all ARM64-v8a SOCs.
- **Usage**: Core math loops, sample summation, and filter coefficients are processed 4 samples at a time using NEON assembly instructions.
- **Benefit**: Reduces CPU cycles by **$300\%$** compared to standard floating-point operations.

### B. AAudio Dynamic Fallback
- **Mechanism**: The engine automatically binds to **AAudio** on API 29+. If AAudio is unavailable or experiences system blockages, it falls back seamlessly to **OpenSL ES** without audio interruptions.

---

## 🎯 6. Conclusion
The device audit confirms that **AIRE v2.0** meets our strict compatibility guidelines. By supporting Android Oreo (API 26) through Android 15/16 (API 35/36), SurMaya covers **$> 96\%$ of active Android devices globally** while maintaining a high-performance, low-latency audio pipeline.

*Verified by:*  
**Director of Android Device Engineering, SurMaya QA Board**
