package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarData
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSummary
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface CalendarRepository {
    val calendar: StateFlow<CalendarData?>
    val revision: StateFlow<Long>

    fun invalidate()

    suspend fun clear()

    suspend fun create(input: CalendarEntryInput): ApiResult<CalendarEntry>

    suspend fun get(
        date: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        forceRefresh: Boolean = false,
    ): ApiResult<CalendarData>

    suspend fun update(id: String, input: CalendarEntryInput): ApiResult<CalendarEntry>

    suspend fun delete(id: String): ApiResult<Unit>

    suspend fun getPeriods(date: LocalDate, forceRefresh: Boolean = false): ApiResult<PeriodSummary>
}
