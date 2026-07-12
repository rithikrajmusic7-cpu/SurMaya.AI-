# ADR-001: Clean Architecture & Layered Decoupling

## Status
**APPROVED**

## Context
The SurMaya AI Music Operating System orchestrates complex, multi-modal workflows including lyrics generation, music composition, microtonal melody plotting, chord progression harmonizing, performance articulations, and multi-track audio rendering. Unstructured coupling between these features would lead to a fragile codebase, slow compile times, difficult-to-write unit tests, and a system highly vulnerable to third-party vendor API changes.

## Decision
We enforce a strict **Decoupled Clean Architecture** combined with **MVVM (Model-View-ViewModel)**. The codebase is strictly partitioned into three independent layers with precise boundaries and unidirectional data flow:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                            │
│                                                                        │
│   Compose Screens (AIInstrumentScreen, AISingerScreen, etc.)           │
│                                  │                                     │
│                                  ▼ (Dispatches Intents)                │
│   ViewModels (InstrumentViewModel, SingerViewModel, etc.)              │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼ (Subscribes to StateFlow Streams)
┌────────────────────────────────────────────────────────────────────────┐
│                             DOMAIN LAYER                               │
│                                                                        │
│   - Pure Domain Models (PhonemeSegment, VocalPhrase, Raga, Melody)     │
│   - Business Engine Interfaces (ISingerEngine, IOrnamentationHandler)  │
│   - Repository Interfaces (ArrangementRepository, SingerRepository)    │
└──────────────────────────────────▲─────────────────────────────────────┘
                                   │
                                   │ (Implements / Binds Interfaces)
┌──────────────────────────────────┴─────────────────────────────────────┐
│                              DATA LAYER                                │
│                                                                        │
│   - AppDatabase (SQLite Room DB, DAOs, Entity Models)                 │
│   - Repository Implementations (SingerRepositoryImpl, MusicRepoImpl)   │
│   - Core Engine Implementations (DefaultVocalOrnamentationHandler)     │
│   - REST API Clients, OkHttp interceptors, & Gemini Neural Gateways    │
└────────────────────────────────────────────────────────────────────────┘
```

1. **Presentation Layer**: Built entirely using Jetpack Compose and Material Design 3. Views never hold business state; they react to StateFlow streams emitted by state-holder ViewModels.
2. **Domain Layer**: The core of SurMaya. Written in **pure Kotlin** with zero dependencies on Android framework classes, UI, databases, or network libraries. It defines domain objects, usecases, and architectural interface contracts.
3. **Data Layer**: Implements domain repository interfaces and orchestrates local persistence (Room Database), remote REST communication (Retrofit/OkHttp), and raw algorithmic computations.

## Rationale
- **Testability**: Because the Domain layer is purely Kotlin and defines explicit interface contracts, we can write robust, lightning-fast local JVM unit tests (and Robolectric tests) without launching emulator devices.
- **Provider Independence**: Downstream engines and components communicate solely via abstract domain interfaces. For instance, whether vocals are rendered using cloud APIs or offline synthesizers, the presentation layer remains completely unaffected.
- **Maintainability**: Clear separation of concerns means bug isolation takes minutes rather than hours, and new developers can onboard rapidly by reviewing isolated layer folders.

## Consequences
- **Code Volume**: Requires writing interface files, entity mappings, and domain models, increasing initial file counts.
- **Unidirectional Rigor**: Developers must resist the temptation of passing database entities directly to Compose screens or invoking remote clients from the UI.
- **Extreme Stability**: Preserves complete functional integrity and allows seamless platform scaling without regression.
