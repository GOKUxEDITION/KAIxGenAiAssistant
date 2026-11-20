# 14 — Complete Screen Inventory

Every screen, sheet, overlay, widget and system surface in the full "all-in-one" product. IDs are used in design files and tickets.

**Legend:** Phase = v1.0 / v1.5 / v2.0 / v3.0 · Type = Screen, Sheet (bottom sheet), Dialog, Overlay, Widget, Tile, Notification, System.
**Global states every screen must handle:** loading, empty, error, offline, permission-missing, dark mode, large font, TalkBack, voice-operable.

---

## S0. App shell & navigation

Bottom navigation (5 max): **Home (KAI) · Lock · Routines · Safety · More**. Find Phone lives under Safety; Productivity/Tools live under More. A global ⚙ and a floating "tap to speak" orb are available everywhere (except on the lock overlay).

| ID | Screen | Type | Phase | Purpose / key components |
|---|---|---|---|---|
| S0-01 | Splash | Screen | 1.0 | Logo animation, migration check from Hey Buddy |
| S0-02 | Main shell (bottom nav) | Screen | 1.0 | Tabs, status chip "Listening • Hey KAI", global orb |
| S0-03 | Global search / command palette | Screen | 1.5 | Type or speak: search features, settings, history, contacts |
| S0-04 | What's new | Sheet | 1.0 | Story-style release notes |
| S0-05 | Force-update / maintenance | Screen | 1.0 | Kill-switch messaging |

## S1. Onboarding & setup (12)

| ID | Screen | Phase | Key components |
|---|---|---|---|
| S1-01 | Welcome + language | 1.0 | Value prop carousel, language chips (EN/HI/…) |
| S1-02 | Migration from Hey Buddy | 1.0 | Detect existing phrase/PIN, "Keep my settings" |
| S1-03 | Microphone permission | 1.0 | Reason, privacy promise, "Allow" |
| S1-04 | Notification permission | 1.0 | Explain service notification |
| S1-05 | Overlay permission guide | 1.0 | Animated how-to, deep link, status check |
| S1-06 | Voice phrase setup | 1.0 | Record, live waveform, "what I heard", redo |
| S1-07 | Voiceprint enrollment | 1.0 | 3–5 repeats, quality meter, noise warning |
| S1-08 | Backup passcode (enter + confirm) | 1.0 | Keypad, dots, strength hint |
| S1-09 | Battery/autostart wizard (OEM-specific) | 1.0 | Step cards, deep links, verify |
| S1-10 | Optional permissions (contacts, phone, usage access, notifications listener, accessibility) | 1.0 | Per-permission cards, skip allowed |
| S1-11 | Choose features (Lock / Assistant / Safety / Driving…) | 1.5 | Personalised setup path |
| S1-12 | Try-it tutorial + completion | 1.0 | "Say: Hey KAI, open WhatsApp", confetti |

## S2. KAI Assistant (Home)

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S2-01 | KAI Home | Screen | 1.0 | Orb, greeting, suggestion chips, daily briefing card, recent commands |
| S2-02 | Listening state | Overlay | 1.0 | Waveform, live transcript, cancel |
| S2-03 | Thinking / result card | Sheet | 1.0 | Recognized text, intent chip, action result, replay TTS, 👍/👎 |
| S2-04 | Confirm action | Sheet | 1.0 | 3-s countdown, Cancel/Confirm, edit text |
| S2-05 | Disambiguation picker | Sheet | 1.0 | Contacts/apps/songs list, voice select |
| S2-06 | Conversation (chat) view | Screen | 1.5 | Threaded messages, voice+text input, sources, copy/share |
| S2-07 | Command history | Screen | 1.5 | Search, filter, replay, delete |
| S2-08 | Command help / "What can I say?" | Screen | 1.0 | Categorised examples, tap-to-try |
| S2-09 | Daily briefing | Screen | 1.5 | Weather, calendar, reminders, news, scores |
| S2-10 | Interpreter mode | Screen | 1.5 | Split-screen two-language live translation |
| S2-11 | Visual assistant (camera) | Screen | 2.0 | Live camera, "what is this", OCR read-aloud |
| S2-12 | Share-to-KAI sheet | Sheet | 1.5 | Summarize / translate / rewrite / save |
| S2-13 | Floating bubble orb | Overlay | 1.5 | Draggable, quick actions |
| S2-14 | Memory vault | Screen | 2.0 | List of remembered facts, edit/delete, export |
| S2-15 | Persona & voice | Screen | 2.0 | Personality sliders, voice picker, preview |

## S3. Voice Lock & Security

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S3-01 | Voice Lock home / dashboard | Screen | 1.0 | Master toggle, protection level, last events, test lock |
| S3-02 | Voice phrase manager | Screen | 1.0 | Lock + unlock phrases, language, re-record |
| S3-03 | Phrase recording | Screen | 1.0 | Waveform, heard text, confirm |
| S3-04 | Voiceprint manager | Screen | 1.0 | Re-enroll, strictness slider, test score |
| S3-05 | Security mode selector | Screen | 1.0 | Convenience / Balanced / Strict with explanations |
| S3-06 | Backup passcode manager | Screen | 1.0 | Change (needs old PIN or device credential), 4/6-digit |
| S3-07 | Lock overlay (voice) | Overlay | 1.0 | Waveform, "Say your phrase", PIN fallback, noise hint, emergency call |
| S3-08 | Lock overlay (PIN/pattern/biometric) | Overlay | 1.0–1.5 | Keypad, pattern grid, BiometricPrompt |
| S3-09 | Cooldown screen | Overlay | 1.0 | Countdown, remaining attempts |
| S3-10 | Forgot PIN / recovery | Screen | 1.0 | Device-credential prompt, reset |
| S3-11 | App Lock list | Screen | 1.0 | Search, toggles, presets (Social, Banking, Gallery) |
| S3-12 | App Lock per-app settings | Screen | 1.5 | Grace period, mode, schedule, decoy |
| S3-13 | Lock presets for Settings/Play Store/Installer | Screen | 1.5 | One-tap hardening |
| S3-14 | Time-based lock rules | Screen | 1.5 | Day/time pickers |
| S3-15 | Location-based lock rules | Screen | 2.0 | Map, geofence, mode |
| S3-16 | Intruder log | Screen | 2.0 | Photo grid, time, attempt type, delete |
| S3-17 | Fake-crash/decoy settings | Screen | 2.0 | Choose decoy screen |
| S3-18 | Decoy PIN setup | Screen | 2.0 | Second PIN → fake profile |
| S3-19 | Vault home | Screen | 2.0 | Photos / Videos / Docs / Notes tabs |
| S3-20 | Vault item viewer & import | Screen | 2.0 | Viewer, import picker, export |
| S3-21 | Secure notes editor | Screen | 2.0 | Rich text, lock per note |
| S3-22 | Guest mode setup | Screen | 2.0 | Allowed apps, exit rules |
| S3-23 | Kids mode setup & kid home | Screen | 2.0 | App whitelist, timer, ask-parent |
| S3-24 | Panic phrase setup | Screen | 2.0 | Phrase, behaviour (silent alert/strict lock) |
| S3-25 | Security health check | Screen | 1.5 | Score ring, fix-it list |
| S3-26 | Security event log | Screen | 1.5 | Locks/unlocks/failures timeline |
| S3-27 | Trusted contacts for alerts | Screen | 2.0 | Add/verify contacts |

## S4. Safety & Find Phone

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S4-01 | Safety home | Screen | 1.0 | Find Phone, SOS, Fake call, Siren, Safe walk cards |
| S4-02 | Find Phone settings | Screen | 1.0 | Voice phrase, clap, flash, duration, volume |
| S4-03 | Ringing state | Overlay | 1.0 | Big STOP, flashing |
| S4-04 | Last known location | Screen | 2.0 | Map, timestamp, battery at save |
| S4-05 | SOS setup | Screen | 2.0 | Trigger phrase, contacts, message template, test |
| S4-06 | SOS active | Screen | 2.0 | Siren, live-share status, cancel with PIN |
| S4-07 | Fake call setup & fake call UI | Screen | 2.0 | Caller name, ringtone, voice script |
| S4-08 | Safe-walk timer | Screen | 2.0 | Duration, destination, check-in |
| S4-09 | Emergency info card | Screen | 2.0 | Editable info, lock-screen visibility |
| S4-10 | Anti-theft alarm settings | Screen | 2.0 | Pocket/charger triggers, sensitivity |
| S4-11 | Phone-left-behind alerts | Screen | 2.0 | Paired device selection |
| S4-12 | Remote find (web/account) setup | Screen | 3.0 | Sign-in, device list |

## S5. Calls & Messaging

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S5-01 | Calls & contacts settings | Screen | 1.0 | Default SIM, aliases, VIPs |
| S5-02 | Contact alias manager | Screen | 1.0 | "Mummy → contact", pronunciation |
| S5-03 | Message composer (voice) | Sheet | 1.5 | Draft, tone chips, read-back, Send/Edit |
| S5-04 | Messaging apps & preferences | Screen | 1.5 | WhatsApp/SMS/Telegram default, auto-send opt-in |
| S5-05 | Read-aloud notifications settings | Screen | 1.5 | Allowed apps, sensitive filters, quiet hours |
| S5-06 | Auto-reply rules | Screen | 2.0 | Templates, contacts, schedule |
| S5-07 | Incoming call announcer | Screen | 1.5 | Conditions (headset/car), voice |
| S5-08 | Auto-answer rules | Screen | 2.0 | Conditions, delay, speaker |
| S5-09 | Call screening (v3) | Screen | 3.0 | Spam hints, rules |
| S5-10 | Meeting join helper | Sheet | 2.0 | Next meeting, "Join" |

## S6. Media & Camera

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S6-01 | Media preferences | Screen | 1.0 | Default music/podcast app per type |
| S6-02 | Now-playing mini control | Sheet | 1.5 | Voice transport |
| S6-03 | Devotional/time-of-day playlists | Screen | 1.5 | Rules, playlists |
| S6-04 | Sleep timer & sounds | Screen | 1.5 | Timer, soundscapes |
| S6-05 | Voice camera | Screen | 1.5 | Hands-free capture, voice shutter |
| S6-06 | Reading mode (read aloud) | Screen | 2.0 | Text pane, highlight, speed |

## S7. Productivity

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S7-01 | Productivity hub | Screen | 1.5 | Cards for Reminders, Notes, Lists, Calendar, Expenses, Habits |
| S7-02 | Reminders list & editor | Screen | 1.5 | Voice add, repeat, location |
| S7-03 | Alarms / timers / stopwatch | Screen | 1.5 | Quick timers, labels |
| S7-04 | Voice notes list & detail | Screen | 1.5 | Audio + transcript, search, share |
| S7-05 | To-do / shopping lists | Screen | 1.5 | Check-off by voice |
| S7-06 | Calendar view & event editor | Screen | 2.0 | Day/agenda, voice create |
| S7-07 | Expense tracker | Screen | 2.0 | Quick add, categories, monthly chart |
| S7-08 | Habit tracker | Screen | 2.0 | Streaks, reminders |
| S7-09 | Focus / Pomodoro | Screen | 2.0 | Timer, blocked apps |
| S7-10 | File finder | Screen | 2.0 | Spoken query, results |
| S7-11 | Meeting notes summarizer | Screen | 2.0 | Record → transcript → minutes |
| S7-12 | Birthdays & occasions | Screen | 2.0 | List, message drafts |
| S7-13 | Voice journal | Screen | 3.0 | Entries, local-only badge |
| S7-14 | KAI keyboard setup | Screen | 3.0 | Enable IME, languages |

## S8. Routines & Automation

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S8-01 | Routines home | Screen | 1.5 | Active routines, templates, run now |
| S8-02 | Template gallery | Screen | 1.5 | Morning, Driving, Office, Gym, Sleep, Study, Travel |
| S8-03 | Routine builder | Screen | 1.5 | Trigger / Conditions / Actions blocks, drag-reorder |
| S8-04 | Trigger picker | Sheet | 1.5 | Voice, time, location, Wi-Fi, BT, charging, app, notification |
| S8-05 | Action picker | Sheet | 1.5 | Categorised actions with permission badges |
| S8-06 | Condition editor | Sheet | 1.5 | Time window, battery, Wi-Fi, DND |
| S8-07 | Create by describing (AI) | Screen | 2.0 | Text/voice description → blocks → review |
| S8-08 | Variables & logic editor | Screen | 2.0 | If/else, loops, delays |
| S8-09 | Custom voice commands | Screen | 1.5 | Phrase → macro |
| S8-10 | Run log & test | Screen | 1.5 | Dry-run, timeline, errors |
| S8-11 | Webhook/HTTP action editor | Screen | 2.0 | URL, headers, test |
| S8-12 | Community gallery | Screen | 3.0 | Browse, install, report |
| S8-13 | Import/export | Sheet | 2.0 | File/QR |

## S9. Driving mode

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S9-01 | Driving home | Screen | 1.5 | Big orb, 4 giant actions (Maps, Call, Music, Messages) |
| S9-02 | Driving settings | Screen | 1.5 | Auto-start (BT/car/activity), allowed notifications |
| S9-03 | Auto-reply while driving | Screen | 2.0 | Templates |
| S9-04 | Parked-car memory | Screen | 2.0 | Map pin, photo, notes |
| S9-05 | Trip summary | Screen | 3.0 | Route, calls, stats |
| S9-06 | Android Auto templates | System | 3.0 | Media/message templates |

## S10. Accessibility & control

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S10-01 | Accessibility hub | Screen | 1.5 | Elder mode, captions, haptics, contrast |
| S10-02 | Elder mode home | Screen | 1.5 | Extra-large buttons, simplified commands |
| S10-03 | Accessibility service consent (prominent disclosure) | Screen | 2.0 | Plain-language disclosure, Agree/Decline |
| S10-04 | Voice control overlay (numbers/grid) | Overlay | 2.0 | Numbered targets, grid zoom |
| S10-05 | Speech-training / personal vocabulary | Screen | 2.0 | Practice phrases, adapt timeouts |
| S10-06 | Screen description sheet | Sheet | 2.0 | "What's on screen" read-out |

## S11. Device control & tools

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S11-01 | Device status | Screen | 1.5 | Battery, storage, network read-out |
| S11-02 | System toggles guide | Sheet | 1.5 | "Open Wi-Fi panel" with tap-assist |
| S11-03 | Tools hub | Screen | 1.5 | Calculator, converter, translator, QR, scanner, compass |
| S11-04 | Translator | Screen | 1.5 | Text/voice, history |
| S11-05 | Document scanner | Screen | 2.0 | Capture, crop, OCR, PDF |
| S11-06 | QR scanner | Screen | 2.0 | Camera, result actions |
| S11-07 | Info cards (weather/AQI/news/scores) | Screen | 1.5 | Cards, refresh |
| S11-08 | Bluetooth device shortcuts | Screen | 2.0 | Named devices, connect by voice |

## S12. Smart home & wearables

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S12-01 | Smart-home hub | Screen | 3.0 | Providers, devices, scenes |
| S12-02 | Provider link flow (Matter/Tuya/HA) | Screen | 3.0 | OAuth/URL+token |
| S12-03 | Scene editor | Screen | 3.0 | Devices & states |
| S12-04 | Wear OS companion screens | Screen | 3.0 | Orb, lock now, find phone |
| S12-05 | TV/Cast control | Screen | 3.0 | Remote UI |

## S13. Health & wellbeing

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S13-01 | Wellbeing hub | Screen | 2.0 | Screen time, reminders, focus |
| S13-02 | Reminders (water/medicine/stretch) | Screen | 2.0 | Schedule, snooze; "not medical advice" note |
| S13-03 | Screen-time report | Screen | 2.0 | Per-app chart |
| S13-04 | Bedtime / detox settings | Screen | 2.0 | Schedule, lock rules |
| S13-05 | Breathing/meditation | Screen | 3.0 | Guided audio |

## S14. Family

| ID | Screen | Type | Phase | Components |
|---|---|---|---|---|
| S14-01 | Profiles switcher | Screen | 2.0 | Voice profile list |
| S14-02 | Profile editor | Screen | 2.0 | Phrase, voiceprint, apps, theme |
| S14-03 | Family circle | Screen | 3.0 | Invites, roles, shared lists |
| S14-04 | Child-device disclosure screen | Screen | 2.0 | Transparency to child |

## S15. Settings (global)

| ID | Screen | Phase | Contents |
|---|---|---|---|
| S15-01 | Settings home | 1.0 | Grouped list + search |
| S15-02 | Language & region | 1.0 | UI, ASR, TTS languages, Hinglish toggle |
| S15-03 | Wake word & listening | 1.0 | Phrase, sensitivity, schedule, geofence, test |
| S15-04 | Voice & speech output | 1.0 | TTS voice, speed, pitch, volume, mute |
| S15-05 | Command behaviour | 1.0 | Confirmation levels, timeouts, haptics |
| S15-06 | Permissions health | 1.0 | Every permission: status, why, fix |
| S15-07 | Notifications settings | 1.0 | Service notification, alerts |
| S15-08 | Appearance | 1.0 | Theme, accent, orb style, font scale |
| S15-09 | Widgets & tiles | 1.5 | Add instructions |
| S15-10 | Shortcuts & triggers | 1.5 | Hardware triggers, QS tiles |
| S15-11 | Default assistant setup | 2.0 | Role request, guide |
| S15-12 | AI settings | 1.5 | On-device/cloud toggle, daily cap, data use explainer |
| S15-13 | Integrations | 2.0 | Calendar, music, messaging, smart home |
| S15-14 | Backup & restore / transfer | 2.0 | Encrypted export, QR transfer |
| S15-15 | Account | 2.0 | Sign-in, devices, delete account |
| S15-16 | Subscription / Pro | 1.5 | Plans, restore, manage |
| S15-17 | Developer options | 2.0 | Logs, flags, intents API keys |
| S15-18 | About | 1.0 | Version, licences, SBOM |
| S15-19 | Legal | 1.0 | ToS, privacy, DPDP notice, grievance officer |
| S15-20 | Language packs & models | 1.5 | Download/delete, sizes |
| S15-21 | Battery & performance | 1.0 | Usage, duty-cycle, OEM wizard |
| S15-22 | Accessibility settings | 1.5 | Link to S10 |
| S15-23 | Notification listener consent | 1.5 | Prominent disclosure |
| S15-24 | Usage-access consent | 1.0 | Prominent disclosure |
| S15-25 | Location consent | 2.0 | Prominent disclosure |

## S16. Privacy & data

| ID | Screen | Phase | Contents |
|---|---|---|---|
| S16-01 | Privacy Center | 1.0 | Data map, per-feature toggles |
| S16-02 | Listening log | 1.0 | Sessions, reasons |
| S16-03 | Cloud AI consent | 1.5 | What is sent, opt-in/out |
| S16-04 | Analytics & crash toggles | 1.0 | Switches |
| S16-05 | Data export | 1.5 | Machine-readable export |
| S16-06 | Delete all data | 1.0 | Confirm with PIN |
| S16-07 | Voiceprint consent | 1.0 | Separate explicit consent |
| S16-08 | Children's privacy notice | 2.0 | For kids mode |

## S17. Pro, growth & support

| ID | Screen | Phase | Contents |
|---|---|---|---|
| S17-01 | Paywall | 1.5 | Plans, restore, no dark patterns |
| S17-02 | Plan comparison | 1.5 | Free vs Pro table |
| S17-03 | Referral | 2.0 | Code, rewards |
| S17-04 | Help center | 1.0 | Search (voice), articles, OEM guides |
| S17-05 | Contact support / feedback | 1.0 | Form, log attach (opt-in) |
| S17-06 | Roadmap voting | 2.0 | Feature requests |
| S17-07 | Diagnostics | 1.0 | Mic test, wake test, speaker-score test, export log |
| S17-08 | Rate us | 1.0 | After 3 successes |

## S18. Widgets, tiles, notifications, system surfaces

| ID | Surface | Type | Phase | Contents |
|---|---|---|---|---|
| S18-01 | Orb widget (1×1) | Widget | 1.5 | Tap to speak |
| S18-02 | Quick actions widget (4×1) | Widget | 1.5 | Lock, Find, Torch, Recent |
| S18-03 | Lock status widget | Widget | 1.5 | Toggle + level |
| S18-04 | QS tile: Listen | Tile | 1.5 | Toggle wake word |
| S18-05 | QS tile: Lock now | Tile | 1.5 | Instant lock |
| S18-06 | QS tile: Find phone | Tile | 1.5 | Ring |
| S18-07 | Service notification | Notification | 1.0 | Pause 1h / Stop / Open |
| S18-08 | Reminder / routine notifications | Notification | 1.5 | Snooze/Done |
| S18-09 | App shortcuts | System | 1.0 | Long-press actions |
| S18-10 | Assistant invocation UI (default assistant) | System | 2.0 | Half-sheet orb |
| S18-11 | Lock-screen emergency card | System | 2.0 | Read-only |

---

## Screen counts

| Group | Count |
|---|---|
| S0 Shell | 5 |
| S1 Onboarding | 12 |
| S2 Assistant | 15 |
| S3 Lock & Security | 27 |
| S4 Safety | 12 |
| S5 Calls & Messaging | 10 |
| S6 Media | 6 |
| S7 Productivity | 14 |
| S8 Automation | 13 |
| S9 Driving | 6 |
| S10 Accessibility | 6 |
| S11 Device & Tools | 8 |
| S12 Smart home | 5 |
| S13 Wellbeing | 5 |
| S14 Family | 4 |
| S15 Settings | 25 |
| S16 Privacy | 8 |
| S17 Pro & Support | 8 |
| S18 Widgets/Tiles/System | 11 |
| **Total** | **200** |

Design deliverables: low-fi flows for every group → hi-fi for v1.0 set first (≈ 70 screens), component library in Figma with Compose parity.
