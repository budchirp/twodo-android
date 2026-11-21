package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.overview.Overview
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface OverviewRepository {
    val overview: StateFlow<Overview?>
    val revision: StateFlow<Long>
    suspend fun get(date: LocalDate): ApiResult<Overview>
    fun invalidate()
    fun clear()
}
