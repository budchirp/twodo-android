package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.calendar.toDto
import dev.cankolay.twodo.android.data.api.model.response.calendar.toDomain
import dev.cankolay.twodo.android.data.api.service.CalendarService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import java.time.LocalDate
import javax.inject.Inject

class CalendarRepositoryImpl
@Inject
constructor(
    private val calendarService: CalendarService
) : CalendarRepository {
    override suspend fun create(input: CalendarEntryInput) =
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

    override suspend fun getRange(startDate: LocalDate?, endDate: LocalDate?) =
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

    override suspend fun get(id: String) =
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

    override suspend fun update(id: String, input: CalendarEntryInput) =
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

    override suspend fun delete(id: String) =
        calendarService.delete(id = id)

    override suspend fun getPredictionSummary() =
        when (val result = calendarService.getPredictionSummary()) {
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
