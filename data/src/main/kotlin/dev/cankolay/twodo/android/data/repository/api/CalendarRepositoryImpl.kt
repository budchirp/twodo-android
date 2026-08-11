package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.calendar.toDto
import dev.cankolay.twodo.android.data.api.model.response.calendar.toDomain
import dev.cankolay.twodo.android.data.api.service.CalendarService
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import java.time.LocalDate
import javax.inject.Inject

class CalendarRepositoryImpl
@Inject
constructor(
    private val calendarService: CalendarService
) : CalendarRepository {
    override suspend fun create(input: CalendarEntryInput) =
        calendarService.create(dto = input.toDto()).map { it.toDomain() }

    override suspend fun getRange(startDate: LocalDate?, endDate: LocalDate?) =
        calendarService.getRange(startDate = startDate, endDate = endDate)
            .map { entries -> entries.map { it.toDomain() } }

    override suspend fun get(id: String) =
        calendarService.get(id = id).map { it.toDomain() }

    override suspend fun update(id: String, input: CalendarEntryInput) =
        calendarService.update(id = id, dto = input.toDto()).map { it.toDomain() }

    override suspend fun delete(id: String) =
        calendarService.delete(id = id)

    override suspend fun getPredictionSummary() =
        calendarService.getPredictionSummary().map { it.toDomain() }
}
