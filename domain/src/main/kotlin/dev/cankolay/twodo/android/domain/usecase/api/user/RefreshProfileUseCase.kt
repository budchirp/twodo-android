package dev.cankolay.twodo.android.domain.usecase.api.user

import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class RefreshProfileUseCase @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke() = repository.refreshProfile()
}
