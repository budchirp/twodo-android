package dev.cankolay.twodo.android.domain.repository.api

import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import kotlinx.coroutines.flow.StateFlow

interface NoteRepository {
    val notes: StateFlow<List<Note>?>
    suspend fun clear()
    suspend fun create(title: String): ApiResult<Note>

    suspend fun getAll(forceRefresh: Boolean = false): ApiResult<List<Note>>
    suspend fun get(id: String, forceRefresh: Boolean = false): ApiResult<Note>

    suspend fun update(
        id: String,
        note: Note
    ): ApiResult<Note>

    suspend fun delete(id: String): ApiResult<Unit>
}
