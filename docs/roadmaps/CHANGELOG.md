# SURMAYA AI - SYSTEM CHANGELOG

All notable changes to the SurMaya AI Music Operating System are documented in this file. SurMaya AI adheres strictly to semantic versioning.

---

## [1.0.0 - Mastering Release] - 2026-07-07
### Added
- **AI Mastering Intelligence Engine (AIME v1.0.0)**: Built from scratch to support high-precision brickwall limiting, loudness profiling, stereo field enhancement, and dither export matrices.
- **8 Sub-Engine Core**:
  1. **Loudness Analyzer**: K-weighted filter calculation.
  2. **Multiband Dynamics**: Multi-region compression.
  3. **Stereo Enhancer**: Mid-Side matrix adjustments.
  4. **Harmonic Saturation**: Polynomial harmonics generation.
  5. **True Peak Limiter**: Lookahead inter-sample peak ceiling controls.
  6. **Dither Engine**: TPDF shaped noise addition.
  7. **Streaming Optimizer**: Platform standard offset mapping.
  8. **Reference Matcher**: Mapped Bollywood, Pop, Classical, and EDM target spectra.
- **Mastering Workspace Console**: Stunning luxury gold theme incorporating LUFS meters, TP indicators, custom DSP parameter sliders, and real-time streaming platform checkbox matrices.
- **AI Release Builder Console**: Added local UPC/EAN metadata package ledger creation.
- **Robust Local JVM Verification Tests**: Implemented `AIMasteringEngineTest.kt` confirming the core processing and dither matrix workflows.

---

## [1.0.0] - 2026-07-06
### Added
- **AI Performance Intelligence Engine**: Introduced as the 🥁 **AI Instrument Generator** module, designed to deliver high-fidelity, expressive, performance-aware synthetic output.
- **9 Sub-Engine Architecture**:
  1. **Instrument Selection Engine**: Chooses instruments dynamically based on genre, style, and complexity presets.
  2. **Instrument Capability Engine**: Maintains range limits, timbre descriptions, sustain types, dynamics, and supported articulations.
  3. **Performance Engine**: Synthesizes notes, velocities, timings, and custom musical expressions.
  4. **Articulation Engine**: Generates microtonal slides and ornamentation including meend, gamak, murki, and Western styles like staccato, tremolo, legato.
  5. **Expression Engine**: Generates continuous control CC1 modulation, CC11 expression, vibrato, and pitch bend envelopes.
  6. **Humanization Engine**: Injects timing swing, velocity variations, and microtonal tuning inaccuracies.
  7. **Performance Validation Engine**: Enforces acoustic playability checks, string fingering spreads, bowed limits, and wind player lung/breath capacity rest requirements.
  8. **Sample Routing Engine**: Maps generated tracks to SoundFonts, SFZ formats, ONNX models, and MIDI busses.
  9. **Regional Instrument Knowledge Base**: Native capability definitions for Sitar, Sarod, Santoor, Veena, Bansuri, Shehnai, Sarangi, Harmonium, Tabla, Dholak, Mridangam, Pakhawaj, Nadaswaram, and Odissi Mardala.
- **Instrument Workspace UI Integration**: Fully integrated dynamic performance reports and 9-sub-engine parameters into `AIInstrumentScreen` and `InstrumentViewModel`.
- **System Unit Tests**: Added `PerformanceIntelligenceEngineTest.kt` verifying all 9 sub-engines, playability constraints, and overall compilation pipelines on the JVM via Robolectric.

---

## [1.1.0] - 2026-07-06
### Added
- **AI Arrangement Engine (v1.1.0)**: Frozen for MVP.
- **10-Step Arrangement Pipeline**: Unified song structural scoring, transition engines, and automation planners.
- **AI Conductor Engine**: Regulates dynamic track activation and density.
- **Pattern Library Integration**: Specialized syncopated Indian grooves and Teental/Keharwa thekas.
- **Arrangement Unit Tests**: Integrated `ArrangementEngineUnitTest.kt` and refactored mock issues in `VoiceEngineTest.kt` to run seamlessly inside the Robolectric suite.

---

## [1.0.0] - 2026-07-01
### Added
- **Pre-Arrangement Pipeline Modules**:
  - **AI Lyrics Engine**: Frozen.
  - **AI Composer Engine**: Frozen.
  - **AI Melody Generator**: Frozen.
  - **AI Chord Generator**: Frozen.
- **Local Persistence Layer**: Formed SQLite Room schemas for all primary music modules.
- **Platform Infrastructure**: Configured standard Material Design 3, manual dependency service locator graph, and Gemini-based API gateway handlers.
