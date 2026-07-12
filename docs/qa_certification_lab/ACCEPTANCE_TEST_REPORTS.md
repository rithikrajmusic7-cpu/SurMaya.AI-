# 📋 SurMaya AI - Acceptance Test Reports

**Document Reference**: SRMY-QA-ACCEPT-102  
**Status**: IN-PROGRESS (INTERNAL ACCEPTANCE STAGE)  
**Target Build**: SurMaya AI v1.0 RC2 (`app-debug.apk` / SHA-256: `2a8f3e3bb4c880b47a497085e6fbe6b382907d0231676ef90afdcd7a6cb4e423`)  

---

## 🏛️ 1. Acceptance Testing Protocol

To satisfy the **Final Production Gate** mandated by the Chief Technology Officer, this document provides the formal logging matrix for physical device verification. Before **v1.0 Production Release** can be certified, these 5 core pillars must be executed on real devices representing the core Android API range (API 26 to API 36).

---

## 🧪 2. The 5 Mandatory Acceptance Tests

### 🧪 Test 1: Fresh Install Verification
* **Objective**: Ensure the application installs and launches successfully without immediate crashes or freeze states on a fresh system partition.
* **Execution Steps**:
  1. Uninstall any previous build of SurMaya AI from the target device.
  2. Install `app-debug.apk` via ADB, local package installer, or internal link.
  3. Launch the application from the app drawer.
  4. Verify the splash screen animations and edge-to-edge transition styles load correctly.
* **Pass Criteria**: App opens and displays the primary landing workspace in under **2.5 seconds** with zero ANRs or crashes.

---

### 🧪 Test 2: Project Lifecycle Stability
* **Objective**: Verify that user projects, timeline arrangements, and track assets can be created, saved, closed, and re-opened without data corruption.
* **Execution Steps**:
  1. Create a new multi-track project inside the Professional Studio Workspace.
  2. Add 4 tracks, adjust the volume sliders, and add simple MIDI notes.
  3. Exit the project to return to the dashboard (Triggering auto-save).
  4. Force-stop the application through Android System Settings to clear RAM memory cache.
  5. Relaunch the app and reopen the saved project.
* **Pass Criteria**: Project loads with all track settings, volume positions, MIDI data, and PCM buffers preserved exactly as left.

---

### 🧪 Test 3: AI Song Generation Workflow
* **Objective**: Confirm the entire AI-driven production pipeline is responsive, complete, and operates without placeholder fallback triggers.
* **Execution Steps**:
  1. Input a custom prompt (e.g., *"A relaxing offline acoustic guitar loop"*).
  2. Generate lyrics and verify the layout formatting.
  3. Generate the composition blueprint.
  4. Render the audio using the integrated AIRE Runtime and Suno interface.
  5. Play back the generated file on the multi-track timeline.
* **Pass Criteria**: Audio compiles, imports to the timeline, and plays back with full active waveforms without audio artifacts or timeline offset glitches.

---

### 🧪 Test 4: Audio Recording Interface
* **Objective**: Verify low-latency audio recording functionality across internal microphones, external wired headsets, and professional USB Audio Interfaces.
* **Execution Steps**:
  1. Create a voice or instrument track inside the timeline editor.
  2. Select input source: **Internal Mic**, then repeat with a **USB Audio Interface** or USB mic (if available).
  3. Enable **Live Monitoring** to test latency and routing.
  4. Record 60 seconds of continuous voice/instrument capture.
  5. Stop recording and verify the recorded region is rendered as a clean PCM wave.
* **Pass Criteria**: Input signals are captured cleanly at 44.1kHz or 48kHz (mono or stereo), written to disk without audio stutters, and align precisely with the timeline grid.

---

### 🧪 Test 5: Export Validation
* **Objective**: Ensure that rendered WAV and MP3 mixes comply with digital distribution standards and play back accurately on external applications and DAWs.
* **Execution Steps**:
  1. Complete a multi-track mix inside the studio mixer workspace.
  2. Trigger "Export WAV (Lossless 24-bit)" and "Export MP3 (320kbps CBR)".
  3. Locate the exported files in the device's public `Music/SurMaya/` directory.
  4. Transfer files to a secondary device and play them in external players: **VLC**, **MX Player**, and professional DAWs (**Audacity**, **FL Studio**, or **Ableton**).
  5. Check ID3 tag metadata (Artist: Sri Itnaa, Founder: PRADEEP SINGH).
* **Pass Criteria**: Rendered files compile with correct metadata tags and play back with full spectral depth and zero audio pops or dropouts.

---

## 📊 3. Physical Device Test Logging Matrix

Use the following tables to record execution outcomes on physical test targets.

### Target Device 1: Legacy Compatibility Profile (Android 8.0 - 9.0)
* *Typical Hardware: Redmi 6A, Galaxy J6 (RAM: 2GB-3GB, API 26-28)*

| Test ID | Test Case | Tester | Date | Result | Technical Logs / Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TEST-01** | Fresh Install | | | | |
| **TEST-02** | Project Lifecycle | | | | |
| **TEST-03** | AI Song Gen | | | | |
| **TEST-04** | Audio Recording | | | | |
| **TEST-05** | WAV/MP3 Export | | | | |

---

### Target Device 2: Mid-Range Profile (Android 10.0 - 12.0)
* *Typical Hardware: Moto G40, Redmi Note 10 (RAM: 4GB-6GB, API 29-31)*

| Test ID | Test Case | Tester | Date | Result | Technical Logs / Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TEST-01** | Fresh Install | | | | |
| **TEST-02** | Project Lifecycle | | | | |
| **TEST-03** | AI Song Gen | | | | |
| **TEST-04** | Audio Recording | | | | |
| **TEST-05** | WAV/MP3 Export | | | | |

---

### Target Device 3: Modern Flagship Profile (Android 13.0 - 15.0+)
* *Typical Hardware: Pixel 8, OnePlus 12, Galaxy S24 (RAM: 8GB-12GB+, API 33-35/36)*

| Test ID | Test Case | Tester | Date | Result | Technical Logs / Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TEST-01** | Fresh Install | | | | |
| **TEST-02** | Project Lifecycle | | | | |
| **TEST-03** | AI Song Gen | | | | |
| **TEST-04** | Audio Recording | | | | |
| **TEST-05** | WAV/MP3 Export | | | | |

---

## 🚦 4. Verification Verdict Sign-off

When all tests are completed across the entire compatibility spectrum, the Lead QA Engineer will issue the final certification status:

* **Current Phase Status**: 🟡 **UNDERGOING ACCEPTANCE TESTING**
* **Certification Gate**: ⬜ **CERTIFIED & READY FOR v1.0 PRODUCTION RELEASE** (Requires 100% Pass)

*Authorized Verification Officers:*  
```text
__________________________________
Lead Device Assurance Architect

__________________________________
Chief Quality Officer, Sri Itnaa
```
