package dev.cankolay.twodo.android.presentation.viewmodel.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class NoteEditorUiState(
    val note: ApiResult<Note>? = null,
    val draft: Note? = null,
    val save: ApiResult<Note>? = null,
    val delete: ApiResult<Unit>? = null,
    val close: ApiResult<Unit>? = null,
    val showActions: Boolean = false,
    val showDelete: Boolean = false,
) {
    val changed: Boolean
        get() {
            val current = draft ?: return false

            val saved = (note as? ApiResult.Success)?.data ?: return true

            return current.title != saved.title || current.content != saved.content
        }
}

@HiltViewModel
class NoteEditorViewModel
@Inject
constructor(
    private val notes: NoteRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<NoteEditorUiState>(initialState = NoteEditorUiState()) {
    private val saveMutex = Mutex()

    private var saveJob: Job? = null

    private var loadJob: Job? = null

    private fun schedule() {
        saveJob?.cancel()

        saveJob = viewModelScope.launch {
            delay(timeMillis = 700L)

            // New edits cancel the delay, not an in-flight write.
            viewModelScope.launch { saveDraft() }
        }
    }

    fun load(id: String) {
        if (loadJob?.isActive == true || uiState.value.draft?.id == id) return

        _uiState.update { it.copy(note = ApiResult.Loading) }

        loadJob = viewModelScope.launch {
            val result = notes.get(id = id)

            val saved = (result as? ApiResult.Success)?.data

            val draft = saved?.let {
                if (savedStateHandle.get<String>("noteId") == id)
                    it.copy(
                        title = savedStateHandle.get<String>("title") ?: it.title,
                        content = savedStateHandle.get<String>("content") ?: it.content,
                    )
                else it
            }

            _uiState.update {
                it.copy(
                    note = result,
                    draft = draft,
                )
            }

            if (uiState.value.changed) schedule()
        }
    }

    fun updateTitle(title: String) = updateDraft { copy(title = title) }

    fun updateContent(content: String) = updateDraft { copy(content = content) }

    private fun updateDraft(transform: Note.() -> Note) {
        if (
            uiState.value.close == ApiResult.Loading ||
            uiState.value.close is ApiResult.Success ||
            uiState.value.delete != null
        )
            return

        val draft = uiState.value.draft ?: return

        val updated = draft.transform()

        if (updated == draft) return

        _uiState.update { it.copy(draft = updated) }

        savedStateHandle["noteId"] = updated.id
        savedStateHandle["title"] = updated.title
        savedStateHandle["content"] = updated.content

        schedule()
    }

    fun openActions() {
        _uiState.update { it.copy(showActions = true) }
    }

    fun askDelete() {
        _uiState.update { it.copy(showActions = false, showDelete = true, delete = null) }
    }

    fun dismiss() {
        if (uiState.value.delete == ApiResult.Loading) return

        _uiState.update { it.copy(showActions = false, showDelete = false, delete = null) }
    }

    fun close() {
        if (uiState.value.close == ApiResult.Loading || uiState.value.delete != null) return

        _uiState.update { it.copy(close = ApiResult.Loading) }

        viewModelScope.launch {
            val result = saveDraft()

            _uiState.update { it.copy(close = result) }
        }
    }

    fun save() {
        if (uiState.value.close == ApiResult.Loading || uiState.value.delete != null) return

        viewModelScope.launch { saveDraft() }
    }

    fun delete() {
        if (uiState.value.delete == ApiResult.Loading) return

        val id = uiState.value.draft?.id ?: return

        _uiState.update { it.copy(delete = ApiResult.Loading) }

        viewModelScope.launch {
            saveMutex.withLock {
                val result = notes.delete(id = id)

                _uiState.update {
                    it.copy(
                        delete = result,
                        showDelete =
                            when (result) {
                                is ApiResult.Success -> false

                                else -> it.showDelete
                            },
                    )
                }

                if (result is ApiResult.Success) clearSavedDraft()
            }
        }
    }

    private suspend fun saveDraft(): ApiResult<Unit> = saveMutex.withLock {
        if (uiState.value.delete != null || uiState.value.close is ApiResult.Success)
            return@withLock ApiResult.Success(data = Unit)

        while (uiState.value.changed && uiState.value.delete == null) {
            val draft = uiState.value.draft ?: break

            _uiState.update { it.copy(save = ApiResult.Loading) }

            when (val result = notes.update(id = draft.id, note = draft)) {
                ApiResult.Loading -> return@withLock ApiResult.Loading

                is ApiResult.Failure -> {
                    _uiState.update { it.copy(save = result) }

                    return@withLock result
                }

                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            note = result,
                            save = result,
                            draft = if (it.draft == draft) result.data else it.draft,
                        )
                    }

                    if (!uiState.value.changed) clearSavedDraft()
                }
            }
        }

        if (!uiState.value.changed) {
            _uiState.update { it.copy(save = it.note) }

            clearSavedDraft()
        }

        ApiResult.Success(data = Unit)
    }

    private fun clearSavedDraft() {
        savedStateHandle.remove<String>(key = "noteId")
        savedStateHandle.remove<String>(key = "title")
        savedStateHandle.remove<String>(key = "content")
    }
}
