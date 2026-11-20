# 05 — Technical Architecture

## 1. Principles
On-device first · modular · testable · permission-minimal · OEM-resilient · privacy by construction.

## 2. Tech stack (proposed)

| Layer | Choice | Notes |
|---|---|---|
| Language | Kotlin (100%) | Coroutines + Flow |
| UI | Jetpack Compose + Material 3 | Custom orb via Canvas/AGSL shader |
| Architecture | MVVM + Clean Architecture, multi-module | `:app :core :feature-lock :feature-assistant :feature-findphone :feature-routines :nlu :audio :data` |
| DI | Hilt | |
| Storage | Room (history/routines), EncryptedSharedPreferences/DataStore + Android Keystore | SQLCipher optional |
| Wake word | Porcupine (commercial) or openWakeWord/TFLite (open-source) | Decision pending (licensing, Hindi accent quality) |
| ASR | Android `SpeechRecognizer` (on-device where available) + optional Whisper-tiny/small via ONNX/TFLite for offline multilingual | Language packs on demand |
| Speaker verification | ECAPA-TDNN / x-vector small model via TFLite/ONNX; embeddings + cosine similarity | Enroll 3–5 samples; threshold calibrated per user |
| NLU fast path | Rule grammar + fuzzy match + small intent classifier (TFLite) | Deterministic, < 50 ms |
| NLU generative | Hybrid: (a) on-device small LLM (Gemini Nano / AICore if available, or 1–3B quantized) (b) cloud LLM via own gateway, opt-in | Always JSON-schema-constrained output |
| TTS | Android TTS (Google/Indic engines); optional neural voice pack | |
| Background | Foreground service (`microphone`), WorkManager for deferred jobs | |
| Lock UI | `SYSTEM_ALERT_WINDOW` overlay + transparent activity + `FLAG_SECURE` | |
| Analytics/Crash | Firebase Crashlytics; privacy-safe analytics (no transcripts) | |
| Remote config | Firebase Remote Config / own flags | Staged rollouts |
| CI/CD | GitHub Actions, Gradle build cache, Fastlane → Play internal/closed/open tracks | |
| Min/Target SDK | 26 / latest | |

## 3. High-level architecture

```
┌──────────────────────────── Android Device ────────────────────────────┐
│  UI (Compose)  ◄──── ViewModels ◄──── UseCases ◄──── Repositories      │
│                                                                        │
│  ┌──────────────┐   ┌───────────┐   ┌─────────┐   ┌─────────────────┐  │
│  │ Listening FGS│──►│ Wake Word │──►│  ASR    │──►│  NLU Router     │  │
│  │ (mic)        │   └───────────┘   └─────────┘   │ fast-path / LLM │  │
│  └──────┬───────┘                                 └────────┬────────┘  │
│         │ audio frames                                     │ intent    │
│         ▼                                                  ▼           │
│  ┌──────────────┐                              ┌─────────────────────┐ │
│  │ Speaker Verif│◄─────── lock flow ───────────│ Action Executor     │ │
│  └──────────────┘                              │ App/Call/Media/Sys  │ │
│                                                │ Lock/FindPhone/Msg  │ │
│  ┌────────────┐  ┌──────────────┐              └──────────┬──────────┘ │
│  │ Secure Store│  │ Room (local) │◄─────────────────────────┘          │
│  │ Keystore    │  │ history/rout.│                                     │
│  └────────────┘  └──────────────┘                                     │
└───────────────────────────────┬────────────────────────────────────────┘
                                │ (opt-in only; transcript text only)
                         ┌──────▼──────┐
                         │ LLM Gateway │  auth, rate limit, no logging of content
                         └─────────────┘
```

## 4. Key components

### 4.1 Listening Service
- Foreground service type `microphone`; started from a user-visible context (Android 14+ restriction). On Android 15+ a microphone FGS cannot be launched from `BOOT_COMPLETED`, so after reboot the app shows a one-tap notification ("Tap to resume Hey KAI"); on older versions it may auto-restart.
- Audio: 16 kHz mono PCM ring buffer (≤ 2 s) in RAM only; discarded unless wake word hits.
- Handles audio focus, call-state, other-app mic capture (`AudioRecordingCallback`), battery saver, Doze.

### 4.2 NLU Router
1. Normalize (lowercase, transliterate Devanagari↔Latin, strip fillers: "please", "zara", "na").
2. Fast-path: pattern + slot extraction → confidence.
3. Fallback to LLM if enabled and confidence < threshold.
4. Output `Intent(name, slots, confidence, source)`.
5. Safety gate evaluates `RiskLevel` (LOW/MED/HIGH) → confirm policy.

### 4.3 Action Executor (capability interfaces)
`AppLauncher` (PackageManager + intents) · `CallManager` (`TelecomManager`, `ANSWER_PHONE_CALLS`) · `MediaController` (MediaSession / deep links) · `SystemSettings` (torch, volume, DND, Bluetooth) · `MessageSender` (WhatsApp deep link / SmsManager, user-confirmed) · `LockController` · `FindPhoneController`.

Each capability exposes `canExecute()` (permission check) and `execute()` returning `Result` with a user-presentable error.

### 4.4 Voice Lock subsystem
- `PhraseMatcher`: ASR transcript vs enrolled phrase using normalized + phonetic comparison (threshold configurable).
- `SpeakerVerifier`: embedding extraction → cosine similarity vs enrolled centroid; threshold τ per user from enrollment variance; optional score fusion with phrase confidence.
- `LockOverlayManager`: WindowManager overlay (`TYPE_APPLICATION_OVERLAY`), consumes back/home where possible, hides content from recents (`FLAG_SECURE`).
- `PinVault`: PBKDF2-HMAC-SHA256 (≥ 600k iterations, per current OWASP guidance) or Argon2id, per-install salt, Keystore-held pepper (note: a 4-digit PIN has only 10,000 combinations, so security rests on the Keystore pepper + attempt limits, not hashing alone); attempt counter persisted tamper-resistantly.
- `AppLockWatcher`: foreground-app detection via UsageStatsManager or (v2) Accessibility events.

### 4.5 Language & model management
Play Asset Delivery on-demand packs per language (ASR + NLU lexicon + TTS hint). Model integrity verified by SHA-256 signature; fallback to system recognizer.

## 5. Data model (local)

```
Profile(id, language, createdAt)
Phrase(id, type[LOCK|UNLOCK], textNormalized, phoneticKey, lang)       -- encrypted
Voiceprint(id, embeddingBlob, threshold, createdAt, version)           -- encrypted, device-bound
PinRecord(hash, salt, iterations, failedCount, cooldownUntil, changedAt)
LockedApp(packageName, graceMode)
Command(id, ts, transcript, intent, slotsJson, status, latencyMs)      -- encrypted, user-deletable
Routine(id, name, triggerJson, actionsJson, enabled)
Alias(id, kind[APP|CONTACT], spoken, target)
Setting(key, value)
```
Nothing above is synced to any server.

## 6. Permissions matrix

| Permission | Feature | When requested | Notes |
|---|---|---|---|
| RECORD_AUDIO | All voice | Onboarding | Required |
| FOREGROUND_SERVICE + FOREGROUND_SERVICE_MICROPHONE | Wake word | On enabling listening | Notification mandatory |
| POST_NOTIFICATIONS (13+) | FGS notice | Onboarding | |
| SYSTEM_ALERT_WINDOW | Lock overlay | When enabling Voice Lock | User toggles in Settings |
| READ_CONTACTS | Call/message by name | First call command | Optional |
| CALL_PHONE | Direct dial | First call command | Falls back to dialer |
| ANSWER_PHONE_CALLS / READ_PHONE_STATE | Answer/reject | When enabling feature | |
| QUERY_ALL_PACKAGES or `<queries>` | Open apps by name | Install | Needs Play declaration; prefer `<queries>` launcher intent |
| PACKAGE_USAGE_STATS | App Lock detection | When enabling App Lock | Special access |
| BIND_ACCESSIBILITY_SERVICE | v2 in-app actions | Explicit opt-in | Strict Play policy |
| BIND_NOTIFICATION_LISTENER_SERVICE | Read-out/summaries | Explicit opt-in | |
| CAMERA | Intruder snapshot (v2), torch | Opt-in | |
| ACCESS_NOTIFICATION_POLICY | Ring through DND | Optional | |
| WRITE_SETTINGS / BLUETOOTH_CONNECT | System toggles | On use | |
| USE_BIOMETRIC | PIN recovery | On recovery | |

## 7. Backend (minimal, only for opt-in features)

- **LLM Gateway** (serverless): auth via Play Integrity token + anonymous install ID; per-install rate limiting; request = transcript + schema version; response = JSON intent; **no content logging**, 0-day retention contract with provider.
- **Config service**: feature flags, model manifests, language pack versions.
- **Entitlements** (if paid): Play Billing server verification.

## 8. Performance budget

| Item | Budget |
|---|---|
| Wake word CPU | < 3% on Snapdragon 6-series class |
| RAM (idle service) | < 80 MB |
| Speaker verify | < 300 ms |
| Fast-path NLU | < 50 ms |
| LLM (cloud) | < 1.5 s p90 incl. network on 4G |
| App size | AAB ≤ 60 MB; each language pack ≤ 40 MB |

## 9. OEM resilience

Xiaomi/MIUI, Oppo/ColorOS, Vivo/FuntouchOS, Realme, Samsung One UI, OnePlus: in-app guided steps for Autostart, Battery "No restrictions", lock app in Recents, overlay permission quirks. Maintain a `oem_playbook.json` remotely updatable.

## 10. Observability

Crashlytics, ANR tracking, non-PII performance traces (wake latency, ASR latency, NLU source, action success), service-uptime heartbeat (local counters, aggregated opt-in).
