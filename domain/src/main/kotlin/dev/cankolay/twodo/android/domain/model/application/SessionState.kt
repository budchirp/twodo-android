package dev.cankolay.twodo.android.domain.model.application

import dev.cankolay.twodo.android.domain.model.api.ApiFailure
import dev.cankolay.twodo.android.domain.model.api.user.User

sealed interface SessionState {
    data object Starting : SessionState
    data object SignedOut : SessionState
    data object ProfileRequired : SessionState
    data class Active(val user: User) : SessionState
    data class Failed(val error: ApiFailure) : SessionState
}
