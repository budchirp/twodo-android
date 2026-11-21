package dev.cankolay.twodo.android.presentation.viewmodel.task

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.domain.model.api.task.TaskLimits
import dev.cankolay.twodo.android.domain.model.api.user.User
import java.time.LocalDate

data class TasksUiState(
    val tasks: List<Task>? = null,
    val load: ApiResult<Unit>? = null,
    val toggle: ApiResult<Unit>? = null,
)

data class TaskEditorState(
    val original: Task? = null,
    val title: String = original?.title.orEmpty(),
    val description: String = original?.description.orEmpty(),
    val dueDate: LocalDate? = original?.dueDate,
    val assignedToUserId: String? = original?.assignedToUserId,
    val completed: Boolean = original?.completed ?: false,
    val validate: Boolean = false,
    val load: ApiResult<Unit>? = null,
    val save: ApiResult<Unit>? = null,
    val delete: ApiResult<Unit>? = null,
) {
    val busy: Boolean
        get() = load == ApiResult.Loading || save == ApiResult.Loading || delete == ApiResult.Loading

    val titleLength: Int
        get() = title.trim().let { it.codePointCount(0, it.length) }

    val descriptionLength: Int
        get() = description.codePointCount(0, description.length)

    val valid: Boolean
        get() = title.isNotBlank() &&
                titleLength <= TaskLimits.TITLE &&
                descriptionLength <= TaskLimits.DESCRIPTION

    val changed: Boolean
        get() = original == null ||
                title.trim() != original.title ||
                description != original.description ||
                dueDate != original.dueDate ||
                assignedToUserId != original.assignedToUserId ||
                completed != original.completed
}

data class TaskUiState(
    val editor: TaskEditorState = TaskEditorState(),
    val members: List<User> = emptyList(),
    val userId: String? = null,
    val membersLoad: ApiResult<Unit>? = null,
    val finished: Boolean = false,
)
