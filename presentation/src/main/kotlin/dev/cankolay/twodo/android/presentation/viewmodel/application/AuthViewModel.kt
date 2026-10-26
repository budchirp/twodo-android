package dev.cankolay.twodo.android.presentation.viewmodel.application

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.application.AuthState
import dev.cankolay.twodo.android.domain.usecase.api.user.InitializeUserUseCase
import dev.cankolay.twodo.android.domain.usecase.application.auth.GetAuthStateUseCase
import dev.cankolay.twodo.android.domain.usecase.application.auth.UpdateAuthStateUseCase
import dev.cankolay.twodo.android.domain.usecase.application.environment.GetEnvironmentConfigUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.UiEvent
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val authState: AuthState = AuthState(),
    val isAuthenticating: Boolean = false,
    val authUrl: String = "",
    val isInitialized: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    getAuthStateUseCase: GetAuthStateUseCase,
    private val updateAuthStateUseCase: UpdateAuthStateUseCase,
    private val initializeUserUseCase: InitializeUserUseCase,
    getEnvironmentConfigUseCase: GetEnvironmentConfigUseCase
) : BaseViewModel<AuthUiState>(AuthUiState(authUrl = getEnvironmentConfigUseCase().authUrl)) {
    private var handledToken: String? = null

    init {
        viewModelScope.launch {
            getAuthStateUseCase()
                .catch { emit(AuthState()) }
                .collect { authState ->
                    updateState { copy(authState = authState, isInitialized = true) }
                }
        }
    }

    fun authenticate(uri: Uri) {
        val token = uri.getQueryParameter("token")
        if (token.isNullOrBlank()) {
            sendEvent(
                UiEvent.ShowSnackbar(
                    context.getString(R.string.auth_missing_token)
                )
            )
            return
        }
        if (token == handledToken) return

        launchOnce("authenticate") {
            updateState { copy(isAuthenticating = true) }
            try {
                updateAuthStateUseCase(AuthState(token))
                initializeUserUseCase()
                    .onError { message, _ -> sendEvent(UiEvent.ShowSnackbar(message)) }
                handledToken = token
            } finally {
                updateState { copy(isAuthenticating = false) }
            }
        }
    }

    fun logout() {
        launchOnce("logout") {
            handledToken = null
            updateState { copy(isAuthenticating = false) }
            updateAuthStateUseCase(AuthState(token = ""))
        }
    }
}
