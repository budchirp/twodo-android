package dev.cankolay.twodo.android.presentation.viewmodel.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.usecase.api.note.DeleteNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.GetNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.UpdateNoteUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class NoteEditorUiState(
    val note: Note? = null,
    val load: ApiResult<Unit>? = null,
    val draft: Note? = null,
    val save: ApiResult<Unit>? = null,
    val delete: ApiResult<Unit>? = null,
    val close: ApiResult<Unit>? = null,
    val showActions: Boolean = false,
    val showDelete: Boolean = false,
) {
    val changed: Boolean
        get() {
            val current = draft ?: return false

            val saved = note ?: return true

            return current.title != saved.title || current.content != saved.content
        }
}

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val getNote: GetNoteUseCase,
    private val updateNote: UpdateNoteUseCase,
    private val deleteNote: DeleteNoteUseCase,
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

    fun load(id: String, forceRefresh: Boolean = false) {
        if (loadJob?.isActive == true || (!forceRefresh && uiState.value.draft?.id == id)) return

        _uiState.update {
            if (it.draft != null && it.draft.id != id) NoteEditorUiState(load = ApiResult.Loading)
            else it.copy(load = ApiResult.Loading)
        }

        loadJob = viewModelScope.launch {
            val result = getNote(id = id, forceRefresh = forceRefresh)

            val saved = (result as? ApiResult.Success)?.data

            val draft = uiState.value.draft ?: saved?.let {
                if (savedStateHandle.get<String>("noteId") == id)
                    it.copy(
                        title = savedStateHandle.get<String>("title") ?: it.title,
                        content = savedStateHandle.get<String>("content") ?: it.content,
                    )
                else it
            }

            _uiState.update {
                it.copy(
                    note = saved ?: it.note,
                    load = result.map { },
                    draft = draft ?: it.draft,
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
        if (
            uiState.value.save == ApiResult.Loading ||
            uiState.value.close == ApiResult.Loading || uiState.value.delete != null
        ) return

        _uiState.update { it.copy(save = ApiResult.Loading) }
        viewModelScope.launch { saveDraft() }
    }

    fun delete() {
        if (uiState.value.delete == ApiResult.Loading) return

        val id = uiState.value.draft?.id ?: return

        _uiState.update { it.copy(delete = ApiResult.Loading) }

        viewModelScope.launch {
            saveMutex.withLock {
                val result = deleteNote(id = id)

                _uiState.update {
                    it.copy(
                        delete = result,
                        showDelete = result !is ApiResult.Success && it.showDelete,
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

            when (val result = updateNote(id = draft.id, note = draft)) {
                ApiResult.Loading -> return@withLock ApiResult.Loading

                is ApiResult.Failure -> {
                    _uiState.update { it.copy(save = result) }

                    return@withLock result
                }

                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            note = result.data,
                            save = result.map { Unit },
                            draft = if (it.draft == draft) result.data else it.draft,
                        )
                    }

                    if (!uiState.value.changed) clearSavedDraft()
                }
            }
        }

        if (!uiState.value.changed) {
            _uiState.update { it.copy(save = if (it.note != null) ApiResult.Success(data = Unit) else null) }

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
