# KAIxGenAiAssistant v2.0 Pro
**Har Smartphone Ka Smart Assistant** — Offline-first Indian Hinglish/Hindi Voice Assistant, Dual-Factor Voice Biometric Lock, Per-App Voice Vault, Intruder Selfie Guard & Acoustic Find-My-Phone Suite.

## Key Features (v2.0 Pro Upgrade)
- **Real-Time Acoustic DSP Engine (`AcousticDspEngine`)**: 16 kHz PCM Voice Activity Detection (VAD), impulsive Double-Clap detector, and 16-band Goertzel spectral voiceprint embeddings (cosine similarity verification).
- **Hardware Find-My-Phone (`FindPhoneAlarmEngine`)**: Overrides silent/vibrate with `STREAM_ALARM`, SOS camera flashlight strobe, and rhythmic vibration with automatic 60-second battery-safe shutoff.
- **Per-App Voice Vault (`KaiAppVaultMonitorService`)**: `UsageStatsManager`-based foreground app guard with configurable unlock grace periods and boot-completed auto-restore (`KaiBootReceiver`).
- **Silent Intruder Selfie (`IntruderSelfieCapture`)**: CameraX front-camera capture on 3+ failed PIN/Voice attempts, stored strictly in app-private storage.
- **Hybrid Hinglish/Hindi AI (`GeminiHybridAssistant` + `KaiTtsManager`)**: Instant offline Hinglish command parser with opt-in Gemini 2.5 Flash fallback and real-time `UtteranceProgressListener` TTS state tracking.
- **Bank-Grade Backup PIN (`PinVault`)**: 16-byte random salt + Android Keystore HMAC pepper + 120,000-iteration PBKDF2-HMAC-SHA256 + exponential lockout cooldown + non-destructive Room v1->v2 migration.

## Build & Run
Compatible with **Android Studio**, **GitHub Actions CI**, and **Mobile IDEs (Code on the Go / ACS IDE / Termux)** on Java 17:
```bash
cp .env.example .env
./gradlew testDebugUnitTest assembleDebug
```
