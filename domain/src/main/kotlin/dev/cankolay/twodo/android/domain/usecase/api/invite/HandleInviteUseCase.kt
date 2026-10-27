package dev.cankolay.twodo.android.domain.usecase.api.invite

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class HandleInviteUseCase
@Inject
constructor(
    private val inviteRepository: InviteRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(id: String, action: InviteAction): ApiResult<Unit> {
        val result = inviteRepository.handleInvite(action = action, id = id)
        if (result !is ApiResult.Success || action != InviteAction.Accept) return result

        return when (val refreshed = sessionRepository.refresh()) {
            ApiResult.Loading -> ApiResult.Loading

            is ApiResult.Failure -> refreshed

            is ApiResult.Success -> ApiResult.Success(data = Unit)
        }
    }
}
