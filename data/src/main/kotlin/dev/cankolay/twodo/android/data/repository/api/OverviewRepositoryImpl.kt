package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.response.overview.toDomain
import dev.cankolay.twodo.android.data.api.service.OverviewService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.overview.Overview
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
class OverviewRepositoryImpl @Inject constructor(private val service: OverviewService) :
    OverviewRepository {
    private val data = MutableStateFlow<Overview?>(value = null)
    override val overview = data.asStateFlow()
    private val changes = MutableStateFlow(value = 0L)
    override val revision = changes.asStateFlow()
    private val cacheLock = Any()
    private val requestLock = Mutex()
    private var generation = 0L

    override fun clear() {
        synchronized(cacheLock) {
            generation++
            data.value = null
            changes.value++
        }
    }

    override fun invalidate() {
        synchronized(cacheLock) { changes.value++ }
    }

    override suspend fun get(date: LocalDate): ApiResult<Overview> {
        val owner = synchronized(cacheLock) { generation }

        return requestLock.withLock {
            val result = service.get(date = date).map { it.toDomain() }
            currentCoroutineContext().ensureActive()

            synchronized(cacheLock) {
                if (owner != generation) throw CancellationException("Overview scope changed")
                if (result is ApiResult.Success) data.value = result.data
            }

            result
        }
    }
}
