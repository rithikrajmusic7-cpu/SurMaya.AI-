# 🎤 SurMaya AI - AI Vocal Intelligence Engine Specification
**Version:** 1.0.0  
**Status:** `FOUNDATION APPROVED` 🟡 (Staged for MVP Integration)  
**Author:** CTO & Engineering Team  
**Date:** July 2026

---

## 1. Founder Document

### Objective & Core Identity
The **AI Vocal Intelligence Engine** (formerly *AI Singer Engine*) is the core performance synthesis layer of SurMaya AI. Its primary mission is to convert structured, symbolic musical data (chords, melody, tempo, and lyrics) into highly expressive, culturally authentic, and multilingual human-like vocal performances. 

Unlike standard text-to-speech (TTS) or static singing synthesizers, the AI Vocal Intelligence Engine acts as an expressive performer. It interprets the emotional subtext of a piece and models physical breathing constraints, while injecting traditional regional micro-tonal ornamentations (such as *Meend*, *Gamak*, and *Murki* in Indian classical music) to create a performance that feels human, alive, and distinctly Indian.

### Core Architecture Pillars
1. **Multilingual Phonetic Translation**: Dynamic syllable-level decomposition and International Phonetic Alphabet (IPA) mapping.
2. **Physical Respiration Modeling**: Dynamic lung capacity simulation, realistic breath scheduling, and oxygen-depletion warnings.
3. **Micro-tonal Ornamentation synthesis**: Real-time insertion of traditional ornaments based on genre, tempo, and pitch intervals.
4. **Emotional Prosody Modulation**: Direct influence of emotional vectors (e.g., *Sad*, *Romantic*, *Devotional*) on vocal attributes like vibrato, breathiness, and vocal register power.

---

## 2. Staged Approval Framework
To maintain the high engineering standards of SurMaya AI, this module follows a rigorous staged lifecycle before final release:

```
[ Foundation Approved ] ──► [ Stable for MVP Integration ] ──► [ Architecture Frozen ]
      (Current)                       (Pending UI/Storage)              (Production Ready)
```

1. **Foundation Approved (v1.0.0) [CURRENT STATUS]**
   * ✅ Core domain models structured (`PhonemeSegment`, `VocalOrnamentation`, `BreathMarker`, `VocalPhrase`, `VoiceIdentity`).
   * ✅ Core service interfaces declared (`ISingerEngine`, `IPhonemeMapper`, `IOrnamentationHandler`).
   * ✅ Algorithms implemented for Phoneme conversion, Ornamentation injection, Breath planning, and Emotional modulation.
   * ✅ Clean Architecture repository layer implemented (`SingerRepositoryImpl`).
   * ✅ Fully verified with comprehensive local unit tests running on JVM/Robolectric.

2. **Stable for MVP Integration (v1.1.0) [PENDING]**
   * ⏳ Complete Jetpack Compose Vocal Workspace (UI/UX).
   * ⏳ Integration with AI Studio Gateway / Gemini REST Client.
   * ⏳ Local persistence (Room Database integration) for vocals, lyrics, and configurations.
   * ⏳ End-to-end vocal render pipelines with offline fallback support.

3. **Architecture Frozen (v1.2.0) [PENDING]**
   * ⏳ Comprehensive integration testing.
   * ⏳ Final multi-singer voice database validation.
   * ⏳ Audio stream rendering/playback locks.

---

## 3. Technical Specification (v1.0.0)

### Interface Contracts
Located in `com.example.domain.model.singer`:

```kotlin
// Master Engine Orchestrator
interface ISingerEngine {
    fun getVoiceIdentity(voiceId: String): VoiceIdentity
    
    fun synthesizeVocals(
        projectId: String,
        lyrics: String,
        config: SingerConfiguration,
        tempo: Float,
        keyMidi: Int
    ): VocalSynthesisResult
}

// Phoneme mapping contract
interface IPhonemeMapper {
    fun convertLyricsToPhonemes(
        lyrics: String,
        language: String,
        tempo: Float,
        baseMidi: Int
    ): List<PhonemeSegment>
}

// Ornamentation generation contract
interface IOrnamentationHandler {
    fun injectOrnamentations(
        phonemes: List<PhonemeSegment>,
        style: String,
        meendIntensity: Float,
        gamakIntensity: Float,
        murkiIntensity: Float
    ): List<VocalOrnamentation>
}
```

### Data Pipelines (v1.0.0 Input / Output Contracts)

#### Input: `PerformanceBlueprint` / Engine Config
* **Lyrics**: Plain text lyrics in Hindi, Odia, Sanskrit, or English.
* **SingerConfiguration**:
  * `voiceId`: Selected vocal identity.
  * `style`: Vocal style (e.g., "Classical", "Bollywood", "Rap").
  * `emotion`: Target emotion (e.g., "Sad", "Romantic", "Devotional").
  * `vibratoDepth`, `breathiness`, `power`, `softness`.
* **Tempo**: Current BPM of the arrangement.
* **KeyMidi**: Base root key of the melody.

#### Output: `VocalBlueprint` (`VocalSynthesisResult`)
* **VoiceIdentity**: Metadata profile of the synthesized singer.
* **VocalPhrases**: Contains list of `PhonemeSegment`s with IPA characters, `VocalOrnamentation` markers (such as Meend slides, Gamak, and Murkis), and `BreathMarker`s.
* **VocalSynthesisValidation**: Explainable report checking for:
  * Out-of-range physical pitch limits (sub-bass floors/soprano ceilings).
  * Oxygen starvation (warning generated when singing runs too long without a breath marker).
* **SummaryAuditReport**: Comprehensive, visually scannable diagnostic log of the synthesized performance.

---

## 4. Future Engine Roadmap (v1.1.0+)

The following sub-engines are designated for future implementation to elevate the system to a full-fledged **AI Vocal Intelligence Engine**:

### A. Sub-Engines
1. **Prosody Engine**: Dynamic word-level sentence stress and rhythm emphasis mapping.
2. **Syllable Alignment Engine**: Exact timing alignment between MIDI notes, lyrics, and bar boundaries.
3. **Regional Pronunciation Engine**: Expanding standard Hindi lookup to native phonetic maps for Bengali, Tamil, Telugu, Kannada, Malayalam, and Punjabi.
4. **Harmony Voice Engine**: Automatic generation of 2-part and 3-part harmonies, octave doubling, and backing choir textures.
5. **Lip Sync Metadata Engine**: Exporting visemes, mouth shapes, and facial timing parameters for future 3D AI vocalist avatar support.

### B. Vocal Database Schema
Each profile in the Voice Database will be expanded to encompass:
```text
VoiceID | Gender | Age Range | Language | Accent | Range | Tessitura | Brightness | Warmth | Power | Breathiness | Vibrato | Preferred Genres | Emotion Range
```

---

*SurMaya AI Engineering Board - Approved for Integration Phase.*
