# SURMAYA AI - TECHNICAL ARCHITECTURE MANUAL

This manual describes the design system, structural architecture, layered isolation boundaries, and database flows of the SurMaya AI Music Operating System.

---

## 🏛️ Layered Architectural Structure

SurMaya AI is engineered using **Decoupled Clean Architecture** with **MVVM (Model-View-ViewModel)**. The layout strictly enforces unidirectional data flow and separates raw database/network representations from domain-specific business rules.

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│                                                                        │
│   Compose Screens (AIInstrumentScreen, AIMelodyScreen, etc.)           │
│                                  │                                     │
│                                  ▼ (Dispatches Intents)                │
│   ViewModels (InstrumentViewModel, MelodyViewModel, etc.)              │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼ (Subscribes to StateFlow Streams)
┌────────────────────────────────────────────────────────────────────────┐
│                             DOMAIN LAYER                               │
│                                                                        │
│   - Domain Models (PerformanceModels, ArrangementModels, etc.)         │
│   - Engine Interfaces (PerformanceIntelligenceEngine, etc.)            │
│   - Repository Interfaces (ArrangementRepository, MusicRepository)      │
└──────────────────────────────────▲─────────────────────────────────────┘
                                   │
                                   │ (Implements / Binds Interfaces)
┌──────────────────────────────────┴─────────────────────────────────────┐
│                              DATA LAYER                                │
│                                                                        │
│   - AppDatabase (Room DB Setup & Migrations)                           │
│   - DAOs (UserDao, ArrangementDao, SongDao, etc.)                      │
│   - Repository Implementations (ArrangementRepositoryImpl)             │
│   - Core Engine Implementations (PerformanceIntelligenceEngine)        │
│   - REST API clients, interceptors & Gemini Gateways                  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 🎹 Music Generation Pipeline Dependency Flow

The generation of a full Indian classical song is modeled as a sequential, technically coherent dependency chain:

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│  Lyrics Engine  ├────>│ Composer Engine  ├────>│   Melody Engine     │
│  (Generates     │     │ (Decides overall │     │   (Seqs note pitches│
│  rhythmic poetry│     │ structures, BPM) │     │   and scale steps)  │
└─────────────────┘     └──────────────────┘     └──────────────────┬──┘
                                                                    │
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────▼──┐
│Singer & Vocalist│     │Performance Engine│     │    Chord Engine     │
│(Synthesizes vocal     │(9 Sub-engines,   │     │(Harmonizes notes    │
│syllable tracks) │<────│Articulation/Expr)│<────│into progressions)   │
└────────┬────────┘     └──────────────────┘     └─────────────────────┘
         │
         ▼
┌─────────────────┐     ┌──────────────────┐
│  Mixing Engine  ├────>│ Mastering Engine │
│  (Consolidates  │     │ (Applies brick   │
│  multitrack stems)    │ limiter, LUFS)   │
└─────────────────┘     └──────────────────┘
```

---

## 🥁 AI Performance Intelligence Engine (Deep-Dive)

The **AI Performance Intelligence Engine** (AI Instrument Generator) bridges planning and physical rendering. It breaks down into **9 core sub-engines**:

1. **Instrument Selection Engine**: Analyzes genre tags (e.g. "Sufi rock", "Carnatic traditional") and track complexity parameters to choose acoustic partners.
2. **Instrument Capability Engine**: Enforces musicological realities (range octaves, base tuning frequencies, acoustic sustain modes).
3. **Performance Engine**: Generates raw notes, base velocities, and timing sequences based on the selected Raga scale and global tempo.
4. **Articulation Engine**: Automatically injects realistic expressions. Adds Indian slides (**Meend**), fine oscillations (**Gamak**), and fast grace notes (**Murki**).
5. **Expression Engine**: Generates fine-grained continuous controller envelopes (CC1 modulation, CC11 expression volume, pitch bend).
6. **Humanization Engine**: Emulates organic human inaccuracies. Introduces microtonal tuning drift (cents), subtle timing jitter (ms), and minor velocity deviations.
7. **Performance Validation Engine**: Inspects physical playability bounds (fretboard finger stretches on Sitar/Veena, bowed change limits on Sarangi, and human breath capacity limits on Bansuri/Shehnai).
8. **Sample Routing Engine**: Outputs mapping configurations to interface with SFZ, SoundFonts, Kontakt, or ONNX synthesizer hosts.
9. **Regional Instrument Knowledge Base**: A pre-loaded database containing comprehensive capabilities, ranges, and typical playing techniques for 14 Indian instruments and 6 Western/electronic variants.

---

## 🗄️ Persistence & Dependency Injection

- **Persistence Layer**: AppDatabase is powered by SQLite Room. Database queries are executed on `Dispatchers.IO` with direct Flow tracking to dynamically refresh view screens.
- **Dependency Isolation**: A manual `ServiceLocator` pattern constructs repository Singletons, resolving Database dependencies and providing single sources of truth. Avoids the overhead of heavy DI compilation processors.
