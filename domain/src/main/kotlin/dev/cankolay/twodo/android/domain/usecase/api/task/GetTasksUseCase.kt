package dev.cankolay.twodo.android.domain.usecase.api.task

import dev.cankolay.twodo.android.domain.repository.api.TaskRepository
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(forceRefresh: Boolean = false) =
        repository.getAll(forceRefresh = forceRefresh)
}
