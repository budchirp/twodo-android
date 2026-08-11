package dev.cankolay.twodo.android.domain.usecase.application.environment

import dev.cankolay.twodo.android.domain.model.application.EnvironmentConfig
import dev.cankolay.twodo.android.domain.repository.application.EnvironmentConfigRepository
import javax.inject.Inject

class GetEnvironmentConfigUseCase @Inject constructor(
    private val repository: EnvironmentConfigRepository
) {
    operator fun invoke(): EnvironmentConfig = repository.get()
}
