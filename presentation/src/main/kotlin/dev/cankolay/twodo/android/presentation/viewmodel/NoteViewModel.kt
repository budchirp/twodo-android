package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.usecase.api.note.CreateNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.DeleteNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.GetNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.GetNotesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.UpdateNoteUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import javax.inject.Inject

data class CreateNoteFormState(
    val title: FormField<String> = FormField(value = "")
) {
    val canSubmit = title.value.isNotBlank()
}

data class NoteUiState(
    val notes: List<Note>? = null,
    val note: Note? = null,
    val noteDraft: Note? = null,
    val createNoteForm: CreateNoteFormState? = null,
    val isNoteActionsSheetVisible: Boolean = false,
    val isDeleteNoteSheetVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val errorCode: String? = null
)

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val getNoteUseCase: GetNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState = _uiState.asStateFlow()
    private var saveNoteDraftJob: Job? = null

    fun openCreateNoteSheet() {
        _uiState.update { it.copy(createNoteForm = CreateNoteFormState()) }
    }

    fun dismissCreateNoteSheet() {
        _uiState.update { it.copy(createNoteForm = null) }
    }

    fun updateCreateNoteTitle(title: String) {
        _uiState.update { state ->
            state.createNoteForm?.let { form ->
                state.copy(createNoteForm = form.copy(title = form.title.update(value = title)))
            } ?: state
        }
    }

    suspend fun submitCreateNote(): ApiResult<Note> {
        val form =
            _uiState.value.createNoteForm ?: return validationError(message = "Title is required.")
        val title = form.title.validateRequired(error = R.string.title_required)
        if (title.error != null) {
            _uiState.update { it.copy(createNoteForm = form.copy(title = title)) }
            return validationError(message = "Title is required.")
        }

        _uiState.update { it.copy(createNoteForm = form.copy(title = title)) }
        return createNote(title = title.value.trim())
    }

    fun updateNoteDraftTitle(title: String) {
        val draft = _uiState.value.noteDraft ?: return
        if (draft.title == title) return

        _uiState.update {
            it.copy(
                noteDraft = draft.copy(
                    title = title,
                    updatedAt = OffsetDateTime.now().toString()
                )
            )
        }
        scheduleNoteDraftSave()
    }

    fun updateNoteDraftContent(content: String) {
        val draft = _uiState.value.noteDraft ?: return
        if (draft.content == content) return

        _uiState.update {
            it.copy(
                noteDraft = draft.copy(
                    content = content,
                    updatedAt = OffsetDateTime.now().toString()
                )
            )
        }
        scheduleNoteDraftSave()
    }

    fun openNoteActionsSheet() {
        _uiState.update { it.copy(isNoteActionsSheetVisible = true) }
    }

    fun dismissNoteActionsSheet() {
        _uiState.update { it.copy(isNoteActionsSheetVisible = false) }
    }

    fun requestDeleteNote() {
        _uiState.update {
            it.copy(
                isNoteActionsSheetVisible = false,
                isDeleteNoteSheetVisible = true
            )
        }
    }

    fun dismissDeleteNoteSheet() {
        _uiState.update { it.copy(isDeleteNoteSheetVisible = false) }
    }

    fun fetchNotes() {
        viewModelScope.launch {
            if (_uiState.value.notes == null) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            when (val result = getNotesUseCase()) {
                is ApiResult.Error -> _uiState.update {
                    it.copy(error = result.message, errorCode = result.code)
                }

                is ApiResult.Fatal -> _uiState.update {
                    it.copy(error = result.exception.messageOrDefault(), errorCode = null)
                }

                is ApiResult.Success -> _uiState.update {
                    it.copy(notes = result.data, error = null, errorCode = null)
                }

                else -> Unit
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun fetchNote(id: String) {
        saveNoteDraftJob?.cancel()
        viewModelScope.launch {
            val existingNote = _uiState.value.notes?.find { it.id == id }
            if (existingNote != null && _uiState.value.note?.id != id) {
                _uiState.update {
                    it.copy(
                        note = existingNote,
                        noteDraft = existingNote,
                        error = null
                    )
                }
            } else if (_uiState.value.note?.id != id) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            when (val result = getNoteUseCase(id = id)) {
                is ApiResult.Error -> _uiState.update {
                    it.copy(error = result.message, errorCode = result.code)
                }

                is ApiResult.Fatal -> _uiState.update {
                    it.copy(error = result.exception.messageOrDefault(), errorCode = null)
                }

                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        note = result.data,
                        noteDraft = result.data,
                        error = null,
                        errorCode = null
                    )
                }

                else -> Unit
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun createNote(title: String): ApiResult<Note> {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val result = createNoteUseCase(title = title.trim())
        when (result) {
            is ApiResult.Error -> _uiState.update {
                it.copy(error = result.message, errorCode = result.code)
            }

            is ApiResult.Fatal -> _uiState.update {
                it.copy(error = result.exception.messageOrDefault(), errorCode = null)
            }

            is ApiResult.Success -> _uiState.update { state ->
                state.copy(
                    notes = state.notes.orEmpty() + result.data,
                    note = result.data,
                    noteDraft = result.data,
                    error = null,
                    errorCode = null
                )
            }

            else -> Unit
        }

        _uiState.update { it.copy(isLoading = false) }
        return result
    }

    suspend fun updateNote(id: String, note: Note): ApiResult<Note> {
        _uiState.update { it.copy(isSaving = true, error = null) }

        val result = updateNoteUseCase(id = id, note = note)
        when (result) {
            is ApiResult.Error -> _uiState.update {
                it.copy(error = result.message, errorCode = result.code)
            }

            is ApiResult.Fatal -> _uiState.update {
                it.copy(error = result.exception.messageOrDefault(), errorCode = null)
            }

            is ApiResult.Success -> _uiState.update { state ->
                state.copy(
                    notes = state.notes?.map { item ->
                        if (item.id == id) result.data else item
                    },
                    note = result.data,
                    error = null,
                    errorCode = null
                )
            }

            else -> Unit
        }

        _uiState.update { it.copy(isSaving = false) }
        return result
    }

    suspend fun deleteNote(id: String): ApiResult<Nothing?> {
        saveNoteDraftJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }

        val result = deleteNoteUseCase(id = id)
        when (result) {
            is ApiResult.Error -> _uiState.update {
                it.copy(error = result.message, errorCode = result.code)
            }

            is ApiResult.Fatal -> _uiState.update {
                it.copy(error = result.exception.messageOrDefault(), errorCode = null)
            }

            is ApiResult.Success -> _uiState.update { state ->
                state.copy(
                    notes = state.notes?.filterNot { it.id == id },
                    note = null,
                    noteDraft = null,
                    isNoteActionsSheetVisible = false,
                    isDeleteNoteSheetVisible = false,
                    error = null,
                    errorCode = null
                )
            }

            else -> Unit
        }

        _uiState.update { it.copy(isLoading = false) }
        return result
    }

    private fun scheduleNoteDraftSave() {
        saveNoteDraftJob?.cancel()
        saveNoteDraftJob = viewModelScope.launch {
            delay(timeMillis = 500)
            val draft = _uiState.value.noteDraft ?: return@launch
            updateNote(id = draft.id, note = draft)
        }
    }

    private fun validationError(message: String) = ApiResult.Error(
        message = message,
        reason = ErrorReason.CLIENT,
        code = "validation_error"
    )
}

private fun Throwable.messageOrDefault() =
    localizedMessage ?: message ?: "Unexpected error"
