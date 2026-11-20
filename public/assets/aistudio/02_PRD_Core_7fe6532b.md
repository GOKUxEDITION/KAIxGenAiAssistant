# 02 — PRD Core

## 1. Overview

KAIxGenAssistant is organized into four product pillars (mirroring the current bottom navigation, extended):

| Pillar | Tab | Summary |
|---|---|---|
| Voice Lock | 🔒 Voice Lock | Voice-secured phone/app lock with speaker verification and backup PIN |
| Assistant | 🎙 KAI (was "Buddy") | Natural-language, multilingual voice commands powered by on-device NLU + optional GenAI |
| Find Phone | 📱 Find Phone | Locate phone by voice/clap, ring at max volume, last-known location |
| Automations (new) | ⚡ Routines | Voice-triggered routines, reminders, quick actions |

Settings (gear icon) is global.

> **Extended vision:** the full "all-in-one" feature set (206 features, 20 modules) is in `13_Advanced_Feature_Catalog.md`; all screens in `14_Complete_Screen_Inventory.md`; functions/APIs in `15_Functions_and_API_Spec.md`. This document defines the v1.0–v2.0 core scope.

## 2. Personas

**P1 — Riya, 27, working professional (Hinglish speaker).** Cooks and commutes. Wants "Hey KAI, mummy ko call karo" and WhatsApp replies by voice. Pain: assistants mis-hear Hinglish.

**P2 — Suresh, 52, shopkeeper (Hindi/Telugu).** Low English literacy. Wants simple big-button UI, voice-only operation, a lock that his kids can't guess.

**P3 — Aman, 19, student.** Shares phone with siblings; wants selected apps (Instagram, Photos) locked by his own voice.

**P4 — Meera, 34, user with limited hand mobility.** Needs full voice operation including answering calls and scrolling.

## 3. Scope

### 3.1 In scope — v1.0 (MVP)
- Rebrand and UI refresh to KAIxGenAssistant
- Wake word ("Hey KAI" / "Hey Buddy") via foreground service
- Existing commands: open app, call, answer/reject call, play song, lock phone
- Hindi / English / Hinglish NLU (on-device fast-path)
- Voice Lock with speaker verification (opt-in strength levels)
- App Lock with voice phrase (per-app)
- Backup passcode with rate-limit and cooldown
- Find Phone v2
- "What I heard" confirmation; command history (local)

### 3.2 In scope — v1.5
- Messaging by voice (WhatsApp/SMS compose + confirm + send)
- Generative understanding (LLM) for free-form commands (opt-in)
- Voice routines ("Good morning" → set of actions)
- Notification read-out and summaries
- Reminders/alarms/timers via voice

### 3.3 In scope — v2.0
- Context/memory ("call him again", "play the same song")
- In-app actions via Accessibility Service (scroll, tap, back, home)
- Intruder alert (front-camera snapshot after N failed unlocks)
- Multi-profile voiceprints (family)
- Widget, Quick Settings tile, lock-screen shortcut

### 3.4 Out of scope
iOS, wearables, smart-home, enterprise MDM, payments by voice.

## 4. Functional requirements

Priority: **P0** = must for MVP, **P1** = should (v1.5), **P2** = could (v2+).

### 4.1 Onboarding & permissions
| ID | Requirement | Pri |
|---|---|---|
| FR-ON-01 | Guided onboarding ≤ 5 steps: language → microphone → overlay → voice phrase → backup PIN | P0 |
| FR-ON-02 | Each permission shown with plain-language reason and "skip" where non-essential | P0 |
| FR-ON-03 | Detect missing permissions at runtime and show inline fix-it cards | P0 |
| FR-ON-04 | Battery-optimization exemption guidance per OEM (Xiaomi, Oppo, Vivo, Samsung, Realme) | P0 |
| FR-ON-05 | Sample "try it now" command at end of onboarding | P1 |

### 4.2 Assistant (KAI)
| ID | Requirement | Pri |
|---|---|---|
| FR-AS-01 | Activate via wake word, tap-to-speak orb, or hardware shortcut | P0 |
| FR-AS-02 | Open installed app by spoken name (fuzzy match, aliases, Hindi names) | P0 |
| FR-AS-03 | Call a contact by spoken name; disambiguate when multiple matches | P0 |
| FR-AS-04 | Answer / reject incoming call by voice | P0 |
| FR-AS-05 | Play a song/artist/playlist via default or chosen music app | P0 |
| FR-AS-06 | Hinglish and code-switching understood ("WhatsApp kholo") | P0 |
| FR-AS-07 | Show live transcript + recognized intent before executing risky actions | P0 |
| FR-AS-08 | Send message by voice with read-back and confirm | P1 |
| FR-AS-09 | Free-form requests handled by LLM → structured intent (opt-in) | P1 |
| FR-AS-10 | Toggle Wi-Fi/Bluetooth/torch/volume/brightness/DND by voice | P1 |
| FR-AS-11 | Follow-up context (“phir se bajao”, “usko call karo”) | P2 |
| FR-AS-12 | Spoken responses (TTS) in user's language; can be muted | P0 |
| FR-AS-13 | Command history with delete-all | P1 |

### 4.3 Voice Lock
| ID | Requirement | Pri |
|---|---|---|
| FR-VL-01 | Record custom lock phrase and unlock phrase (2–3 words each) | P0 |
| FR-VL-02 | Show recognized text before saving ("what I heard") | P0 |
| FR-VL-03 | Cannot enable Voice Lock without a backup passcode | P0 |
| FR-VL-04 | Lock screen overlay covers all apps; Back/Home do not dismiss | P0 |
| FR-VL-05 | Unlock by voice phrase **and** speaker match (configurable: Phrase-only / Phrase + Voiceprint) | P0 |
| FR-VL-06 | Backup 4–6 digit passcode always available (existing 4-digit Hey Buddy PINs migrate unchanged) | P0 |
| FR-VL-07 | Rate-limit passcode attempts with exponential cooldown | P0 |
| FR-VL-08 | App Lock: choose apps; lock/unlock per app by voice | P0 |
| FR-VL-09 | Enrollment of voiceprint (3–5 samples, noise check, quality meter) | P0 |
| FR-VL-10 | Liveness/replay-attack heuristics (random challenge word option) | P1 |
| FR-VL-11 | Intruder snapshot after N failed attempts | P2 |
| FR-VL-12 | Recovery flow if PIN forgotten (re-auth via device credentials) | P0 |

### 4.4 Find Phone
| ID | Requirement | Pri |
|---|---|---|
| FR-FP-01 | Trigger ring at max volume via voice ("KAI, where are you?") | P0 |
| FR-FP-02 | Optional double-clap trigger | P1 |
| FR-FP-03 | Flashlight blink + vibrate while ringing | P1 |
| FR-FP-04 | Stop ringing by voice/tap | P0 |
| FR-FP-05 | Last-known location saved locally when battery < 5% (opt-in) | P2 |

### 4.5 Routines & automation
| ID | Requirement | Pri |
|---|---|---|
| FR-RT-01 | Create routine: trigger phrase → ordered action list | P1 |
| FR-RT-02 | Prebuilt templates (Good Morning, Driving Mode, Sleep, Workout) | P1 |
| FR-RT-03 | Create routine by voice description via LLM | P2 |

### 4.6 Settings
Language(s), wake word sensitivity, voice/TTS, privacy & data, permissions status, lock options, theme, backup/restore (local export), about/legal.

## 5. Non-functional requirements

| Area | Requirement |
|---|---|
| Performance | Wake word latency ≤ 300 ms; fast-path command p50 ≤ 1.2 s; cold start ≤ 2 s |
| Battery | Always-on listening ≤ 2% battery/hour on reference mid-range device; adaptive duty cycling |
| Accuracy | Wake word false-activation ≤ 1 per 10 hours idle; recall ≥ 95% in quiet, ≥ 85% in moderate noise |
| Reliability | Crash-free sessions ≥ 99.5%; service auto-restart after OS kill / reboot (where permitted) |
| Offline | Open app, call, lock, find phone, torch, volume fully offline |
| Privacy | No raw audio leaves the device unless user opts in to cloud AI per feature |
| Security | PIN salted + hashed (PBKDF2/Argon2) with Keystore-wrapped key; voiceprint in encrypted storage |
| Accessibility | TalkBack labels, ≥ 48 dp touch targets, contrast ≥ 4.5:1, font scaling to 200% |
| Localization | Launch (v1.0): EN + HI UI, ASR and Hinglish. v1.5: TE, TA, KN, ML, MR, OR, BN language packs. Existing Hey Buddy lock-phrase languages keep working from day 1 via system speech recognition |
| Compatibility | Android 8.0–latest; 1.5 GB RAM devices degrade gracefully (no on-device LLM) |
| Size | Base APK/AAB ≤ 60 MB; models via on-demand download (Play Asset Delivery) |

## 6. Dependencies

Android SpeechRecognizer / on-device ASR model, wake-word engine, speaker-embedding model, optional LLM provider, Play Console policy approvals (see doc 10).

## 7. Open questions

1. Which LLM path: fully on-device small model, or hybrid cloud with opt-in?
2. Wake word licensing (open-source vs commercial)?
3. Monetization at launch (free + ads vs freemium)? See doc 12.
4. Tab naming: current screens show both "Sunio" and "Buddy" — pick one (proposed: **KAI**).
