package dev.cankolay.twodo.android.domain.repository.application

import dev.cankolay.twodo.android.domain.model.application.AuthState
import kotlinx.coroutines.flow.Flow

interface AuthStateRepository {
    val state: Flow<AuthState>
    suspend fun update(state: AuthState)

    /** Clear only the credentials used by the rejected request. */
    suspend fun invalidate(token: String)
}
