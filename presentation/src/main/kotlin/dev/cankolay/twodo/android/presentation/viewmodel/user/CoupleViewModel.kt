package dev.cankolay.twodo.android.presentation.viewmodel.user

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.couple.LeaveCoupleUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoupleUiState(
    val user: User? = null,
    val load: ApiResult<Unit>? = null,
    val result: ApiResult<Unit>? = null,
    val showLeave: Boolean = false,
)

@HiltViewModel
class CoupleViewModel @Inject constructor(
    private val observeSession: ObserveSessionUseCase,
    private val refreshSession: RefreshSessionUseCase,
    private val leaveCouple: LeaveCoupleUseCase,
) : BaseViewModel<CoupleUiState>(
    initialState = CoupleUiState(user = (observeSession().value as? SessionState.Active)?.user)
) {

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            observeSession().collect { session ->
                _uiState.update {
                    when (session) {
                        is SessionState.Active -> it.copy(
                            user = session.user,
                            load = if (it.user == null && it.load == ApiResult.Loading)
                                ApiResult.Success(data = Unit) else it.load,
                        )

                        is SessionState.Failed -> it.copy(load = ApiResult.Failure(error = session.error))
                        SessionState.Starting -> CoupleUiState(load = ApiResult.Loading)
                        SessionState.SignedOut -> CoupleUiState()
                        SessionState.ProfileRequired -> it.copy(user = null, load = null)
                    }
                }
            }
        }
    }

    fun confirm() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showLeave = true, result = null) }
    }

    fun dismiss() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showLeave = false, result = null) }
    }

    fun refresh() {
        if (loadJob?.isActive == true) return

        if (uiState.value.load == ApiResult.Loading) return

        _uiState.update { it.copy(load = ApiResult.Loading) }

        loadJob = viewModelScope.launch {
            val result = refreshSession()

            _uiState.update { it.copy(load = result.map { }) }
        }
    }

    fun leave() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = leaveCouple()

            _uiState.update { it.copy(result = result) }
        }
    }
}
