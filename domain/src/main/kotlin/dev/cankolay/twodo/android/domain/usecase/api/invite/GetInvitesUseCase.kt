package dev.cankolay.twodo.android.domain.usecase.api.invite

import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import javax.inject.Inject

class GetInvitesUseCase @Inject constructor(private val repository: InviteRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false) =
        repository.getAll(forceRefresh = forceRefresh)
}
