package dev.cankolay.twodo.android.domain.usecase.api.invite

import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import javax.inject.Inject

class CreateInviteUseCase @Inject constructor(private val repository: InviteRepository) {
    suspend operator fun invoke(username: String) = repository.create(username = username)
}
