package com.kaixgen.assistant.navigation

enum class KaiBottomTab(val route: String) {
    VOICE_LOCK(KaiRoutes.VOICE_LOCK_HOME),
    KAI_BUDDY(KaiRoutes.KAI_BUDDY),
    ROUTINES(KaiRoutes.ROUTINES),
    FIND_PHONE(KaiRoutes.FIND_PHONE),
    MORE(KaiRoutes.MORE_HUB)
}

object KaiRoutes {
    const val VOICE_LOCK_HOME = "voice_lock_home"
    const val SET_VOICE_PHRASE = "set_voice_phrase"
    const val SET_BACKUP_PASSCODE = "set_backup_passcode"
    const val APP_LOCK_LIST = "app_lock_list"
    const val VOICE_LOCK_OVERLAY = "voice_lock_overlay"
    const val KAI_BUDDY = "kai_buddy"
    const val COMMAND_HELP_ALIASES = "command_help_aliases"
    const val ROUTINES = "routines"
    const val FIND_PHONE = "find_phone"
    const val MORE_HUB = "more_hub"
    const val SETTINGS = "settings"
    const val DIAGNOSTICS = "diagnostics"
}
