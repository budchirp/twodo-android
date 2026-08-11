package dev.cankolay.twodo.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.cankolay.twodo.android.presentation.AppUI
import dev.cankolay.twodo.android.presentation.viewmodel.application.AuthViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.application.SettingsViewModel

@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    private val settingsViewModel by viewModels<SettingsViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    private var uri by mutableStateOf<Uri?>(value = null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            !settingsViewModel.uiState.value.isInitialized ||
                    !authViewModel.uiState.value.isInitialized
        }

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        uri = intent.data

        setContent {
            AppUI(
                uri = uri,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel,
                onAuthIntentConsumed = ::consumeAuthIntent
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        uri = intent.data
    }

    private fun consumeAuthIntent() {
        uri = null

        intent = Intent(intent).apply {
            data = null
        }
    }
}
