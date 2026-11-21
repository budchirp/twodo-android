package dev.cankolay.twodo.android.domain.usecase.api.couple

import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import java.time.LocalDate
import javax.inject.Inject

class UpdateRelationshipDateUseCase @Inject constructor(private val repository: SessionRepository) {
    suspend operator fun invoke(date: LocalDate?) = repository.updateRelationshipDate(date = date)
}
