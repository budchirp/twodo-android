package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.note.CreateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.request.note.UpdateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.note.toDomain
import dev.cankolay.twodo.android.data.api.service.NoteService
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import javax.inject.Inject

class NoteRepositoryImpl
@Inject
constructor(
    private val noteService: NoteService
) : NoteRepository {

    override suspend fun create(title: String) =
        noteService.create(dto = CreateNoteRequestDto(title = title)).map { it.toDomain() }

    override suspend fun getAll() =
        noteService.getAll().map { notes -> notes.map { it.toDomain() } }

    override suspend fun get(id: String) =
        noteService.get(id = id).map { it.toDomain() }

    override suspend fun update(
        id: String,
        note: Note
    ) = noteService.update(
        id = id,
        dto = UpdateNoteRequestDto(
            title = note.title,
            content = note.content
        )
    ).map { it.toDomain() }

    override suspend fun delete(id: String) =
        noteService.delete(id = id)
}
