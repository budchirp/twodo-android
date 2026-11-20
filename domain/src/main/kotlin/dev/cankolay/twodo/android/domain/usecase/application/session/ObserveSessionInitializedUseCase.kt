package dev.cankolay.twodo.android.domain.usecase.application.session

import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class ObserveSessionInitializedUseCase @Inject constructor(private val repository: SessionRepository) {
    operator fun invoke() = repository.isInitialized
}
