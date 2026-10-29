package dev.cankolay.twodo.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.cankolay.twodo.android.presentation.AppUI
import dev.cankolay.twodo.android.presentation.viewmodel.application.AppViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.application.SettingsViewModel

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    private val settingsViewModel by viewModels<SettingsViewModel>()
    private val appViewModel by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            settingsViewModel.uiState.value.isLoading || !appViewModel.isInitialized.value
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        consumeAuthIntent(source = intent)

        setContent {
            AppUI(
                settingsViewModel = settingsViewModel,
                appViewModel = appViewModel,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        consumeAuthIntent(source = intent)
    }

    private fun consumeAuthIntent(source: Intent) {
        source.data?.let(appViewModel::authenticate)
        source.data = null
    }
}
