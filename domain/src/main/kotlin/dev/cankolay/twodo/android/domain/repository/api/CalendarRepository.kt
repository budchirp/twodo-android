package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodPrediction
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodTrackerSummary
import java.time.LocalDate

interface CalendarRepository {
    suspend fun create(input: CalendarEntryInput): ApiResult<CalendarEntry>
    suspend fun getRange(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): ApiResult<List<CalendarEntry>>

    suspend fun get(id: String): ApiResult<CalendarEntry>
    suspend fun update(id: String, input: CalendarEntryInput): ApiResult<CalendarEntry>
    suspend fun delete(id: String): ApiResult<Nothing?>
    suspend fun getPeriodTrackerSummary(): ApiResult<PeriodTrackerSummary>
    suspend fun getPeriodTrackerPrediction(): ApiResult<PeriodPrediction>
}
