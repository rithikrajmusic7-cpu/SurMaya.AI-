# SURMAYA AI - MODULE DEPENDENCY HANDBOOK

This document outlines the architectural relationships, sequence flow, and public contract dependencies across the SurMaya AI pipeline.

---

## 🗺️ High-Level Module Topology

Each core musical module in SurMaya AI behaves as an isolated planning block, feeding structured metadata into the subsequent stage.

```
Lyrics Engine (Lyrics & Syllables)
  │
  ▼
Composer Engine (Song Structure, Sections, Global BPM)
  │
  ▼
Melody Generator (Scales/Ragas, Pitch Sequences, Durations)
  │
  ▼
Chord Generator (Progressions, Harmonizations, Tension Maps)
  │
  ▼
Arrangement Engine (Tracks, Density, Conductor Patterns)
  │
  ▼
Performance Engine (Expression Envelopes, Articulations, Playability Limits)
  │
  ├──────────────────────────────────────────────────────┐
  ▼                                                      ▼
Singer Engine (Vocal Synthesis, Breath-bound Notes)   Mixing Engine (AMIE) (Stem Gain, EQ, Comp, Bus Sum)
  │                                                      │
  └──────────────────────────┬───────────────────────────┘
                             ▼
                      Mastering Engine (AIME) (Loudness, Brickwall Limiter, Release Package)
```

---

## 📦 Interface Contracts

| Module | Consumes | Produces | Storage Entity |
| :--- | :--- | :--- | :--- |
| **Lyrics Engine** | Theme / Mood prompts | Rhythmic verses and syllable counts | `LyricsEntity` |
| **Composer Engine** | Global BPM, Section configurations | Song section outlines & structures | `ProjectEntity` |
| **Melody Generator** | Lyric syllables, Scale/Raga profiles | Melodic note-pitch sequences | `MelodyTrackEntity` |
| **Chord Generator** | Melodic pitch lines, Harmonization styles | Chord progression sequences | `ChordProgressionEntity` |
| **Arrangement Engine** | Sections, Melodies, Chords, Grooves | Multi-track orchestration layout | `ArrangementTrackEntity` |
| **Performance Engine** | Orchestrated tracks, Expressive curves | Microtonal articulations, CC envelopes | `PerformanceTrackEntity` |
| **Singer Engine** | Performance notes, Articulations, Syllables | Voice synthesis and breathing markers | `SingerVoiceEntity` |
| **Mixing Engine (AMIE)** | Stems and arrangements | Balanced stereo track sum and mix reports | `MixProjectEntity` |
| **Mastering Engine (AIME)** | Stereo track sum | Loudness optimized release packages | `MasteringProjectEntity` |

---

## 🛠️ Interface Isolation Rules
1. **Unidirectional Flow**: Downstream modules MUST NOT modify the output structures of upstream modules.
2. **Provider Independence**: Modulating parameters (e.g. humanization or expression points) must be stored in standard JSON format rather than proprietary platform types, ensuring seamless portability to other platforms or synthesizers.
