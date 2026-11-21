package dev.cankolay.twodo.android.domain.usecase.api.couple

import dev.cankolay.twodo.android.domain.repository.api.CoupleRepository
import javax.inject.Inject

class GetCoupleUseCase @Inject constructor(private val repository: CoupleRepository) {
    suspend operator fun invoke() = repository.get()
}
