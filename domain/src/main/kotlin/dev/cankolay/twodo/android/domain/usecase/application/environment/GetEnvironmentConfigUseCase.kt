package dev.cankolay.twodo.android.domain.usecase.application.environment

import dev.cankolay.twodo.android.domain.repository.application.EnvironmentConfigRepository
import javax.inject.Inject

class GetEnvironmentConfigUseCase @Inject constructor(private val repository: EnvironmentConfigRepository) {
    operator fun invoke() = repository.get()
}
