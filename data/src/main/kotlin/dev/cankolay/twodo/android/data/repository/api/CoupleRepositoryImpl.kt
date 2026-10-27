package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.service.CoupleService
import dev.cankolay.twodo.android.domain.repository.api.CoupleRepository
import javax.inject.Inject

class CoupleRepositoryImpl @Inject constructor(private val coupleService: CoupleService) :
    CoupleRepository {
    override suspend fun leave() = coupleService.leave()
}
