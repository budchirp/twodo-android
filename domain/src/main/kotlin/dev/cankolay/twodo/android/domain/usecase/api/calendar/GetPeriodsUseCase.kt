package dev.cankolay.twodo.android.domain.usecase.api.calendar

import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import java.time.LocalDate
import javax.inject.Inject

class GetPeriodsUseCase @Inject constructor(private val repository: CalendarRepository) {
    suspend operator fun invoke(date: LocalDate, forceRefresh: Boolean = false) =
        repository.getPeriods(date = date, forceRefresh = forceRefresh)
}
