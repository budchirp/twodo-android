package dev.cankolay.twodo.android.presentation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppMainLayout
import dev.cankolay.twodo.android.presentation.composition.ProvideNavBackStack
import dev.cankolay.twodo.android.presentation.composition.ProvideSnackbarHostState
import dev.cankolay.twodo.android.presentation.navigation.AppNavigation
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.theme.AppTheme
import dev.cankolay.twodo.android.presentation.viewmodel.UserViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.application.AuthViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.application.SettingsViewModel

@Composable
fun AppUI(
    uri: Uri?,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onAuthIntentConsumed: () -> Unit = {}
) {
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val userUiState by userViewModel.uiState.collectAsStateWithLifecycle()

    val settingsState = settingsUiState.settingsState
    val authState = authUiState.authState
    val user = userUiState.user

    HandleAuthUri(
        authUri = uri,
        authViewModel = authViewModel,
        onAuthIntentConsumed = onAuthIntentConsumed
    )

    SyncUserState(
        token = authState?.token,
        isAuthenticating = authUiState.isAuthenticating,
        userId = user?.id,
        isUserLoading = userUiState.isLoading,
        isUserInitialized = userUiState.isInitialized,
        userViewModel = userViewModel
    )

    if (settingsState == null || authState == null) return

    val startRoute = resolveStartRoute(
        token = authState.token,
        isAuthenticating = authUiState.isAuthenticating,
        user = user,
        userError = userUiState.error,
        userErrorCode = userUiState.errorCode
    )

    AppTheme(settingsState = settingsState) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            startRoute?.let { route ->
                AppRoot(
                    startRoute = route,
                    authViewModel = authViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}

@Composable
private fun HandleAuthUri(
    authUri: Uri?,
    authViewModel: AuthViewModel,
    onAuthIntentConsumed: () -> Unit
) {
    LaunchedEffect(authUri) {
        authUri ?: return@LaunchedEffect

        authViewModel.authenticate(uri = authUri)
        onAuthIntentConsumed()
    }
}

@Composable
private fun SyncUserState(
    token: String?,
    isAuthenticating: Boolean,
    userId: String?,
    isUserLoading: Boolean,
    isUserInitialized: Boolean,
    userViewModel: UserViewModel
) {
    LaunchedEffect(
        token,
        isAuthenticating,
        userId,
        isUserLoading,
        isUserInitialized
    ) {
        when {
            token == null -> Unit
            token.isEmpty() -> userViewModel.clearUser()

            !isAuthenticating &&
                    userId == null &&
                    !isUserLoading &&
                    !isUserInitialized -> {
                userViewModel.fetchUser()
            }
        }
    }
}

private fun resolveStartRoute(
    token: String,
    isAuthenticating: Boolean,
    user: User?,
    userError: String?,
    userErrorCode: String?
): Route? {
    return when {
        token.isEmpty() -> Route.Welcome
        isAuthenticating -> null

        userErrorCode == ERROR_PROFILE_REQUIRED -> Route.ProfileSetup

        user == null && userError == null -> null
        user == null && userError != null -> Route.StartupError

        user?.profileCompleted == false -> Route.ProfileSetup
        user?.couple != null -> Route.Notes

        else -> Route.CoupleSetup
    }
}

@Composable
private fun AppRoot(
    startRoute: Route,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    ProvideNavBackStack(startRoute = startRoute) {
        ProvideSnackbarHostState {
            AppMainLayout {
                AppNavigation(
                    authViewModel = authViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}

private const val ERROR_PROFILE_REQUIRED = "error-profile-required"