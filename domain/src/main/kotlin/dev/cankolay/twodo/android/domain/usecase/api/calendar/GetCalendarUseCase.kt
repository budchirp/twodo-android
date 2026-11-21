package dev.cankolay.twodo.android.domain.usecase.api.calendar

import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import java.time.LocalDate
import javax.inject.Inject

class GetCalendarUseCase @Inject constructor(private val repository: CalendarRepository) {
    suspend operator fun invoke(
        date: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        forceRefresh: Boolean = false,
    ) = repository.get(
        date = date,
        startDate = startDate,
        endDate = endDate,
        forceRefresh = forceRefresh
    )
}
