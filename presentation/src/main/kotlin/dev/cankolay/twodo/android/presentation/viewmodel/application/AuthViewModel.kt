package dev.cankolay.twodo.android.presentation.viewmodel.application

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.application.AuthState
import dev.cankolay.twodo.android.domain.usecase.api.user.InitializeUserUseCase
import dev.cankolay.twodo.android.domain.usecase.application.auth.GetAuthStateUseCase
import dev.cankolay.twodo.android.domain.usecase.application.auth.UpdateAuthStateUseCase
import dev.cankolay.twodo.android.domain.usecase.application.environment.GetEnvironmentConfigUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val authState: AuthState? = null,
    val isAuthenticating: Boolean = false,
    val error: String? = null,
    val authUrl: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase,
    private val updateAuthStateUseCase: UpdateAuthStateUseCase,
    private val initializeUserUseCase: InitializeUserUseCase,
    getEnvironmentConfigUseCase: GetEnvironmentConfigUseCase
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(AuthUiState(authUrl = getEnvironmentConfigUseCase().authUrl))
    val uiState = _uiState.asStateFlow()
    private var authenticationJob: Job? = null
    private var handledToken: String? = null

    init {
        viewModelScope.launch {
            getAuthStateUseCase().collect { authState ->
                _uiState.update { it.copy(authState = authState) }
            }
        }
    }

    fun authenticate(uri: Uri) {
        val token = uri.getQueryParameter("token")
        if (token.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isAuthenticating = false,
                    error = "Authentication callback is missing a token"
                )
            }
            return
        }

        if (authenticationJob?.isActive == true || token == handledToken) return

        authenticationJob = viewModelScope.launch {
            authenticate(token = token)
        }
    }

    suspend fun authenticate(token: String): Boolean {
        if (token.isBlank()) {
            _uiState.update {
                it.copy(error = "Authentication callback is missing a token")
            }
            return false
        }

        _uiState.update { it.copy(isAuthenticating = true, error = null) }

        return try {
            updateAuthStateUseCase(state = AuthState(token = token))
            val result = initializeUserUseCase()
            handledToken = token
            result.onError { message, _ ->
                _uiState.update { it.copy(error = message) }
            }
            true
        } finally {
            _uiState.update { it.copy(isAuthenticating = false) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            handledToken = null
            _uiState.update { it.copy(isAuthenticating = false, error = null) }
            updateAuthStateUseCase(state = AuthState(token = ""))
        }
    }
}
