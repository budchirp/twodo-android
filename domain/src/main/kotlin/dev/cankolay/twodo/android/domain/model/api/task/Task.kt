package dev.cankolay.twodo.android.domain.model.api.task

import dev.cankolay.twodo.android.domain.model.api.user.User
import java.time.LocalDate

data class FieldChange<T>(val value: T)

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val dueDate: LocalDate? = null,
    val assignedToUserId: String? = null,
    val assignedTo: User? = null,
)

object TaskLimits {
    const val TITLE = 100
    const val DESCRIPTION = 500
}
