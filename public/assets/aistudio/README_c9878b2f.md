# KAIxGenAssistant — Product Requirements Document (PRD) Pack

| Field | Value |
|---|---|
| Product | KAIxGenAssistant (successor to "Hey Buddy") |
| Platform | Android (min SDK 26 / Android 8.0, target latest stable) |
| Document version | 1.0 (Draft for review) |
| Date | 2026-11-20 |
| Product Owner | [Your name] |
| Status | Draft — ready for stakeholder review |

## Contents

| # | File | Purpose |
|---|---|---|
| 00 | README.md | This index, glossary, how to use the pack |
| 01 | 01_Vision_and_Strategy.md | Problem, vision, market, positioning, goals |
| 02 | 02_PRD_Core.md | Personas, scope, functional + non-functional requirements, priorities |
| 03 | 03_Feature_Specifications.md | Detailed spec per module with acceptance criteria |
| 04 | 04_UX_Flows_and_Screens.md | Navigation, screen list, flows, audit of current Hey Buddy UI |
| 05 | 05_Technical_Architecture.md | Architecture, tech stack, data model, permissions |
| 06 | 06_Security_and_Privacy.md | Threat model, controls, data handling, compliance |
| 07 | 07_Roadmap_and_Release_Plan.md | Phases, milestones, MVP cut, team plan |
| 08 | 08_Metrics_and_Analytics.md | KPIs, event taxonomy, experiments |
| 09 | 09_QA_and_Test_Plan.md | Test strategy, device matrix, test cases |
| 10 | 10_Risks_and_Play_Store_Compliance.md | Risk register, Play policy checklist |
| 11 | 11_Voice_Command_Catalog.md | Full intent/command catalog (EN / Hindi / Hinglish) |
| 12 | 12_Monetization_and_GTM.md | Pricing, launch plan, ASO |
| 13 | 13_Advanced_Feature_Catalog.md | **All-in-one vision:** 20 modules, 206 features with phase, Play/Android feasibility and Must/Should/Nice priority |
| 14 | 14_Complete_Screen_Inventory.md | **All 200 screens/sheets/overlays/widgets** with phase and components |
| 15 | 15_Functions_and_API_Spec.md | Module map, function contracts, intent schema, REST API, data entities, plugin SDK |

## Baseline: what Hey Buddy does today (from current app screens)

- **Voice Lock tab:** custom voice phrase (e.g. "Hare Krishna"), 4-digit backup passcode, "App Lock with Voice Phrase", master toggle.
- **Buddy tab:** "Tap to speak" orb; example command "Buddy, Open WhatsApp"; open apps, call, answer calls, play songs.
- **Find Phone tab:** locate phone by voice.
- Local-only privacy model: phrases/passcode never uploaded; passcode stored hashed.
- Supported lock-phrase languages: Hindi, English, Telugu, Tamil, Kannada, Malayalam, Marathi, Odia, Bangla.

KAIxGenAssistant keeps all of this and upgrades it with generative-AI understanding, speaker verification, an always-on wake word, messaging, routines and a stronger security model.

## Assumptions (please confirm)

1. "KAI" is the assistant's persona name; "Gen" signals generative AI. Wake words proposed: "Hey KAI" and legacy "Hey Buddy".
2. Android-only at launch; iOS is out of scope for v1.
3. Privacy-first: on-device by default; cloud AI only with explicit opt-in.
4. Team is small (3–6 people); the roadmap is sized for that.
5. India is the primary market (Hindi + regional languages + Hinglish are first-class).

## Glossary

- **Wake word** — phrase that activates listening ("Hey KAI").
- **Intent** — structured meaning of a command (e.g. `OPEN_APP{app=WhatsApp}`).
- **Speaker verification (SV)** — checks *who* is speaking, not just *what* is said.
- **ASR** — automatic speech recognition (speech → text).
- **NLU** — natural-language understanding (text → intent).
- **TTS** — text-to-speech.
- **LLM** — large language model.
- **FGS** — Android foreground service.
