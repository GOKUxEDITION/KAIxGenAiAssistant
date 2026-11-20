# 13 — Advanced Feature Catalog ("All-in-One" Edition)

This document extends doc 02/03 into the full "god-level" vision: **20 modules, 206 features (199 buildable + 7 explicitly "do not build")**. Everything here is a *candidate backlog*; shipping order is set in §22 and doc 07.

**Phase:** v1.0 / v1.5 / v2.0 / v3.0 (v3.0 = platform & ecosystem)
**Priority (added v1.2):** 🔴 Must = product is not credible without it · 🟡 Should = strong value, ship after core is stable · 🟢 Nice = delight/differentiator, validate demand first · ⛔ = do not build.

**Feasibility on Android / Google Play:**
- ✅ Standard APIs, normal Play review
- ⚠️ Possible, but needs a restricted permission/declaration, OEM workaround, or has ToS/legal caveats (read the note)
- ⛔ Not possible for a normal Play app, or violates policy/law → **do not build** (listed so the team knows why)

---

## M01. Voice Core & Wake Word

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-VC-01 | Always-on wake word "Hey KAI" / "Hey Buddy" | v1.0 | ✅ | 🔴 Must |
| F-VC-02 | Custom wake word (user records 3 samples) | v2.0 | ✅ | 🟢 Nice |
| F-VC-03 | Tap-to-speak orb, floating bubble orb over any app | v1.0 | ⚠️ overlay | 🔴 Must |
| F-VC-04 | Hardware triggers: power-button double-press / volume combo / headset button | v1.5 | ⚠️ OEM-dependent | 🟡 Should |
| F-VC-05 | Set KAI as **default digital assistant** (home-button long-press, lock-screen invoke) | v2.0 | ⚠️ `VoiceInteractionService` | 🟡 Should |
| F-VC-06 | Continuous conversation mode (no repeated wake word for 10 s) | v1.5 | ✅ | 🟡 Should |
| F-VC-07 | Barge-in: interrupt KAI while speaking | v1.5 | ✅ | 🟡 Should |
| F-VC-08 | Whisper mode (quiet-voice recognition + low TTS volume) | v2.0 | ✅ | 🟢 Nice |
| F-VC-09 | Noise-adaptive listening (auto gain, noise suppression, beamforming where HW allows) | v1.5 | ✅ | 🟡 Should |
| F-VC-10 | Bluetooth headset / car-kit mic support | v1.0 | ✅ | 🔴 Must |
| F-VC-11 | Multi-language auto-detect per utterance (Hinglish code-switch) | v1.0 | ✅ | 🔴 Must |
| F-VC-12 | Voice-activity "I'm listening" earcon, haptics, on-screen captions | v1.0 | ✅ | 🔴 Must |
| F-VC-13 | Listening schedule (e.g., only 7am–11pm) and geofence ("not at office") | v1.5 | ✅ | 🟢 Nice |
| F-VC-14 | Mic privacy indicator + listening log (when mic used & why) | v1.0 | ✅ | 🔴 Must |

## M02. GenAI Brain (KAI Intelligence)

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-AI-01 | Free-form natural-language command understanding → structured intents | v1.5 | ✅ | 🔴 Must |
| F-AI-02 | Conversational Q&A (general knowledge) in Hindi/Hinglish/English | v1.5 | ✅ cloud opt-in | 🟡 Should |
| F-AI-03 | On-device small LLM mode (offline Q&A, rewriting) on supported devices | v2.0 | ⚠️ device-limited (AICore/Gemini Nano or quantized 1–3B) | 🟡 Should |
| F-AI-04 | Summarize: notifications, long messages, articles shared to KAI | v1.5 | ✅ | 🟡 Should |
| F-AI-05 | Rewrite/compose: "polite bana do", "short karo", tone presets (formal/friendly/funny) | v1.5 | ✅ | 🟡 Should |
| F-AI-06 | Smart reply suggestions for incoming messages (read aloud, user picks) | v2.0 | ⚠️ notification listener | 🟡 Should |
| F-AI-07 | Translate: speak → translate → speak (face-to-face interpreter mode, 20+ langs) | v1.5 | ✅ | 🟡 Should |
| F-AI-08 | Visual assistant: point camera → "yeh kya hai?", read text aloud (OCR), identify plants/objects, scan menu | v2.0 | ✅ | 🟡 Should |
| F-AI-09 | Screen Q&A: "is screen pe kya likha hai?", summarize current screen | v2.0 | ⚠️ accessibility/MediaProjection, explicit consent each time | 🟡 Should |
| F-AI-10 | Web answers with sources (search-grounded) | v1.5 | ✅ | 🟡 Should |
| F-AI-11 | Personal memory: "yaad rakhna meri car ka number…" (user-visible, editable, deletable vault) | v2.0 | ✅ | 🟡 Should |
| F-AI-12 | Context follow-ups ("usko call karo", "phir se bajao") | v2.0 | ✅ | 🟡 Should |
| F-AI-13 | Voice persona & personality sliders (formal ↔ friendly, brief ↔ detailed) | v2.0 | ✅ | 🟢 Nice |
| F-AI-14 | Learning from corrections (local alias/pronunciation learning) | v1.5 | ✅ | 🟡 Should |
| F-AI-15 | Multi-step task planning ("subah 7 baje alarm, mummy ko reminder, aur playlist chalao") | v2.0 | ✅ with confirmations | 🟡 Should |
| F-AI-16 | Document/Photo Q&A (share PDF/image to KAI, ask questions) | v3.0 | ✅ | 🟢 Nice |
| F-AI-17 | Voice notes → structured output (meeting notes, to-dos, minutes) | v2.0 | ✅ | 🟡 Should |
| F-AI-18 | Safety layer: schema-bound output, red-team tested, confirmation for high-risk actions | v1.5 | ✅ | 🔴 Must |
| F-AI-19 | Daily briefing: weather, calendar, reminders, news headlines, cricket score | v1.5 | ✅ | 🟡 Should |
| F-AI-20 | Learning/Tutor mode: explain concept, quiz me, language-practice partner | v3.0 | ✅ | 🟢 Nice |
| F-AI-21 | Image generation / wallpaper by voice | v3.0 | ✅ cloud, cost | 🟢 Nice |

## M03. Voice Lock & Security Suite

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-SE-01 | Voice phrase lock/unlock + backup PIN (existing) | v1.0 | ✅ | 🔴 Must |
| F-SE-02 | Speaker verification (voiceprint), 3 modes: Convenience / Balanced / Strict | v1.0 | ✅ | 🔴 Must |
| F-SE-03 | Random challenge word (anti-replay) | v1.5 | ✅ | 🟡 Should |
| F-SE-04 | Per-app App Lock with grace periods | v1.0 | ⚠️ usage-stats/accessibility | 🔴 Must |
| F-SE-05 | Lock modes: Voice / PIN / Pattern / Biometric (fingerprint/face via BiometricPrompt) | v1.5 | ✅ | 🟡 Should |
| F-SE-06 | Intruder selfie after N failures (+ time, approx location, saved in encrypted vault) | v2.0 | ✅ foreground-only camera | 🟡 Should |
| F-SE-07 | Fake-crash / "App not responding" decoy screen for locked apps | v2.0 | ✅ | 🟢 Nice |
| F-SE-08 | Decoy PIN → opens harmless profile/fake vault | v2.0 | ✅ | 🟢 Nice |
| F-SE-09 | Hidden vault: photos, videos, documents, notes (AES-256, voice/PIN to open) | v2.0 | ✅ | 🟡 Should |
| F-SE-10 | Secure notes & password hints (not a full password manager) | v2.0 | ✅ | 🟢 Nice |
| F-SE-11 | Notification privacy: hide content of locked apps' notifications | v2.0 | ⚠️ listener | 🟡 Should |
| F-SE-12 | Time-based locks (e.g., Instagram locked 10pm–7am) | v1.5 | ✅ | 🟡 Should |
| F-SE-13 | Location-based locks (auto-relax at home / auto-strict outside) | v2.0 | ⚠️ location disclosure | 🟡 Should |
| F-SE-14 | Lock **Settings, Play Store, Installer, Phone dialer** presets to prevent tampering | v1.5 | ✅ | 🟡 Should |
| F-SE-15 | Guest mode (limited apps for a friend/child, voice-exit only by owner) | v2.0 | ✅ | 🟡 Should |
| F-SE-16 | Kids mode: whitelist apps, session timer, voice-based "ask parent" | v2.0 | ✅ | 🟡 Should |
| F-SE-17 | Panic phrase ("emergency lock"): instant strict lock + silent alert | v2.0 | ✅ | 🟡 Should |
| F-SE-18 | Failed-attempt alert to a trusted contact | v2.0 | ⚠️ needs messaging channel (see M05) | 🟡 Should |
| F-SE-19 | Security health score + recommendations (weak PIN, no OS lock, permissions missing) | v1.5 | ✅ | 🟡 Should |
| F-SE-20 | Anti-uninstall protection via Device Admin | — | ⚠️ Play restricts; **offer as optional, non-default, with clear disclosure** or skip | 🟡 Should |
| F-SE-21 | Remote lock via SMS keyword | — | ⛔ SMS permission restricted; use account/FCM instead (F-FP-08) | ⛔ Don't build |
| F-SE-22 | Screen-capture protection in locked apps (`FLAG_SECURE` where we own the window) | v1.5 | ⚠️ only our own windows | 🟡 Should |
| F-SE-23 | Call recording | — | ⛔ blocked by Android/Play + legal consent issues | ⛔ Don't build |
| F-SE-24 | Spy/stealth mode (hide app icon to spy on others) | — | ⛔ stalkerware policy — never build | ⛔ Don't build |

## M04. Find Phone & Personal Safety

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-FP-01 | Voice ring at max volume ("phone kahan hai?") | v1.0 | ✅ | 🔴 Must |
| F-FP-02 | Double-clap / whistle detection | v1.5 | ✅ | 🟡 Should |
| F-FP-03 | Flashlight strobe + vibration pattern + screen flash | v1.5 | ✅ | 🟡 Should |
| F-FP-04 | Ring through silent/DND (alarm stream + policy access) | v1.0 | ⚠️ policy access optional | 🔴 Must |
| F-FP-05 | "Last known location" saved when battery < 5% | v2.0 | ⚠️ location disclosure | 🟡 Should |
| F-FP-06 | Phone-left-behind alert (Bluetooth earbuds/watch disconnect) | v2.0 | ✅ | 🟡 Should |
| F-FP-07 | SOS voice trigger ("help help help" / custom phrase) → share live location link to trusted contacts + sound siren | v2.0 | ⚠️ SMS restricted → use share-intent/WhatsApp deep link or app-to-app push | 🟡 Should |
| F-FP-08 | Web "Find my KAI phone" (account required): remote ring/lock via push (FCM) | v3.0 | ⚠️ needs backend + account; Google Find My Device already exists | 🟡 Should |
| F-FP-09 | Fake incoming call (escape-uncomfortable-situation) with custom caller & voice | v2.0 | ✅ | 🟢 Nice |
| F-FP-10 | Safe-walk timer: check-in or auto-alert if not cancelled | v2.0 | ⚠️ | 🟡 Should |
| F-FP-11 | Emergency info card on lock screen (user-entered blood group, contacts) | v2.0 | ✅ | 🟡 Should |
| F-FP-12 | Siren / loud alarm on demand | v1.5 | ✅ | 🟡 Should |
| F-FP-13 | SIM-change alert | — | ⚠️ limited API access; evaluate | 🟢 Nice |
| F-FP-14 | Charger-removal/pocket-pick alarm ("anti-theft alarm") | v2.0 | ✅ | 🟢 Nice |

## M05. Calls & Messaging

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-CM-01 | Call by name/number/relationship alias | v1.0 | ✅ | 🔴 Must |
| F-CM-02 | Answer/reject call by voice, speaker on | v1.0 | ✅ | 🔴 Must |
| F-CM-03 | Caller announcer: speak caller name when ringing (headphones/car) | v1.5 | ⚠️ phone-state/contacts | 🟡 Should |
| F-CM-04 | Auto-answer rules (driving, headset connected) | v2.0 | ⚠️ | 🟡 Should |
| F-CM-05 | Dual-SIM choice by voice ("SIM 2 se call karo") | v1.5 | ✅ | 🟡 Should |
| F-CM-06 | Compose SMS/WhatsApp/Telegram draft by voice → opens prefilled chat; user taps Send | v1.5 | ✅ | 🟡 Should |
| F-CM-07 | **Hands-free auto-send** (say "send") | v2.0 | ⚠️ needs accessibility click; WhatsApp has no public send API and may object — present as optional, user-initiated, with ToS risk disclosure | 🟡 Should |
| F-CM-08 | Read incoming messages aloud + voice reply | v1.5 | ⚠️ notification listener | 🟡 Should |
| F-CM-09 | Email dictation (opens Gmail/Outlook draft with subject/body) | v1.5 | ✅ | 🟡 Should |
| F-CM-10 | Auto-reply when driving/busy (with AI-generated polite text) | v2.0 | ⚠️ | 🟡 Should |
| F-CM-11 | Contact intelligence: nicknames, relationships ("meri behen"), VIP list | v1.5 | ✅ | 🟡 Should |
| F-CM-12 | Missed-call summary read-out | v2.0 | ⚠️ call-log restricted → derive from notifications | 🟡 Should |
| F-CM-13 | Call screening / spam hints | v3.0 | ⚠️ `CallScreeningService` role | 🟢 Nice |
| F-CM-14 | Live call translation / call transcription | — | ⛔ call audio capture blocked | ⛔ Don't build |
| F-CM-15 | Meeting join by voice (open Meet/Zoom link from calendar) | v2.0 | ✅ | 🟡 Should |

## M06. Media, Entertainment & Camera

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-ME-01 | Play song/artist/album/playlist in chosen app (YouTube Music, Spotify, JioSaavn, Gaana, local) | v1.0 | ✅ | 🔴 Must |
| F-ME-02 | Transport controls: pause/next/seek/volume/repeat/shuffle | v1.0 | ✅ | 🔴 Must |
| F-ME-03 | Bhajan/devotional mode: time-of-day playlists ("subah ki aarti") | v1.5 | ✅ | 🟡 Should |
| F-ME-04 | Podcast, radio, audiobook by name | v1.5 | ✅ | 🟡 Should |
| F-ME-05 | "What song is this?" (audio fingerprint via partner API) | v3.0 | ⚠️ partner | 🟢 Nice |
| F-ME-06 | Voice camera: "cheese" → photo, "video start/stop", switch camera, timer | v1.5 | ✅ (launch camera intent; in-app camera for hands-free) | 🟡 Should |
| F-ME-07 | Play on Cast/Bluetooth speaker by voice | v2.0 | ✅ | 🟢 Nice |
| F-ME-08 | Sleep timer / sleep sounds | v1.5 | ✅ | 🟡 Should |
| F-ME-09 | Video control (YouTube skip/forward) | v2.0 | ⚠️ accessibility | 🟢 Nice |
| F-ME-10 | Voice-controlled reading mode (read articles, ebooks aloud) | v2.0 | ✅ | 🟢 Nice |

## M07. Productivity

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-PR-01 | Alarms, timers, stopwatch | v1.5 | ✅ | 🟡 Should |
| F-PR-02 | Reminders (time/location/recurring) in natural language | v1.5 | ✅ | 🟡 Should |
| F-PR-03 | Voice notes + transcripts + search | v1.5 | ✅ | 🟡 Should |
| F-PR-04 | To-do lists / shopping lists (shareable) | v1.5 | ✅ | 🟡 Should |
| F-PR-05 | Calendar: add/read/move events | v2.0 | ✅ | 🟡 Should |
| F-PR-06 | Dictation into any text field | v2.0 | ⚠️ accessibility; or build KAI Keyboard (v3.0) | 🟡 Should |
| F-PR-07 | KAI Voice Keyboard (IME): Hindi/Hinglish dictation + AI rewrite | v3.0 | ✅ large effort | 🟢 Nice |
| F-PR-08 | Voice expense tracker ("200 rupaye chai pe") + monthly summary | v2.0 | ✅ | 🟡 Should |
| F-PR-09 | Habit tracker & streaks by voice | v2.0 | ✅ | 🟢 Nice |
| F-PR-10 | Pomodoro/Focus sessions with app blocking | v2.0 | ✅ | 🟢 Nice |
| F-PR-11 | Clipboard voice actions ("copy this", "paste") | v2.0 | ⚠️ | 🟢 Nice |
| F-PR-12 | Quick calculator, unit & currency conversion, date math | v1.5 | ✅ | 🟡 Should |
| F-PR-13 | File finder by voice ("kal wali PDF dikhao") | v2.0 | ⚠️ scoped storage/MediaStore | 🟢 Nice |
| F-PR-14 | Birthday/anniversary reminders with message drafts | v2.0 | ✅ | 🟢 Nice |
| F-PR-15 | Voice journal (daily diary, optional AI reflection prompts) | v3.0 | ✅ sensitive — local-only default | 🟢 Nice |

## M08. Device Control

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-DC-01 | Open any app; open specific in-app screens via deep links | v1.0 | ✅ | 🔴 Must |
| F-DC-02 | Torch, volume, brightness, ring mode, DND | v1.0–1.5 | ✅ | 🔴 Must |
| F-DC-03 | Wi-Fi / Bluetooth / Mobile data / Hotspot / Airplane toggles | v1.5 | ⚠️ Android blocks silent toggles — KAI opens the exact settings panel and can tap via accessibility if enabled; hotspot/airplane ⛔ silent | ⛔ Don't build |
| F-DC-04 | Screenshot, back/home/recents, notification shade, quick settings | v2.0 | ⚠️ accessibility global actions | 🟡 Should |
| F-DC-05 | Scroll/swipe/tap by voice ("neeche scroll karo", "Send dabao") | v2.0 | ⚠️ accessibility | 🟡 Should |
| F-DC-06 | **Voice cursor / number overlay** ("show numbers" → "tap 7") | v2.0 | ⚠️ accessibility | 🟡 Should |
| F-DC-07 | Rotate lock, battery saver prompts, dark mode toggle | v1.5 | ⚠️ | 🟢 Nice |
| F-DC-08 | Battery, storage, RAM, network status read-out | v1.5 | ✅ | 🟡 Should |
| F-DC-09 | App management: "uninstall X" (opens dialog), "clear cache hint" | v2.0 | ⚠️ | 🟢 Nice |
| F-DC-10 | Screen-off / actual device lock command | v1.5 | ⚠️ accessibility lock action or Device Admin | 🔴 Must |
| F-DC-11 | Share-to-KAI target (share any text/link/image → ask KAI) | v1.5 | ✅ | 🟡 Should |

## M09. Automation & Routines (Macro Engine)

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-AU-01 | Routine builder: Trigger → Conditions → Actions | v1.5 | ✅ | 🟡 Should |
| F-AU-02 | Triggers: voice phrase, time, location enter/exit, Wi-Fi/Bluetooth connect, charging, headset, app open, notification received, calendar event, battery level | v1.5–2.0 | ✅ (location ⚠️) | 🟡 Should |
| F-AU-03 | Conditions: time window, weekday, Wi-Fi name, battery %, DND state | v1.5 | ✅ | 🟡 Should |
| F-AU-04 | Actions library (60+): app, media, settings, speak, message draft, notification, HTTP webhook, set variable | v1.5–2.0 | ✅ | 🟡 Should |
| F-AU-05 | Prebuilt templates (Morning, Driving, Office, Gym, Sleep, Study, Travel, Pooja time) | v1.5 | ✅ | 🟡 Should |
| F-AU-06 | Create routine by describing it in words (AI → visual blocks, user reviews) | v2.0 | ✅ | 🟡 Should |
| F-AU-07 | Custom voice commands/macros ("movie night" → dim screen, DND, open Netflix) | v1.5 | ✅ | 🟡 Should |
| F-AU-08 | Variables, loops (repeat N), delays, if/else | v2.0 | ✅ | 🟡 Should |
| F-AU-09 | Import/export routines, community gallery | v3.0 | ✅ moderated | 🟢 Nice |
| F-AU-10 | Tasker/Automate/MacroDroid interop via broadcast intents | v2.0 | ✅ | 🟡 Should |
| F-AU-11 | Run log + dry-run/test mode | v1.5 | ✅ | 🟡 Should |

## M10. Driving & Commute

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-DR-01 | Driving Mode (auto on car Bluetooth / speed via activity recognition) | v1.5 | ⚠️ activity/location | 🟡 Should |
| F-DR-02 | Big-button simplified UI + voice-only operation | v1.5 | ✅ | 🟡 Should |
| F-DR-03 | Navigation by voice (open Maps with destination, "ghar chalo", saved places) | v1.0 | ✅ | 🟡 Should |
| F-DR-04 | Read notifications aloud; reply by voice | v1.5 | ⚠️ | 🟡 Should |
| F-DR-05 | Auto-reply "driving, call you back" | v2.0 | ⚠️ | 🟡 Should |
| F-DR-06 | Speed-limit/Fuel/parking memory ("car yahin park ki") | v2.0 | ✅ | 🟢 Nice |
| F-DR-07 | Android Auto integration (media/messaging templates) | v3.0 | ⚠️ category restrictions | 🟢 Nice |
| F-DR-08 | Trip summary & hands-free call log of the drive | v3.0 | ✅ | 🟢 Nice |

## M11. Accessibility & Inclusion

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-AC-01 | Full voice navigation of KAI itself (every screen voice-operable) | v1.0 | ✅ | 🔴 Must |
| F-AC-02 | Elder mode: large text/buttons, simplified commands, family SOS | v1.5 | ✅ | 🟡 Should |
| F-AC-03 | Voice control of other apps (see F-DC-05/06) | v2.0 | ⚠️ | 🟡 Should |
| F-AC-04 | Screen reader-friendly summaries ("what's on screen") | v2.0 | ⚠️ | 🟡 Should |
| F-AC-05 | Speech-impairment tolerant mode (slower timeouts, personal vocabulary training) | v2.0 | ✅ | 🟡 Should |
| F-AC-06 | Live captions for KAI speech; haptic-only feedback mode | v1.0 | ✅ | 🔴 Must |
| F-AC-07 | Color-blind safe themes, high-contrast, dyslexia-friendly font | v1.5 | ✅ | 🟢 Nice |
| F-AC-08 | Literacy-light UX: icons + audio instructions in the user's language | v1.0 | ✅ | 🔴 Must |

## M12. Health & Wellbeing (non-medical)

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-HW-01 | Medicine/water/stretch reminders (no medical advice, no diagnosis) | v2.0 | ✅ health data = sensitive, local-only | 🟡 Should |
| F-HW-02 | Screen-time & app-usage voice report ("aaj kitna phone chalaya?") | v2.0 | ⚠️ usage stats | 🟡 Should |
| F-HW-03 | Digital detox: focus mode, bedtime lock, "grayscale" suggestion | v2.0 | ✅ | 🟢 Nice |
| F-HW-04 | Guided breathing/meditation audio | v3.0 | ✅ | 🟢 Nice |
| F-HW-05 | Mood journaling | v3.0 | ✅ local-only; no mental-health claims | 🟢 Nice |

## M13. Smart Home & Connected Devices

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-SH-01 | Control lights/fans/AC via Google Home / Matter / Tuya / Home Assistant webhooks | v3.0 | ✅ | 🟢 Nice |
| F-SH-02 | Scenes by voice ("movie mode") bridging to routines | v3.0 | ✅ | 🟢 Nice |
| F-SH-03 | Bluetooth device shortcuts (connect earbuds/speaker by name) | v2.0 | ✅ | 🟢 Nice |
| F-SH-04 | Wear OS companion (wake word on watch, quick lock, find phone) | v3.0 | ✅ | 🟢 Nice |
| F-SH-05 | TV remote via Android TV/Cast | v3.0 | ✅ | 🟢 Nice |

## M14. Utilities Toolbox

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-UT-01 | Calculator, converters, currency, percentage, EMI | v1.5 | ✅ | 🟡 Should |
| F-UT-02 | Translator & dictionary (Hindi ↔ English ↔ regional) | v1.5 | ✅ | 🟡 Should |
| F-UT-03 | QR/barcode scan by voice ("QR scan karo") | v2.0 | ✅ | 🟡 Should |
| F-UT-04 | Document scan → read aloud / save PDF | v2.0 | ✅ | 🟡 Should |
| F-UT-05 | Flashlight morse/SOS, compass, level, ruler | v2.0 | ✅ | 🟢 Nice |
| F-UT-06 | Weather, AQI, sunrise/sunset, panchang/festival dates | v1.5 | ✅ cloud | 🟡 Should |
| F-UT-07 | Cricket/sports scores, news headlines, stock price (info only) | v1.5 | ✅ | 🟡 Should |
| F-UT-08 | Trains/flights/PNR status via official/partner APIs | v3.0 | ⚠️ partner | 🟢 Nice |
| F-UT-09 | UPI/payments by voice | — | ⛔ out of scope (security/regulatory) | ⛔ Don't build |
| F-UT-10 | Bill/OTP read-out | — | ⛔ OTP/SMS content restricted & sensitive; do not build | ⛔ Don't build |

## M15. Personalization & UI

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-PE-01 | Themes (light/dark/AMOLED/dynamic color) + accent picker | v1.0 | ✅ | 🔴 Must |
| F-PE-02 | KAI avatar/orb styles; animated reactions | v1.5 | ✅ | 🟢 Nice |
| F-PE-03 | Multiple TTS voices (male/female/neutral), speed/pitch | v1.0 | ✅ | 🔴 Must |
| F-PE-04 | Home widgets (orb, quick commands, lock toggle, find phone) | v1.5 | ✅ | 🟡 Should |
| F-PE-05 | Quick Settings tiles (Listen, Lock now, Find phone) | v1.5 | ✅ | 🟡 Should |
| F-PE-06 | App shortcuts (long-press icon) | v1.0 | ✅ | 🟡 Should |
| F-PE-07 | Lock-screen shortcut/notification quick actions | v1.5 | ⚠️ | 🟡 Should |
| F-PE-08 | Custom aliases & pronunciation dictionary | v1.0 | ✅ | 🔴 Must |
| F-PE-09 | Greeting & name personalization, festival themes | v1.5 | ✅ | 🟢 Nice |
| F-PE-10 | Gamification: voice streaks, badges ("Lock Master"), tips | v2.0 | ✅ keep optional | 🟢 Nice |

## M16. Family & Multi-user

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-FA-01 | Multiple voice profiles on one phone (each with own phrase/voiceprint/apps) | v2.0 | ✅ | 🟡 Should |
| F-FA-02 | Family circle (opt-in, invite-based): share location in SOS, family reminders | v3.0 | ⚠️ account/backend, privacy | 🟢 Nice |
| F-FA-03 | Parent controls: app time limits, bedtime lock (child's own device, with child-visible disclosure) | v2.0 | ✅ must be transparent — no covert monitoring | 🟡 Should |
| F-FA-04 | Shared lists/reminders across family | v3.0 | ✅ | 🟢 Nice |

## M17. Account, Cloud & Backup (all optional)

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-AB-01 | Anonymous use by default; optional sign-in (Google / phone OTP) | v2.0 | ✅ | 🟡 Should |
| F-AB-02 | End-to-end encrypted backup of settings/routines/aliases (not voiceprint by default) | v2.0 | ✅ | 🟡 Should |
| F-AB-03 | Migrate to new phone (QR/encrypted export) | v2.0 | ✅ | 🟡 Should |
| F-AB-04 | Cross-device sync (phone ↔ tablet ↔ watch) | v3.0 | ✅ | 🟢 Nice |
| F-AB-05 | Web dashboard: manage routines, view history, remote ring | v3.0 | ✅ | 🟢 Nice |

## M18. Platform & Ecosystem

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-PL-01 | **Skills/Plugins store**: third-party actions with declared permissions, sandboxed | v3.0 | ✅ big effort, review pipeline needed | 🟢 Nice |
| F-PL-02 | Public Intents API / deep links for other apps to call KAI | v2.0 | ✅ | 🟡 Should |
| F-PL-03 | Developer console (webhooks, custom intents) | v3.0 | ✅ | 🟢 Nice |
| F-PL-04 | OEM/partner SDK (white-label voice lock) | v3.0 | ✅ business | 🟢 Nice |
| F-PL-05 | Community alias/command packs by region/language | v2.0 | ✅ moderated | 🟡 Should |

## M19. Monetization & Growth

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-MG-01 | Free + Pro tiers, lifetime plan, family plan | v1.5 | ✅ Play Billing | 🟡 Should |
| F-MG-02 | Referral rewards, promo codes | v2.0 | ✅ | 🟢 Nice |
| F-MG-03 | Review prompt after successes; in-app feedback & roadmap voting | v1.0 | ✅ | 🟡 Should |
| F-MG-04 | Contextual upgrade nudges (never on lock/emergency screens) | v1.5 | ✅ | 🟡 Should |

## M20. Support, Trust & Operations

| ID | Feature | Phase | Feas. | Priority |
|---|---|---|---|---|
| F-OP-01 | Interactive onboarding & "try it" tutorials in voice | v1.0 | ✅ | 🔴 Must |
| F-OP-02 | In-app help center (EN/HI+), voice search of help | v1.0 | ✅ | 🔴 Must |
| F-OP-03 | OEM battery/autostart wizard with deep links | v1.0 | ✅ | 🔴 Must |
| F-OP-04 | Diagnostics screen: permission health, mic test, wake test, speaker-score test, export anonymized log | v1.0 | ✅ | 🔴 Must |
| F-OP-05 | Privacy Center, data export & delete-all | v1.0 | ✅ | 🔴 Must |
| F-OP-06 | Remote config/kill switches, staged rollouts, A/B | v1.0 | ✅ | 🔴 Must |
| F-OP-07 | What's-new stories, changelog | v1.0 | ✅ | 🟡 Should |

---

## 21. Policy & feasibility summary (read before promising features)

1. **⛔ Never build:** call recording, stealth/spy mode, OTP/SMS harvesting, silent airplane/hotspot toggling, UPI by voice, anything that covertly monitors another person.
2. **High Play-review risk (plan fallbacks):** overlay lock, AccessibilityService actions (F-DC-04/05/06, F-CM-07), NotificationListener features, usage-stats App Lock, Device Admin.
3. **Messaging truth:** KAI can *prepare* messages and open the right chat; fully automatic sending in third-party apps depends on accessibility clicking and may conflict with those apps' terms. Default behaviour = user taps Send (or says "send" if opted in).
4. **System toggles truth:** since Android 10+ apps can't silently flip Wi-Fi/Bluetooth/airplane/hotspot. KAI opens the exact panel and, if the user enabled accessibility, completes the tap.
5. **Sensitive data:** voiceprint, health reminders, journal, vault = on-device encrypted by default.

## 22. Suggested shipping order

| Release | Theme | Modules |
|---|---|---|
| v1.0 "Core" | Voice + Lock + Find Phone, rock-solid | M01 core, M03 core, M04 core, M08 basics, M06 basics, M15/M20 basics |
| v1.5 "Smart" | GenAI + messaging + routines + productivity | M02, M05, M07, M09, M10, M14 |
| v2.0 "Pro" | Accessibility control, security extras, family, memory, vault | M02 adv., M03 adv., M04 adv., M08 adv., M11, M12, M16, M17 |
| v3.0 "Platform" | Smart home, Wear OS, plugin store, keyboard, web dashboard | M13, M18, M17 adv., M07 IME |

**Totals:** 206 catalogued features across 20 modules (7 are ⛔ and intentionally not built). A small team cannot ship all of this at once — treat v1.0 as the quality bar for everything that follows.

## 23. Priority summary (Must / Should / Nice)

| Priority | Count | Meaning |
|---|---|---|
| 🔴 Must | 32 | The "real useful" core: wake word, Hinglish commands, call/app/music, Voice Lock + speaker verification, App Lock, Find Phone, device basics, accessibility basics, onboarding/OEM wizard/privacy/diagnostics |
| 🟡 Should | 114 | GenAI, messaging, routines, reminders/notes, driving mode, SOS, vault, intruder selfie, family basics, widgets |
| 🟢 Nice | 53 | Delight and platform bets: image generation, gamification, journaling, smart home, plugin store, Wear OS, keyboard |
| ⛔ Don't build | 7 | Policy/legal/technical blockers (see §21) |

**Rule of thumb:** ship all 🔴 first at high quality; pull 🟡 in by user demand and by usage data from v1.0; build 🟢 only after a survey/beta shows ≥ 15% of active users ask for it.
