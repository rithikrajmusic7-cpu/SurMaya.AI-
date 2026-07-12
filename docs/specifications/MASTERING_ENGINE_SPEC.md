# AI Mastering Intelligence Engine (AIME) Technical Specification

**Version:** v1.0.0  
**Authors:** SurMaya AI Architecture Board  
**Status:** APPROVED  

---

## 1. Architectural Overview

The **AI Mastering Intelligence Engine (AIME)** is the final core module in the SurMaya end-to-end AI music production pipeline. Positioned directly after the **AI Mixing Intelligence Engine (AMIE)**, AIME ingests a mixed audio sum and optimizes it for global distribution platforms using offline-first digital signal processing (DSP) algorithms and Explainable AI diagnostics.

### High-Level Block Diagram

```
                 +------------------------------------+
                 |       Input Mixed Audio Sum        |
                 +-----------------+------------------+
                                   |
                                   v
                 +------------------------------------+
                 |     AIMasteringEngine (Orch.)      |
                 +-----------------+------------------+
                                   |
         +-------------------------+-------------------------+
         |                         |                         |
         v                         v                         v
+--------+--------+       +--------+--------+       +--------+--------+
| Loudness Anal. |       | Multiband Proc. |       | Stereo Enhancer |
+--------+--------+       +--------+--------+       +--------+--------+
         |                         |                         |
         +-------------------------+-------------------------+
                                   |
         +-------------------------+-------------------------+
         |                         |                         |
         v                         v                         v
+--------+--------+       +--------+--------+       +--------+--------+
| Harmonic Excit. |       | True Peak Lim.  |       | Dither Engine   |
+--------+--------+       +--------+--------+       +--------+--------+
         |                         |                         |
         +-------------------------+-------------------------+
                                   |
         +-------------------------+-------------------------+
         |                                                   |
         v                                                   v
+--------+--------+                                 +--------+--------+
| Streaming Opt.  |                                 | Reference Match.|
+--------+--------+                                 +--------+--------+
         |                                                   |
         +-------------------------+-------------------------+
                                   |
                                   v
                 +-----------------+------------------+
                 |       Final Master Output          |
                 +-----------------+------------------+
                                   |
                                   v
                 +-----------------+------------------+
                 |  Global Distribution Deliverable   |
                 +------------------------------------+
```

---

## 2. Core Sub-Engine Specifications

AIME is composed of eight distinct, modular sub-engines isolated via interfaces.

### 2.1. Loudness Analyzer (`ILoudnessAnalyzer`)
- **Responsibility:** Calculates instantaneous, short-term, and integrated LUFS (Loudness Units Full Scale) and True Peak metrics.
- **Processing Principle:** Uses K-weighting filters (RLB and high-shelf filters conforming to ITU-R BS.1770-4) to simulate human hearing perception and computes root-mean-square loudness levels across sliding blocks.

### 2.2. Multiband Compressor & Dynamics Processor (`IMultibandProcessor`)
- **Responsibility:** Glues the mix together across four frequency bands:
  - Low (20 Hz - 150 Hz)
  - Low-Mid (150 Hz - 1 kHz)
  - High-Mid (1 kHz - 6 kHz)
  - High (6 kHz - 20 kHz)
- **Processing Principle:** Computes band-specific RMS envelopes and applies localized compression ratios (1.5:1 to 3.0:1) with slow attacks and fast releases.

### 2.3. Stereo Enhancer (`IStereoEnhancer`)
- **Responsibility:** Optimizes the spatial soundstage. Ensures mono compatibility and wide stereo fields.
- **Processing Principle:** Performs Mid/Side (M/S) matrix decomposition. Independently scales the Side signal relative to Mid while monitoring Phase Correlation Coefficients to prevent out-of-phase collapse.

### 2.4. Harmonic Exciter (`IHarmonicExciter`)
- **Responsibility:** Injects analog-style warmth, clarity, and shimmer.
- **Processing Principle:** Uses asymmetric polynomial waveshaping (even and odd harmonics mimicking tube and tape saturation) to enrich high-frequency bands (> 5 kHz) without introducing harsh digital clipping.

### 2.5. True Peak Limiter (`ITruePeakLimiter`)
- **Responsibility:** Guarantees absolute peak safety. Prevents inter-sample peaks (ISPs) from causing digital distortion.
- **Processing Principle:** Uses lookahead buffers (typically 2ms to 5ms) and fast brickwall envelope detectors to catch peaks before they exceed the ceiling.

### 2.6. Dither Engine (`IDitherEngine`)
- **Responsibility:** Mitigates quantization distortion when downsampling bits.
- **Processing Principle:** Adds shaped triangular probability density function (TPDF) noise to push quantization noise into high-frequency regions least sensitive to human hearing.

### 2.7. Streaming Optimizer (`IStreamingOptimizer`)
- **Responsibility:** Ensures the audio meets specific platform normalizations.
- **Processing Principle:** Compares integrated LUFS levels against target standards (Spotify: -14 LUFS, Apple Music: -16 LUFS, YouTube: -14 LUFS) and adjusts gain matching offsets.

### 2.8. Reference Matcher (`IReferenceMatcher`)
- **Responsibility:** Matches the master spectral and dynamics curve to reference profiles.
- **Processing Principle:** Evaluates spectral tilt and dynamics variance against Bollywood, Pop, Classical, or EDM benchmarks and returns matching scores.

---

## 3. Data Flow and State Persistence

1. **Instantiation:** Ingests mixed track entity parameters.
2. **Analysis/Synthesis Run:** Sequentially runs the 8 sub-engines.
3. **Database Ledger:** Serializes the resulting `MasteringResult` JSON and saves it in the local Room SQLite database under `MasteringProjectEntity` to ensure complete offline persistence.

---

## 4. Explainable AI (XAI) Matrix

Every mastering run outputs an explainable report explaining *why* decisions were made:
- **EQ decisions:** Based on reference spectral tilt mismatch.
- **Compression decisions:** Based on crest factor or dynamics range calculation.
- **Limiting decisions:** Based on peak envelope excursions.
