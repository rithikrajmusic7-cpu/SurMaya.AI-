# SURMAYA AI - TECHNICAL DEBT REGISTER

This document tracks identified architectural flaws, code smells, or deferred optimizations within the SurMaya AI Music Operating System, establishing a clear path for mitigation.

---

## ⚠️ High-Priority Debt Items

### 1. AudioTrack Deprecation & Main-Thread Blocking Risk
- **Description**: Synthesizers in `InstrumentViewModel.kt` utilize deprecated java-constructors for `AudioTrack` and execute some initialization on main UI thread.
- **Risk Level**: **Medium-High**. Can cause frame stuttering or sluggishness when starting audio preview.
- **Remedy**: Refactor `AudioTrack` constructors to use `AudioTrack.Builder` API (Android 23+) and isolate all track play/stop cycles strictly within dedicated background coroutine scopes.

### 2. Monolithic ViewModel Complexity
- **Description**: `InstrumentViewModel.kt` has expanded to over 850 lines of code, managing both UI screen states, multi-track studio channels, and direct synthesis audio tasks.
- **Risk Level**: **Medium**. Complicates maintenance and tests.
- **Remedy**: Subdivide `InstrumentViewModel` by extracting the audio synthesis operations out to a specialized `VirtualSynthManager` utility class.

### 3. AIME Multiband Crossover DSP Approximation
- **Description**: The multiband dynamics crossover filters inside `AIMasteringEngine` use linear mathematical division rather than actual discrete-time IIR/Linkwitz-Riley crossover networks.
- **Risk Level**: **Medium-Low**.
- **Remedy**: Integrate high-precision double-precision Linkwitz-Riley 4th-order (24 dB/octave) crossover DSP algorithms into the multiband dynamics processor.

---

## 🗓️ Medium-Priority Debt Items

### 3. Hardcoded Synthesizer Waveforms
- **Description**: Synthesizers generate deterministic mathematical waveforms (`sin(2.0 * Math.PI * baseFreq * t)`) instead of parsing high-fidelity multi-sample wavetable buffers.
- **Risk Level**: **Low-Medium**. Limits realism of audio preview.
- **Remedy**: Upgrade preview engine to support loading tiny SoundFonts (`.sf2` format) inside the local app cache.

### 4. Dynamic Content Descriptions
- **Description**: Several custom interactive elements in `AIInstrumentScreen.kt` use static content descriptions instead of localized and context-aware values.
- **Risk Level**: **Low**. Can affect screen readers (TalkBack) accessibility.
- **Remedy**: Review string values in `strings.xml` and pass dynamic, context-specific content descriptions to Compose `Modifier.semantics`.
