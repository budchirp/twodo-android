package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.data.api.service.CoupleService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.repository.api.CoupleRepository
import dev.cankolay.twodo.android.domain.repository.api.OverviewRepository
import java.time.LocalDate
import javax.inject.Inject

class CoupleRepositoryImpl @Inject constructor(
    private val coupleService: CoupleService,
    private val overview: OverviewRepository,
) : CoupleRepository {
    override suspend fun get() = coupleService.get().map { it.toDomain() }

    override suspend fun updateRelationshipDate(date: LocalDate?) =
        coupleService.updateRelationshipDate(date).map { it.toDomain() }.also {
            if (it is ApiResult.Success) overview.invalidate()
        }

    override suspend fun leave() = coupleService.leave()
}
