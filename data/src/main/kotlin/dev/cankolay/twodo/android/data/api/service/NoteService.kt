package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.model.request.note.CreateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.request.note.UpdateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.note.NoteDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import javax.inject.Inject

class NoteService
@Inject
constructor(private val client: KtorClient) {
    suspend fun create(dto: CreateNoteRequestDto) = request<NoteDto> {
        client().post {
            url {
                path(ApiConstants.Endpoints.NOTES)
            }

            setBody(body = dto)
        }
    }

    suspend fun getAll() = request<List<NoteDto>> {
        client().get {
            url {
                path(ApiConstants.Endpoints.NOTES)
            }
        }
    }

    suspend fun get(id: String) = request<NoteDto> {
        client().get {
            url {
                path(ApiConstants.Endpoints.NOTES, id)
            }
        }
    }

    suspend fun update(id: String, dto: UpdateNoteRequestDto) = request<NoteDto> {
        client().patch {
            url {
                path(ApiConstants.Endpoints.NOTES, id)
            }

            setBody(body = dto)
        }
    }

    suspend fun delete(id: String) = request(noReturn = true) {
        client().delete {
            url {
                path(ApiConstants.Endpoints.NOTES, id)
            }
        }
    }
}
