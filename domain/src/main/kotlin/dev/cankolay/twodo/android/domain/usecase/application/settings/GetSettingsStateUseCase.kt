package dev.cankolay.twodo.android.domain.usecase.application.settings

import dev.cankolay.twodo.android.domain.repository.application.SettingsStateRepository
import javax.inject.Inject

class GetSettingsStateUseCase @Inject constructor(private val repository: SettingsStateRepository) {
    operator fun invoke() = repository.state
}
