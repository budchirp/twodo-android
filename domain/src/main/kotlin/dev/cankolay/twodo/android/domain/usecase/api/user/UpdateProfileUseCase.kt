package dev.cankolay.twodo.android.domain.usecase.api.user

import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke(name: String, gender: Gender) =
        repository.updateProfile(name = name, gender = gender)
}
