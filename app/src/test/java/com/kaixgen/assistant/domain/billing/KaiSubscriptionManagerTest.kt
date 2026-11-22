package com.kaixgen.assistant.domain.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KaiSubscriptionManagerTest {

    @Test
    fun freeTier_allowsCoreAndSafety_andEnforcesQuotas() {
        val manager = KaiSubscriptionManager(context = null)
        assertFalse(manager.isPro)
        assertTrue(manager.canAccess(KaiFeatureGate.EMERGENCY_SOS_AND_HELPLINES))
        assertTrue(manager.canAccess(KaiFeatureGate.BASIC_VOICE_LOCK_AND_PIN))
        assertFalse(manager.canAccess(KaiFeatureGate.INTRUDER_SELFIE_VAULT))

        assertTrue(manager.canLockAnotherApp(2))
        assertFalse(manager.canLockAnotherApp(3))
        assertTrue(manager.canCreateAnotherRoutine(1))
        assertFalse(manager.canCreateAnotherRoutine(2))
        assertTrue(manager.canUseCloudAiToday(19))
        assertFalse(manager.canUseCloudAiToday(20))
    }

    @Test
    fun proTier_unlocksAllGatedFeaturesAndUnlimitedQuotas() {
        val manager = KaiSubscriptionManager(context = null)
        manager.activateTier(KaiPlanTier.PRO_YEARLY)
        assertTrue(manager.isPro)
        assertTrue(manager.canAccess(KaiFeatureGate.INTRUDER_SELFIE_VAULT))
        assertTrue(manager.canAccess(KaiFeatureGate.DOUBLE_CLAP_AND_ANTI_THEFT))
        assertTrue(manager.canLockAnotherApp(50))
        assertTrue(manager.canCreateAnotherRoutine(25))
        assertTrue(manager.canUseCloudAiToday(500))
    }
}
