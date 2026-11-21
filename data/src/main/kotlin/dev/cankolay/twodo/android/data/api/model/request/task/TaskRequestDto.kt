package dev.cankolay.twodo.android.data.api.model.request.task

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequestDto(
    val title: String,
    val description: String,
    val dueDate: String? = null,
    val assignedToUserId: String? = null,
)

