package dev.cankolay.twodo.android.domain.usecase.api.task

import dev.cankolay.twodo.android.domain.repository.api.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(
        title: String,
        description: String,
        dueDate: LocalDate? = null,
        assignedToUserId: String? = null,
    ) = repository.create(
        title = title.trim(),
        description = description,
        dueDate = dueDate,
        assignedToUserId = assignedToUserId,
    )
}
