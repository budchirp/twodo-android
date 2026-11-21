package dev.cankolay.twodo.android.domain.usecase.api.task

import dev.cankolay.twodo.android.domain.repository.api.TaskRepository
import javax.inject.Inject

class GetTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(id: String) = repository.get(id = id)
}
