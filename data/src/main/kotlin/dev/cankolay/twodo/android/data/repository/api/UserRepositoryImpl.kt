package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.user.UpdateProfileRequestDto
import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.data.api.service.UserService
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.repository.api.UserRepository
import javax.inject.Inject

class UserRepositoryImpl
@Inject
constructor(
    private val userService: UserService
) : UserRepository {
    override suspend fun initialize() = userService.initialize().map { null }

    override suspend fun get() = userService.get().map { it.toDomain() }

    override suspend fun updateProfile(name: String, gender: Gender) =
        userService.updateProfile(
            dto = UpdateProfileRequestDto(
                name = name,
                gender = gender.value
            )
        ).map { it.toDomain() }
}
