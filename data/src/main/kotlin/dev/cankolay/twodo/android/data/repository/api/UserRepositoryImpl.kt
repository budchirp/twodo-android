package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.user.UpdateProfileRequestDto
import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.data.api.service.UserService
import dev.cankolay.twodo.android.data.cache.SessionCache
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.repository.api.UserRepository
import javax.inject.Inject

class UserRepositoryImpl
@Inject
constructor(
    private val userService: UserService,
    private val sessionCache: SessionCache
) : UserRepository {
    override suspend fun initialize() = when (val result = userService.initialize()) {
        is ApiResult.Success -> ApiResult.Success(
            message = result.message,
            data = null,
            code = result.code
        )

        is ApiResult.Loading -> result

        is ApiResult.Error -> result
        is ApiResult.Fatal -> result
    }

    override suspend fun get(): ApiResult<dev.cankolay.twodo.android.domain.model.api.user.User> {
        return when (val result = userService.get()) {
            is ApiResult.Success -> {
                val user = result.data.toDomain()
                sessionCache.setUser(user)
                ApiResult.Success(
                    message = result.message,
                    data = user,
                    code = result.code
                )
            }

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun updateProfile(name: String, gender: Gender) =
        when (val result = userService.updateProfile(
            dto = UpdateProfileRequestDto(
                name = name,
                gender = gender.value
            )
        )) {
            is ApiResult.Success -> {
                val user = result.data.toDomain()
                sessionCache.setUser(user)
                ApiResult.Success(
                    message = result.message,
                    data = user,
                    code = result.code
                )
            }

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
}
