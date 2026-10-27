package dev.cankolay.twodo.android.presentation.viewmodel.user

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.userResult
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoupleUiState(
    val user: ApiResult<User>? = null,
    val result: ApiResult<Unit>? = null,
    val showLeave: Boolean = false,
)

@HiltViewModel
class CoupleViewModel @Inject constructor(private val session: SessionRepository) :
    BaseViewModel<CoupleUiState>(initialState = CoupleUiState()) {

    init {
        viewModelScope.launch {
            session.state.collect { state ->
                _uiState.update { it.copy(user = state.userResult()) }
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
        if (uiState.value.user == ApiResult.Loading) return

        _uiState.update { it.copy(user = ApiResult.Loading) }

        viewModelScope.launch {
            val result = session.refresh()

            _uiState.update { it.copy(user = result) }
        }
    }

    fun leave() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = session.leaveCouple()

            _uiState.update { it.copy(result = result) }
        }
    }
}
