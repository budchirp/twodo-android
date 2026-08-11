package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.invite.CreateInviteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.invite.toDomain
import dev.cankolay.twodo.android.data.api.service.InviteService
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import javax.inject.Inject

class InviteRepositoryImpl
@Inject
constructor(val inviteService: InviteService) : InviteRepository {
    override suspend fun create(
        username: String
    ) = inviteService.create(dto = CreateInviteRequestDto(username = username)).map { null }

    override suspend fun getAll() =
        inviteService.getAll().map { invites -> invites.map { it.toDomain() } }

    override suspend fun handleInvite(
        action: InviteAction,
        id: String
    ) = inviteService.handleInvite(action = action, id = id)
}
