package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.calendar.toDto
import dev.cankolay.twodo.android.data.api.model.response.calendar.toDomain
import dev.cankolay.twodo.android.data.api.service.CalendarService
import dev.cankolay.twodo.android.data.api.service.CoupleService
import dev.cankolay.twodo.android.data.cache.SessionCache
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import java.time.LocalDate
import javax.inject.Inject
import dev.cankolay.twodo.android.data.api.model.response.user.toDomain as userToDomain

class CalendarRepositoryImpl
@Inject
constructor(
    private val calendarService: CalendarService,
    private val coupleService: CoupleService,
    private val sessionCache: SessionCache
) : CalendarRepository {
    override suspend fun create(input: CalendarEntryInput) = withCouple {
        when (val result = calendarService.create(dto = input.toDto())) {
            is ApiResult.Success -> ApiResult.Success(
                message = result.message,
                data = result.data.toDomain(),
                code = result.code
            )

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun getRange(startDate: LocalDate?, endDate: LocalDate?) = withCouple {
        when (val result = calendarService.getRange(startDate = startDate, endDate = endDate)) {
            is ApiResult.Success -> ApiResult.Success(
                message = result.message,
                data = result.data.map { it.toDomain() },
                code = result.code
            )

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun get(id: String) = withCouple {
        when (val result = calendarService.get(id = id)) {
            is ApiResult.Success -> ApiResult.Success(
                message = result.message,
                data = result.data.toDomain(),
                code = result.code
            )

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun update(id: String, input: CalendarEntryInput) = withCouple {
        when (val result = calendarService.update(id = id, dto = input.toDto())) {
            is ApiResult.Success -> ApiResult.Success(
                message = result.message,
                data = result.data.toDomain(),
                code = result.code
            )

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun delete(id: String) = withCouple {
        calendarService.delete(id = id)
    }

    private suspend fun <T> withCouple(block: suspend () -> ApiResult<T>): ApiResult<T> {
        if (sessionCache.isCoupleCached()) {
            return if (sessionCache.getCouple() == null) {
                ApiResult.Error(
                    message = "Create a couple before using the calendar.",
                    reason = ErrorReason.CLIENT,
                    code = "couple_required"
                )
            } else {
                block()
            }
        }

        return when (val result = coupleService.getMe()) {
            is ApiResult.Success -> {
                val couple = result.data?.userToDomain()
                sessionCache.setCouple(couple)
                if (couple == null) {
                    ApiResult.Error(
                        message = "Create a couple before using the calendar.",
                        reason = ErrorReason.CLIENT,
                        code = "couple_required"
                    )
                } else {
                    block()
                }
            }

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }
}
