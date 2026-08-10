package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.data.api.service.CoupleService
import dev.cankolay.twodo.android.data.cache.SessionCache
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.repository.api.CoupleRepository
import javax.inject.Inject

class CoupleRepositoryImpl
@Inject
constructor(
    private val coupleService: CoupleService,
    private val sessionCache: SessionCache
) : CoupleRepository {
    override suspend fun getMe() = when (val result = coupleService.getMe()) {
        is ApiResult.Success -> {
            val couple = result.data?.toDomain()
            sessionCache.setCouple(couple)
            ApiResult.Success(
                message = result.message,
                data = couple,
                code = result.code
            )
        }

        is ApiResult.Loading -> result

        is ApiResult.Error -> result
        is ApiResult.Fatal -> result
    }

    override suspend fun leave() = when (val result = coupleService.leave()) {
        is ApiResult.Success -> {
            sessionCache.setCouple(null)
            result
        }

        else -> result
    }
}
