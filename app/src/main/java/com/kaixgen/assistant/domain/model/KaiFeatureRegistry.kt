package com.kaixgen.assistant.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.GppGood
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.kaixgen.assistant.navigation.KaiRoutes
import com.kaixgen.assistant.navigation.KaiRoutesV2

enum class KaiFeatureCategory(val label: String) {
    AI("AI Assistant"),
    SECURITY("Security"),
    SAFETY("Safety"),
    UTILITIES("Utilities"),
    ACCOUNT("Settings")
}

data class KaiFeatureItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: KaiFeatureCategory,
    val icon: ImageVector,
    val route: String,
    val isPro: Boolean = false,
    val testTag: String
)

object KaiFeatureRegistry {
    val allFeatures: List<KaiFeatureItem> = listOf(
        // AI Assistant
        KaiFeatureItem("ai_chat", "KAI AI Chat", "Hinglish NLU & Hybrid Q&A", KaiFeatureCategory.AI, Icons.AutoMirrored.Rounded.Chat, KaiRoutesV2.AI_CHAT, false, "more_ai_chat_row"),
        KaiFeatureItem("ai_tools", "AI Tools Hub", "All intelligent workflows", KaiFeatureCategory.AI, Icons.Rounded.AutoAwesome, KaiRoutesV2.AI_TOOLS_HUB, false, "more_ai_tools_hub_row"),
        KaiFeatureItem("interpreter", "Interpreter & Memory", "Live Hindi/English & saved facts", KaiFeatureCategory.AI, Icons.Rounded.Translate, KaiRoutesV2.INTERPRETER_MEMORY, true, "more_interpreter_memory_row"),

        KaiFeatureItem("student_mode", "Student Study Mode", "Flashcards, focus timer & AI tutor (padhai)", KaiFeatureCategory.AI, Icons.Rounded.School, KaiRoutesV2.STUDENT_MODE, false, "more_student_mode_row"),
        // Privacy & Security
        KaiFeatureItem("hyper_security", "Hyper Security Shield", "Threat scan, Decoy PIN & Panic lock", KaiFeatureCategory.SECURITY, Icons.Rounded.GppGood, KaiRoutesV2.HYPER_SECURITY_CENTER, true, "more_hyper_security_row"),
        KaiFeatureItem("custom_lock", "Custom Lock & Anti-Theft", "Custom words, charger & snatch guard", KaiFeatureCategory.SECURITY, Icons.Rounded.Security, KaiRoutesV2.REAL_LIFE_POWER_HUB, true, "more_real_life_hub_row"),
        KaiFeatureItem("voiceprint", "Speaker Voiceprint", "Biometric vocal verification", KaiFeatureCategory.SECURITY, Icons.Rounded.GraphicEq, KaiRoutesV2.VOICEPRINT_MANAGER, true, "more_voiceprint_row"),
        KaiFeatureItem("intruder_vault", "Security Health & Intruders", "Health score & intruder selfies", KaiFeatureCategory.SECURITY, Icons.Rounded.Security, KaiRoutesV2.SECURITY_HEALTH_INTRUDER, true, "more_security_health_row"),

        // Personal Safety & Driving
        KaiFeatureItem("safety_hub", "SOS & Fake Call", "Emergency alert, 112 & medical card", KaiFeatureCategory.SAFETY, Icons.Rounded.Shield, KaiRoutesV2.SAFETY_HUB, false, "more_safety_hub_row"),
        KaiFeatureItem("driving_mode", "Driving Cockpit", "Hands-free commute & parking pin", KaiFeatureCategory.SAFETY, Icons.Rounded.DirectionsCar, KaiRoutesV2.DRIVING_MODE, true, "more_full_driving_row"),
        KaiFeatureItem("senior_mode", "Senior Citizen Mode", "Large buttons, dawai alarm & 112 (bujurg)", KaiFeatureCategory.SAFETY, Icons.Rounded.AccessibilityNew, KaiRoutesV2.SENIOR_MODE, false, "more_senior_mode_row"),
        
        // Everyday Utilities
        KaiFeatureItem("super_utils", "Direct WhatsApp & Alerts", "Message without saving, battery voice", KaiFeatureCategory.UTILITIES, Icons.Rounded.AutoAwesome, KaiRoutesV2.SUPER_UTILITIES_HUB, false, "more_super_utilities_row"),
        KaiFeatureItem("smart_money", "Smart Money", "EMI, GST & friends bill split", KaiFeatureCategory.UTILITIES, Icons.Rounded.Timer, KaiRoutesV2.SMART_MONEY_TOOLS, false, "more_smart_money_row"),
        KaiFeatureItem("productivity", "Expenses & Focus", "Voice expenses & 25m Pomodoro", KaiFeatureCategory.UTILITIES, Icons.Rounded.Timer, KaiRoutesV2.PRODUCTIVITY_HUB, false, "more_productivity_hub_row"),

        // Account & Settings
        KaiFeatureItem("kai_pro", "KAI Pro", "Unlock unlimited protection & AI", KaiFeatureCategory.ACCOUNT, Icons.Rounded.WorkspacePremium, KaiRoutesV2.SUBSCRIPTION_PRO, false, "more_subscription_pro_row"),
        KaiFeatureItem("profile", "Profile & Account", "Personal greeting & local profile", KaiFeatureCategory.ACCOUNT, Icons.Rounded.AccountCircle, KaiRoutesV2.PROFILE_ACCOUNT, false, "more_profile_account_row"),
        KaiFeatureItem("privacy_oem", "Privacy & Battery Wizard", "Keep KAI alive on your device", KaiFeatureCategory.ACCOUNT, Icons.Rounded.PrivacyTip, KaiRoutesV2.PRIVACY_OEM_WIZARD, false, "more_privacy_oem_row"),
        KaiFeatureItem("command_guide", "Voice Command Guide", "Hinglish commands & aliases", KaiFeatureCategory.ACCOUNT, Icons.Rounded.HelpOutline, KaiRoutes.COMMAND_HELP_ALIASES, false, "more_command_guide_row"),
        KaiFeatureItem("diagnostics", "Diagnostics", "NLU speed & permission health", KaiFeatureCategory.ACCOUNT, Icons.Rounded.Build, KaiRoutes.DIAGNOSTICS, false, "more_diagnostics_row"),
        KaiFeatureItem("settings", "Settings", "Language, voice, theme & data", KaiFeatureCategory.ACCOUNT, Icons.Rounded.Settings, KaiRoutes.SETTINGS, false, "more_settings_row"),
        KaiFeatureItem("about", "About KAIxGenAssistant", "Version & KAIxGenShopLabs", KaiFeatureCategory.ACCOUNT, Icons.Rounded.Info, KaiRoutesV2.ABOUT_KAI, false, "more_about_kai_row")
    )

    fun search(query: String, category: KaiFeatureCategory?): List<KaiFeatureItem> {
        val q = query.trim().lowercase()
        return allFeatures.filter { item ->
            val catOk = category == null || item.category == category
            val qOk = q.isBlank() || item.title.lowercase().contains(q) || item.subtitle.lowercase().contains(q)
            catOk && qOk
        }
    }
}
