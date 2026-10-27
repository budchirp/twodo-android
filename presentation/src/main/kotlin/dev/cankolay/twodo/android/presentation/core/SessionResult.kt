package dev.cankolay.twodo.android.presentation.core

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.SessionState

fun SessionState.userResult(): ApiResult<User>? =
    when (this) {
        is SessionState.Active -> ApiResult.Success(data = user)

        is SessionState.Failed -> ApiResult.Failure(error = error)

        SessionState.Starting -> ApiResult.Loading

        else -> null
    }
