package dev.cankolay.twodo.android.presentation.viewmodel.user

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.userResult
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(private val session: SessionRepository) :
    BaseViewModel<ApiResult<User>?>(initialState = session.state.value.userResult()) {

    init {
        viewModelScope.launch {
            session.state.collect { state -> _uiState.value = state.userResult() }
        }
    }

    fun refresh() {
        if (uiState.value == ApiResult.Loading) return

        _uiState.value = ApiResult.Loading

        viewModelScope.launch {
            _uiState.value = session.refresh()
        }
    }
}
