package dev.cankolay.twodo.android.domain.usecase.api.calendar

import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import java.time.LocalDate
import javax.inject.Inject

class GetCalendarEntriesUseCase
@Inject
constructor(private val calendarRepository: CalendarRepository) {
    suspend operator fun invoke(startDate: LocalDate? = null, endDate: LocalDate? = null) =
        calendarRepository.getRange(startDate = startDate, endDate = endDate)
}
