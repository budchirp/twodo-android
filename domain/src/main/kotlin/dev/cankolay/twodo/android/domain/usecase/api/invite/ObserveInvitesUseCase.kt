package dev.cankolay.twodo.android.domain.usecase.api.invite

import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import javax.inject.Inject

class ObserveInvitesUseCase @Inject constructor(private val repository: InviteRepository) {
    operator fun invoke() = repository.invites
}
