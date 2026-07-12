# ADR-003: Canonical Music Representation & Blueprints

## Status
**APPROVED**

## Context
The SurMaya generation pipeline depends on passing structured representations between successive engines (e.g., Lyric Generator, Chord Generator, Melody Generator, Articulation Handler, and Vocal Synthesizer). Communicating through raw audio files or MIDI byte arrays is extremely limiting—it prevents live non-destructive editing, introduces latency, and hides the underlying musical semantics.

## Decision
We establish a **Canonical Music Representation** system. Every engine in the pipeline processes and produces lightweight, immutable, highly structured symbolic data models, referred to as **Blueprints**:

```
[Lyrics Engine] ──► LyricsBlueprint (String, Rhythmic Poetry)
      │
      ▼
[Composer Engine] ──► SongStructureBlueprint (Sections, BPM, Scale)
      │
      ▼
[Melody Engine] ──► MelodyBlueprint (Notes, Pitches, Durations)
      │
      ▼
[Chord Engine] ──► ChordBlueprint (Harmonic Progressions)
      │
      ▼
[Performance Engine] ──► PerformanceBlueprint (Physical Expression Vectors)
      │
      ▼
[Vocal Engine] ──► VocalBlueprint (Phonemes, Breath Markers, Ornaments)
```

These blueprints are standard Kotlin data classes decorated with `@Serializable` for simple local key-value state persistence and network-neutral JSON transport.

## Rationale
- **Non-Destructive Editing**: The user can modify chord selections, adjust tempo, or rewrite lyrics at any intermediate step without re-triggering the entire cloud generation pipeline. The audio renderer simply re-interprets the updated blueprints.
- **Multi-Host Portability**: Because the representations are purely symbolic, they can easily be exported as MIDI, rendered on-device using synthetic oscillators, passed to cloud rendering farms, or converted into viseme blueprints for 3D lipsync.
- **Incremental Cacheable State**: Every stage has clear inputs and outputs, allowing us to cache composition steps locally.

## Consequences
- **Data Schemas Maintenance**: Schema modifications require meticulous backwards-compatibility planning (e.g., default arguments) to prevent serialized project files from breaking on future updates.
- **Mathematical Decoupling**: Separation of MIDI generation logic from rendering ensures high flexibility but requires a robust playback/rendering layer to translate symbolic nodes to audio.
