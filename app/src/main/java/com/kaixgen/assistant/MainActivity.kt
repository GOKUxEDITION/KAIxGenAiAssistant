package com.kaixgen.assistant

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaixgen.assistant.di.KaiAppContainer
import com.kaixgen.assistant.domain.model.ThemePreference
import com.kaixgen.assistant.navigation.KaiMainNavigationHost
import com.kaixgen.assistant.ui.theme.KAIxGenTheme
import com.kaixgen.assistant.ui.viewmodel.SettingsViewModel

class MainActivity : FragmentActivity() {

    private val appContainer by lazy { KaiAppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val container = remember { appContainer }
            val settingsViewModel: SettingsViewModel = viewModel(factory = container.viewModelFactory)
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            val useDarkTheme = when (settingsState.themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            KAIxGenTheme(
                darkTheme = useDarkTheme,
                dynamicColor = settingsState.dynamicColorEnabled
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KaiMainNavigationHost(
                        appContainer = container,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            appContainer.release()
        }
    }
}
