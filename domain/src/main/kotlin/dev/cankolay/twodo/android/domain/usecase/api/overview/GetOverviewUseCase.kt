package dev.cankolay.twodo.android.domain.usecase.api.overview

import dev.cankolay.twodo.android.domain.repository.api.OverviewRepository
import java.time.LocalDate
import javax.inject.Inject

class GetOverviewUseCase @Inject constructor(private val repository: OverviewRepository) {
    suspend operator fun invoke(date: LocalDate) = repository.get(date = date)
}
