# ADR-004: Offline-First AI Strategy & Multi-Tier Resilience

## Status
**APPROVED**

## Context
SurMaya AI is a professional mobile audio workstation. Since mobile musicians and music creators frequently operate in high-latency or completely offline environments (such as studios, trains, or live stages), relying solely on cloud AI servers would result in constant failure states and a broken user experience.

## Decision
We enforce an **Offline-First AI Strategy** across all generative modules. Every generation pipeline implements a rigid 3-Tier offline fallback architecture:

| Tier | Component | Behavior | Primary Target |
| :--- | :--- | :--- | :--- |
| **Tier 1** | **Memory Cache** | Immediate caching of active compositions, lyrics, and layouts to ensure fluid navigation transitions. | Kotlin `LruCache` / `StateFlow` |
| **Tier 2** | **Durable SQLite Persistence** | Completed compositions, vocals, and user metadata are saved locally in the SQLite database via Room. | Room (SQLite) |
| **Tier 3** | **On-Device Synthetic Renderer** | If the device has no internet, all melody and vocal blueprints are routed to custom procedural mathematical oscillators. | Android `AudioTrack` Synthesizers |

```
                       [User Generation Request]
                                   │
                                   ▼
                       [Check Network Status]
                                   ├──► [Online]  ──► Route to Cloud Neural API
                                   │
                                   └──► [Offline] ──► Trigger Local Synth Fallback
```

## Rationale
- **Zero Downtime**: The application never blocks or displays dead-end screens when offline. The creator is always able to draft and listen to their musical compositions.
- **Predictable Costs**: Local synthetics minimize cloud API request charges during drafting and iterative editing phases.
- **Ultra-low Latency**: Allows instant on-device feedback on parameter changes (such as sliding pitch bends) without waiting for network transport roundtrips.

## Consequences
- **Synthetic Playback Quality**: Procedural synthesizers sound synthetic compared to cloud neural audio models, which is addressed by framing on-device rendering as "Offline Draft Mode" with prominent indicators in the UI.
- **Storage Footprint**: Keeping local cached projects requires disciplined Room entity cleanup procedures to prevent excessive storage expansion.
