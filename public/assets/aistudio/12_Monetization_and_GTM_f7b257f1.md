# 12 — Monetization and Go-To-Market

## 1. Monetization options

| Model | Pros | Cons | Fit |
|---|---|---|---|
| Free + ads | Wide adoption | Ads + security product = trust conflict; overlay-policy risk | **Avoid** on lock screens/overlays; at most small banner on non-sensitive screens |
| **Freemium subscription/one-time** | Aligned with privacy positioning | Lower conversion | **Recommended** |
| One-time purchase | Simple, India-friendly | No recurring revenue to fund cloud AI | Good for "Pro Lock" |
| B2B / OEM licensing | Scale | Long sales cycles | Later |

### Proposed tiers (to be validated via pricing research; amounts are placeholders)

| Feature | Free | Pro (e.g. ₹49–99/mo or ₹499–999/yr; lifetime option) |
|---|---|---|
| Core commands (open, call, answer, play) | ✅ | ✅ |
| Wake word | ✅ | ✅ |
| Voice Lock (phrase) + PIN | ✅ | ✅ |
| Speaker verification | 1 profile | Multiple profiles, Strict mode |
| App Lock | up to 3 apps | Unlimited |
| Find Phone | ✅ | + clap, last-seen location |
| GenAI requests | 20/day | Higher/unlimited (fair use) |
| Routines | 2 | Unlimited |
| Messaging, notification read-out | Limited | Full |
| Intruder alert, themes, widgets | — | ✅ |

Principles: safety features (PIN, recovery, basic lock) are never paywalled; no dark patterns; restore-purchase and clear cancellation; UPI/Play Billing support.

## 2. Unit economics (to model)

- Cloud LLM cost per request × requests/user/day × paying/free mix. Mitigate via on-device NLU first; target < ₹X per MAU per month (set after provider quotes).
- Break-even conversion = cloud cost per MAU ÷ ARPPU.

## 3. Go-to-market

### 3.1 Positioning & messaging
Tagline options: *"Bolo. Ho jayega."* · *"Your phone, hands-free."* · *"Your voice. Your access."* (already used in current art).
Pillars: Hinglish-native · Private by default · Voice lock for your apps · Works offline.

### 3.2 Launch phases
1. **Existing Hey Buddy users:** in-app migration + "What's new" story; invite to beta.
2. **Closed beta (≈ 500):** Telegram/WhatsApp community, feedback loop, OEM coverage.
3. **Soft launch (India)**, staged rollout; collect reviews.
4. **Public launch:** PR to Indian tech media, YouTube/Instagram creators (productivity, android tips, accessibility), Product Hunt India, Reddit r/Android & r/developersIndia.

### 3.3 ASO (Play Store)

- **Title (≤ 30):** KAIxGenAssistant – Voice Lock
- **Short description (≤ 80):** Hands-free voice assistant in Hindi & English. Open apps, call, lock by voice.
- **Keywords:** voice assistant, voice lock, app lock, voice commands, Hindi voice assistant, hands free, find my phone.
- **Screenshots:** 1) "Bolo. Ho jayega." orb, 2) Voice Lock, 3) Hinglish commands, 4) Privacy-first, 5) Find Phone, 6) Routines.
- Localized listings: EN, HI + regional.
- Promo video 30 s; feature graphic; rating prompt after 3 successful commands (not after errors).

### 3.4 Growth loops
- Share "voice lock" setup as a fun challenge; referral (1 month Pro for 3 invites).
- Community-contributed alias packs/regional commands.
- Creator partnerships; accessibility NGOs outreach.

### 3.5 Support
In-app help center (EN/HI), FAQ for OEM battery steps, WhatsApp/email support, SLA 48 h.

## 4. Success criteria (first 6 months)

100k installs · 25% D30 retention · 4.4★ · < 1% crash/ANR · ≥ 2% paid conversion · positive gross margin on cloud AI.
