package dev.cankolay.twodo.android.domain.usecase.api.task

import dev.cankolay.twodo.android.domain.model.api.task.FieldChange
import dev.cankolay.twodo.android.domain.repository.api.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(private val repository: TaskRepository) {
    suspend operator fun invoke(
        id: String,
        title: String? = null,
        description: String? = null,
        completed: Boolean? = null,
        dueDate: FieldChange<LocalDate?>? = null,
        assignedToUserId: FieldChange<String?>? = null,
    ) = repository.update(
        id = id,
        title = title?.trim(),
        description = description,
        completed = completed,
        dueDate = dueDate,
        assignedToUserId = assignedToUserId,
    )
}
