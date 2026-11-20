# 03 — Feature Specifications

Each module lists behaviour, rules, edge cases and acceptance criteria (AC) in Given/When/Then style.

---

## F1. Wake Word & Listening Engine

**Behaviour**
- Foreground service (type `microphone`) keeps a low-power keyword spotter running when enabled.
- Wake phrases: "Hey KAI" (default), "Hey Buddy" (legacy), custom 2–3 word phrase (P2).
- On detection: chime + haptic, orb animates, ASR session starts (max 8 s speech, 1.5 s silence timeout).
- Adaptive duty cycle: reduces polling when device is stationary/screen off at night (user-configurable).

**Rules**
- Persistent notification required (Android policy) with "Pause 1 h" and "Stop" actions.
- Never record or store audio before the wake word fires.
- Pause automatically during phone calls (except answer-call intent) and when another app holds the mic.

**AC**
- Given listening is ON and phone is idle, When user says "Hey KAI", Then orb opens within 300 ms.
- Given another app is recording, When wake word is spoken, Then service stays paused and shows a status hint.
- Given device reboot, When user unlocks, Then service restarts automatically on Android ≤ 14 where allowed, or a one-tap "Resume listening" notification appears on Android 15+.

---

## F2. Voice Assistant (KAI)

### 2.1 Command pipeline
```
Audio → ASR → Normalizer (Hinglish/transliteration)
      → Fast-path intent matcher (rules + fuzzy + small classifier)
      → [if no confident match] LLM intent parser (opt-in)
      → Slot resolver (app/contact/song)
      → Safety gate (confirm if risky)
      → Action executor → TTS + UI feedback
```
- Confidence ≥ 0.85 → execute; 0.6–0.85 → confirm ("WhatsApp kholun?"); < 0.6 → ask to repeat/clarify.
- Risky actions (send message, call unknown number, delete, lock) show a 3-second cancelable confirmation.

### 2.2 Core intents (MVP)
`OPEN_APP`, `CALL_CONTACT`, `ANSWER_CALL`, `REJECT_CALL`, `PLAY_MEDIA`, `LOCK_PHONE`, `FIND_PHONE`, `TOGGLE_SETTING`. Full catalog in doc 11.

### 2.3 Entity resolution
- **Apps:** package-label index + user aliases ("insta" → Instagram, "वॉट्सएप" → WhatsApp). Phonetic/fuzzy (Double Metaphone + edit distance) across scripts.
- **Contacts:** `READ_CONTACTS` index on-device; relationship aliases ("mummy", "papa", "bhaiya"); disambiguation prompt when top-2 scores within 10%.
- **Media:** deep-link to chosen player (YouTube Music, Spotify, JioSaavn, Gaana, local files).

**AC**
- Given "Buddy, WhatsApp kholo", Then WhatsApp opens and KAI says "WhatsApp khol diya."
- Given two contacts named "Rahul", Then KAI asks "Kaun sa Rahul — Rahul Sharma ya Rahul Verma?" and accepts spoken choice or tap.
- Given no network, Then open-app, call, lock, torch, volume still succeed.

### 2.4 Generative layer (v1.5, opt-in)
- LLM receives *only the transcript* + a compact schema of allowed intents/slots (no contact list, no audio) and returns JSON.
- Output validated against schema; unknown/unsafe → fallback to clarification.
- Conversation memory limited to the last 5 turns, stored in RAM; persistent memory (v2) is user-visible and deletable.
- Daily request cap on free tier.

**AC**
- Given "Rahul ko bol do main 10 minute mein pahunch raha hoon", Then KAI proposes WhatsApp/SMS message text, reads it back, and sends only after "haan/send".

---

## F3. Voice Lock

### 3.1 Enrollment
1. Choose language.
2. Record **lock phrase** → show "what I heard" → confirm/redo.
3. Record **unlock phrase** (may equal lock phrase) → confirm.
4. Voiceprint enrollment: say phrase 3–5 times; quality meter (SNR, duration, clipping).
5. Create backup passcode (4–6 digits) — mandatory.
6. Review screen + test unlock.

### 3.2 Security modes
| Mode | Verifies | Use case |
|---|---|---|
| Convenience | Phrase only | Quiet home, low risk |
| Balanced (default) | Phrase + voiceprint | Daily use |
| Strict | Phrase + voiceprint + random challenge word | Shared/public places |

### 3.3 Lock behaviour
- Say lock phrase (wake word not required if "Always listening for lock" is ON) → full-screen overlay locks current app.
- Overlay blocks Back/Home/Recents gestures where the OS permits; kiosk-style handling via task pinning is documented as best-effort.
- Unlock: speak unlock phrase or enter PIN. 5 wrong PINs → 30 s cooldown, doubling up to 15 min.
- After 3 failed voice attempts, PIN pad is surfaced automatically.

### 3.4 App Lock
- Select apps from list (search, suggestions: WhatsApp, Gallery, Banking, Play Store).
- On launch of a locked app, overlay prompts voice/PIN.
- Grace period options: immediately / 1 min / 5 min / until screen off.

### 3.5 Edge cases
- Noisy environment → suggest PIN, show noise indicator.
- Voice changes (cold) → "Re-enroll voice" shortcut; PIN always works.
- Phrase mismatch due to ASR variance → store phonetic variants at enrollment.
- Phone call incoming while locked → allow answer/reject without unlocking (system UI).
- Forgotten PIN → re-authenticate with device screen lock / biometric, then reset.

**AC**
- Given Voice Lock ON, When user says lock phrase, Then overlay appears within 500 ms and Back/Home do not dismiss.
- Given Balanced mode, When someone else says the correct phrase, Then unlock is rejected and failed-attempt counter increments.
- Given no backup passcode, Then the Voice Lock toggle cannot be switched on and an inline explanation is shown.

> **Security note:** an overlay lock is an app-level lock, not the OS keyguard. It must be positioned as "extra privacy layer", never as a replacement for the device screen lock. See doc 06.

---

## F4. Find Phone

- Voice: "Hey KAI, phone kahan hai?" → ring at max volume (media + alarm stream), flashlight strobe, vibrate; auto-stop after 60 s or on "stop".
- Clap detection (opt-in): double clap within 1 s.
- Ringing ignores silent/DND using alarm stream and notification-policy access if granted.
- Optional "last seen" location snapshot when battery critical (explicit opt-in).

**AC**
- Given phone on silent, When user triggers Find Phone, Then it rings audibly within 2 s.

---

## F5. Routines (v1.5)

- Model: `Trigger` (voice phrase | time | charging | Bluetooth connect) → `Actions[]` (open app, set volume, toggle setting, play media, speak text, send template message).
- Builder UI: drag-reorder list, test-run button.
- Templates: Good Morning (read time/weather summary, open news), Driving Mode (DND on, maps, auto-answer calls on speaker), Sleep (DND, alarm check, lock).

**AC**
- Given routine "Driving mode" saved, When user says "Driving mode on", Then actions run in order and KAI confirms completion.

---

## F6. Notifications read-out & summaries (v1.5)

- `NotificationListenerService` (explicit opt-in) reads selected apps' notifications aloud while in Driving Mode.
- Summaries use on-device or opt-in LLM; sensitive apps (banking/OTP) excluded by default.

---

## F7. History, Privacy Center & Data controls

- Command history (local, encrypted): transcript, intent, result, timestamp. No audio stored by default.
- "Delete everything" button wipes history, voiceprint, phrases and resets app.
- Privacy dashboard shows exactly which features use mic, network, or cloud AI.

---

## F8. Accessibility Actions (v2.0)

Accessibility Service (explicit, Play-compliant disclosure) enabling: back, home, recents, scroll, tap by label ("tap Send"), read screen. Strictly user-initiated; never used for ads/analytics/automation without a spoken command.
