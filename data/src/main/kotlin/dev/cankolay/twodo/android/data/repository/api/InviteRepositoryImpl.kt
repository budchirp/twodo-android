package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.invite.CreateInviteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.invite.toDomain
import dev.cankolay.twodo.android.data.api.service.InviteService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.Invite
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.invite.InviteStatus
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InviteRepositoryImpl @Inject constructor(private val inviteService: InviteService) :
    InviteRepository {
    private val _invites = MutableStateFlow<List<Invite>?>(value = null)
    override val invites = _invites.asStateFlow()
    private val mutex = Mutex()

    override suspend fun clear() = mutex.withLock { _invites.value = null }

    override suspend fun getAll(forceRefresh: Boolean): ApiResult<List<Invite>> = mutex.withLock {
        if (!forceRefresh)
            _invites.value?.let {
                return@withLock ApiResult.Success(data = it)
            }
        inviteService
            .getAll()
            .map { invites -> invites.map { it.toDomain() } }
            .also { result ->
                if (result is ApiResult.Success) _invites.value = result.data
            }
    }

    override suspend fun create(username: String): ApiResult<Invite> = mutex.withLock {
        inviteService
            .create(dto = CreateInviteRequestDto(username = username))
            .map { it.toDomain() }
            .also { result ->
                if (result is ApiResult.Success) _invites.update { it?.plus(result.data) }
            }
    }

    override suspend fun handleInvite(action: InviteAction, id: String): ApiResult<Unit> =
        mutex.withLock {
            inviteService.handleInvite(action = action, id = id).also { result ->
                if (result is ApiResult.Success) {
                    _invites.update { invites ->
                        invites?.map {
                            if (it.id == id)
                                it.copy(
                                    status =
                                        if (action == InviteAction.Accept) InviteStatus.ACCEPTED
                                        else InviteStatus.REJECTED
                                )
                            else it
                        }
                    }
                }
            }
        }
}
