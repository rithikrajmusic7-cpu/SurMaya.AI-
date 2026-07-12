# SurMaya AI - Phase 3A.5 Known Issues & Limitations Register

**Document Reference**: SRMY-QA-KIR-3A5-07  
**Status**: ACTIVE  
**Date**: July 9, 2026  
**Author**: Quality Assurance Lead  
**Runtime**: AIRE v2.0 Production Engine  

---

## 📋 1. Executive Summary
This register catalogs known system limitations, boundary behaviors, and minor non-blocking issues identified during the Phase 3A.5 Production Hardening and Stress Testing cycles. All items listed here are classified as Low or Medium severity and have verified, production-ready workarounds. 

There are **0 High or Critical (P0/P1) issues** remaining.

---

## 🗃️ 2. Issues & Limitations Register

### Issue #01: Real-time Playback Glitch during Intense Background Disk writes
- **Severity**: Low (P3)
- **Symptom**: Minor click/pop during active real-time playback if the device simultaneously initiates a large background disk operation (e.g., app updates or large exports).
- **Root Cause**: Storage I/O priority competition. AAudio thread waits briefly on a resource lock during a disk sync.
- **Workaround / Mitigation**: Background exports now run at lower file-system write chunk sizes (e.g., 64KB instead of 1MB) to prevent disk I/O starvation.

---

### Issue #02: MP3 Encoder JNI Overhead on Low-end Devices
- **Severity**: Low (P3)
- **Symptom**: Exporting to MP3 on very low-end Tier-1 devices (e.g., quad-core CPUs) runs at $4\times$ real-time instead of the targeted $6\times$.
- **Root Cause**: The MP3 encoding algorithm utilizes heavy Huffman coding operations that are single-threaded and CPU-bound.
- **Workaround / Mitigation**: System detects low-end hardware profiles and increases the frame buffer cache, keeping background processing smooth without blocking the main thread.

---

### Issue #03: Loss of Metadata Tags in Older Windows Media Player Versions
- **Severity**: Low (P3)
- **Symptom**: Custom metadata tags (such as ISRC or UPC) embedded in WAV files are not parsed by pre-Windows 11 media players.
- **Root Cause**: Legacy media players only parse standard `ID3` chunks but do not support the modern RIFF list chunk layout for high-resolution audio.
- **Workaround / Mitigation**: Recommended standard behavior. The files are fully compliant and parse perfectly in professional DAWs (Pro Tools, Logic Pro, Ableton Live) and modern media players.

---

### Issue #04: Low Memory Eviction of Playback Resources
- **Severity**: Medium (P2)
- **Symptom**: If the app is minimized for over 30 minutes in low-memory environments, resuming active playback results in a 1-second delay before sound triggers.
- **Root Cause**: Android OS reclaims JVM Heap caches during low-memory conditions, forcing the engine to re-load PCM samples from raw resources on resume.
- **Workaround / Mitigation**: Handled gracefully. ViewModels re-initialize the audio buffers in the background on transition to foreground before the user triggers play.

---

## 🎯 3. Conclusion
All issues listed above have been evaluated and determined safe for release. They do not impact basic playback, mixing, mastering, or exporting pipelines. We will continue tracking these items as part of the Phase 3B roadmap.

*Approved by:*  
**Chief Engineering Lead, SurMaya QA Board**
