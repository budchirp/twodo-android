package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.client.requestUnit
import dev.cankolay.twodo.android.data.api.model.request.task.CreateTaskRequestDto
import dev.cankolay.twodo.android.data.api.model.response.task.TaskDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

class TaskService @Inject constructor(private val client: KtorClient) {
    suspend fun getAll() = request<List<TaskDto>> {
        client().get {
            url { path(ApiConstants.Endpoints.TASKS) }
        }
    }

    suspend fun get(id: String) = request<TaskDto> {
        client().get {
            url { path(ApiConstants.Endpoints.TASKS, id) }
        }
    }

    suspend fun create(dto: CreateTaskRequestDto) = request<TaskDto> {
        client().post {
            url { path(ApiConstants.Endpoints.TASKS) }
            setBody(body = dto)
        }
    }

    suspend fun update(id: String, dto: JsonObject) = request<TaskDto> {
        client().patch {
            url { path(ApiConstants.Endpoints.TASKS, id) }
            setBody(body = dto)
        }
    }

    suspend fun delete(id: String) = requestUnit {
        client().delete {
            url { path(ApiConstants.Endpoints.TASKS, id) }
        }
    }
}
