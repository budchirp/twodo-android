package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.calendar.toDto
import dev.cankolay.twodo.android.data.api.model.response.calendar.toDomain
import dev.cankolay.twodo.android.data.api.service.CalendarService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarData
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSummary
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import dev.cankolay.twodo.android.domain.repository.api.OverviewRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl
@Inject
constructor(
    private val calendarService: CalendarService,
    private val overview: OverviewRepository,
) : CalendarRepository {
    private val data = MutableStateFlow<CalendarData?>(value = null)
    override val calendar = data.asStateFlow()
    private val changes = MutableStateFlow(0L)
    override val revision = changes.asStateFlow()
    private var stale = true
    private var period: PeriodSummary? = null
    private val cacheLock = Any()
    private val requestLock = Mutex()
    private var generation = 0L

    override suspend fun clear() {
        synchronized(cacheLock) {
            generation++
            data.value = null
            period = null
            stale = true
        }
    }

    override suspend fun get(
        date: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        forceRefresh: Boolean,
    ): ApiResult<CalendarData> {
        if (!forceRefresh) {
            synchronized(cacheLock) {
                data.value
                    ?.takeIf {
                        !stale &&
                                it.date == date &&
                                it.startDate == startDate &&
                                it.endDate == endDate
                    }
                    ?.let {
                        return ApiResult.Success(data = it)
                    }
            }
        }

        val revision = changes.value

        return request(
            block = {
                calendarService
                    .getRange(date = date, startDate = startDate, endDate = endDate)
                    .map { it.toDomain(startDate = startDate, endDate = endDate) }
            },
            publish = {
                data.value = it
                stale = changes.value != revision
            },
        )
    }

    override suspend fun create(input: CalendarEntryInput): ApiResult<CalendarEntry> =
        request(
            block = {
                calendarService.create(dto = input.toDto(creating = true)).map { it.toDomain() }
            },
            publish = { invalidate() },
        )

    override suspend fun update(id: String, input: CalendarEntryInput): ApiResult<CalendarEntry> =
        request(
            block = {
                calendarService.update(id = id, dto = input.toDto(creating = false)).map {
                    it.toDomain()
                }
            },
            publish = { invalidate() },
        )

    override suspend fun delete(id: String): ApiResult<Unit> =
        request(
            block = { calendarService.delete(id = id) },
            publish = {
                data.value =
                    data.value?.let { current ->
                        current.copy(
                            entries = current.entries.filterNot { it.key == "calendar:$id" }
                        )
                    }
                invalidate()
            },
        )

    override suspend fun getPeriods(
        date: LocalDate,
        forceRefresh: Boolean,
    ): ApiResult<PeriodSummary> {
        if (!forceRefresh) {
            synchronized(cacheLock) {
                period
                    ?.takeIf { it.date == date }
                    ?.let {
                        return ApiResult.Success(data = it)
                    }
            }
        }

        return request(
            block = { calendarService.getPeriods(date = date).map { it.toDomain() } },
            publish = { period = it },
        )
    }

    override fun invalidate() {
        synchronized(cacheLock) {
            stale = true
            period = null
            changes.value++
        }
        overview.invalidate()
    }

    private suspend fun <T> request(
        block: suspend () -> ApiResult<T>,
        publish: (T) -> Unit,
    ): ApiResult<T> {
        val owner = synchronized(cacheLock) { generation }

        return requestLock.withLock {
            synchronized(cacheLock) {
                if (owner != generation) throw CancellationException("Calendar scope changed")
            }

            val result = block()
            currentCoroutineContext().ensureActive()

            synchronized(cacheLock) {
                if (owner != generation) throw CancellationException("Calendar scope changed")
                if (result is ApiResult.Success) publish(result.data)
            }

            result
        }
    }
}
