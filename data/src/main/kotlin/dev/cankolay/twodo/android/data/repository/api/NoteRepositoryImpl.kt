package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.note.CreateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.request.note.UpdateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.note.toDomain
import dev.cankolay.twodo.android.data.api.service.NoteService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import javax.inject.Inject

class NoteRepositoryImpl
@Inject
constructor(
    private val noteService: NoteService
) : NoteRepository {

    override suspend fun create(title: String) =
        when (val result = noteService.create(dto = CreateNoteRequestDto(title = title))) {
            is ApiResult.Success -> ApiResult.Success(
                message = result.message,
                data = result.data.toDomain(),
                code = result.code
            )

            is ApiResult.Loading -> result
            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }

    override suspend fun getAll() =
        when (val result = noteService.getAll()) {
            is ApiResult.Success -> ApiResult.Success(
                message = result.message,
                data = result.data.map { it.toDomain() },
                code = result.code
            )

            is ApiResult.Loading -> result
            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }

    override suspend fun get(id: String) =
        when (val result = noteService.get(id = id)) {
            is ApiResult.Success -> ApiResult.Success(
                message = result.message,
                data = result.data.toDomain(),
                code = result.code
            )

            is ApiResult.Loading -> result
            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }

    override suspend fun update(
        id: String,
        note: Note
    ) = when (val result = noteService.update(
        id = id,
        dto = UpdateNoteRequestDto(
            title = note.title,
            content = note.content
        )
    )) {
        is ApiResult.Success -> ApiResult.Success(
            message = result.message,
            data = result.data.toDomain(),
            code = result.code
        )

        is ApiResult.Loading -> result
        is ApiResult.Error -> result
        is ApiResult.Fatal -> result
    }

    override suspend fun delete(id: String) =
        noteService.delete(id = id)
}
