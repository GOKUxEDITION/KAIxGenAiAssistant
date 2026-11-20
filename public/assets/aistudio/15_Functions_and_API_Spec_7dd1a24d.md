# 15 — Functions, Modules and API Specification

Developer-facing contract for the full product. Kotlin-style signatures are illustrative; final names are set in implementation.

## 1. Module map

```
:app                     shell, DI, navigation
:core-common             result types, dispatchers, logging (PII-safe), feature flags
:core-security           Keystore, PinVault, encryption, integrity
:core-audio              mic capture, ring buffer, VAD, noise suppression, AEC
:engine-wakeword         keyword spotting
:engine-asr              recognizer abstraction (system / on-device / cloud)
:engine-speaker          embeddings, enrollment, verification, liveness
:engine-nlu              normalizer, fast-path matcher, entity resolvers, LLM router
:engine-tts              speech output, voices, earcons
:engine-actions          capability executors (see §3)
:feature-assistant  :feature-lock  :feature-safety  :feature-comms  :feature-media
:feature-productivity  :feature-routines  :feature-driving  :feature-a11y
:feature-devicecontrol  :feature-tools  :feature-smarthome  :feature-wellbeing
:feature-family  :feature-settings  :feature-privacy  :feature-billing
:data-local (Room/DataStore)  :data-remote (gateway/account/sync)
:wear  :widgets  :tiles  :plugin-sdk
```

## 2. Core pipeline functions

```kotlin
interface WakeWordEngine {
  fun start(config: WakeConfig): Flow<WakeEvent>      // emits Detected(conf, ts)
  fun stop(); fun setSensitivity(level: Int)
}
interface SpeechRecognizerEngine {
  suspend fun listen(opts: AsrOptions): Flow<AsrResult>   // partial + final, lang detected
  fun supportedLanguages(): Set<Locale>
}
interface NluEngine {
  suspend fun parse(text: String, ctx: DialogContext): NluResult   // Intent(name,slots,conf,source)
}
interface ActionExecutor {
  val capability: Capability
  fun requiredPermissions(): List<PermissionSpec>
  suspend fun canExecute(intent: Intent): Feasibility   // OK | NeedsPermission | Unsupported | NeedsA11y
  suspend fun execute(intent: Intent, confirmed: Boolean): ActionResult
}
interface SpeakerVerifier {
  suspend fun enroll(samples: List<AudioClip>): EnrollResult      // quality, threshold
  suspend fun verify(clip: AudioClip, mode: SecurityMode): VerifyResult  // score, accept, liveness
}
interface PinVault {
  suspend fun set(pin: CharArray); suspend fun verify(pin: CharArray): PinResult // Ok | Wrong(left) | Cooldown(until)
  suspend fun changeWithDeviceCredential(newPin: CharArray)
}
```

Pipeline orchestrator: `CommandOrchestrator.handle(audioOrText) → CommandOutcome` coordinating wake → ASR → NLU → risk gate → executor → TTS → history.

## 3. Capability executors (engine-actions)

| Capability | Key functions | Permissions / notes |
|---|---|---|
| AppLauncher | `open(appQuery)`, `openDeepLink(app, target)`, `listInstalled()` | `<queries>` launcher intent |
| CallManager | `call(contact/number, sim?)`, `answer()`, `reject()`, `speaker(on)` | CALL_PHONE, ANSWER_PHONE_CALLS |
| MessageComposer | `draft(app, contact, text)`, `send(draft, mode)` | `send` auto = accessibility opt-in only |
| ContactResolver | `resolve(spoken): List<Candidate>`, `aliasFor(contact)` | READ_CONTACTS, local index |
| MediaController | `play(query, app)`, `transport(cmd)`, `setVolume(n)` | MediaSession/deep links |
| SystemSettings | `torch`, `volume`, `brightness`, `dnd`, `openPanel(Wifi/BT/Data/Hotspot/Airplane)` | Panel API on Android 10+ |
| AccessibilityBridge | `globalAction(Back/Home/Recents/Shade/Screenshot/Lock)`, `tapByLabel`, `scroll`, `showNumbers`, `readScreen` | Accessibility service, consent |
| LockController | `lockNow()`, `showOverlay(mode)`, `unlock(method)`, `lockApp(pkg)` | Overlay, UsageStats/A11y |
| FindPhoneController | `ring(durationSec)`, `stop()`, `strobe()`, `saveLastLocation()` | Alarm stream, optional location |
| SosController | `trigger(profile)`, `cancel(pin)`, `shareLocationLink(contacts)` | Location, share intents |
| ReminderManager | `create(spec)`, `update/delete`, `snooze` | AlarmManager exact alarms |
| CalendarManager | `list(range)`, `create(event)`, `move(id,newTime)` | CalendarContract |
| NotesManager / ListsManager | `add`, `search`, `check(item)` | local DB |
| NotificationReader | `readUnread(apps)`, `reply(notifId, text)` | NotificationListener |
| CameraController | `capture()`, `record(start/stop)`, `ocr()`, `describe()` | CAMERA |
| VaultManager | `import`, `open`, `export`, `wipe` | AES-GCM, Keystore |
| RoutineEngine | `run(id)`, `schedule(trigger)`, `dryRun(id)` | WorkManager, geofencing |
| SmartHomeBridge | `listDevices()`, `setState(device, state)`, `runScene()` | Matter/Home/HA |
| TranslationService | `translate(text/audio, from, to)` | on-device or gateway |
| KnowledgeService | `answer(question, ctx)`, `summarize(text)`, `rewrite(text, tone)` | LLM router |
| WellbeingService | `usageReport(range)`, `startFocus(spec)` | UsageStats |
| BillingService | `entitlements()`, `purchase(sku)`, `restore()` | Play Billing |

## 4. Intent schema (NLU ⇄ LLM contract)

```json
{
  "schema_version": "1.0",
  "intent": "SEND_MESSAGE",
  "confidence": 0.93,
  "source": "fast|llm",
  "risk": "HIGH",
  "slots": {
    "app": "whatsapp",
    "recipient": {"spoken": "Rahul", "resolved_id": null},
    "text": "Main 10 minute mein pahunch raha hoon",
    "language": "hi-Latn"
  },
  "needs_confirmation": true,
  "followup_question": null
}
```
Rules: the LLM may only return intents from the allow-list; unknown → `CLARIFY`; slot values validated by type; any URL/phone number slot validated; no executable code; max 1 action per response unless `PLAN` intent (list of ≤ 5 steps, each confirmed according to risk).

**Intent list:** see doc 11 + `PLAN`, `CLARIFY`, `ANSWER`, `TRANSLATE`, `SUMMARIZE`, `REWRITE`, `REMEMBER`, `RECALL`, `START_ROUTINE`, `CREATE_ROUTINE`, `SOS`, `LOCK_APP`, `GUEST_MODE`, `KIDS_MODE`, `SMARTHOME_SET`, `NAVIGATE`, `ADD_EXPENSE`, `LOG_HABIT`, `START_FOCUS`, `SCAN_QR`, `CAMERA_CAPTURE`, `READ_SCREEN`, `DESCRIBE_SCENE`.

## 5. Risk & confirmation policy

| Risk | Examples | Policy |
|---|---|---|
| LOW | open app, torch, volume, time | Execute immediately |
| MED | call, play media, toggles, add reminder | Execute if conf ≥ 0.85, else confirm |
| HIGH | send message, delete, call unknown number, change lock/security settings, share location, SOS cancel, smart-home door/lock | Always confirm; Strict mode also requires speaker match |
| CRITICAL | uninstall app, factory/data wipe, payments | Not voice-executable; show deep link only |

## 6. Public Intents API (other apps/automation → KAI) (v2.0)

Android broadcast / deep link, signature-permission protected + per-caller user approval:

- `kai://command?text=<url-encoded>`
- `com.kai.action.RUN_ROUTINE` extras: `routine_id`
- `com.kai.action.LOCK_NOW`
- `com.kai.action.SPEAK` extras: `text`, `lang`
Replies via `PendingIntent` result. Unapproved callers are ignored and logged.

## 7. Backend REST API (only for opt-in cloud features)

Base: `https://api.<domain>/v1` · TLS 1.2+ · auth: `Authorization: Bearer <short-lived token>` obtained via Play Integrity attestation (anonymous) or user login.

| Method & path | Purpose | Notes |
|---|---|---|
| POST `/auth/attest` | Exchange Play Integrity token → session token | Anonymous install ID |
| POST `/auth/login` | Google/phone OTP login | Optional account |
| POST `/nlu/parse` | Transcript → intent JSON | Body: `text`, `lang`, `schema_version`; no logging of content |
| POST `/ai/answer` | Q&A with optional grounding | Streaming (SSE); rate-limited |
| POST `/ai/summarize` `/ai/rewrite` `/ai/translate` | Text tools | |
| POST `/ai/vision` | Image + question | Image deleted after response |
| GET `/config` | Remote config, flags, model manifest, OEM playbook | Cached, signed |
| GET `/models/{lang}` | Language-pack manifest + CDN URL + SHA-256 | |
| POST `/backup` / GET `/backup/latest` | Encrypted blob (client-side keys) | Optional |
| POST `/devices/register` | FCM token for remote find | v3.0 |
| POST `/devices/{id}/ring` `/lock` | Remote actions | Account required |
| POST `/family/*` | Circles, invites, shared lists | v3.0 |
| GET `/entitlements` / POST `/billing/verify` | Subscription state | Server-verified |
| POST `/feedback` | Thumbs/text (user-submitted only) | |
| POST `/telemetry/batch` | Aggregated anonymous events | Opt-out respected |

Standard errors: `400 invalid_request`, `401 unauthenticated`, `403 integrity_failed`, `429 rate_limited` (with `retry_after`), `503 degraded` (client falls back to on-device).

## 8. Additional local data entities (extends doc 05 §5)

```
Profile(id, name, avatar, voiceprintRef, themeId)
VoicePrint(profileId, embedding, threshold, v)
LockRule(id, scope[APP|DEVICE], target, mode, schedule?, geofence?, grace)
IntruderEvent(id, ts, method, photoRef, approxLoc?)
VaultItem(id, type, nameEnc, blobRef, createdAt)
Note(id, titleEnc, bodyEnc, locked, updatedAt)
Reminder(id, text, trigger, repeat, state)
ListItem(listId, text, checked)
Expense(id, amountMinor, currency, category, ts, noteEnc)
Habit(id, name, schedule, streak)
Routine(id, name, enabled, triggerJson, conditionsJson, actionsJson, version)
RoutineRun(id, routineId, ts, status, logJson)
Memory(id, keyEnc, valueEnc, source, createdAt)
TrustedContact(id, contactRef, role[SOS|ALERT|FAMILY], verified)
Device(id, name, type, bluetoothAddrHash)
SmartDevice(id, provider, name, room, capabilities)
Entitlement(sku, state, expiresAt)
```
All sensitive columns encrypted at rest; exportable and deletable via Privacy Center.

## 9. Events & feature flags

- Internal event bus (`SharedFlow`): `WakeDetected`, `CommandStarted/Completed`, `LockStateChanged`, `PermissionChanged`, `RoutineTriggered`, `ServiceStateChanged`.
- Flags (remote-config): `llm_enabled`, `cloud_asr_enabled`, `sv_strict_enabled`, `a11y_actions_enabled`, `sos_enabled`, `plugin_store_enabled`, per-module kill switches, per-OEM overrides, percentage rollouts.

## 10. Plugin / Skill SDK (v3.0)

- Manifest: `id`, `version`, `intents[]`, `permissions[]`, `data_use`, `network_domains[]`, `signature`.
- Sandbox: runs out-of-process, no direct access to mic/contacts/accessibility; receives only slots for its declared intents; output = `ActionResult` + optional UI card.
- Review: static analysis, permission diff, human review, revocation list, user consent per plugin.

## 11. Non-functional hooks

Every executor logs `{intent, risk, latency, outcome}` only (no PII). Timeouts: ASR 8 s, NLU fast 100 ms, LLM 4 s (fallback to fast result/clarify), action 3 s default. Idempotency keys for message/reminder creation; retries with exponential backoff for backend calls.
