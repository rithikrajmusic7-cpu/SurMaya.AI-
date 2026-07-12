# SURMAYA AI MUSIC OPERATING SYSTEM - ENGINEERING AUDIT PACKAGE (v1.1)
---
**Status**: APPROVED AND ARCHITECTURE FROZEN FOR MVP (v1.1)  
**Date**: July 6, 2026  
**Auditor**: Google AI Studio Engineering & Principal AI Systems Architect  
**Client**: Chief Technology Officer & Founder, SurMaya AI  

---

## 1. Executive Summary

This **Engineering Audit Package (v1.1)** serves as the formal architectural freeze and technical review for the **AI Arrangement Engine Module** of the **SurMaya AI Music Operating System**. Following the successful deployment of the UI Integration Layer (navigation, home dashboard, interactive workspace), we have implemented, optimized, and compiled the **Core Arrangement Intelligence Engine (v1.1)**. 

### Key Technical Achievements in v1.1:
1. **10-Step Arrangement Intelligence Pipeline**: Unified melody/motif analysis, harmony mapping, song structure generation, multi-instrument scoring, automation planners, transition engines, and musicological assessment.
2. **AI Conductor Engine**: Regulates dynamic track activation and layer density ("Sparse", "Medium", "Rich", "Huge", "Orchestra") to establish natural "musical breathing" and realistic crescendos.
3. **Pattern Library Integration**: Injects specialized syncopated patterns (e.g., Indian Dadra, Keherwa, Teental, cinematic Timpani rolls) into instrument tracks.
4. **Bollywood & Cinematic Scoring Presets**: Specialized presets matching custom prompt context (e.g., Modern Bollywood, 90s Nostalgic, Soft Romantic, and Epic Hybrid Hollywood Scoring).
5. **Robust Room Persistence**: Local offline databases for all arrangement entities (projects, tracks, sections, lanes, transitions).
6. **Full Test & Compile Pass**: Added `ArrangementEngineUnitTest.kt`, compiling cleanly and passing with 100% success.

---

## 2. Architecture Validation Report

SurMaya AI uses a **decoupled, multi-layered Clean Architecture** with **MVVM (Model-View-ViewModel)**. The layout isolates core business logic from direct network clients or SQLite database interfaces:

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│  Compose UI Screens (AIArrangementScreen) <───> VM     │
└───────────┬────────────────────────────────┬───────────┘
            │                                │
            ▼ (Subscribes to StateFlow)      ▼ (Calls Repository)
┌────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                      │
│  - Entities (ArrangementProject, Track, Section, etc.) │
│  - Repository Interfaces (ArrangementRepository)       │
│  - Engine Interfaces (ArrangementEngine)               │
└───────────▲────────────────────────────────▲───────────┘
            │                                │
            │ (Implements Interfaces)        │ (Resolves dependencies)
┌───────────┴────────────────────────────────┴───────────┐
│                       DATA LAYER                       │
│  - Room AppDatabase & DAOs (ArrangementDao)            │
│  - Implementations (ArrangementRepositoryImpl)        │
│  - Core Logic (ArrangementEngineImpl)                  │
│  - API Clients & Diagnostics Interceptors              │
└────────────────────────────────────────────────────────┘
```

### Flow Map: AI Generation & Conductor Execution
1. **User Prompt**: The user requests a specific arrangement style (e.g., "90s Bollywood romance") in the Compose UI.
2. **ViewModel Orchestration**: `ArrangementViewModel` triggers generation, launching a coroutine on the background dispatcher (`Dispatchers.IO`).
3. **Repository Interfacing**: `ArrangementRepositoryImpl` pulls associated Melody & Chord profiles from the local Room database, passing them to `ArrangementEngineImpl`.
4. **Pipeline Execution**: The `ArrangementEngine` triggers the **10-step compilation sequence**, resolving the aesthetic preset and building the tracks, sections, automation curves, and transitions.
5. **Database Sync**: The returned `ArrangementBlueprint` is persisted directly in Room via `ArrangementDao` transactions.
6. **UI State Flow**: The UI is updated reactively, displaying a rich, interactive timeline visualizer.

---

## 3. Package & Folder Structure

The structural layout of SurMaya AI is organized as follows:

```
/app/src/main/java/com/example/
├── MainActivity.kt               <-- Main entry point activity
├── data/
│   ├── local/
│   │   ├── ApiCredentialManager.kt
│   │   ├── AppDatabase.kt        <-- Room database entrypoint
│   │   ├── DeveloperPrefsManager.kt
│   │   ├── dao/
│   │   │   ├── ArrangementDao.kt  <-- CRUD for arrangements
│   │   │   └── UserDao.kt, SongDao.kt, etc.
│   │   └── entity/
│   │       ├── ArrangementEntities.kt <-- Room database tables
│   │       └── UserEntity.kt, SongEntity.kt, etc.
│   ├── mapper/
│   │   └── Mappers.kt            <-- Data converters (Entity <-> Domain)
│   ├── remote/
│   │   └── DiagnosticsInterceptor.kt
│   └── repository/
│       ├── ArrangementEngineImpl.kt    <-- CORE PIPELINE IMPLEMENTATION
│       ├── ArrangementRepositoryImpl.kt <-- SQLite & Cloud orchestration
│       └── ChordRepositoryImpl.kt, MelodyRepositoryImpl.kt
├── di/
│   └── ServiceLocator.kt         <-- Manual Dependency Injection graph
├── domain/
│   ├── model/
│   │   ├── arrangement/
│   │   │   └── ArrangementModels.kt   <-- Domain objects and blueprit models
│   │   └── chord/, melody/, user/
│   └── repository/
│       ├── ArrangementEngine.kt       <-- Abstract Engine interface
│       ├── ArrangementRepository.kt   <-- Abstract Repository interface
│       └── ChordRepository.kt, MelodyRepository.kt
└── ui/
    ├── navigation/
    │   └── NavGraph.kt           <-- Jetpack Navigation type-safe routing
    ├── screens/
    │   ├── arrangement/
    │   │   └── AIArrangementScreen.kt <-- Rich 10-step workspace UI
    │   └── home/, settings/, diagnostics/
    └── viewmodel/
        ├── ArrangementViewModel.kt    <-- Handles state and generation tasks
        └── HomeViewModel.kt, SettingsViewModel.kt
```

---

## 4. Kotlin File Inventory

Core source files governing the AI Arrangement Engine ecosystem:

| Path | Primary Responsibility | Size (Est.) |
| --- | --- | --- |
| `domain/model/arrangement/ArrangementModels.kt` | Defines domain models (`ArrangementProject`, `ArrangementTrack`, `ArrangementSection`, `AutomationLane`, `CounterMelody`, `ArrangementTransition`, `ArrangementEvaluation`). | ~150 lines |
| `domain/repository/ArrangementEngine.kt` | Abstract orchestrator interface declaring the unified `orchestrate()` entrypoint. | ~30 lines |
| `domain/repository/ArrangementRepository.kt` | Abstract repository interface specifying data storage and sync signatures. | ~50 lines |
| `data/local/entity/ArrangementEntities.kt` | Declares 100% of the Room DB table configurations, primary keys, and foreign keys. | ~140 lines |
| `data/local/dao/ArrangementDao.kt` | Declares high-efficiency Room transaction methods, including deletion cascading and Flow-based queries. | ~110 lines |
| `data/repository/ArrangementEngineImpl.kt` | Implements the complete **10-step core intelligence compilation pipeline**, live conductor profiles, pattern libraries, and musicological scoring. | ~450 lines |
| `data/repository/ArrangementRepositoryImpl.kt` | Handles Room database transactions, manages multi-thread scheduling via `withContext(Dispatchers.IO)`, and hooks into offline procedural backups. | ~840 lines |
| `ui/viewmodel/ArrangementViewModel.kt` | Exposes reactive state flows, manages generation commands, handles trace log captures, and runs export tasks. | ~400 lines |
| `ui/screens/arrangement/AIArrangementScreen.kt` | Full-scale presentation UI, featuring horizontal timeline grids, trace log outputs, automation lane visualizers, and musicological assessment reports. | ~1350 lines |

---

## 5. Class & Interface Inventory

Core Classes & Interfaces in the Arrangement Ecosystem:

### Domain Interfaces & Models
* **`ArrangementEngine` (Interface)**: Mandates the orchestration interface.
* **`ArrangementRepository` (Interface)**: Dictates project loading, upserting, deletion, and timeline mapping.
* **`ArrangementProject` (Data Class)**: Domain-level parent model tracking metadata, tempo, key, scale, and references to melody/chords.
* **`InstrumentTrack` (Data Class)**: Track definitions holding instrument types, colors, sargam notes, and pattern assignments.
* **`ArrangementSection` (Data Class)**: Individual dynamic sections (Verse, Chorus, etc.) containing localized energy levels, scales, active tracks, and dynamics.
* **`AutomationLane` & `AutomationPoint` (Data Class)**: Holds coordinate mappings tracking parameter adjustments over time.
* **`ArrangementTransition` (Data Class)**: Governs dynamic risers, falls, and fills between sections.
* **`CounterMelody` (Data Class)**: Stores call-and-response sargam phrases.
* **`ArrangementEvaluation` (Data Class)**: Scores final musicological parameters (0-100%).

### Data Layer Implementations
* **`ArrangementEngineImpl` (Class)**: The operational heart of the module. Performs prompt analysis, sets presets, executes Conductor rules, applies Pattern libraries, and evaluates composition flow.
* **`ArrangementRepositoryImpl` (Class)**: Glues local database operations with asynchronous tasks. Automatically triggers `ArrangementEngineImpl` offline, maps database structures, and fallbacks smoothly.

### Local Storage & Presentation
* **`ArrangementDao` (Interface)**: Room DAO mapping CRUD interfaces.
* **`ArrangementViewModel` (Class)**: Presentation bridge keeping StateFlows in lockstep with Room DB changes and executing async pipelines.

---

## 6. Room Database Schema

The database relies on SQLite (via Android Jetpack Room) with a fully normalized 1:N relational layout:

```
                    ┌────────────────────────────┐
                    │ arrangement_projects       │
                    │ - id: TEXT (PK)            │
                    │ - title: TEXT              │
                    │ - genre/mood/key/bpm/scale │
                    └─────────────┬──────────────┘
                                  │
         ┌────────────────────────┼────────────────────────┬────────────────────────┐
         ▼ (1:N)                  ▼ (1:N)                  ▼ (1:N)                  ▼ (1:N)
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│arr_tracks        │     │arr_sections      │     │arr_transitions   │     │arr_automation    │
│- id: TEXT (PK)   │     │- id: TEXT (PK)   │     │- id: TEXT (PK)   │     │- id: TEXT (PK)   │
│- projId: TEXT(FK)│     │- projId: TEXT(FK)│     │- projId: TEXT(FK)│     │- projId: TEXT(FK)│
│- name: TEXT      │     │- secName: TEXT   │     │- fromSec: TEXT   │     │- paramName: TEXT │
│- notes/pattern   │     │- energy: INTEGER │     │- toSec: TEXT     │     │- points: TEXT(JS)│
└──────────────────┘     └──────────────────┘     └──────────────────┘     └──────────────────┘
```

### Table Properties:
1. **`arrangement_projects`**: Parent table storing standard project details, genre parameters, and raga definitions.
2. **`arrangement_tracks`**: Individual track settings. Child table linked via foreign key constraint (`projectId` references `arrangement_projects.id` with `onDelete = Cascade`).
3. **`arrangement_sections`**: Core timeline pieces. Linked to `arrangement_projects` with cascading deletion. Tracks localized automation overrides and instruments.
4. **`automation_lanes`**: Holds lists of time-value points stored as JSON string serialization mapping physical pan, volume, and low-pass filter sweeps.
5. **`arrangement_transitions`**: Declares transition effects between consecutive sections.
6. **`counter_melodies`**: Links call & response counterlines to target sections.
7. **`arrangement_evaluations`**: Evaluation scores linked directly to parent projects.

---

## 7. Dependency Injection Graph

SurMaya AI implements a lightweight, high-performance **ServiceLocator** architecture. This avoids reflection-based overhead at runtime and ensures rapid compilation:

```
                      ┌──────────────────┐
                      │  ServiceLocator  │
                      └────────┬─────────┘
                               │
            ┌──────────────────┼──────────────────┐
            ▼                  ▼                  ▼
   ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
   │   AppDatabase   │ │ArrangementEngine│ │ MelodyRepository│
   └────────┬────────┘ └────────┬────────┘ └─────────────────┘
            │                   │
            ▼                   ▼
   ┌─────────────────┐ ┌─────────────────┐
   │ ArrangementDao  │ │ArrangementRepo  │
   └────────┬────────┘ └────────┬────────┘
            │                   │
            └─────────┬─────────┘
                      ▼
            ┌──────────────────┐
            │ArrangementViewMdl│
            └──────────────────┘
```

### Dependency Instantiation Map:
1. **`ServiceLocator.getDatabase(context)`**: Lazy-loaded singleton instantiation of Room's `AppDatabase`.
2. **`ServiceLocator.getArrangementDao(context)`**: Resolves as `getDatabase(context).arrangementDao()`.
3. **`ServiceLocator.getArrangementEngine()`**: Instantiates thread-safe singleton `ArrangementEngineImpl`.
4. **`ServiceLocator.getArrangementRepository(context)`**: Generates a singleton `ArrangementRepositoryImpl` by injecting the `ArrangementDao`, `ArrangementEngineImpl`, and `context`.
5. **`ArrangementViewModelFactory`**: Accesses `ServiceLocator.getArrangementRepository(context)` to construct the `ArrangementViewModel`, ensuring constructor injection is fully validated.

---

## 8. Navigation Graph

All user navigation journeys in SurMaya AI are type-safe and built with Jetpack Navigation Compose `@Serializable` objects.

```
                  ┌──────────────┐
                  │  HomeScreen  │
                  └──────┬───────┘
                         │
        ┌────────────────┼────────────────┐
        ▼ (Click Compose)                 ▼ (Click Diagnostics Card)
┌──────────────┐                  ┌──────────────────┐
│AIArrangement │                  │DiagnosticsScreen │
└──────────────┘                  └──────────────────┘
```

* **Route Keys**:
  * `com.example.ui.navigation.Home`: Clears backstack, returns to home.
  * `com.example.ui.navigation.AIArrangement(projectId: String)`: Deep links the workspace screen, injecting the unique `projectId` parameter to retrieve the active composition timeline directly from Room.
  * `com.example.ui.navigation.Diagnostics`: Screen displaying real-time API trace telemetry curves and packet exchanges.

---

## 9. AI Gateway Integration Map

To protect SurMaya AI from single-vendor API lock-in, the system routes remote tasks through a centralized integration broker interface:

```
                  ┌────────────────────┐
                  │     AI Gateway     │
                  │ (Unified Broker)   │
                  └─────────┬──────────┘
                            │
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
┌─────────────────┐┌─────────────────┐┌─────────────────┐
│ Gemini Provider ││ ElevenLabs Prov.││ Procedural Core │
│ (Google REST)   ││ (Vocal Synth)   ││ (Local Offline) │
└─────────────────┘└─────────────────┘└─────────────────┘
```

* **Interceptors & Trace Routing**: The REST connection binds an OkHttp network interceptor (`GeminiDiagnosticInterceptor`) to record trace frames. Packet exchanges, network latencies, response payloads, and exception stacks are serialized and piped directly to the interactive UI trace logger.
* **Provider Independence**: Repositories interact only with the gateway broker interface, allowing swapping cloud vendor engines under the hood without breaking client implementation layers.

---

## 10. Public APIs & Repository Interfaces

Core interface declarations ensuring strict, standardized API contracts:

### `ArrangementEngine` Interface
```kotlin
package com.example.domain.repository

import com.example.domain.model.arrangement.ArrangementBlueprint
import com.example.domain.model.chord.GeneratedChordProgression
import com.example.domain.model.melody.GeneratedMelodyPlan

interface ArrangementEngine {
    fun orchestrate(
        title: String,
        songStructureType: String,
        chordProgression: GeneratedChordProgression?,
        melodyPlan: GeneratedMelodyPlan?,
        prompt: String
    ): ArrangementBlueprint
}
```

### `ArrangementRepository` Interface
```kotlin
package com.example.domain.repository

import com.example.domain.model.arrangement.ArrangementProject
import kotlinx.coroutines.flow.Flow

interface ArrangementRepository {
    fun getAllProjects(): Flow<List<ArrangementProject>>
    fun getProjectById(id: String): Flow<ArrangementProject?>
    suspend fun insertProject(project: ArrangementProject)
    suspend fun deleteProjectById(id: String)
    suspend fun generateArrangement(projectId: String): Boolean
}
```

---

## 11. Unit & Integration Test Summary

A robust suite of local JVM and Robolectric tests are configured to verify core algorithms and mapping logic instantly without emulator dependency.

### Active Unit Tests:
1. **`ArrangementEngineUnitTest.kt` (NEW)**:
   * *`testOrchestrateBasicAndPresets()`*: Tests prompts triggering the "Epic Cinematic" scoring template, ensuring French Horns, Strings, Timpani, and correct transitions are assigned.
   * *`testBollywoodPresets()`*: Tests "90s Bollywood" romance prompts, verifying automatic assignment of retro Dholak, Sitar, and custom tempos.
   * *`testEvaluationMetrics()`*: Ensures that overall quality metrics compute correctly, returning a structured score above 80%.
2. **`ChordEngineUnitTest.kt`**:
   * *`testChordProjectMapping()`*: Verifies mapping conversions between database entities and domain objects.
   * *`testMidiNoteAndPitchCalculations()`*: Tests standard mathematical pitch calculations.
3. **`VoiceEngineTest.kt`**: Tests vocal synthesis algorithms and parameter boundaries.

### Integration Tests:
* **`ExampleRobolectricTest.kt`**: Uses Robolectric to verify UI container creation and database loading on local JVM machines.
* **`GreetingScreenshotTest.kt` (Roborazzi)**: Performs pixel-perfect UI verification of screens, catching regressions automatically.

---

## 12. Known Limitations & Technical Debt Register

### Known Limitations (v1.1)
1. **Prompt Matching Strategy**: Currently uses fast string-pattern parsing for preset detection (`isCinematicEpic`, `is90sBollywood`). While fast and reliable offline, this is expanded in the cloud backend to direct Gemini semantic categorization.
2. **MIDI Rendering Capacity**: The arrangement blueprint plans notes, ranges, and patterns but actual MIDI rendering relies on client-side software synthesizers. This keeps CPU usage lightweight but depends on local playback engine parameters.

### Technical Debt Register
1. **Dynamic Audio Rendering Buffer**: Implement on-device PCM synthesizer pipelines to directly play generated arrangements in real-time.
2. **XML Export Formatter**: Finalize full MusicXML and DAW metadata export templates (currently exports JSON data format).

---

## 13. Version History

* **v1.0 (Initial Integration)**: UI dashboard, screen navigation, simple workflow.
* **v1.1 (Current Stable Freeze - MVP Approved)**:
  * Implemented complete **Arrangement Intelligence Pipeline** (10 Steps).
  * Added **AI Conductor** for orchestrating track density.
  * Added **Pattern Library** for syncopated grooves (Dadra, Keherwa, Teental).
  * Incorporated **Cinematic & Bollywood dynamic presets**.
  * Complete Local JVM verification test suite passing 100%.

---
*Signed by: Chief Technology Officer & Principal AI Systems Architect, SurMaya AI.*
