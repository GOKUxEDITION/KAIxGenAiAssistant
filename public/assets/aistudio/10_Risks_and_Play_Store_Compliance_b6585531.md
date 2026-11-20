# 10 — Risks and Google Play Compliance

## 1. Risk register

Score = Likelihood (1–5) × Impact (1–5).

| ID | Risk | L | I | Score | Mitigation | Owner |
|---|---|---|---|---|---|---|
| R1 | Play policy rejection (overlay, accessibility, call permissions, mic FGS) | 4 | 5 | 20 | Early policy review, prominent disclosures, in-app demo video, minimal permissions, staged declarations | PM |
| R2 | OEM kills background service → wake word unreliable | 5 | 4 | 20 | OEM playbook, battery guidance, heartbeat + recovery prompts, tap-to-speak fallback | Android |
| R3 | Hindi/Hinglish ASR accuracy below target | 3 | 5 | 15 | Hybrid ASR, custom vocab biasing, user aliases, correction learning (local), eval sets | ML |
| R4 | Speaker verification poor on low-end mics / noise | 4 | 4 | 16 | Threshold calibration, Convenience mode, mandatory PIN fallback, noise meter | ML |
| R5 | Users locked out | 3 | 5 | 15 | Mandatory PIN, device-credential recovery, cooldown max 15 min, clear onboarding test | PM/QA |
| R6 | Battery drain complaints | 4 | 4 | 16 | Duty cycling, small KWS model, battery budget gate, "Pause" actions | ML/Android |
| R7 | Privacy backlash (always-listening perception) | 3 | 5 | 15 | Transparent Privacy Center, on-device default, open privacy whitepaper, no audio storage | PM |
| R8 | LLM safety / hallucinated actions | 3 | 4 | 12 | Schema-bound JSON, allow-list, confirmations, red-team eval | ML |
| R9 | LLM cost overrun | 3 | 3 | 9 | On-device first, caps, caching, remote kill switch | Backend |
| R10 | Wake word licensing cost / terms | 3 | 3 | 9 | Evaluate open-source early; budget for commercial | PM |
| R11 | Android API changes (14/15 mic FGS, overlay, background starts) | 4 | 3 | 12 | Track betas, quarterly compatibility sprints | Android |
| R12 | Trademark / naming conflict ("KAI", "Hey Buddy") | 2 | 4 | 8 | Trademark search (India + global), domain/handle reservation | PM |
| R13 | Scope creep for small team | 4 | 3 | 12 | MVP cut-line, change control, feature flags | PM |
| R14 | Overlay lock bypassed → reputation damage | 3 | 4 | 12 | Honest positioning, hardening, responsible-disclosure program | Security |
| R15 | Migration from Hey Buddy loses user settings | 2 | 4 | 8 | Versioned secure migration + rollback + tests | Android |

## 2. Google Play compliance checklist

### Permissions & sensitive APIs
- [ ] **Microphone / FGS type `microphone`:** declare use; show persistent notification; start from user-visible state; Play Console FGS declaration with demo video.
- [ ] **SYSTEM_ALERT_WINDOW:** core-functionality justification (lock overlay); ensure not used for ads/disruptive content.
- [ ] **READ_CONTACTS / CALL_PHONE / ANSWER_PHONE_CALLS / READ_PHONE_STATE:** if using call-log or SMS permission groups, Play requires being the default handler or qualifying exception — **avoid SMS/Call-log permissions**; use intents (`ACTION_DIAL/CALL`, WhatsApp deep link) instead.
- [ ] **QUERY_ALL_PACKAGES:** avoid; use `<queries>` for launcher intents. If required for "open any app", file the declaration with justification.
- [ ] **PACKAGE_USAGE_STATS:** disclose; used only for App Lock foreground detection.
- [ ] **AccessibilityService (v2):** must be core, user-initiated functionality; prominent in-app disclosure + consent before enabling; Play declaration form; no data harvesting; do not claim it is an "accessibility tool" unless designed for users with disabilities (the Meera persona supports this but verify with policy at submission).
- [ ] **NotificationListener:** disclosure; excluded sensitive apps default.
- [ ] **Camera (intruder selfie):** disclosure; local storage only.

### Data & disclosures
- [ ] Privacy policy URL (EN + HI) in listing and in-app.
- [ ] Data Safety form: audio (processed on-device / optional cloud), app activity, device IDs — consistent with actual behaviour.
- [ ] Prominent disclosure dialog before mic, contacts, notification, accessibility access.
- [ ] Account/data deletion: in-app "Delete all my data".
- [ ] Target API level requirement met each year; 64-bit; AAB.

### Listing & ASO integrity
- [ ] No misleading claims ("unbreakable", "replaces fingerprint"). Say: "Extra privacy layer".
- [ ] Screenshots reflect actual UI (fix typos found in current mockups: "sone" → "done"; "Sunio" vs "Buddy").
- [ ] Impersonation: do not use Google Assistant/Alexa trademarks in title/icon.
- [ ] Content rating questionnaire; ads policy compliance if monetizing with ads.

## 3. Legal / IP

- Trademark clearance for "KAIxGenAssistant" / "KAI".
- Third-party licenses (wake word, models, fonts, Lottie/illustrations); maintain an SBOM and in-app OSS notices.
- Illustration assets and brand imagery: confirm ownership/licence of the current Hey Buddy illustrations (a person/character image used in the onboarding art).
- Voice sample consent forms for any data collected during research/beta.

## 4. Contingency plans

| Scenario | Plan |
|---|---|
| Play rejects overlay or accessibility use | Ship with fallback (notification-based lock shortcut / PIN-only app lock via usage stats); appeal with video evidence |
| Wake word unreliable on OEM X | Show OEM guide; offer persistent notification quick-action/tile |
| LLM provider outage | Auto fall back to fast-path; cached clarifications; feature flag off |
| Security incident | Doc 06 §5 process |
