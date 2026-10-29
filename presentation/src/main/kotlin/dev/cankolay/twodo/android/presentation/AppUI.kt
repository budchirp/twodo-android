package dev.cankolay.twodo.android.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppMainLayout
import dev.cankolay.twodo.android.presentation.composition.ProvideNavBackStack
import dev.cankolay.twodo.android.presentation.composition.ProvideNotesLayout
import dev.cankolay.twodo.android.presentation.navigation.AppNavigation
import dev.cankolay.twodo.android.presentation.theme.AppTheme
import dev.cankolay.twodo.android.presentation.view.LoginErrorSheet
import dev.cankolay.twodo.android.presentation.view.StartupErrorView
import dev.cankolay.twodo.android.presentation.viewmodel.application.AppViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.application.SettingsViewModel

@Composable
fun AppUI(appViewModel: AppViewModel, settingsViewModel: SettingsViewModel) {
    val state by appViewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.uiState.collectAsStateWithLifecycle()

    AppTheme(settingsState = settings.settings) {
        Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
            val route = state.phase.route
            if (settings.isLoading || route == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else
                key(state.id) {
                    ProvideNavBackStack(startRoute = route) {
                        AppMainLayout {
                            ProvideNotesLayout {
                                AppNavigation(
                                    authUrl = appViewModel.authUrl,
                                    onLogout = appViewModel::logout,
                                    startup = {
                                        StartupErrorView(
                                            result = state.result,
                                            onRetry = appViewModel::retry,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            state.error?.let { error ->
                LoginErrorSheet(error = error, onDismiss = appViewModel::dismissError)
            }
        }
    }
}
