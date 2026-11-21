package dev.cankolay.twodo.android.domain.usecase.api.calendar

import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import javax.inject.Inject

class ObserveCalendarUseCase @Inject constructor(private val repository: CalendarRepository) {
    operator fun invoke() = repository.calendar
}
