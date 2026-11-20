# 06 — Security and Privacy

## 1. Positioning (important)

Voice Lock is an **additional privacy layer**, not a replacement for the Android device lock. It runs as an overlay and cannot defend against a determined attacker with physical and ADB/safe-mode access. Marketing and in-app copy must state this honestly ("extra protection for your apps and screen").

## 2. Threat model

| # | Threat | Likelihood | Mitigation |
|---|---|---|---|
| T1 | Someone overhears the unlock phrase and repeats it | High | Speaker verification (Balanced/Strict); random challenge word in Strict |
| T2 | Replay attack using recorded voice | Medium | Challenge-response word; basic liveness heuristics (spectral/ replay artifacts); rate limit |
| T3 | Brute-force of backup PIN | Medium | Exponential cooldown, attempt counter in Keystore-protected storage, 6-digit option |
| T4 | Overlay dismissal / bypass via safe mode, kill process, ADB | Medium | Re-arm on restart; document limitation; recommend OS lock; watchdog service |
| T5 | Malware reading app data | Low–Med | Keystore-backed encryption, no exported components, no backups of sensitive data (`allowBackup=false`) |
| T6 | Mic eavesdropping by the app itself (trust) | — | No audio persisted; open privacy policy; mic indicator respected; optional audit log of listening sessions |
| T7 | LLM prompt injection via spoken/overheard text | Medium | Schema-constrained output; allow-listed actions; confirmation for high-risk intents; no tool access beyond intent set |
| T8 | Accidental activation (TV/other people) | Medium | Wake-word threshold, speaker-gated sensitive actions (send message, call, lock settings) |
| T9 | Data leak via logs/analytics | Medium | No transcripts or contact names in logs; PII scrubber; analytics allow-list |
| T10 | Reverse-engineering / tampering | Medium | R8 obfuscation, Play Integrity, signature checks on model packs |

## 3. Security controls

### 3.1 Credentials
- PIN: PBKDF2-HMAC-SHA256 ≥ 600,000 iterations (or Argon2id), random 16-byte salt, pepper wrapped with an Android Keystore key (hardware-backed / StrongBox when available). Constant-time compare.
- Because a 4-digit PIN is trivially enumerable offline, the pepper must be Keystore-bound (non-exportable) and 6-digit PIN is recommended in Strict mode.
- Failed-attempt state stored in encrypted DataStore; reset only after success.
- PIN change requires old PIN or device credential.

### 3.2 Voice data
- Lock/unlock phrase: stored as normalized text + phonetic key, encrypted; **no raw enrollment audio retained** after embedding extraction.
- Voiceprint: embedding vector only (not reversible to audio), AES-GCM encrypted with Keystore key (non-exportable), excluded from backup/transfer.
- Re-enrollment invalidates old voiceprint.

### 3.3 App hardening
- `android:allowBackup="false"`, `dataExtractionRules` excluding sensitive stores.
- All components `exported=false` unless required; intent validation.
- `FLAG_SECURE` on lock overlay and PIN screens.
- Certificate pinning for gateway; TLS 1.2+.
- Play Integrity API for LLM gateway access.
- Root/jailbreak signal → reduces Strict-mode trust and warns user (does not block).
- Dependency scanning (OWASP Dependency-Check), SAST (CodeQL/Detekt), periodic third-party pen test before v1.0 GA.

### 3.4 Safe automation
- Risk levels: LOW (open app), MED (call, play), HIGH (send message, delete, change lock settings, toggle security features).
- HIGH requires on-screen/voice confirmation; "Strict" requires voiceprint match too.
- LLM never executes anything directly; it only proposes an intent that goes through the same validator.

## 4. Privacy

### 4.1 Data inventory

| Data | Where | Leaves device? | Retention |
|---|---|---|---|
| Raw audio | RAM buffer only | No (unless user opts-in to cloud ASR per feature) | Discarded after processing |
| Transcript | Local DB (history) | Only transcript text to LLM if opted-in | User-controlled, default 30 days |
| Lock phrase / PIN / voiceprint | Encrypted local | **Never** | Until user deletes |
| Contacts | Read on demand, local index | Never | Index rebuilt, deletable |
| Analytics | Anonymous events | Yes (aggregated) | 14 months |
| Crash reports | Crashlytics | Yes | 90 days |

### 4.2 User controls
- Privacy Center: per-feature toggles (cloud AI, analytics, crash reports, history).
- One-tap "Delete all my data".
- Clear, in-product consent screens — separate from ToS; withdrawal as easy as granting.
- Children: not directed at under-13s; no ads personalization if age unknown.

### 4.3 Compliance
- **India DPDP Act 2023:** notice in English + Hindi (+ Schedule 8 languages as feasible), purpose limitation, consent management, grievance officer contact, erasure on request, breach notification process.
- **Google Play User Data policy**, prominent disclosure for sensitive permissions, accurate **Data Safety** form.
- **GDPR/CCPA readiness** (if distributed globally): data minimization, DSAR flow.
- Biometric-like data: voiceprint treated as sensitive personal data — explicit separate consent, on-device only.

## 5. Incident response

Severity levels S0–S3, 24 h triage for S0/S1, kill-switch via remote config for cloud AI, coordinated disclosure email (security@<domain>), post-mortem within 7 days.

## 6. Security acceptance criteria (release gate)

- [ ] No audio/PIN/voiceprint in logs, backups or network traces (verified via proxy capture)
- [ ] PIN brute force limited per spec
- [ ] FAR ≤ 1% / FRR ≤ 5% on internal speaker test set (see doc 09)
- [ ] Pen-test critical/high findings closed
- [ ] Data Safety form matches observed behaviour
