# ADR-005: AI Gateway Abstraction & Neural Routing

## Status
**APPROVED**

## Context
To execute advanced musical, lyric, and vocal generations, SurMaya AI relies on remote deep learning models (such as Google Gemini, ElevenLabs, VITS, Bark, or custom-hosted diffusion nodes). Interfacing directly with these APIs from within separate repositories would tie the application to vendor-specific schemas, exposing the code to breaking changes, key leakage, and unstable network retry handling.

## Decision
We establish the **SurMaya AI Gateway** as a single client-side orchestrator, credential manager, and telemetry router.

```
+-----------------------------------------------------------------+
|                         SurMaya Repositories                    |
+--------------------------------+--------------------------------+
                                 │
                                 ▼ (Dispatches Normalized Requests)
+--------------------------------v--------------------------------+
|                        AI Gateway Client                        |
|                                                                 |
|   - Credential Decorator (AES-256 GCM Keys via Android Keystore) |
|   - Diagnostics Interceptor (Latency, Payload Size, Errors)     |
|   - Token Bucket Rate Limiter (Prevent runaway API cost)        |
+--------------------------------+--------------------------------+
                                 │
                 +---------------+---------------+
                 │                               │
                 ▼ (Cloud Engines)               ▼ (Local Engines)
        +--------+--------+             +--------+--------+
        |  Gemini REST    |             | Local On-Device |
        |  VITS Server    |             | PCM Synthesizers|
        +-----------------+             +-----------------+
```

- **Vendor Normalization**: The Gateway transforms raw vendor responses (JSON payloads, audio streams) into unified SurMaya domain Blueprints before exposing them.
- **Diagnostic Interceptor**: Implements `GeminiDiagnosticInterceptor` to track payload size, HTTP status codes, and latency, feeding live telemetry to a developer diagnostic dashboard.
- **Security Isolation**: API credentials are saved using EncryptedSharedPreferences and injected solely at the Gateway's network layer.

## Rationale
- **Plug-and-Play Providers**: We can switch cloud vendors (e.g., from ElevenLabs to a custom-hosted VITS node) with zero changes to repositories, ViewModels, or UI screens.
- **Dynamic Cost Protection**: Client-side rate-limiting prevents infinite generation loops from incurring large cloud bills.
- **Secured Credentials**: No plain-text API keys can leak during logcats or debugging.

## Consequences
- **Schema Mapping Overhead**: Requires maintaining translator classes to map vendor schemas into SurMaya schemas.
- **Strict Architecture Discipline**: No inline HTTP clients are allowed anywhere in the codebase outside of the Gateway module.
