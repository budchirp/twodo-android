package dev.cankolay.twodo.android.domain.usecase.api.calendar

import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import javax.inject.Inject

class DeleteCalendarEntryUseCase @Inject constructor(private val repository: CalendarRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id = id)
}
