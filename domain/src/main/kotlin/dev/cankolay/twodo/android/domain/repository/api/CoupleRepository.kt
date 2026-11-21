package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.Couple
import java.time.LocalDate

interface CoupleRepository {
    suspend fun get(): ApiResult<Couple>
    suspend fun updateRelationshipDate(date: LocalDate?): ApiResult<Couple>
    suspend fun leave(): ApiResult<Unit>
}
