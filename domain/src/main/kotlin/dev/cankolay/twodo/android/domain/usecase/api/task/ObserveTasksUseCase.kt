package dev.cankolay.twodo.android.domain.usecase.api.task

import dev.cankolay.twodo.android.domain.repository.api.TaskRepository
import javax.inject.Inject

class ObserveTasksUseCase @Inject constructor(private val repository: TaskRepository) {
    operator fun invoke() = repository.tasks
}
