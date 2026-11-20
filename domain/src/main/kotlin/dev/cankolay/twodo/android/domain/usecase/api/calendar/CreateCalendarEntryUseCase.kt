package dev.cankolay.twodo.android.domain.usecase.api.calendar

import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import javax.inject.Inject

class CreateCalendarEntryUseCase @Inject constructor(private val repository: CalendarRepository) {
    suspend operator fun invoke(input: CalendarEntryInput) = repository.create(input = input)
}
