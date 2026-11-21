package dev.cankolay.twodo.android.data.api.model.response.task

import dev.cankolay.twodo.android.data.api.model.response.user.UserSummaryDto
import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.domain.model.api.task.Task
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val dueDate: String? = null,
    val assignedToUserId: String? = null,
    val assignedTo: UserSummaryDto? = null,
)

fun TaskDto.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    completed = completed,
    createdAt = createdAt,
    updatedAt = updatedAt,
    dueDate = dueDate?.let(LocalDate::parse),
    assignedToUserId = assignedToUserId,
    assignedTo = assignedTo?.toDomain(),
)
