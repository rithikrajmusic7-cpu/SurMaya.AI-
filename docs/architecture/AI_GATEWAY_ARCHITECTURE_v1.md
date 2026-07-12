# SurMaya AI - AI Gateway Architecture Specification (v1.0)
## Document Information
* **Project**: SurMaya AI Music Operating System
* **Domain**: AI Infrastructure & Core Routing
* **Version**: 1.0.0-draft
* **Author**: Chief Technology Officer & AI Systems Architect
* **Target Audience**: Core Engineering, Integration Teams, Devops

---

## 1. Architectural Vision & Objectives
The **SurMaya AI Gateway** is the central neural router of the SurMaya AI Music Operating System. It abstracts complex, heterogeneous, multi-modal generative AI pipelines into a single, high-performance, resilient endpoint client interface on Android.

### 1.1 Core Objectives
* **API Vendor Abstraction**: Isolate application-level business logic from vendor-specific REST/gRPC API changes (e.g., Google Gemini, Suno AI, Meta MusicGen, ElevenLabs, or self-hosted stable-audio models).
* **Robust Failover & Fallbacks**: Implement intelligent client-side dynamic fallbacks (e.g., migrating to local on-device synthetic models if network latency spikes or API tokens fail).
* **Granular Observability & Telemetry**: Intercept, trace, and audit all request-response flows with millisecond-precision diagnostic logging.
* **Security & Credential Isolation**: Securely coordinate API key injection using cryptographic credential storage, protecting keys from memory sniffing or leakage during logging.
* **Rate Limiting & Cost Control**: Implement token-bucket token budgeting at the client level to prevent runaway API billing.

---

## 2. Structural Topology & Data Flow

```
                                  +---------------------------------------+
                                  |         SurMaya Android UI            |
                                  |  (HomeScreen, CreateSongScreen, etc.) |
                                  +-------------------+-------------------+
                                                      |
                                                      | Exposes state flows
                                                      v
                                  +-------------------+-------------------+
                                  |         ViewModels & Usecases         |
                                  |  (MusicViewModel, SingerViewModel)    |
                                  +-------------------+-------------------+
                                                      |
                                                      | Calls Repository methods
                                                      v
                                  +-------------------+-------------------+
                                  |      MusicGenerationRepository        |
                                  |     (Mediates DB + Network Jobs)     |
                                  +-------------------+-------------------+
                                                      |
                                                      | Dispatches to
                                                      v
                               +----------------------+----------------------+
                               |             AI Gateway Client               |
                               | (Core Orchestrator, Token Budgeter, Router) |
                               +----------------------+----------------------+
                                                      |
                        +-----------------------------+-----------------------------+
                        |                             |                             |
                        v                             v                             v
           +------------+------------+   +------------+------------+   +------------+------------+
           |     Lyrics Router       |   |      Audio Router       |   |     Vocalizer Router    |
           +------------+------------+   +------------+------------+   +------------+------------+
                        |                             |                             |
         +--------------+--------------+     +--------+--------+            +-------+-------+
         |                             |     |                 |            |               |
         v                             v     v                 v            v               v
  +------+------+               +------+------+  +------+------+     +------+------+ +------+------+
  | Gemini      |               | Suno        |  | Local       |     | ElevenLabs  | | Bark        |
  | Lyrics      |               | Music Engine|  | Synth Backup|     | Vocal Engine| | Voice Synth |
  +-------------+               +-------------+  +-------------+     +-------------+ +-------------+
```

### 2.1 Request Lifecycle
1. **Initiation**: The user clicks "Generate Song". The presentation layer passes parameters to `MusicViewModel`.
2. **Enqueueing**: The request is converted into a durable state model in the Room database (`JobEntity`) and managed via `WorkManager`.
3. **Gateway Dispatch**: The `JobWorker` invokes the `AIGateway`.
4. **Credential Decoration**: The gateway intercepts the call, fetches credentials from `ApiCredentialManager`, and injects them.
5. **Dynamic Routing**: The Gateway determines the best active provider based on connectivity, credit balance, and developer preferences (e.g., "Developer Mode -> Force Real APIs" vs "Offline Fallback").
6. **Execution & Interception**: The HTTP call is routed through `GeminiDiagnosticInterceptor` to measure latency, parse status codes, and catch edge-case failure exceptions.
7. **Response Normalization**: Vendor-specific schemas are deserialized and transformed into uniform SurMaya domain models.
8. **Callback / State Update**: The UI state updates reactively via cold Flow emissions.

---

## 3. Core Subsystems

### 3.1 Network Transport Layer
Using a single optimized OkHttp Client instance shared across all retrofitted provider interfaces to enforce connection-pooling, HTTP/2 multiplexing, and custom timeout policies.

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(GeminiDiagnosticInterceptor())
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    .build()
```

### 3.2 Security, Keys & Auth Injection
Credentials must never exist in plain text or standard `SharedPreferences`. The gateway sources credentials from `ApiCredentialManager` which integrates:
1. **EncryptedSharedPreferences**: Underlying key-value storage encrypted with AES-256 GCM using Android Keystore System keys.
2. **Static BuildConfig Injection**: Fallback keys injected during Gradle packaging from highly protected local environment files (`.env`) using the **Secrets Gradle Plugin**.

### 3.3 Rate Limiting & Dynamic Cost Control
To prevent excessive billing under high-frequency compilation loops, the gateway uses a token-bucket rate limiter.
* **On-Device Bucket**: Tracks execution occurrences locally. If requests for a specific pipeline exceed a configurable threshold (e.g., 5 requests per minute for high-cost Suno tracks), the gateway intercepts with a `RateLimitException`.
* **State Mapping**: Emits standard status updates directly to `JobPipeline` signaling to back off.

---

## 4. Caching & Offline Resilience Engine

To maintain operational integrity on poor connections (very common for mobile audio production), the gateway implements a multi-tier offline strategy:

| Tier | Component | Behavior | Primary Storage |
|------|-----------|----------|-----------------|
| **1** | Memory Cache | Fast caching of generated lyrics/structure configurations to prevent redundant LLM invocations on UI orientation changes. | Kotlin `LruCache` / `StateFlow` |
| **2** | Local DB Storage | Completed songs, lyrics, and model weights metadata persist locally in the Room Database. | Room (SQLite) |
| **3** | Synthetic Fallback Engine | If all cloud providers fail and the user is completely offline, the gateway routes MIDI/Chords creation to the on-device PCM Waveform/Synthesizer engine to generate custom procedurally-generated scratch tracks. | Local Audio Generator (`AudioTrack`) |

---

## 5. Diagnostic Tracing & Observability
We have integrated a state-of-the-art diagnostic reporting engine (`GeminiDiagnosticInterceptor`) that records detailed trace metrics for every API transaction.

### 5.1 Recorded Metrics
* **Total Transaction Latency**: Time elapsed from request writing to response parsing (measured in milliseconds).
* **Payload Verification**: Size of audio buffers/JSON responses.
* **Header Analysis**: Inbound/Outbound authorization tokens validation without exposing raw keys in the output logs.
* **Status Code Mapping**: Explicit tracking of HTTP status codes:
  * `200 OK`: Success state.
  * `401 Unauthorized`: Bad API keys.
  * `429 Too Many Requests`: Triggered vendor rate limits.
  * `5xx Server Error`: Downstream vendor failures.

### 5.2 Realtime Diagnostics Screen Integration
Telemetry metrics stream directly into `NetworkDiagnosticScreen` in real-time. This provides engineers with full visibility into the live API connection pool, active thread allocations, and raw response payloads during diagnostics.
