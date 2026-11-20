# 11 — Voice Command Catalog

Wake prefix is optional when tapping the orb. Patterns are examples; the NLU should generalize (word order, fillers, code-switching).

Risk: L = low, M = medium, H = high (confirmation required).

## 1. Apps & navigation (MVP)

| Intent | English | Hindi / Hinglish | Slots | Risk |
|---|---|---|---|---|
| OPEN_APP | "Open WhatsApp" | "WhatsApp kholo", "व्हाट्सऐप खोलो" | app | L |
| CLOSE / HOME | "Go home" | "Home pe jao" | — | L |
| BACK (v2) | "Go back" | "Peeche jao" | — | L |
| OPEN_SETTINGS | "Open Wi-Fi settings" | "Wi-Fi settings kholo" | page | L |
| SEARCH_WEB | "Search for biryani near me" | "Biryani near me search karo" | query | L |

## 2. Calls (MVP)

| Intent | English | Hindi / Hinglish | Slots | Risk |
|---|---|---|---|---|
| CALL_CONTACT | "Call Rahul" | "Rahul ko call karo", "Mummy ko phone lagao" | contact, sim? | M |
| CALL_NUMBER | "Call 98765 43210" | "Is number pe call karo" | number | H |
| ANSWER_CALL | "Answer" | "Call utha lo", "Haan utha lo" | — | L |
| REJECT_CALL | "Reject" | "Call kaat do" | — | L |
| SPEAKER_ON | "Put it on speaker" | "Speaker on karo" | — | L |
| REDIAL | "Call him again" | "Usko dobara call karo" | — | M |

## 3. Media (MVP)

| Intent | English | Hindi / Hinglish | Slots | Risk |
|---|---|---|---|---|
| PLAY_MEDIA | "Play Arijit Singh songs" | "Arijit ke gaane bajao" | query, app | L |
| PAUSE/RESUME/NEXT | "Next song" | "Agla gaana", "Ruk jao" | — | L |
| VOLUME | "Volume 60 percent" | "Awaaz kam karo" | level | L |

## 4. Security (MVP)

| Intent | English | Hindi / Hinglish | Notes | Risk |
|---|---|---|---|---|
| LOCK_PHONE | *user-defined lock phrase* | *user-defined* | Works in any supported language | M |
| UNLOCK | *user-defined unlock phrase* | — | Speaker-verified | H |
| LOCK_APP | "Lock Instagram" | "Instagram lock karo" | Requires App Lock on | M |
| FIND_PHONE | "Where are you?" | "Phone kahan hai?" | Rings max volume | L |
| STOP_RING | "Stop" | "Bas, ruk jao" | — | L |

## 5. Device controls (v1.5)

| Intent | English | Hindi / Hinglish | Risk |
|---|---|---|---|
| TORCH | "Turn on flashlight" | "Torch chalu karo" | L |
| WIFI/BLUETOOTH | "Turn off Bluetooth" | "Bluetooth band karo" | L |
| DND | "Do not disturb on" | "Disturb mat karo" | L |
| BRIGHTNESS | "Brightness 80" | "Brightness badhao" | L |
| SCREENSHOT (v2) | "Take screenshot" | "Screenshot lo" | L |
| BATTERY_STATUS | "How much battery?" | "Kitni battery hai?" | L |

## 6. Productivity (v1.5)

| Intent | English | Hindi / Hinglish | Risk |
|---|---|---|---|
| SET_ALARM | "Wake me at 6:30" | "Subah 6:30 ka alarm lagao" | L |
| SET_TIMER | "Timer 10 minutes" | "10 minute ka timer" | L |
| REMINDER | "Remind me to pay bill at 5" | "5 baje bill bharne ka yaad dilana" | L |
| TIME/DATE | "What's the time?" | "Kitne baje hain?" | L |
| WEATHER (cloud) | "Weather today" | "Aaj mausam kaisa hai?" | L |
| CALENDAR_ADD (v2) | "Add meeting tomorrow 4 pm" | "Kal 4 baje meeting add karo" | M |

## 7. Messaging (v1.5)

| Intent | English | Hindi / Hinglish | Risk |
|---|---|---|---|
| SEND_MESSAGE | "Message Rahul I'm late" | "Rahul ko message karo main late hoon" | H (read-back + confirm) |
| READ_NOTIFS | "Read my notifications" | "Notifications padh do" | M |
| REPLY | "Reply yes" | "Haan reply kar do" | H |

## 8. Routines (v1.5)

| Phrase | Actions |
|---|---|
| "Good morning" | Time + weather + news app |
| "Driving mode" | DND, Maps, auto-answer, notification read-out |
| "Sleep mode" | DND, brightness low, alarm check, lock |
| "Workout" | Music playlist, DND, timer |

## 9. Conversation & meta

| Intent | Examples |
|---|---|
| CANCEL | "Cancel", "Rehne do" |
| CONFIRM | "Yes / Haan / Send / Kar do" |
| REPEAT | "Say that again", "Phir se bolo" |
| HELP | "What can you do?", "Tum kya kar sakte ho?" |
| CHANGE_LANGUAGE | "Speak Hindi", "English mein bolo" |
| MUTE_TTS | "Be quiet", "Chup raho" |

## 10. Disambiguation & fallback copy

- Multiple matches: "Kaun sa — A ya B?"
- Low confidence: "Maine suna: '<heard>'. Sahi hai?"
- Not understood: "Samajh nahi aaya. Aap aise bol sakte hain: 'WhatsApp kholo'."
- Unsupported: "Yeh abhi nahi kar sakta, lekin jaldi aayega."

## 11. NLU quality bars

- ≥ 95% intent accuracy on MVP intents (quiet); ≥ 85% in noise.
- Slot F1 ≥ 0.92 for app names; ≥ 0.90 for contacts.
- Hinglish code-switch test set ≥ 500 utterances per intent group.
