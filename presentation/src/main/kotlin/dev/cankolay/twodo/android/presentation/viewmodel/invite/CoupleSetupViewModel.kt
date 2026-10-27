package dev.cankolay.twodo.android.presentation.viewmodel.invite

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.Invite
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import dev.cankolay.twodo.android.domain.usecase.api.invite.HandleInviteUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.userResult
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvitePartnerFormState(val username: FormField<String> = FormField(value = "")) {
    val canSubmit: Boolean
        get() = username.value.isNotBlank()
}

data class CoupleSetupUiState(
    val user: ApiResult<User>? = null,
    val invites: ApiResult<List<Invite>>? = null,
    val result: ApiResult<Invite>? = null,
    val action: ApiResult<Unit>? = null,
    val form: InvitePartnerFormState = InvitePartnerFormState(),
    val showForm: Boolean = false,
)

@HiltViewModel
class CoupleSetupViewModel
@Inject
constructor(
    private val invites: InviteRepository,
    private val session: SessionRepository,
    private val handleInvite: HandleInviteUseCase,
) : BaseViewModel<CoupleSetupUiState>(initialState = CoupleSetupUiState()) {

    init {
        viewModelScope.launch {
            session.state.collect { state ->
                _uiState.update { it.copy(user = state.userResult()) }
            }
        }

        viewModelScope.launch {
            invites.invites.collect { data ->
                if (uiState.value.invites != ApiResult.Loading) {
                    _uiState.update {
                        it.copy(invites = data?.let { ApiResult.Success(data = it) })
                    }
                }
            }
        }

        fetch()
    }

    fun openForm() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(form = InvitePartnerFormState(), showForm = true, result = null)
        }
    }

    fun dismiss() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showForm = false, result = null) }
    }

    fun updateUsername(username: String) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(form = it.form.copy(username = it.form.username.update(value = username)))
        }
    }

    fun create() {
        if (uiState.value.result == ApiResult.Loading) return

        val username =
            uiState.value.form.username.validateRequired(error = R.string.username_required)

        _uiState.update { it.copy(form = it.form.copy(username = username)) }

        if (username.error != null) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = invites.create(username = username.value.trim())

            _uiState.update { it.copy(result = result) }
        }
    }

    fun handle(id: String, action: InviteAction) {
        if (uiState.value.action == ApiResult.Loading) return

        _uiState.update { it.copy(action = ApiResult.Loading) }

        viewModelScope.launch {
            val result = handleInvite(id = id, action = action)

            _uiState.update { it.copy(action = result) }
        }
    }

    fun fetch(forceRefresh: Boolean = false) {
        if (uiState.value.invites == ApiResult.Loading) return

        _uiState.update { it.copy(invites = ApiResult.Loading) }

        viewModelScope.launch {
            val result = invites.getAll(forceRefresh = forceRefresh)

            _uiState.update { it.copy(invites = result) }
        }
    }

    fun refresh() {
        fetch(forceRefresh = true)

        if (uiState.value.user == ApiResult.Loading) return

        _uiState.update { it.copy(user = ApiResult.Loading, action = null) }

        viewModelScope.launch {
            val result = session.refresh()

            _uiState.update { it.copy(user = result) }
        }
    }
}
