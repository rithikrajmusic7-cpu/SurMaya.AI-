# SurMaya AI - Production Voice Engine (Milestone 3)

## 1. Architectural Design

The Voice Engine is a secure, decoupled, and provider-independent system designed to manage voice recording, cryptographic consent verification, biometric identification, asynchronous modeling queues, and high-fidelity vocal synthesis. 

### Core Decoupling Layer

```
             ┌───────────────────────────────────────────────┐
             │                  Android UI                   │
             └───────────────────────┬───────────────────────┘
                                     │
                                     ▼
             ┌───────────────────────────────────────────────┐
             │                 Voice Gateway                 │
             └───────────────────────┬───────────────────────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         ▼                           ▼                           ▼
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│ Voice Recording │         │ Voice Consensus │         │ Biometric Voice │
│     Module      │         │   Manager (IP)  │         │  Verification   │
└─────────────────┘         └─────────────────┘         └─────────────────┘
         │                           │                           │
         └───────────────────────────┼───────────────────────────┘
                                     ▼
             ┌───────────────────────────────────────────────┐
             │             Voice Job State Queue             │
             └───────────────────────┬───────────────────────┘
                                     │
                                     ▼
             ┌───────────────────────────────────────────────┐
             │             Voice Provider Manager            │
             └───────────────────────┬───────────────────────┘
                                     │
                        ┌────────────┴────────────┐
                        ▼                         ▼
             ┌─────────────────────┐   ┌─────────────────────┐
             │ Primary Voice AI    │   │ Fallback Synthesizer│
             │ Provider Interface  │   │       Service       │
             └─────────────────────┘   └─────────────────────┘
```

---

## 2. Dynamic Sequence Diagrams

### Vocal Cloning Register Flow

This diagram describes the step-by-step pipeline when a user records or uploads their voice characteristics to build a custom cloned vocal model:

```
User UI                VoiceGateway         RecordingModule     UploadValidator     ConsentManager    VerificationEngine    VoiceJobQueue
  │                         │                     │                   │                   │                   │                 │
  ├─► Start Recording ─────►│                     │                   │                   │                   │                 │
  │   (Up to 15 seconds)    ├─► startRecording() ─►                   │                   │                   │                 │
  │                         │                     │                   │                   │                   │                 │
  ├─► Stop Recording ──────►│                     │                   │                   │                   │                 │
  │                         ├─► stopRecording() ──┤                   │                   │                   │                 │
  │                         │                     │                   │                   │                   │                 │
  ├─► Initiate Cloning ────►│                                         │                   │                   │                 │
  │   (Name + Signature)    ├─► recordConsent(Signature) ────────────────────────────────►                    │                 │
  │                         │   ◄─────────────────────────────────────────────────────────┤                    │                 │
  │                         │                                         │                   │                   │                 │
  │                         ├─► validateAudioFile(Output File) ──────►                    │                   │                 │
  │                         │   ◄─────────────────────────────────────┤                   │                   │                 │
  │                         │                                                             │                   │                 │
  │                         ├─► verifyVoiceSignature() ──────────────────────────────────────────────────────►                 │
  │                         │   ◄─────────────────────────────────────────────────────────────────────────────┤                 │
  │                         │                                                                                                   │
  │                         ├─► enqueueJob(VoiceJob) ──────────────────────────────────────────────────────────────────────────►
  │                         │                                                                                                   │
  │   (Asynchronous Task)   │                                                                                                   │
  │   ◄─ Job Registered ────┤                                                                                                   │
```

---

## 3. Component Taxonomy

1. **`VoiceGateway`**: The core API contract and entry point. The UI speaks exclusively to this gateway. It completely prevents downstream components from calling external servers or providers directly.
2. **`VoiceRecordingModule`**: Records high-fidelity microphone input streams via system hardware wrappers. Supports noise reduction, echo suppression (AEC), decibel calculations (RMS dB), silence tracking, and audio trimming.
3. **`VoiceUploadModule`**: Performs automated validation of external audio formats. Rejects clips that do not meet minimum durations, high sample rates (minimum 16kHz), or speech quality thresholds.
4. **`VoiceVerificationEngine`**: Performs biometric checking using acoustic fingerprints. Ensures similarity validation before completing synthesis models.
5. **`VoiceConsentManager`**: Logs digital consent signatures cryptographically, securing legal authorization structures before any vocal training occurs.
6. **`VoiceModelManager`**: Stores, deletes, archives, renames, and exports local or cloud voice profile models.
7. **`VoiceJobQueue`**: Orchestrates non-blocking multi-step modeling schedules asynchronously.
8. **`VoiceProviderManager`**: Manages primary (e.g., ElevenLabs Neural AI) and fallback rendering services, instantly mapping errors to localized `SurMayaException` configurations.
