package com.kaixgen.assistant.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kaixgen.assistant.data.local.KaiLocalRepository
import com.kaixgen.assistant.data.local.RoomKaiLocalRepository
import com.kaixgen.assistant.data.local.db.KaiDatabase
import com.kaixgen.assistant.domain.voice.HinglishVoiceCommandParser
import com.kaixgen.assistant.domain.voice.KaiActionExecutor
import com.kaixgen.assistant.domain.voice.KaiTtsManager
import com.kaixgen.assistant.security.BiometricHelper
import com.kaixgen.assistant.security.KeystorePepperProvider
import com.kaixgen.assistant.security.PinVault
import com.kaixgen.assistant.ui.viewmodel.CommandHelpViewModel
import com.kaixgen.assistant.ui.viewmodel.DiagnosticsViewModel
import com.kaixgen.assistant.ui.viewmodel.FindPhoneViewModel
import com.kaixgen.assistant.ui.viewmodel.KaiBuddyViewModel
import com.kaixgen.assistant.ui.viewmodel.MoreHubViewModel
import com.kaixgen.assistant.ui.viewmodel.RoutinesViewModel
import com.kaixgen.assistant.ui.viewmodel.SetBackupPasscodeViewModel
import com.kaixgen.assistant.ui.viewmodel.SetVoicePhraseViewModel
import com.kaixgen.assistant.ui.viewmodel.SettingsViewModel
import com.kaixgen.assistant.ui.viewmodel.VoiceLockViewModel

/**
 * Manual Constructor Injection Container wiring Room Database, Keystore PinVault,
 * System BiometricHelper, Hinglish NLU Parser, Play-compliant Action Executor, and On-device TTS.
 */
class KaiAppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: KaiDatabase by lazy {
        KaiDatabase.getInstance(appContext)
    }

    val pinVault: PinVault by lazy {
        PinVault(
            pinRecordDao = database.pinRecordDao(),
            pepperProvider = KeystorePepperProvider()
        )
    }

    val biometricHelper: BiometricHelper by lazy {
        BiometricHelper(appContext)
    }

    val localRepository: KaiLocalRepository by lazy {
        RoomKaiLocalRepository(
            database = database,
            pinVault = pinVault
        )
    }

    val commandParser: HinglishVoiceCommandParser by lazy {
        HinglishVoiceCommandParser()
    }

    val actionExecutor: KaiActionExecutor by lazy {
        KaiActionExecutor(
            context = appContext,
            repository = localRepository,
            parser = commandParser
        )
    }

    val ttsManager: KaiTtsManager by lazy {
        KaiTtsManager(appContext)
    }

    val viewModelFactory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return when {
                modelClass.isAssignableFrom(VoiceLockViewModel::class.java) -> {
                    VoiceLockViewModel(localRepository) as T
                }
                modelClass.isAssignableFrom(SetVoicePhraseViewModel::class.java) -> {
                    SetVoicePhraseViewModel(localRepository) as T
                }
                modelClass.isAssignableFrom(SetBackupPasscodeViewModel::class.java) -> {
                    SetBackupPasscodeViewModel(localRepository) as T
                }
                modelClass.isAssignableFrom(KaiBuddyViewModel::class.java) -> {
                    KaiBuddyViewModel(
                        repository = localRepository,
                        parser = commandParser,
                        actionExecutor = actionExecutor,
                        ttsManager = ttsManager
                    ) as T
                }
                modelClass.isAssignableFrom(RoutinesViewModel::class.java) -> {
                    RoutinesViewModel(
                        repository = localRepository,
                        parser = commandParser,
                        actionExecutor = actionExecutor,
                        ttsManager = ttsManager
                    ) as T
                }
                modelClass.isAssignableFrom(CommandHelpViewModel::class.java) -> {
                    CommandHelpViewModel(localRepository) as T
                }
                modelClass.isAssignableFrom(DiagnosticsViewModel::class.java) -> {
                    DiagnosticsViewModel(commandParser) as T
                }
                modelClass.isAssignableFrom(FindPhoneViewModel::class.java) -> {
                    FindPhoneViewModel(localRepository) as T
                }
                modelClass.isAssignableFrom(MoreHubViewModel::class.java) -> {
                    MoreHubViewModel(
                        appContext = appContext,
                        parser = commandParser,
                        actionExecutor = actionExecutor
                    ) as T
                }
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                    SettingsViewModel(localRepository) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    fun release() {
        ttsManager.shutdown()
    }
}
