package dev.cankolay.twodo.android.presentation.viewmodel.application

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.application.environment.GetEnvironmentConfigUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.AuthenticateUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.LogoutUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionInitializedUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppPhase(val route: Route?) {
    Starting(route = null),
    SignedOut(route = Route.Welcome),
    ProfileRequired(route = Route.ProfileSetup),
    CoupleRequired(route = Route.CoupleSetup),
    Ready(route = Route.Notes),
    Failed(route = Route.StartupError),
}

data class AppUiState(
    val phase: AppPhase = AppPhase.Starting,
    val id: String? = null,
    val result: ApiResult<Unit>? = null,
    val error: Int? = null,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val observeSession: ObserveSessionUseCase,
    private val observeInitialized: ObserveSessionInitializedUseCase,
    private val refreshSession: RefreshSessionUseCase,
    private val authenticateSession: AuthenticateUseCase,
    private val logoutSession: LogoutUseCase,
    private val getEnvironment: GetEnvironmentConfigUseCase,
) : BaseViewModel<AppUiState>(initialState = AppUiState()) {
    val authUrl = getEnvironment().authUrl
    val isInitialized = observeInitialized()

    init {
        viewModelScope.launch {
            observeSession().collect { state ->
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
                                id = state.user.id,
                            )
                    }
                }
            }
        }
    }

    fun authenticate(uri: Uri) {
        val token = uri.getQueryParameter("token")

        if (token.isNullOrBlank()) {
            _uiState.update { it.copy(error = R.string.auth_missing_token) }
            return
        }

        viewModelScope.launch { authenticateSession(token = token) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun logout() {
        viewModelScope.launch { logoutSession() }
    }

    fun retry() {
        if (_uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = refreshSession()

            _uiState.update { it.copy(result = result.map { }) }
        }
    }
}
