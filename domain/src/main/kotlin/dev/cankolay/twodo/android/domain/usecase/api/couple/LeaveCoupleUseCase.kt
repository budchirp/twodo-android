package dev.cankolay.twodo.android.domain.usecase.api.couple

import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class LeaveCoupleUseCase @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke() = repository.leaveCouple()
}
