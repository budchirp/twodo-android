package dev.cankolay.twodo.android.presentation.viewmodel.invite

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.Invite
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.invite.CreateInviteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.GetInvitesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.HandleInviteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.ObserveInvitesUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvitePartnerFormState(val username: FormField<String> = FormField(value = "")) {
    val canSubmit: Boolean
        get() = username.value.isNotBlank()
}

data class InviteActionRequest(val id: String, val action: InviteAction)

data class CoupleSetupUiState(
    val user: User? = null,
    val userLoad: ApiResult<Unit>? = null,
    val invites: List<Invite>? = null,
    val invitesLoad: ApiResult<Unit>? = null,
    val result: ApiResult<Unit>? = null,
    val action: ApiResult<Unit>? = null,
    val pendingAction: InviteActionRequest? = null,
    val form: InvitePartnerFormState = InvitePartnerFormState(),
    val showForm: Boolean = false,
)

@HiltViewModel
class CoupleSetupViewModel @Inject constructor(
    private val observeInvites: ObserveInvitesUseCase,
    private val getInvites: GetInvitesUseCase,
    private val createInvite: CreateInviteUseCase,
    private val observeSession: ObserveSessionUseCase,
    private val refreshSession: RefreshSessionUseCase,
    private val handleInvite: HandleInviteUseCase,
) : BaseViewModel<CoupleSetupUiState>(
    initialState = CoupleSetupUiState(
        user = (observeSession().value as? SessionState.Active)?.user,
        invites = observeInvites().value
    )
) {

    private var invitesJob: Job? = null
    private var userJob: Job? = null

    init {
        viewModelScope.launch {
            observeSession().collect { session ->
                _uiState.update {
                    when (session) {
                        is SessionState.Active -> it.copy(
                            user = session.user,
                            userLoad = if (it.user == null && it.userLoad == ApiResult.Loading)
                                ApiResult.Success(data = Unit) else it.userLoad,
                        )

                        is SessionState.Failed -> it.copy(userLoad = ApiResult.Failure(error = session.error))
                        SessionState.Starting -> CoupleSetupUiState(userLoad = ApiResult.Loading)
                        SessionState.SignedOut -> CoupleSetupUiState()
                        SessionState.ProfileRequired -> it.copy(user = null, userLoad = null)
                    }
                }
            }
        }

        viewModelScope.launch {
            observeInvites().collect { data ->
                _uiState.update {
                    it.copy(
                        invites = data,
                        invitesLoad = if (data == null && it.invites != null) null else it.invitesLoad,
                    )
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
            val result = createInvite(username = username.value.trim())

            _uiState.update { it.copy(result = result.map { }) }
        }
    }

    fun handle(id: String, action: InviteAction) {
        if (uiState.value.action == ApiResult.Loading) return

        _uiState.update {
            it.copy(
                action = ApiResult.Loading,
                pendingAction = InviteActionRequest(id = id, action = action),
            )
        }

        viewModelScope.launch {
            val result = handleInvite(id = id, action = action)

            _uiState.update { it.copy(action = result) }
        }
    }

    fun retryAction() {
        val pending = uiState.value.pendingAction ?: return
        handle(id = pending.id, action = pending.action)
    }

    fun fetch(forceRefresh: Boolean = false) {
        if (invitesJob?.isActive == true) return

        if (uiState.value.invitesLoad == ApiResult.Loading) return

        _uiState.update { it.copy(invitesLoad = ApiResult.Loading) }

        invitesJob = viewModelScope.launch {
            val result = getInvites(forceRefresh = forceRefresh)

            _uiState.update { it.copy(invitesLoad = result.map { }) }
        }
    }

    fun refresh() {
        fetch(forceRefresh = true)
        refreshUser()
    }

    fun refreshUser() {
        if (userJob?.isActive == true) return

        if (uiState.value.userLoad == ApiResult.Loading) return

        _uiState.update { it.copy(userLoad = ApiResult.Loading) }

        userJob = viewModelScope.launch {
            val result = refreshSession()

            _uiState.update { it.copy(userLoad = result.map { }) }
        }
    }
}
