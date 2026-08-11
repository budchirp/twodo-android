package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.data.api.service.CoupleService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.repository.api.CoupleRepository
import javax.inject.Inject

class CoupleRepositoryImpl
@Inject
constructor(
    private val coupleService: CoupleService
) : CoupleRepository {
    override suspend fun getMe() = when (val result = coupleService.getMe()) {
        is ApiResult.Success -> ApiResult.Success(
            message = result.message,
            data = result.data?.toDomain(),
            code = result.code
        )

        is ApiResult.Loading -> result
        is ApiResult.Error -> result
        is ApiResult.Fatal -> result
    }

    override suspend fun leave() = coupleService.leave()
}
