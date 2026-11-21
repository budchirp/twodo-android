package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.FieldChange
import dev.cankolay.twodo.android.domain.model.api.task.Task
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface TaskRepository {
    val tasks: StateFlow<List<Task>?>

    suspend fun clear()
    suspend fun getAll(forceRefresh: Boolean = false): ApiResult<List<Task>>
    suspend fun get(id: String): ApiResult<Task>
    suspend fun create(
        title: String,
        description: String,
        dueDate: LocalDate? = null,
        assignedToUserId: String? = null,
    ): ApiResult<Task>

    suspend fun update(
        id: String,
        title: String? = null,
        description: String? = null,
        completed: Boolean? = null,
        dueDate: FieldChange<LocalDate?>? = null,
        assignedToUserId: FieldChange<String?>? = null,
    ): ApiResult<Task>

    suspend fun delete(id: String): ApiResult<Unit>
}
