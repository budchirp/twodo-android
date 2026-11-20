package dev.cankolay.twodo.android.domain.usecase.api.calendar

import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import javax.inject.Inject

class GetCalendarPredictionSummaryUseCase @Inject constructor(private val repository: CalendarRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false) =
        repository.getPredictionSummary(forceRefresh = forceRefresh)
}
