package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.task.CreateTaskRequestDto
import dev.cankolay.twodo.android.data.api.model.response.task.toDomain
import dev.cankolay.twodo.android.data.api.service.TaskService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.task.FieldChange
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import dev.cankolay.twodo.android.domain.repository.api.TaskRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl
@Inject
constructor(private val service: TaskService, private val calendar: CalendarRepository) :
    TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>?>(value = null)
    override val tasks = _tasks.asStateFlow()

    private val requestLock = Mutex()
    private val cacheLock = Any()
    private var generation = 0L

    // Invalidate immediately, even while an old account/couple request is in flight.
    override suspend fun clear() {
        synchronized(cacheLock) {
            generation++
            _tasks.value = null
        }
    }

    override suspend fun getAll(forceRefresh: Boolean): ApiResult<List<Task>> {
        if (!forceRefresh) {
            _tasks.value?.let {
                return ApiResult.Success(data = it)
            }
        }

        return request(
            block = { service.getAll().map { list -> list.map { it.toDomain() } } },
            publish = { _tasks.value = it },
        )
    }

    override suspend fun get(id: String): ApiResult<Task> =
        request(block = { service.get(id = id).map { it.toDomain() } }, publish = ::replace)

    override suspend fun create(
        title: String,
        description: String,
        dueDate: LocalDate?,
        assignedToUserId: String?,
    ): ApiResult<Task> =
        request(
            block = {
                service
                    .create(
                        dto =
                            CreateTaskRequestDto(
                                title = title,
                                description = description,
                                dueDate = dueDate?.toString(),
                                assignedToUserId = assignedToUserId,
                            )
                    )
                    .map { it.toDomain() }
            },
            publish = { task ->
                calendar.invalidate()
                _tasks.value = _tasks.value?.let { listOf(task) + it }
            },
        )

    override suspend fun update(
        id: String,
        title: String?,
        description: String?,
        completed: Boolean?,
        dueDate: FieldChange<LocalDate?>?,
        assignedToUserId: FieldChange<String?>?,
    ): ApiResult<Task> =
        request(
            block = {
                service
                    .update(
                        id = id,
                        dto =
                            buildJsonObject {
                                title?.let { put("title", it) }
                                description?.let { put("description", it) }
                                completed?.let { put("completed", it) }
                                dueDate?.let {
                                    put(
                                        "dueDate",
                                        it.value?.toString()?.let(::JsonPrimitive) ?: JsonNull,
                                    )
                                }
                                assignedToUserId?.let {
                                    put(
                                        "assignedToUserId",
                                        it.value?.let(::JsonPrimitive) ?: JsonNull,
                                    )
                                }
                            },
                    )
                    .map { it.toDomain() }
            },
            publish = {
                replace(it)
                calendar.invalidate()
            },
        )

    override suspend fun delete(id: String): ApiResult<Unit> =
        request(
            block = { service.delete(id = id) },
            publish = {
                calendar.invalidate()
                _tasks.value = _tasks.value?.filterNot { it.id == id }
            },
        )

    private fun replace(task: Task) {
        _tasks.value = _tasks.value?.map { if (it.id == task.id) task else it }
    }

    private suspend fun <T> request(
        block: suspend () -> ApiResult<T>,
        publish: (T) -> Unit,
    ): ApiResult<T> {
        val owner = synchronized(cacheLock) { generation }

        return requestLock.withLock {
            currentCoroutineContext().ensureActive()

            synchronized(cacheLock) {
                if (owner != generation) throw CancellationException("Task scope changed")
            }

            val result = block()
            currentCoroutineContext().ensureActive()

            synchronized(cacheLock) {
                if (owner != generation) throw CancellationException("Task scope changed")

                if (result is ApiResult.Success) publish(result.data)
            }

            result
        }
    }
}
