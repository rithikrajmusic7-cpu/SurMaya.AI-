# SURMAYA AI - MVP KNOWN LIMITATIONS

This document lists the deliberate bounds, constraints, and limitations of the SurMaya AI Music Operating System MVP release.

---

## 🚫 Functional Scope Boundaries

### 1. Offline Simulation Mode
- **Description**: The audio synthesizer built into view models is designed for immediate feedback during composition. It generates lightweight synthetic representations of instruments rather than full, heavy multi-gigabyte virtual instruments (VSTs).
- **Workaround**: Real-time rendering utilizes local fast mathematical oscillators. Stems generated via the "AI Performance Engine" export clean performance sheets (MIDI details, CC automation vectors) intended to be routed to specialized sampler environments.

### 2. Multi-Time Signature Limitations
- **Description**: The arrangement and performance engines are tuned for standard linear time signatures (`4/4`, `3/4`, `Teental 16-beats`, `Keharwa 8-beats`). Highly volatile multi-time signature transitions or freeform floating non-metered alaap formats are not fully automated in the MVP.
- **Workaround**: Users can segment sections manually and define individual tempos or rhythmic signatures per project section.

---

## ⚙️ Performance & System Constraints

### 3. AudioTrack Buffer Underflow on Older Hardware
- **Description**: High-density multitrack audio playback may cause crackling (buffer underflow) on low-spec devices or devices running background resource-heavy operations.
- **Workaround**: Limit the active studio channels or lower the audio thread synthesis buffer frequency.

### 4. Database Schema Migrations
- **Description**: During active MVP development, SQLite Room schemas are subject to alteration. Backward compatibility for local projects is not guaranteed across major alpha versions.
- **Workaround**: Database schemas utilize destructive reconstruction on migrations (`fallbackToDestructiveMigration()`). User projects should be exported as backup templates periodically.
