package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import kotlinx.coroutines.flow.StateFlow

interface CalendarRepository {
    val entries: StateFlow<List<CalendarEntry>?>
    suspend fun clear()
    suspend fun create(input: CalendarEntryInput): ApiResult<CalendarEntry>
    suspend fun getAll(forceRefresh: Boolean = false): ApiResult<List<CalendarEntry>>
    suspend fun update(id: String, input: CalendarEntryInput): ApiResult<CalendarEntry>
    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun getPredictionSummary(forceRefresh: Boolean = false): ApiResult<CalendarPredictionSummary>
}
