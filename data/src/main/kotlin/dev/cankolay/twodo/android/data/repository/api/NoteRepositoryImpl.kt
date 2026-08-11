package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.note.CreateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.request.note.UpdateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.note.toDomain
import dev.cankolay.twodo.android.data.api.service.CoupleService
import dev.cankolay.twodo.android.data.api.service.NoteService
import dev.cankolay.twodo.android.data.cache.SessionCache
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import javax.inject.Inject
import dev.cankolay.twodo.android.data.api.model.response.user.toDomain as userToDomain

class NoteRepositoryImpl
@Inject
constructor(
    private val noteService: NoteService,
    private val coupleService: CoupleService,
    private val sessionCache: SessionCache
) : NoteRepository {
    private var cachedNotes: List<Note>? = null

    override suspend fun create(title: String) = withCouple {
        when (val result = noteService.create(dto = CreateNoteRequestDto(title = title))) {
            is ApiResult.Success -> {
                val newNote = result.data.toDomain()
                cachedNotes =
                    listOf(newNote) + (cachedNotes.orEmpty().filterNot { it.id == newNote.id })
                ApiResult.Success(
                    message = result.message,
                    data = newNote,
                    code = result.code
                )
            }

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun getAll() = withCouple {
        when (val result = noteService.getAll()) {
            is ApiResult.Success -> {
                val notes = result.data.map { it.toDomain() }
                cachedNotes = notes
                ApiResult.Success(
                    message = result.message,
                    data = notes,
                    code = result.code
                )
            }

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun get(id: String) = withCouple {
        when (val result = noteService.get(id = id)) {
            is ApiResult.Success -> {
                val note = result.data.toDomain()
                cachedNotes =
                    cachedNotes?.map { if (it.id == note.id) note else it } ?: listOf(note)
                ApiResult.Success(
                    message = result.message,
                    data = note,
                    code = result.code
                )
            }

            is ApiResult.Loading -> result
            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun update(
        id: String,
        note: Note
    ) = withCouple {
        when (val result = noteService.update(
            id = id,
            dto = UpdateNoteRequestDto(
                title = note.title,
                content = note.content
            )
        )) {
            is ApiResult.Success -> {
                val updatedNote = result.data.toDomain()
                cachedNotes = cachedNotes?.map { if (it.id == updatedNote.id) updatedNote else it }
                ApiResult.Success(
                    message = result.message,
                    data = updatedNote,
                    code = result.code
                )
            }

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }

    override suspend fun delete(id: String) = withCouple {
        val result = noteService.delete(id = id)
        if (result is ApiResult.Success) {
            cachedNotes = cachedNotes?.filterNot { it.id == id }
        }
        result
    }

    private suspend fun <T> withCouple(block: suspend () -> ApiResult<T>): ApiResult<T> {
        if (sessionCache.isCoupleCached()) {
            return if (sessionCache.getCouple() == null) {
                ApiResult.Error(
                    message = "Create a couple before using notes.",
                    reason = ErrorReason.CLIENT,
                    code = "couple_required"
                )
            } else {
                block()
            }
        }

        return when (val result = coupleService.getMe()) {
            is ApiResult.Success -> {
                val couple = result.data?.userToDomain()
                sessionCache.setCouple(couple)
                if (couple == null) {
                    ApiResult.Error(
                        message = "Create a couple before using notes.",
                        reason = ErrorReason.CLIENT,
                        code = "couple_required"
                    )
                } else {
                    block()
                }
            }

            is ApiResult.Loading -> result

            is ApiResult.Error -> result
            is ApiResult.Fatal -> result
        }
    }
}
