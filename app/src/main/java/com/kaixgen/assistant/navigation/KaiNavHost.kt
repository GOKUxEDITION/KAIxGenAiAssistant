package com.kaixgen.assistant.navigation

import android.content.Context
import android.os.Build
import com.kaixgen.assistant.ui.screens.settings.ProfileAccountScreen
import com.kaixgen.assistant.ui.screens.settings.SubscriptionPaywallScreen
import com.kaixgen.assistant.ui.screens.security.HyperSecurityCenterScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kaixgen.assistant.R
import com.kaixgen.assistant.di.KaiAppContainer
import android.app.Activity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.kaixgen.assistant.KaiApplication
import com.kaixgen.assistant.security.ScreenSecurityHelper
import com.kaixgen.assistant.ui.screens.assistant.InterpreterAndMemoryScreen
import com.kaixgen.assistant.ui.screens.driving.DrivingModeScreen
import com.kaixgen.assistant.ui.screens.productivity.ProductivityHubScreen
import com.kaixgen.assistant.ui.screens.safety.SafetyHubScreen
import com.kaixgen.assistant.ui.screens.security.SecurityHealthAndIntruderScreen
import com.kaixgen.assistant.ui.screens.security.VoiceprintManagerScreen
import com.kaixgen.assistant.ui.screens.security.RealLifePowerHubScreen
import com.kaixgen.assistant.ui.screens.settings.PrivacyAndOemWizardScreen
import com.kaixgen.assistant.ui.screens.more.SuperUtilitiesHubScreen
import com.kaixgen.assistant.ui.screens.productivity.SmartMoneyToolsScreen
import com.kaixgen.assistant.ui.components.KaiFloatingBottomBar
import com.kaixgen.assistant.ui.screens.assistant.AiToolsHubScreen
import com.kaixgen.assistant.ui.screens.onboarding.WelcomeOnboardingScreen
import com.kaixgen.assistant.ui.screens.assistant.KaiAiChatScreen
import com.kaixgen.assistant.ui.screens.settings.AboutKaiScreen
import com.kaixgen.assistant.ui.screens.buddy.CommandHelpAndAliasesScreen
import com.kaixgen.assistant.ui.screens.buddy.KaiBuddyScreen
import com.kaixgen.assistant.ui.screens.findphone.FindPhoneScreen
import com.kaixgen.assistant.ui.screens.more.MoreHubScreen
import com.kaixgen.assistant.ui.screens.routines.RoutinesScreen
import com.kaixgen.assistant.ui.screens.settings.DiagnosticsScreen
import com.kaixgen.assistant.ui.screens.settings.SettingsScreen
import com.kaixgen.assistant.ui.screens.voicelock.AppLockListScreen
import com.kaixgen.assistant.ui.screens.voicelock.SetBackupPasscodeScreen
import com.kaixgen.assistant.ui.screens.voicelock.SetVoicePhraseScreen
import com.kaixgen.assistant.ui.screens.voicelock.VoiceLockHomeScreen
import com.kaixgen.assistant.ui.screens.voicelock.VoiceLockOverlayScreen
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

@Composable
fun KaiMainNavigationHost(
    appContainer: KaiAppContainer,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val kaiApp = context.applicationContext as? KaiApplication
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: KaiRoutes.VOICE_LOCK_HOME

    val voiceLockViewModel: VoiceLockViewModel = viewModel(factory = appContainer.viewModelFactory)
    val setVoicePhraseViewModel: SetVoicePhraseViewModel = viewModel(factory = appContainer.viewModelFactory)
    val setBackupPasscodeViewModel: SetBackupPasscodeViewModel = viewModel(factory = appContainer.viewModelFactory)
    val kaiBuddyViewModel: KaiBuddyViewModel = viewModel(factory = appContainer.viewModelFactory)
    val routinesViewModel: RoutinesViewModel = viewModel(factory = appContainer.viewModelFactory)
    val commandHelpViewModel: CommandHelpViewModel = viewModel(factory = appContainer.viewModelFactory)
    val diagnosticsViewModel: DiagnosticsViewModel = viewModel(factory = appContainer.viewModelFactory)
    val findPhoneViewModel: FindPhoneViewModel = viewModel(factory = appContainer.viewModelFactory)
    val moreHubViewModel: MoreHubViewModel = viewModel(factory = appContainer.viewModelFactory)

    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val selectedTab = when (currentRoute) {
        KaiRoutes.KAI_BUDDY -> KaiBottomTab.KAI_BUDDY
        KaiRoutes.ROUTINES -> KaiBottomTab.ROUTINES
        KaiRoutes.FIND_PHONE -> KaiBottomTab.FIND_PHONE
        KaiRoutes.MORE_HUB -> KaiBottomTab.MORE
        else -> KaiBottomTab.VOICE_LOCK
    }

    val fullScreenSubRoutes = setOf(
        KaiRoutes.SETTINGS,
        KaiRoutes.APP_LOCK_LIST,
        KaiRoutes.VOICE_LOCK_OVERLAY,
        KaiRoutes.COMMAND_HELP_ALIASES,
        KaiRoutes.DIAGNOSTICS,
        KaiRoutesV2.SECURITY_HEALTH_INTRUDER,
        KaiRoutesV2.HYPER_SECURITY_CENTER,
        KaiRoutesV2.VOICEPRINT_MANAGER,
        KaiRoutesV2.SAFETY_HUB,
        KaiRoutesV2.DRIVING_MODE,
        KaiRoutesV2.INTERPRETER_MEMORY,
        KaiRoutesV2.PRODUCTIVITY_HUB,
        KaiRoutesV2.SUBSCRIPTION_PRO,
KaiRoutesV2.PROFILE_ACCOUNT,
        KaiRoutesV2.PRIVACY_OEM_WIZARD,
        KaiRoutesV2.AI_CHAT,
        KaiRoutesV2.AI_TOOLS_HUB,
        KaiRoutesV2.ABOUT_KAI,
        KaiRoutesV2.REAL_LIFE_POWER_HUB,
        KaiRoutesV2.SUPER_UTILITIES_HUB,
        KaiRoutesV2.SMART_MONEY_TOOLS        ,
        KaiRoutesV2.WELCOME_ONBOARDING
    )
    val showNavigationChrome = currentRoute !in fullScreenSubRoutes

    fun navigateToTab(tab: KaiBottomTab) {
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
    val onboardingPrefs = remember(context) {
        context.getSharedPreferences("kai_onboarding_prefs", Context.MODE_PRIVATE)
    }
    val startRoute = remember(onboardingPrefs) {
        val isTestEnv = Build.FINGERPRINT.equals("robolectric", ignoreCase = true)
        val completed = onboardingPrefs.getBoolean("onboarding_completed", false)
        if (!isTestEnv && !completed) KaiRoutesV2.WELCOME_ONBOARDING else KaiRoutes.VOICE_LOCK_HOME
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 680.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail && showNavigationChrome) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationRailItem(
                        selected = selectedTab == KaiBottomTab.VOICE_LOCK,
                        onClick = { navigateToTab(KaiBottomTab.VOICE_LOCK) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == KaiBottomTab.VOICE_LOCK) Icons.Rounded.Lock else Icons.Outlined.Lock,
                                contentDescription = stringResource(R.string.nav_voice_lock)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_voice_lock)) }
                    )
                    NavigationRailItem(
                        selected = selectedTab == KaiBottomTab.KAI_BUDDY,
                        onClick = { navigateToTab(KaiBottomTab.KAI_BUDDY) },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = stringResource(R.string.nav_kai_buddy)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_kai_buddy)) }
                    )
                    NavigationRailItem(
                        selected = selectedTab == KaiBottomTab.ROUTINES,
                        onClick = { navigateToTab(KaiBottomTab.ROUTINES) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == KaiBottomTab.ROUTINES) Icons.Rounded.Bolt else Icons.Outlined.Bolt,
                                contentDescription = stringResource(R.string.nav_routines)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_routines)) }
                    )
                    NavigationRailItem(
                        selected = selectedTab == KaiBottomTab.FIND_PHONE,
                        onClick = { navigateToTab(KaiBottomTab.FIND_PHONE) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == KaiBottomTab.FIND_PHONE) Icons.Rounded.Shield else Icons.Outlined.Shield,
                                contentDescription = stringResource(R.string.nav_find_phone)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_find_phone)) }
                    )
                    NavigationRailItem(
                        selected = selectedTab == KaiBottomTab.MORE,
                        onClick = { navigateToTab(KaiBottomTab.MORE) },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == KaiBottomTab.MORE) Icons.Rounded.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = stringResource(R.string.nav_more)
                            )
                        },
                        label = { Text(stringResource(R.string.nav_more)) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                NavHost(
                    navController = navController,
                  startDestination = startRoute,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(KaiRoutesV2.WELCOME_ONBOARDING) {
                        WelcomeOnboardingScreen(
                            onFinishOnboarding = {
                                onboardingPrefs.edit().putBoolean("onboarding_completed", true).apply()
                                navController.navigate(KaiRoutes.VOICE_LOCK_HOME) {
                                    popUpTo(KaiRoutesV2.WELCOME_ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(KaiRoutes.VOICE_LOCK_HOME) {
                        val uiState by voiceLockViewModel.uiState.collectAsStateWithLifecycle()
                        VoiceLockHomeScreen(
                            uiState = uiState,
                            isWakeWordActive = settingsState.wakeWordEnabled,
                            onToggleVoiceLock = voiceLockViewModel::onToggleVoiceLock,
                            onDismissPinWarning = voiceLockViewModel::dismissPinWarning,
                            onNavigateToSetPhrase = { navController.navigate(KaiRoutes.SET_VOICE_PHRASE) },
                            onNavigateToSetPasscode = { navController.navigate(KaiRoutes.SET_BACKUP_PASSCODE) },
                            onNavigateToAppLock = { navController.navigate(KaiRoutes.APP_LOCK_LIST) },
                            onNavigateToSettings = { navController.navigate(KaiRoutes.SETTINGS) },
                            onSelectSecurityMode = voiceLockViewModel::onSelectSecurityMode,
                            onOpenTestLock = {
                                voiceLockViewModel.dismissTestLockPreview()
                                navController.navigate(KaiRoutes.VOICE_LOCK_OVERLAY)
                            },
                            onDismissTestLock = voiceLockViewModel::dismissTestLockPreview,
                            onTestVoiceUnlock = voiceLockViewModel::onTestVoiceUnlock,
                            onTestPinUnlock = voiceLockViewModel::onTestPinUnlock
                        )
                    }

                    composable(KaiRoutes.VOICE_LOCK_OVERLAY) {
                    DisposableEffect(Unit) {
                        ScreenSecurityHelper.enableScreenCaptureProtection(activity)
                        onDispose { ScreenSecurityHelper.disableScreenCaptureProtection(activity) }
                    }
                        val uiState by voiceLockViewModel.uiState.collectAsStateWithLifecycle()
                        VoiceLockOverlayScreen(
                            uiState = uiState,
                            biometricHelper = appContainer.biometricHelper,
                            onTestVoiceUnlock = voiceLockViewModel::onTestVoiceUnlock,
                            onTestPinUnlock = voiceLockViewModel::onTestPinUnlock,
                            onBiometricUnlockSuccess = voiceLockViewModel::onBiometricUnlockSuccess,
                            onBiometricFeedback = voiceLockViewModel::onBiometricFeedback,
                            onExitOverlay = {
                                voiceLockViewModel.dismissTestLockPreview()
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(KaiRoutes.SET_VOICE_PHRASE) {
                        val uiState by setVoicePhraseViewModel.uiState.collectAsStateWithLifecycle()
                        SetVoicePhraseScreen(
                            uiState = uiState,
                            onBackClick = { navController.popBackStack() },
                            onSettingsClick = { navController.navigate(KaiRoutes.SETTINGS) },
                            onSelectLanguage = setVoicePhraseViewModel::onSelectLanguage,
                            onOpenRecordSheet = setVoicePhraseViewModel::openRecordSheet,
                            onCloseRecordSheet = setVoicePhraseViewModel::closeRecordSheet,
                            onStartListening = setVoicePhraseViewModel::onStartListening,
                            onStopListening = setVoicePhraseViewModel::onStopListening,
                            onSpeechRecognized = setVoicePhraseViewModel::onSpeechRecognized,
                            onSpeechError = setVoicePhraseViewModel::onSpeechError,
                            onDraftPhraseTyped = setVoicePhraseViewModel::onDraftPhraseTyped,
                            onConfirmSavePhrase = setVoicePhraseViewModel::onConfirmSavePhrase,
                            onNavigateToBackupPasscode = { navController.navigate(KaiRoutes.SET_BACKUP_PASSCODE) }
                        )
                    }

                    composable(KaiRoutes.SET_BACKUP_PASSCODE) {
                    DisposableEffect(Unit) {
                        ScreenSecurityHelper.enableScreenCaptureProtection(activity)
                        onDispose { ScreenSecurityHelper.disableScreenCaptureProtection(activity) }
                    }
                        val uiState by setBackupPasscodeViewModel.uiState.collectAsStateWithLifecycle()
                        SetBackupPasscodeScreen(
                            uiState = uiState,
                            onBackClick = {
                                if (!setBackupPasscodeViewModel.onBackStep()) {
                                    navController.popBackStack()
                                }
                            },
                            onSettingsClick = { navController.navigate(KaiRoutes.SETTINGS) },
                            onTogglePinLength = setBackupPasscodeViewModel::onTogglePinLength,
                            onDigitPressed = setBackupPasscodeViewModel::onDigitPressed,
                            onBackspacePressed = setBackupPasscodeViewModel::onBackspacePressed,
                            onToggleVisibility = setBackupPasscodeViewModel::onToggleVisibility,
                            onPrimaryAction = { setBackupPasscodeViewModel.onPrimaryAction() },
                            onSetupFinished = {
                                setBackupPasscodeViewModel.resetSetupFlag()
                                navController.popBackStack(KaiRoutes.VOICE_LOCK_HOME, inclusive = false)
                            }
                        )
                    }

                    composable(KaiRoutes.APP_LOCK_LIST) {
                        val uiState by voiceLockViewModel.uiState.collectAsStateWithLifecycle()
                        AppLockListScreen(
                            uiState = uiState,
                            onBackClick = { navController.popBackStack() },
                            onSearchQueryChange = voiceLockViewModel::onAppSearchQueryChange,
                            onSelectGracePeriod = voiceLockViewModel::onUpdateGracePeriod,
                            onToggleAppLock = voiceLockViewModel::onToggleAppLock
                        )
                    }

                    composable(KaiRoutes.KAI_BUDDY) {
                        val uiState by kaiBuddyViewModel.uiState.collectAsStateWithLifecycle()
                        KaiBuddyScreen(
                            uiState = uiState,
                            languageCode = settingsState.language.code,
                            onStartListening = kaiBuddyViewModel::onStartListening,
                            onAmplitudeChanged = kaiBuddyViewModel::onAmplitudeChanged,
                            onPartialTranscript = kaiBuddyViewModel::onPartialTranscript,
                            onFinalTranscript = kaiBuddyViewModel::onFinalTranscript,
                            onVoiceError = kaiBuddyViewModel::onVoiceError,
                            onCancelListening = kaiBuddyViewModel::onCancelListening,
                            onSuggestionTapped = kaiBuddyViewModel::onSuggestionTapped,
                            onReplayHistoryCommand = kaiBuddyViewModel::onReplayHistoryCommand,
                            onDeleteHistoryItem = kaiBuddyViewModel::onDeleteHistoryItem,
                            onClearHistory = kaiBuddyViewModel::onClearHistory,
                            onConfirmAction = kaiBuddyViewModel::onConfirmPendingAction,
                            onCancelAction = kaiBuddyViewModel::onCancelPendingAction,
                            onFeedbackVote = kaiBuddyViewModel::onFeedbackVote,
                            onDismissFeedback = kaiBuddyViewModel::dismissFeedbackCard,
                            onNavigateToCommandHelp = { navController.navigate(KaiRoutes.COMMAND_HELP_ALIASES) },
                            onNavigateToSettings = { navController.navigate(KaiRoutes.SETTINGS) }
                        )
                    }

                    composable(KaiRoutes.COMMAND_HELP_ALIASES) {
                        val aliases by commandHelpViewModel.aliases.collectAsStateWithLifecycle()
                        CommandHelpAndAliasesScreen(
                            aliases = aliases,
                            onBackClick = { navController.popBackStack() },
                            onTryCommand = { commandText ->
                                kaiBuddyViewModel.onFinalTranscript(commandText)
                                navController.popBackStack()
                                navigateToTab(KaiBottomTab.KAI_BUDDY)
                            },
                            onAddAlias = commandHelpViewModel::onAddAlias,
                            onDeleteAlias = commandHelpViewModel::onDeleteAlias
                        )
                    }

                    composable(KaiRoutes.ROUTINES) {
                        val uiState by routinesViewModel.uiState.collectAsStateWithLifecycle()
                        RoutinesScreen(
                            uiState = uiState,
                            isWakeWordActive = settingsState.wakeWordEnabled,
                            onToggleRoutine = routinesViewModel::onToggleRoutine,
                            onRunRoutineNow = routinesViewModel::onRunRoutineNow,
                            onOpenCreateSheet = routinesViewModel::openCreateSheet,
                            onCloseCreateSheet = routinesViewModel::closeCreateSheet,
                            onCreateRoutine = routinesViewModel::onCreateRoutine,
                            onDeleteRoutine = routinesViewModel::onDeleteRoutine,
                            onDismissBanner = routinesViewModel::dismissBanner,
                            onNavigateToSettings = { navController.navigate(KaiRoutes.SETTINGS) }
                        )
                    }

                    composable(KaiRoutes.FIND_PHONE) {
                        val uiState by findPhoneViewModel.uiState.collectAsStateWithLifecycle()
                        FindPhoneScreen(
                            uiState = uiState,
                            isWakeWordActive = settingsState.wakeWordEnabled,
                            onToggleRing = findPhoneViewModel::onToggleRing,
                            onStopRinging = findPhoneViewModel::onStopRinging,
                            onToggleVoiceTrigger = findPhoneViewModel::onToggleVoiceTrigger,
                            onToggleDoubleClap = findPhoneViewModel::onToggleDoubleClap,
                            onToggleFlashlight = findPhoneViewModel::onToggleFlashlight,
                            onToggleVibrate = findPhoneViewModel::onToggleVibrate,
                            onNavigateToSettings = { navController.navigate(KaiRoutes.SETTINGS) }
                        )
                    }

                    composable(KaiRoutes.MORE_HUB) {
                        val uiState by moreHubViewModel.uiState.collectAsStateWithLifecycle()
                        MoreHubScreen(
                            uiState = uiState,
onNavigateToProfileAccount = { navController.navigate(KaiRoutesV2.PROFILE_ACCOUNT) },
                    onNavigateToSubscriptionPro = { navController.navigate(KaiRoutesV2.SUBSCRIPTION_PRO) },
                            isWakeWordActive = settingsState.wakeWordEnabled,
                      onNavigateToHyperSecurity = { navController.navigate(KaiRoutesV2.HYPER_SECURITY_CENTER) },
                            onRefreshDeviceStatus = moreHubViewModel::refreshDeviceStatus,
                            onToggleFlashlight = moreHubViewModel::toggleFlashlight,
                            onOpenDrivingMode = moreHubViewModel::openDrivingModeSheet,
                            onCloseDrivingMode = moreHubViewModel::closeDrivingModeSheet,
                            onLaunchMaps = moreHubViewModel::launchMapsNavigation,
                            onRunVoiceAction = moreHubViewModel::runVoiceAction,
                            onLaunchTimer = moreHubViewModel::launchQuickTimer,
                            onAddTask = moreHubViewModel::addQuickTask,
                            onToggleTask = moreHubViewModel::toggleTask,
                            onDeleteTask = moreHubViewModel::deleteTask,
                            onDismissBanner = moreHubViewModel::dismissBanner,
                            onNavigateToVoiceBuddy = { navigateToTab(KaiBottomTab.KAI_BUDDY) },
                            onNavigateToCommandHelp = { navController.navigate(KaiRoutes.COMMAND_HELP_ALIASES) },
                            onNavigateToDiagnostics = { navController.navigate(KaiRoutes.DIAGNOSTICS) },
                            onNavigateToSettings = { navController.navigate(KaiRoutes.SETTINGS) },
                    onNavigateToSecurityHealth = { navController.navigate(KaiRoutesV2.SECURITY_HEALTH_INTRUDER) },
                    onNavigateToVoiceprint = { navController.navigate(KaiRoutesV2.VOICEPRINT_MANAGER) },
                    onNavigateToSafetyHub = { navController.navigate(KaiRoutesV2.SAFETY_HUB) },
                    onNavigateToFullDriving = { navController.navigate(KaiRoutesV2.DRIVING_MODE) },
                    onNavigateToInterpreterMemory = { navController.navigate(KaiRoutesV2.INTERPRETER_MEMORY) },
                    onNavigateToProductivityHub = { navController.navigate(KaiRoutesV2.PRODUCTIVITY_HUB) },
                    onNavigateToPrivacyOem = { navController.navigate(KaiRoutesV2.PRIVACY_OEM_WIZARD) } ,
                            onNavigateToAiChat = { navController.navigate(KaiRoutesV2.AI_CHAT) },
                            onNavigateToAiToolsHub = { navController.navigate(KaiRoutesV2.AI_TOOLS_HUB) },
                            onNavigateToAboutKai = { navController.navigate(KaiRoutesV2.ABOUT_KAI) },
                onNavigateToRealLifeHub = { navController.navigate(KaiRoutesV2.REAL_LIFE_POWER_HUB) },
                          onNavigateToSuperUtilities = { navController.navigate(KaiRoutesV2.SUPER_UTILITIES_HUB) },
                           onNavigateToSmartMoney = { navController.navigate(KaiRoutesV2.SMART_MONEY_TOOLS) } 
                        )
                    }

                    composable(KaiRoutes.SETTINGS) {
                        SettingsScreen(
                            uiState = settingsState,
                            onBackClick = { navController.popBackStack() },
                            onSelectLanguage = settingsViewModel::onSelectLanguage,
                            onToggleWakeWord = settingsViewModel::onToggleWakeWord,
                            onSelectSensitivity = settingsViewModel::onSelectSensitivity,
                            onToggleTts = settingsViewModel::onToggleTts,
                            onSelectTheme = settingsViewModel::onSelectTheme,
                            onToggleDynamicColor = settingsViewModel::onToggleDynamicColor,
                            onNavigateToCommandHelp = { navController.navigate(KaiRoutes.COMMAND_HELP_ALIASES) },
                            onNavigateToDiagnostics = { navController.navigate(KaiRoutes.DIAGNOSTICS) },
                            onDeleteAllData = settingsViewModel::onDeleteAllData
                        )
                    }

                    composable(KaiRoutes.DIAGNOSTICS) {
                        val uiState by diagnosticsViewModel.uiState.collectAsStateWithLifecycle()
                        DiagnosticsScreen(
                            uiState = uiState,
                            onBackClick = { navController.popBackStack() },
                            onRunBenchmark = diagnosticsViewModel::runNluBenchmark
                        )
                    }
                composable(KaiRoutesV2.SECURITY_HEALTH_INTRUDER) {
                    val uiState by voiceLockViewModel.uiState.collectAsStateWithLifecycle()
                    SecurityHealthAndIntruderScreen(
                        hasBackupPin = !uiState.showPinRequiredWarning,
                        isVoiceLockEnabled = uiState.config.isVoiceLockEnabled,
                        isVoiceprintEnrolled = uiState.config.voicePhrase.isNotBlank(),
                        lockedAppCount = uiState.lockedAppCount,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(KaiRoutesV2.VOICEPRINT_MANAGER) {
                    val uiState by voiceLockViewModel.uiState.collectAsStateWithLifecycle()
                    VoiceprintManagerScreen(
                        voicePhrase = uiState.config.voicePhrase,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(KaiRoutesV2.SAFETY_HUB) {
                    SafetyHubScreen(
                        onTriggerSiren = { kaiApp?.findPhoneAlarmEngine?.startAlarm(enableFlashlight = true, enableVibrate = true) },
                        onStopSiren = { kaiApp?.findPhoneAlarmEngine?.stopAlarm() },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(KaiRoutesV2.DRIVING_MODE) {
                    DrivingModeScreen(
                        onSpeakCommand = { navigateToTab(KaiBottomTab.KAI_BUDDY) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(KaiRoutesV2.INTERPRETER_MEMORY) {
                    InterpreterAndMemoryScreen(
                        onSpeakText = { text, lang -> appContainer.ttsManager.speak(text, languageCode = lang) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(KaiRoutesV2.PRODUCTIVITY_HUB) {
                    ProductivityHubScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(KaiRoutesV2.PRIVACY_OEM_WIZARD) {
                    PrivacyAndOemWizardScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
composable(KaiRoutesV2.PROFILE_ACCOUNT) { ProfileAccountScreen(onBack = { navController.popBackStack() }) }
        composable(KaiRoutesV2.SUBSCRIPTION_PRO) { SubscriptionPaywallScreen(onBack = { navController.popBackStack() }) }
            composable(KaiRoutesV2.AI_CHAT) {
                KaiAiChatScreen(
                    onOpenVoiceMode = { navigateToTab(KaiBottomTab.KAI_BUDDY) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(KaiRoutesV2.AI_TOOLS_HUB) {
                AiToolsHubScreen(
                    onOpenAiChat = { navController.navigate(KaiRoutesV2.AI_CHAT) },
                    onOpenInterpreterMemory = { navController.navigate(KaiRoutesV2.INTERPRETER_MEMORY) },
                    onOpenProductivity = { navController.navigate(KaiRoutesV2.PRODUCTIVITY_HUB) },
                    onOpenDrivingMode = { navController.navigate(KaiRoutesV2.DRIVING_MODE) },
                    onOpenSecurityHealth = { navController.navigate(KaiRoutesV2.SECURITY_HEALTH_INTRUDER) },
                    onOpenVoiceprint = { navController.navigate(KaiRoutesV2.VOICEPRINT_MANAGER) },
                    onOpenSafetyHub = { navController.navigate(KaiRoutesV2.SAFETY_HUB) },
                    onOpenPrivacyWizard = { navController.navigate(KaiRoutesV2.PRIVACY_OEM_WIZARD) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(KaiRoutesV2.ABOUT_KAI) {
                AboutKaiScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        composable(KaiRoutesV2.REAL_LIFE_POWER_HUB) {
            RealLifePowerHubScreen(onBack = { navController.popBackStack() })
        }
        composable(KaiRoutesV2.SUPER_UTILITIES_HUB) {
            SuperUtilitiesHubScreen(
                    onOpenMoneyTools = { navController.navigate(KaiRoutesV2.SMART_MONEY_TOOLS) }, 
                    onBack = { navController.popBackStack() }
                )
        }

        composable(KaiRoutesV2.SMART_MONEY_TOOLS) {
            SmartMoneyToolsScreen(
                    onBack = { navController.popBackStack() }
                )
        }
                    composable(KaiRoutesV2.HYPER_SECURITY_CENTER) {
                        HyperSecurityCenterScreen(
                                onOpenVoiceprint = { navController.navigate(KaiRoutesV2.VOICEPRINT_MANAGER) },
                                onOpenIntruderVault = { navController.navigate(KaiRoutesV2.SECURITY_HEALTH_INTRUDER) },
                               onOpenSafetySos = { navController.navigate(KaiRoutesV2.SAFETY_HUB) },
                               onBack = { navController.popBackStack() }
                           )
                    }
                }
                if (!useNavigationRail && showNavigationChrome) {
                    KaiFloatingBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = ::navigateToTab,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
