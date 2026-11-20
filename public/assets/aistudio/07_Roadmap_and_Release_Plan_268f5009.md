# 07 — Roadmap and Release Plan

Timeline assumes a team of ~5 and starts after PRD sign-off. Adjust dates to actual kickoff.

## 1. Team (suggested)

| Role | Count |
|---|---|
| Product Manager / Owner | 1 |
| Android engineers (Kotlin/Compose) | 2 |
| ML/Speech engineer (wake word, SV, NLU) | 1 |
| Backend (part-time, LLM gateway) | 0.5 |
| UX/UI designer | 1 |
| QA (manual + automation) | 1 |

## 2. Phases

### Phase 0 — Discovery & Design (Weeks 1–3)
- Finalize PRD, decisions on wake-word engine and LLM path
- Technical spikes: wake word accuracy (Hindi accent), speaker verification on-device, battery profile
- UX: design system, high-fidelity screens, prototype test with 8–10 users
- **Exit:** signed-off PRD, design system, spike report

### Phase 1 — MVP "KAI Core" (Weeks 4–12)
- Rebrand + migration from Hey Buddy (preserve existing phrase/PIN via secure migration)
- Wake word + Listening service + OEM guides
- Fast-path NLU (open app/call/answer/play/lock/find phone) in EN/HI/Hinglish
- Voice Lock v2 (phrase + voiceprint, PIN hardening, recovery)
- App Lock, Find Phone v2
- Privacy Center, history, settings
- **Exit:** internal alpha → closed beta (≈ 500 users)

### Phase 2 — "KAI Smart" v1.5 (Weeks 13–20)
- Generative layer (opt-in), LLM gateway
- Messaging by voice, system toggles
- Routines + templates, reminders/alarms/timers
- Notification read-out (Driving Mode)
- Regional language packs (TE, TA, KN, ML, MR, OR, BN)
- **Exit:** open beta → staged production rollout 5% → 20% → 50% → 100%

### Phase 3 — "KAI Pro" v2.0 (Weeks 21–30)
- Context memory, accessibility actions, intruder alert, family profiles
- Widgets, Quick Settings tile
- Monetization features (see doc 12)

## 3. Milestones

| Milestone | Target | Gate |
|---|---|---|
| M0 PRD sign-off | End Wk 1–2 | Stakeholder approval |
| M1 Design freeze | Wk 3 | Usability test ≥ 80% task success |
| M2 Alpha (internal) | Wk 8 | Core intents work; crash-free ≥ 98% |
| M3 Closed beta | Wk 12 | Release gate checklist (doc 06 §6, doc 09 §8) |
| M4 v1.0 public | Wk 14 | Play review approved incl. permission declarations |
| M5 v1.5 | Wk 20 | LLM safety eval passed |
| M6 v2.0 | Wk 30 | Accessibility policy approval |

### Phase 4 — "KAI Platform" v3.0 (Weeks 31+)
- Smart home, Wear OS, KAI Keyboard, web dashboard & remote find, plugin/skills store, family circle, Android Auto
- Full module-by-module sequencing: `13_Advanced_Feature_Catalog.md` §22. Realistic note: the full catalog is multi-year work; v1.0 quality gates every later phase.

## 4. MVP cut-line (if time is short)

Keep: wake word, 8 core intents, Voice Lock with PIN hardening + speaker verification (Balanced), Find Phone, Privacy Center.
Defer: messaging, LLM, routines, regional packs beyond HI/EN/Hinglish, clap detection.

## 5. Release process

- Branching: trunk-based with short-lived feature branches; release branches `release/x.y`.
- CI: lint, unit tests, instrumented tests on Firebase Test Lab (device matrix), size check, dependency scan.
- Distribution: internal → closed (Play) → open → staged production; remote-config kill switches.
- Versioning: SemVer; `versionCode` automated.
- Release notes bilingual (EN/HI).

## 6. Dependencies & critical path

Wake-word engine decision → Listening service → NLU fast path → Voice Lock SV tuning → Play permission declarations (start submitting drafts at Wk 8 — lead time is the main schedule risk).

## 7. Definition of Done (feature)

Code reviewed · unit + UI tests · accessibility check · localization strings complete · analytics events added · privacy/Data Safety impact assessed · docs updated · QA sign-off · feature flag configured.
