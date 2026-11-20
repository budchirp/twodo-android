package dev.cankolay.twodo.android.domain.usecase.application.session

import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke() = repository.logout()
}
