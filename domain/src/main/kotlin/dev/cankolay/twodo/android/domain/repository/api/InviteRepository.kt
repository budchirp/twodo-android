package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.Invite
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import kotlinx.coroutines.flow.StateFlow

interface InviteRepository {
    val invites: StateFlow<List<Invite>?>
    suspend fun clear()
    suspend fun create(username: String): ApiResult<Invite>

    suspend fun getAll(forceRefresh: Boolean = false): ApiResult<List<Invite>>

    suspend fun handleInvite(action: InviteAction, id: String): ApiResult<Unit>
}
