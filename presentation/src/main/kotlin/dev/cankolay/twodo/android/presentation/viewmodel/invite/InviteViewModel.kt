package dev.cankolay.twodo.android.presentation.viewmodel.invite

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.Invite
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.api.onSuccess
import dev.cankolay.twodo.android.domain.usecase.api.invite.CreateInviteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.GetInvitesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.invite.HandleInviteUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.UiEvent
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import javax.inject.Inject

data class InvitePartnerFormState(
    val username: FormField<String> = FormField("")
) {
    val canSubmit = username.value.isNotBlank()
}

sealed interface InviteSheet {
    data object None : InviteSheet
    data class InvitePartner(val form: InvitePartnerFormState) : InviteSheet
}

data class InviteUiState(
    val invitesResult: ApiResult<List<Invite>> = ApiResult.Loading,
    val activeSheet: InviteSheet = InviteSheet.None
) {
    val invites: List<Invite>? get() = invitesResult.dataOrNull
    val isLoading: Boolean get() = invitesResult.isLoading
    val error: String? get() = invitesResult.errorMessage
}

@HiltViewModel
class InviteViewModel @Inject constructor(
    private val createInviteUseCase: CreateInviteUseCase,
    private val getInvitesUseCase: GetInvitesUseCase,
    private val handleInviteUseCase: HandleInviteUseCase
) : BaseViewModel<InviteUiState>(InviteUiState()) {

    init {
        fetchInvites()
    }

    fun openInvitePartnerSheet() {
        updateState { copy(activeSheet = InviteSheet.InvitePartner(InvitePartnerFormState())) }
    }

    fun dismissSheet() {
        updateState { copy(activeSheet = InviteSheet.None) }
    }

    fun updateInviteUsername(username: String) {
        updateState {
            val sheet = activeSheet as? InviteSheet.InvitePartner ?: return@updateState this
            copy(
                activeSheet = InviteSheet.InvitePartner(
                    sheet.form.copy(username = sheet.form.username.update(username.trim()))
                )
            )
        }
    }

    fun submitInvite() {
        val form = (uiState.value.activeSheet as? InviteSheet.InvitePartner)?.form ?: return
        val username = form.username.validateRequired(error = R.string.username_required)
        if (username.error != null) {
            updateState { copy(activeSheet = InviteSheet.InvitePartner(form.copy(username = username))) }
            return
        }

        dismissSheet()
        launchOnce("create-invite") {
            createInviteUseCase(username.value.trim())
                .onSuccess { fetchInvites() }
                .onError { message, _ -> sendEvent(UiEvent.ShowSnackbar(message)) }
        }
    }

    fun handleInvite(id: String, action: InviteAction) {
        launchOnce("handle-invite:$id") {
            handleInviteUseCase(id, action)
                .onSuccess {
                    fetchInvites()
                    if (action == InviteAction.Accept) sendEvent(UiEvent.InviteAccepted)
                }
                .onError { message, _ -> sendEvent(UiEvent.ShowSnackbar(message)) }
        }
    }

    fun fetchInvites() {
        launchOnce("invites") {
            updateState { copy(invitesResult = ApiResult.Loading) }
            val result = getInvitesUseCase()
            updateState { copy(invitesResult = result) }
        }
    }
}
