# ADR-006: AI Vocal Intelligence Engine

## Status
**FOUNDATION APPROVED** 🟡 (Staged for MVP Integration)

## Context
SurMaya AI requires an expressive, culturally authentic vocal synthesizer capable of translating lyrics and melodies into human singing. Generic TTS engines cannot capture the intricate slides (**Meend**), rapid pitch ornamentations (**Gamak**, **Murki**, **Kan Swar**), physical breathing boundaries, and emotional expression profiles central to classical and modern Indian vocals.

## Decision
We design the **AI Vocal Intelligence Engine** (formerly *AI Singer Engine*) as a core modular service in the domain and data layer of SurMaya AI. It separates vocal processing into six specialized sub-pipelines:

```
Lyrics ──► [IPhonemeMapper] ──► Phonemes (IPA Translate)
                 │
                 ▼
     [IOrnamentationHandler] ──► Inject Meend, Gamak, Murki Ornaments
                 │
                 ▼
         [Breath Planner] ────► Real-time Respiration & Oxygen Model
                 │
                 ▼
       [Emotion Synthesizer] ──► Modulate Vibrato, Power & Breathiness
                 │
                 ▼
       [VocalValidationEngine] ─► Safety and Range Diagnostics
                 │
                 ▼
         [VocalBlueprint] ────► Summary Diagnostics & Rendering Outputs
```

1. **Phoneme Mapper (`IPhonemeMapper`)**: Decomposes written lyrics (Hindi, Sanskrit, Odia, English) into phonetic IPA syllables aligned with tempo timings.
2. **Ornamentation Handler (`IOrnamentationHandler`)**: Contextually injects classical and contemporary ornamentations (Meend, Gamak, Murki, Khatka, Sparsh).
3. **Respiration & Breath Planner**: Analyzes lyric sequences and limits phrase durations to model human lung capacity, placing structural breath markers.
4. **Emotion Synthesizer**: Adjusts power, softness, vibrato, and breathiness based on target emotions (e.g., Sad, Devotional, Sorrow).
5. **Validation Engine**: Flags tessitura boundary violations (Soprano ceilings/Bass floors) and oxygen starvation (missing breaths).
6. **Master Orchestrator (`AISingerEngine`)**: Coordinates all sub-engines to synthesize a complete `VocalSynthesisResult` (VocalBlueprint).

## Rationale
- **Indian Musicology Priority**: Guarantees unmatched emotional depth and ornamentation authenticity.
- **Provider-Agnostic Contracts**: Declares frozen interfaces (`ISingerEngine`, `IPhonemeMapper`, `IOrnamentationHandler`) that support future cloud rendering or on-device local deep voice integration.
- **Durable Diagnostics**: Rich validation reports output diagnostic warnings (such as oxygen warnings) directly into the UI, giving vocal producers high creative control.

## Consequences
- **Compute Intensity**: Extensive phonetic mapping and ornamentation synthesis run on background threads (`Dispatchers.Default`) to keep the UI completely smooth.
- **Development Path**: Initial v1.0.0 establishes the foundation and local unit tests; v1.1.0 delivers Room persistence, the Vocal Workspace UI, and Gateway integrations.
