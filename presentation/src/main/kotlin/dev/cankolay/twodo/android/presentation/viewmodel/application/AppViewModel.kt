package dev.cankolay.twodo.android.presentation.viewmodel.application

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.repository.application.EnvironmentConfigRepository
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppPhase(val route: Route?) {
    Starting(null),
    SignedOut(Route.Welcome),
    ProfileRequired(Route.ProfileSetup),
    CoupleRequired(Route.CoupleSetup),
    Ready(Route.Notes),
    Failed(Route.StartupError),
}

data class AppUiState(
    val phase: AppPhase = AppPhase.Starting,
    val accountId: String? = null,
    val result: ApiResult<User>? = null,
    val loginError: Int? = null,
)

@HiltViewModel
class AppViewModel
@Inject
constructor(
    private val session: SessionRepository,
    environment: EnvironmentConfigRepository,
) : BaseViewModel<AppUiState>(initialState = AppUiState()) {
    val authUrl = environment.get().authUrl
    val isInitialized = session.isInitialized

    init {
        viewModelScope.launch {
            session.state.collect { state ->
                _uiState.update { current ->
                    when (state) {
                        SessionState.Starting -> AppUiState()

                        SessionState.SignedOut -> AppUiState(phase = AppPhase.SignedOut)

                        SessionState.ProfileRequired -> AppUiState(phase = AppPhase.ProfileRequired)

                        is SessionState.Failed ->
                            current.copy(
                                phase = AppPhase.Failed,
                                result = ApiResult.Failure(error = state.error),
                            )

                        is SessionState.Active ->
                            AppUiState(
                                phase =
                                    when {
                                        !state.user.profileCompleted -> AppPhase.ProfileRequired

                                        state.user.couple == null -> AppPhase.CoupleRequired

                                        else -> AppPhase.Ready
                                    },
                                accountId = state.user.id,
                            )
                    }
                }
            }
        }
    }

    fun authenticate(uri: Uri) {
        val token = uri.getQueryParameter("token")

        if (token.isNullOrBlank()) {
            _uiState.update { it.copy(loginError = R.string.auth_missing_token) }
            return
        }

        viewModelScope.launch { session.authenticate(token = token) }
    }

    fun dismissError() {
        _uiState.update { it.copy(loginError = null) }
    }

    fun logout() {
        viewModelScope.launch { session.logout() }
    }

    fun retry() {
        if (_uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = session.refresh()

            _uiState.update { it.copy(result = result) }
        }
    }
}
