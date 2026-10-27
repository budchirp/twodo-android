package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult

interface CoupleRepository {
    suspend fun leave(): ApiResult<Unit>
}
