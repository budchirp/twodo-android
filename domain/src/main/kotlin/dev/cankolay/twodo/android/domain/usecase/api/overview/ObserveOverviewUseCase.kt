package dev.cankolay.twodo.android.domain.usecase.api.overview

import dev.cankolay.twodo.android.domain.repository.api.OverviewRepository
import javax.inject.Inject

class ObserveOverviewUseCase @Inject constructor(private val repository: OverviewRepository) {
    operator fun invoke() = repository.overview
}
