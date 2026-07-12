# SURMAYA AI - SYSTEM COMPATIBILITY MATRIX

This document specifies the supported Android OS levels, hardware targets, build toolchain versions, and external API dependency compatibility.

---

## 📱 Mobile Platform Support

| Element | Target Specification | Status |
| :--- | :--- | :--- |
| **Minimum SDK** | Android 7.0 (API 24 - Nougat) | Supported |
| **Target SDK** | Android 15 (API 35 - Vanilla Ice Cream) | Supported |
| **Compile SDK** | Android 15 (API 35) | Supported |
| **Device Architectures** | armeabi-v7a, arm64-v8a, x86, x86_64 | Verified |
| **Recommended Devices** | Octa-core CPU, 6GB+ RAM (For multi-track synthesis) | High Performance |

---

## 🛠️ Build & Compilation Toolchain

| Component | Version | Notes |
| :--- | :--- | :--- |
| **Gradle Version** | 8.x | Kotlin DSL enabled (`.gradle.kts`) |
| **Android Gradle Plugin** | 8.x | Unified Gradle configuration cache |
| **Kotlin Compiler** | 2.x | High-speed compiler and full coroutines |
| **KSP Version** | Strictly aligned with Kotlin | Fast code generation for Room database |
| **Compose Compiler** | Integrated with Kotlin 2.x | Standard Material 3 UI libraries |

---

## 🌐 External API & Host Services

| Service | Supported Protocols / SDKs | Integration Target |
| :--- | :--- | :--- |
| **Google Gemini API** | REST Gateway / Vertex AI REST API | AI Lyrics & Arrangement Planning |
| **Audio Routing (v2.x)** | SFZ, SoundFont (.sf2), MIDI CC | Sample/Model Routing Engine |
| **Local Database** | SQLite Room v2.6+ | Local Device Persistence |
| **Local JVM Testing** | Robolectric v4.x, Roborazzi screenshot rules | Test Automation Pipeline |
