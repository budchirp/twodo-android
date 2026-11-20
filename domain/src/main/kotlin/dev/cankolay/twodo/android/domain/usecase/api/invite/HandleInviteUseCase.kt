package dev.cankolay.twodo.android.domain.usecase.api.invite

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class HandleInviteUseCase
@Inject
constructor(
    private val inviteRepository: InviteRepository,
    private val sessionRepository: SessionRepository,
) {
    // A successful acceptance must not be resubmitted if only the session refresh failed.
    private val awaitingSessionRefresh = mutableSetOf<String>()

    suspend operator fun invoke(id: String, action: InviteAction): ApiResult<Unit> {
        if (action != InviteAction.Accept || id !in awaitingSessionRefresh) {
            val result = inviteRepository.handleInvite(action = action, id = id)
            if (result !is ApiResult.Success || action != InviteAction.Accept) return result
            awaitingSessionRefresh.add(id)
        }

        val refreshed = sessionRepository.refresh()
        if (refreshed is ApiResult.Success) awaitingSessionRefresh.remove(id)
        return refreshed.map { }
    }
}
