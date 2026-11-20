package dev.cankolay.twodo.android.domain.usecase.application.session

import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class AuthenticateUseCase @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke(token: String) = repository.authenticate(token = token)
}
