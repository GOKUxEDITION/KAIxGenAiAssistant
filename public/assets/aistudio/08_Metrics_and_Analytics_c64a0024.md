# 08 — Metrics and Analytics

## 1. North-star and KPI tree

**North star:** Weekly Successful Voice Actions per Active User (WSVA).

| Category | KPI | Target (6 mo post-launch) |
|---|---|---|
| Acquisition | Install → onboarding start | ≥ 85% |
| Activation | Onboarding completion (all required permissions) | ≥ 60% |
| Activation | First successful command within 24 h | ≥ 70% of activated |
| Engagement | Commands / DAU | ≥ 4 |
| Engagement | Wake-word enabled users | ≥ 50% of MAU |
| Quality | Command success rate (top-20 intents) | ≥ 92% |
| Quality | Wake word false activations / 10 h | ≤ 1 |
| Quality | Voice Lock unlock success (1st try) | ≥ 90% |
| Quality | Voice Lock FAR / FRR | ≤ 1% / ≤ 5% |
| Retention | D1 / D7 / D30 | 45% / 30% / 25% |
| Stability | Crash-free sessions / ANR rate | ≥ 99.5% / < 0.3% |
| Perf | Fast-path latency p50 / p90 | ≤ 1.2 s / ≤ 2.0 s |
| Battery | Listening drain | ≤ 2%/h |
| Satisfaction | Play rating / NPS | ≥ 4.4 / ≥ 40 |
| Business | Free → paid conversion (if freemium) | 2–4% |

## 2. Event taxonomy (privacy-safe)

Never log transcripts, phrases, contact names, app names typed by user, or audio. Use enumerated values only.

| Event | Properties |
|---|---|
| `onboarding_step_view / complete` | step, language |
| `permission_result` | permission, granted, source |
| `wake_detected` | engine_conf_bucket, duty_mode |
| `command_executed` | intent, source[fast/llm], confidence_bucket, latency_ms_bucket, success, error_code |
| `command_cancelled` | intent, stage |
| `clarification_shown` | reason |
| `lock_triggered` | trigger[voice/manual], mode |
| `unlock_attempt` | method[voice/pin], success, fail_reason, noise_bucket |
| `lockout_started` | level |
| `app_lock_launch_blocked` | grace_mode |
| `find_phone_used` | trigger[voice/clap/manual], duration_bucket |
| `routine_run` | template_id/custom, success |
| `llm_request` | schema_version, latency_bucket, outcome |
| `service_state` | running/paused/killed_reason |
| `paywall_view / purchase` | plan, source |
| `privacy_toggle_changed` | toggle, value |

Identifiers: random install ID (resettable); no advertising ID by default.

## 3. Dashboards

1. **Activation funnel** (step drop-offs by OEM/language).
2. **Command quality** (success by intent × language × source).
3. **Lock health** (FAR/FRR proxies, lockouts, fallbacks to PIN).
4. **Service reliability** (uptime heartbeat, kill reasons by OEM).
5. **Retention cohorts** by acquisition channel/feature adoption.
6. **Cost** (LLM requests/user, p90 spend).

## 4. Experiments (A/B via Remote Config)

| Hypothesis | Variant | Metric |
|---|---|---|
| Showing "what I heard" boosts phrase enrollment success | with/without preview | enrollment completion |
| Wake-word sensitivity presets reduce false activations | Low/Med/High default | false act./10h, wake recall |
| Onboarding order (PIN before voice) reduces drop-off | order A/B | completion |
| Rotating suggestions increase first-command rate | static vs rotating | first command in 24 h |
| Confirmation timer 3 s vs 5 s on messages | 3 / 5 | error-send rate, completion |

## 5. Quality evaluation sets (offline)

- **Command test set:** ≥ 2,000 utterances per language (EN, HI, Hinglish), mixed gender/age/accents/noise levels; track WER, intent accuracy, slot F1.
- **Speaker test set:** ≥ 100 speakers, genuine + impostor + replay; compute EER, FAR@FRR=5%.
- **Wake-word set:** 20 h negatives (TV, conversation, traffic) + 2,000 positives.
- Regression gate: no metric may drop > 1 pt vs previous release.

## 6. Feedback loops

In-app "Was this right? 👍/👎" after commands (stores only intent + thumbs, optional text submitted by user); Play review monitoring; support tags; monthly voice-of-customer review.
