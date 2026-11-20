# 04 — UX Flows and Screens

## 1. Navigation

**v1.0 launch nav** (pill style, as in current app), 3 tabs: `🔒 Voice Lock` · `🎙 KAI` · `📱 Find Phone`.

**Full product nav (v1.5+)**, defined in doc 14: `Home (KAI)` · `Lock` · `Routines` · `Safety` (contains Find Phone) · `More` (Productivity, Tools, Driving, Wellbeing, Family). The complete list of 200 screens is in `14_Complete_Screen_Inventory.md`; the screen list below is the v1.0 subset.

Top bar: logo + product name left, ⚙ Settings right. Back arrow only on sub-screens.

## 2. Audit of current Hey Buddy screens

Observations from the 4 provided screens, with fixes to carry into KAIxGenAssistant:

| # | Observation | Fix |
|---|---|---|
| 1 | Second tab labelled **"Sunio"** on the Set-Voice-Phrase screen but **"Buddy"** on others | One consistent name: **KAI** |
| 2 | Typo on Buddy screen: "Get things **sone** on your phone" | "Get things **done** on your phone, hands-free" |
| 3 | Voice Lock master toggle is orange while brand colour is blue | Use brand blue for ON; reserve orange for warnings |
| 4 | Backup PIN screen shows `****` in a text field with a visibility icon but no numeric keypad cue | Numeric keypad, 4–6 dot indicators, confirm-PIN step, strength hint |
| 5 | "Finish Setup" button is the only action; no "Confirm PIN" | Two-step entry (enter + re-enter) |
| 6 | Voice phrase card shows plain text "Hare Krishna" | Add play-back of heard text, language chip, "Re-record" |
| 7 | Backup passcode row shows `****` with "Change" | Add last-changed date; require old PIN/device credential to change |
| 8 | Large empty area on Voice Lock home | Show status card: protection level, last lock event, quick test |
| 9 | Illustration has fine print ("Safer / Smarter / You") unreadable | Simplify illustration; ≥ 12 sp text only |
| 10 | Only one example under "Try saying…" | Rotating suggestions (3–5), localized |
| 11 | No visible listening/permission status | Persistent status chip: "Listening • Hey KAI" / "Paused" |

## 3. Design system

- **Colors:** Primary `#1E5BB8` (brand blue), Accent `#00A3FF`, Success `#22A559`, Warning `#F5A623`, Danger `#D93025`, Surface `#FFFFFF` / dark `#0F1420`, Text `#101828`.
- **Typography:** Inter (Latin) + Noto Sans Devanagari/regional; scale 12/14/16/20/28/34.
- **Shape:** 20–28 dp radius cards, soft elevation, pill bottom bar.
- **Orb:** animated gradient blob; states — idle (slow breathe), listening (amplitude-reactive), thinking (swirl), speaking (pulse), error (shake + red tint).
- **Dark mode** and **dynamic font size** supported at launch.
- **Motion:** 200–300 ms ease-out; respect "reduce motion".

## 4. Screen inventory

### Onboarding
1. Welcome (value prop, language picker)
2. Microphone permission (why + privacy promise)
3. Display-over-apps permission (for lock) — with GIF how-to
4. Set voice phrase → "what I heard" → confirm
5. Voiceprint enrollment (3–5 repeats, quality meter)
6. Backup passcode (enter + confirm)
7. Battery/auto-start guidance (OEM-specific)
8. Try it: "Hey KAI, open WhatsApp"

### Main
- **Voice Lock Home** — master toggle, protection level badge, Voice Phrase, Voiceprint, Backup Passcode, App Lock, Test lock.
- **App Lock List** — search, selected apps, grace period.
- **KAI Home** — orb "Tap to speak", live transcript, suggestions, recent commands.
- **Command Result Sheet** — recognized text, intent chip, Cancel/Confirm, TTS replay.
- **Find Phone** — big "Ring my phone" button, clap toggle, flashlight toggle, test.
- **Routines** — list, templates, builder.
- **History & Privacy Center** — history list, data toggles, delete all.
- **Settings** — Language, Wake word, Voice/TTS, Lock options, Permissions health, Notifications, Theme, Backup/Export, About/Legal, Help.

### Overlays
- **Lock Overlay** — blurred app snapshot hidden, lock icon, "Say your unlock phrase", waveform, PIN pad fallback, noise warning.
- **Listening Overlay** — compact orb at bottom, transcript line.

## 5. Key flows

### Flow A — First-time setup
Welcome → Language → Mic permission → Overlay permission → Phrase → Voiceprint → PIN → Battery guide → Try it → Home.
*Failure paths:* mic denied → explain, deep-link to settings; overlay denied → Voice Lock disabled with clear banner.

### Flow B — Voice command
Wake word / tap orb → listening → transcript streams → intent chip appears → (confidence gate) → action → spoken + visual confirmation → returns to previous app if action opened one.

### Flow C — Lock & unlock
Say lock phrase → overlay (≤ 500 ms) → say unlock phrase → verify (phrase + voice) → success haptic + dismiss. 3 fails → PIN pad. 5 wrong PINs → cooldown timer.

### Flow D — Forgot PIN
Tap "Forgot PIN?" → Android device credential/biometric prompt → set new PIN → voiceprint retained.

### Flow E — Send message by voice (v1.5)
"Rahul ko WhatsApp karo main late hoon" → resolve contact → draft shown + read back → "Send?" → voice "haan"/tap → sent → confirmation.

## 6. Empty, error & edge states

- No mic permission, mic busy, no speech detected ("Kuch sunai nahi diya, phir bolo"), ASR unsupported language, no match found (suggest similar apps/contacts), offline LLM feature (graceful fallback), battery saver blocking service.
- All errors have: plain message, one primary fix action, optional "Learn more".

## 7. Accessibility checklist

TalkBack labels on all controls incl. orb; focus order; haptics for state changes; captions for all spoken output; no information by colour alone; minimum 48 dp targets; supports switch access.

## 8. Copy guidelines

Friendly, short, bilingual. Voice: warm, confident, never robotic. Examples — success: "Ho gaya!"; clarification: "Kaun sa Rahul?"; error: "Samajh nahi aaya, dobara bolo?"
