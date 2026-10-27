package dev.cankolay.twodo.android.domain.repository.application

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.SessionState
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {
    val state: StateFlow<SessionState>
    val isInitialized: StateFlow<Boolean>
    suspend fun authenticate(token: String)
    suspend fun logout()
    suspend fun refresh(): ApiResult<User>
    suspend fun updateProfile(name: String, gender: Gender): ApiResult<User>
    suspend fun leaveCouple(): ApiResult<Unit>
}
