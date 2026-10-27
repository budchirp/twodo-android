package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.calendar.toDto
import dev.cankolay.twodo.android.data.api.model.response.calendar.toDomain
import dev.cankolay.twodo.android.data.api.service.CalendarService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(private val calendarService: CalendarService) :
    CalendarRepository {
    private val _entries = MutableStateFlow<List<CalendarEntry>?>(value = null)
    override val entries = _entries.asStateFlow()
    private var prediction: CalendarPredictionSummary? = null
    private val mutex = Mutex()

    override suspend fun clear() = mutex.withLock {
        _entries.value = null
        prediction = null
    }

    override suspend fun getAll(forceRefresh: Boolean): ApiResult<List<CalendarEntry>> =
        mutex.withLock {
            if (!forceRefresh)
                _entries.value?.let {
                    return@withLock ApiResult.Success(data = it)
                }
            calendarService
                .getRange()
                .map { entries -> entries.map { it.toDomain() } }
                .also { result ->
                    if (result is ApiResult.Success) {
                        _entries.value = result.data
                        prediction = null
                    }
                }
        }

    override suspend fun create(input: CalendarEntryInput): ApiResult<CalendarEntry> =
        mutex.withLock {
            calendarService
                .create(dto = input.toDto())
                .map { it.toDomain() }
                .also { result ->
                    if (result is ApiResult.Success) {
                        _entries.update { it?.plus(result.data) }
                        prediction = null
                    }
                }
        }

    override suspend fun update(id: String, input: CalendarEntryInput): ApiResult<CalendarEntry> =
        mutex.withLock {
            calendarService
                .update(id = id, dto = input.toDto())
                .map { it.toDomain() }
                .also { result ->
                    if (result is ApiResult.Success) {
                        _entries.update { entries ->
                            entries?.map { if (it.id == id) result.data else it }
                        }
                        prediction = null
                    }
                }
        }

    override suspend fun delete(id: String): ApiResult<Unit> = mutex.withLock {
        calendarService.delete(id = id).also { result ->
            if (result is ApiResult.Success) {
                _entries.update { entries -> entries?.filterNot { it.id == id } }
                prediction = null
            }
        }
    }

    override suspend fun getPredictionSummary(
        forceRefresh: Boolean
    ): ApiResult<CalendarPredictionSummary> = mutex.withLock {
        if (!forceRefresh)
            prediction?.let {
                return@withLock ApiResult.Success(data = it)
            }
        calendarService
            .getPredictionSummary()
            .map { it.toDomain() }
            .also { result ->
                if (result is ApiResult.Success) prediction = result.data
            }
    }
}
