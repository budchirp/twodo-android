package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.getOrNull
import dev.cankolay.twodo.android.domain.model.api.invite.Invite
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.api.onSuccess
import dev.cankolay.twodo.android.domain.model.api.validationError
import dev.cankolay.twodo.android.domain.usecase.api.invite.CreateInviteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.GetInvitesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.HandleInviteUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvitePartnerFormState(
    val username: FormField<String> = FormField(value = "")
) {
    val canSubmit = username.value.isNotBlank()
}

sealed interface InviteSheet {
    data object None : InviteSheet
    data class InvitePartner(val form: InvitePartnerFormState) : InviteSheet
}

data class InviteUiState(
    val invitesResult: ApiResult<List<Invite>> = ApiResult.Loading,
    val activeSheet: InviteSheet = InviteSheet.None,
    val actionResult: ApiResult<*>? = null
) {
    val invites: List<Invite>? get() = invitesResult.getOrNull()
    val isLoading: Boolean get() = invitesResult.isLoading || actionResult?.isLoading == true
    val error: String? get() = actionResult?.errorMessage ?: invitesResult.errorMessage
    val inviteForm: InvitePartnerFormState? get() = (activeSheet as? InviteSheet.InvitePartner)?.form
}

@HiltViewModel
class InviteViewModel @Inject constructor(
    private val createInviteUseCase: CreateInviteUseCase,
    private val getInvitesUseCase: GetInvitesUseCase,
    private val handleInviteUseCase: HandleInviteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(InviteUiState())
    val uiState = _uiState.asStateFlow()
    private var fetchInvitesJob: Job? = null

    fun openInvitePartnerSheet() {
        _uiState.update {
            it.copy(
                activeSheet = InviteSheet.InvitePartner(InvitePartnerFormState()),
                actionResult = null
            )
        }
    }

    fun dismissInvitePartnerSheet() {
        _uiState.update { it.copy(activeSheet = InviteSheet.None) }
    }

    fun updateInviteUsername(username: String) {
        _uiState.update { state ->
            val form =
                (state.activeSheet as? InviteSheet.InvitePartner)?.form ?: return@update state
            state.copy(
                activeSheet = InviteSheet.InvitePartner(
                    form.copy(username = form.username.update(username.trim()))
                )
            )
        }
    }

    suspend fun submitInvite(): ApiResult<Nothing?> {
        val form = (_uiState.value.activeSheet as? InviteSheet.InvitePartner)?.form
            ?: return validationError(message = "Username is required.")
        val username = form.username.validateRequired(error = R.string.username_required)
        if (username.error != null) {
            _uiState.update { it.copy(activeSheet = InviteSheet.InvitePartner(form.copy(username = username))) }
            return validationError(message = "Username is required.")
        }

        _uiState.update { it.copy(activeSheet = InviteSheet.InvitePartner(form.copy(username = username))) }
        return createInvite(username = username.value.trim())
    }

    fun fetchInvites() {
        if (fetchInvitesJob?.isActive == true) return

        fetchInvitesJob = viewModelScope.launch {
            refreshInvites()
        }
    }

    suspend fun createInvite(username: String): ApiResult<Nothing?> {
        if (_uiState.value.isLoading) return ApiResult.Loading

        _uiState.update { it.copy(actionResult = ApiResult.Loading) }

        val result = createInviteUseCase(username = username.trim())
        result.onSuccess {
            _uiState.update { state -> state.copy(actionResult = null) }
            refreshInvites(updateLoading = false)
        }.onError { _, _ ->
            _uiState.update { state -> state.copy(actionResult = result) }
        }

        return result
    }

    suspend fun handleInvite(id: String, action: InviteAction): ApiResult<Nothing?> {
        if (_uiState.value.isLoading) return ApiResult.Loading

        _uiState.update { it.copy(actionResult = ApiResult.Loading) }

        val result = handleInviteUseCase(id = id, action = action)
        result.onSuccess {
            _uiState.update { state -> state.copy(actionResult = null) }
            refreshInvites(updateLoading = false)
        }.onError { _, _ ->
            _uiState.update { state -> state.copy(actionResult = result) }
        }

        return result
    }

    private suspend fun refreshInvites(updateLoading: Boolean = true): ApiResult<List<Invite>> {
        if (updateLoading) {
            _uiState.update { it.copy(invitesResult = ApiResult.Loading) }
        }

        val result = getInvitesUseCase()
        _uiState.update { state ->
            state.copy(
                invitesResult = result
            )
        }

        return result
    }
}
