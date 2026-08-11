package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.invite.Invite
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.usecase.api.invite.CreateInviteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.GetInvitesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.HandleInviteUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import dev.cankolay.twodo.android.presentation.state.UiStatus
import dev.cankolay.twodo.android.presentation.state.errorMessage
import dev.cankolay.twodo.android.presentation.state.isLoading
import dev.cankolay.twodo.android.presentation.state.onError
import dev.cankolay.twodo.android.presentation.state.onSuccess
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
    val invites: List<Invite>? = null,
    val activeSheet: InviteSheet = InviteSheet.None,
    val status: UiStatus = UiStatus.Idle
) {
    val isLoading: Boolean get() = status.isLoading
    val error: String? get() = status.errorMessage
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

    fun openInvitePartnerSheet() {
        _uiState.update {
            it.copy(
                activeSheet = InviteSheet.InvitePartner(InvitePartnerFormState()),
                status = UiStatus.Idle
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
                    form.copy(
                        username = form.username.update(
                            username.trim()
                        )
                    )
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
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            refreshInvites()
        }
    }

    suspend fun createInvite(username: String): ApiResult<Nothing?> {
        if (_uiState.value.isLoading) return ApiResult.Loading

        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = createInviteUseCase(username = username.trim())
            .onSuccess {
                refreshInvites(updateLoading = false)
            }
            .onError { msg, _ ->
                _uiState.update { it.copy(status = UiStatus.Error(msg)) }
            }

        _uiState.update { it.copy(status = UiStatus.Idle) }
        return result
    }

    suspend fun handleInvite(id: String, action: InviteAction): ApiResult<Nothing?> {
        if (_uiState.value.isLoading) return ApiResult.Loading

        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = handleInviteUseCase(id = id, action = action)
            .onSuccess {
                refreshInvites(updateLoading = false)
            }
            .onError { msg, _ ->
                _uiState.update { it.copy(status = UiStatus.Error(msg)) }
            }

        _uiState.update { it.copy(status = UiStatus.Idle) }
        return result
    }

    private suspend fun refreshInvites(updateLoading: Boolean = true): ApiResult<List<Invite>> {
        if (updateLoading) {
            _uiState.update { it.copy(status = UiStatus.Loading) }
        }

        val result = getInvitesUseCase()
            .onSuccess { list ->
                _uiState.update { it.copy(invites = list) }
            }
            .onError { msg, _ ->
                _uiState.update { it.copy(status = UiStatus.Error(msg)) }
            }

        if (updateLoading) {
            _uiState.update { it.copy(status = UiStatus.Idle) }
        }

        return result
    }

    private fun validationError(message: String) = ApiResult.Error(
        message = message,
        reason = ErrorReason.CLIENT,
        code = "validation_error"
    )
}
