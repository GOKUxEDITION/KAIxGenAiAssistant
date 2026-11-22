package com.kaixgen.assistant.domain.billing

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class KaiPlanTier(val displayName: String, val skuId: String, val priceLabelInr: String) {
    FREE("KAI Free", "free", "₹0 Forever"),
    PRO_MONTHLY("KAI Pro Monthly", "kai_pro_monthly", "₹79 / month"),
    PRO_YEARLY("KAI Pro Yearly", "kai_pro_yearly", "₹599 / year"),
    PRO_LIFETIME("KAI Pro Lifetime", "kai_pro_lifetime", "₹1,299 one-time")
}

enum class KaiFeatureGate(val requiresPro: Boolean) {
    // Always FREE (Core, Safety & Privacy — never paywalled per PRD 12)
    CORE_VOICE_COMMANDS(false),
    BASIC_VOICE_LOCK_AND_PIN(false),
    EMERGENCY_SOS_AND_HELPLINES(false),
    FAKE_CALL_AND_MEDICAL_CARD(false),
    DIRECT_WHATSAPP_AND_SMART_MONEY(false),
    FIND_PHONE_VOICE_RING(false),

    // KAI PRO Paid Features
    UNLIMITED_APP_LOCK(true),
    UNLIMITED_ROUTINES(true),
    STRICT_VOICEPRINT_BIOMETRICS(true),
    INTRUDER_SELFIE_VAULT(true),
    DOUBLE_CLAP_AND_ANTI_THEFT(true),
    UNLIMITED_HYBRID_AI_CHAT(true),
    DRIVING_MODE_PRO(true),
    BATTERY_VOICE_ALERTS(true)
}

/**
 * Manages Free vs. Pro feature gates, daily AI quota (20/day Free),
 * and Play Billing SKU readiness per PRD 12.
 */
class KaiSubscriptionManager(context: Context? = null) {

    private val prefs = context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentTier = MutableStateFlow(loadTier())
    val currentTier: StateFlow<KaiPlanTier> = _currentTier.asStateFlow()

    val isPro: Boolean
        get() = _currentTier.value != KaiPlanTier.FREE

    fun canAccess(feature: KaiFeatureGate): Boolean {
        return !feature.requiresPro || isPro
    }

    fun canLockAnotherApp(currentLockedCount: Int): Boolean {
        return isPro || currentLockedCount < MAX_FREE_LOCKED_APPS
    }

    fun canCreateAnotherRoutine(currentRoutineCount: Int): Boolean {
        return isPro || currentRoutineCount < MAX_FREE_ROUTINES
    }

    fun canUseCloudAiToday(usedTodayCount: Int): Boolean {
        return isPro || usedTodayCount < MAX_FREE_AI_REQUESTS_PER_DAY
    }

    fun activateTier(tier: KaiPlanTier) {
        _currentTier.value = tier
        prefs?.edit()?.putString(KEY_TIER, tier.name)?.apply()
    }

    fun resetToFree() {
        activateTier(KaiPlanTier.FREE)
    }

    private fun loadTier(): KaiPlanTier {
        val raw = prefs?.getString(KEY_TIER, KaiPlanTier.FREE.name) ?: KaiPlanTier.FREE.name
        return runCatching { KaiPlanTier.valueOf(raw) }.getOrDefault(KaiPlanTier.FREE)
    }

    companion object {
        private const val PREFS_NAME = "kai_subscription_prefs"
        private const val KEY_TIER = "active_plan_tier"
        const val MAX_FREE_LOCKED_APPS = 3
        const val MAX_FREE_ROUTINES = 2
        const val MAX_FREE_AI_REQUESTS_PER_DAY = 20
    }
}
