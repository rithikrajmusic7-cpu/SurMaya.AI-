# SURMAYA AI - ARCHITECTURAL DECISIONS LOG (ADR)

This file records the major technical decisions, rationale, context, and consequences for the SurMaya AI Music Operating System.

---

## [ADR-001] Manual Service Locator Dependency Injection
- **Status**: APPROVED
- **Context**: Choosing between a heavy annotation-processor-driven DI framework (Hilt/Dagger) vs. simple manual dependency management.
- **Decision**: Implemented a thread-safe manual `ServiceLocator` container in the App context.
- **Rationale**: Keeps compilation times extremely fast, eliminates KSP compilation overhead for standard classes, and provides full visibility over singleton lifecycles.
- **Consequences**: Developers must register new repositories manually in `ServiceLocator`, but benefits from zero runtime overhead and rapid incremental builds.

---

## [ADR-002] Multi-Track Mathematical Synthesis for Studio Previews
- **Status**: APPROVED
- **Context**: Needing low-latency real-time preview playback of complex Indian scales (Ragas) on a mobile device without shipping gigabytes of multi-sampled audio files inside the APK.
- **Decision**: Developed high-performance floating-point mathematical oscillators using multi-threaded Android `AudioTrack` modules.
- **Rationale**: Allows instant audio playbacks of microtonal bends (Meend) and pitch oscillations (Gamak) with zero storage footprint.
- **Consequences**: Synthesis is CPU-bound and sounds relatively synthetic compared to full sampler engines; however, it fits perfectly as an offline preview drafting engine.

---

## [ADR-003] Performance Intelligence Engine (9 Sub-Engines)
- **Status**: APPROVED
- **Context**: Shifting from a simple instrument selection model to an expressive performance modeling system.
- **Decision**: Organized the **AI Instrument Generator** into a structured, 9-part modular performance processor.
- **Rationale**: Isolating humanization offsets, continuous CC modulation envelopes, playability validation rules, and sample-mapping routing files decouples planning from rendering and sets up downstream AI vocalists or master busses with rich, structured metadata.
- **Consequences**: High-fidelity structured schemas are generated, setting up robust verification tests and enabling future seamless DAW/MIDI exports.
