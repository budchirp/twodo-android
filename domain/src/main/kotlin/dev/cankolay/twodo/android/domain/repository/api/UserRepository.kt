package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User

interface UserRepository {
    suspend fun initialize(): ApiResult<User>
    suspend fun get(): ApiResult<User>
    suspend fun updateProfile(name: String, gender: Gender): ApiResult<User>
}
