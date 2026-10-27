package dev.cankolay.twodo.android.data.repository.api

import dev.cankolay.twodo.android.data.api.model.request.note.CreateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.request.note.UpdateNoteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.note.toDomain
import dev.cankolay.twodo.android.data.api.service.NoteService
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(private val noteService: NoteService) :
    NoteRepository {
    private val _notes = MutableStateFlow<List<Note>?>(value = null)
    override val notes = _notes.asStateFlow()
    private val details = mutableMapOf<String, Note>()
    private val mutex = Mutex()

    override suspend fun clear() = mutex.withLock {
        details.clear()
        _notes.value = null
    }

    override suspend fun getAll(forceRefresh: Boolean): ApiResult<List<Note>> = mutex.withLock {
        if (!forceRefresh)
            _notes.value?.let {
                return@withLock ApiResult.Success(data = it)
            }
        noteService
            .getAll()
            .map { notes -> notes.map { it.toDomain() } }
            .also { result ->
                if (result is ApiResult.Success) {
                    details.clear()
                    details.putAll(result.data.associateBy { it.id })
                    _notes.value = result.data
                }
            }
    }

    override suspend fun get(id: String, forceRefresh: Boolean): ApiResult<Note> = mutex.withLock {
        if (!forceRefresh)
            details[id]?.let {
                return@withLock ApiResult.Success(data = it)
            }
        noteService
            .get(id = id)
            .map { it.toDomain() }
            .also { result ->
                if (result is ApiResult.Success) details[id] = result.data
            }
    }

    override suspend fun create(title: String): ApiResult<Note> = mutex.withLock {
        noteService
            .create(dto = CreateNoteRequestDto(title = title))
            .map { it.toDomain() }
            .also { result ->
                if (result is ApiResult.Success) {
                    details[result.data.id] = result.data
                    _notes.update { it?.plus(result.data) }
                }
            }
    }

    override suspend fun update(id: String, note: Note): ApiResult<Note> = mutex.withLock {
        noteService
            .update(id = id, dto = UpdateNoteRequestDto(title = note.title, content = note.content))
            .map { it.toDomain() }
            .also { result ->
                if (result is ApiResult.Success) {
                    details[id] = result.data
                    _notes.update { notes -> notes?.map { if (it.id == id) result.data else it } }
                }
            }
    }

    override suspend fun delete(id: String): ApiResult<Unit> = mutex.withLock {
        noteService.delete(id = id).also { result ->
            if (result is ApiResult.Success) {
                details.remove(key = id)
                _notes.update { notes -> notes?.filterNot { it.id == id } }
            }
        }
    }
}
