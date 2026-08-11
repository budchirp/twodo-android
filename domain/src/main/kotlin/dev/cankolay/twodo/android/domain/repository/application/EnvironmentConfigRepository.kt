package dev.cankolay.twodo.android.domain.repository.application

import dev.cankolay.twodo.android.domain.model.application.EnvironmentConfig

interface EnvironmentConfigRepository {
    fun get(): EnvironmentConfig
}
