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
import dev.cankolay.twodo.android.presentation.state.UiStatus
import dev.cankolay.twodo.android.presentation.state.errorMessage
import dev.cankolay.twodo.android.presentation.state.isLoading
import dev.cankolay.twodo.android.presentation.state.onError
import dev.cankolay.twodo.android.presentation.state.onSuccess
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

sealed interface NoteSheet {
    data object None : NoteSheet
    data class CreateNote(val form: CreateNoteFormState) : NoteSheet
    data object NoteActions : NoteSheet
    data class DeleteConfirmation(val noteId: String) : NoteSheet
}

data class NoteUiState(
    val notes: List<Note>? = null,
    val note: Note? = null,
    val noteDraft: Note? = null,
    val activeSheet: NoteSheet = NoteSheet.None,
    val status: UiStatus = UiStatus.Idle
) {
    val isLoading: Boolean get() = status.isLoading
    val error: String? get() = status.errorMessage
}

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
        _uiState.update { it.copy(activeSheet = NoteSheet.CreateNote(CreateNoteFormState())) }
    }

    fun dismissCreateNoteSheet() {
        _uiState.update { it.copy(activeSheet = NoteSheet.None) }
    }

    fun updateCreateNoteTitle(title: String) {
        _uiState.update { state ->
            val form = (state.activeSheet as? NoteSheet.CreateNote)?.form ?: return@update state
            state.copy(activeSheet = NoteSheet.CreateNote(form.copy(title = form.title.update(value = title))))
        }
    }

    suspend fun submitCreateNote(): ApiResult<Note> {
        val form = (_uiState.value.activeSheet as? NoteSheet.CreateNote)?.form
            ?: return validationError(message = "Title is required.")
        val title = form.title.validateRequired(error = R.string.title_required)
        if (title.error != null) {
            _uiState.update { it.copy(activeSheet = NoteSheet.CreateNote(form.copy(title = title))) }
            return validationError(message = "Title is required.")
        }

        _uiState.update { it.copy(activeSheet = NoteSheet.CreateNote(form.copy(title = title))) }
        return createNote(title = title.value.trim())
    }

    fun updateNoteDraftTitle(title: String) {
        val draft = _uiState.value.noteDraft ?: return
        if (draft.title == title) return

        val updatedDraft = draft.copy(
            title = title,
            updatedAt = OffsetDateTime.now().toString()
        )
        _uiState.update { state ->
            state.copy(
                noteDraft = updatedDraft,
                note = updatedDraft,
                notes = state.notes?.map { item ->
                    if (item.id == updatedDraft.id) updatedDraft else item
                }
            )
        }
        scheduleNoteDraftSave()
    }

    fun updateNoteDraftContent(content: String) {
        val draft = _uiState.value.noteDraft ?: return
        if (draft.content == content) return

        val updatedDraft = draft.copy(
            content = content,
            updatedAt = OffsetDateTime.now().toString()
        )
        _uiState.update { state ->
            state.copy(
                noteDraft = updatedDraft,
                note = updatedDraft,
                notes = state.notes?.map { item ->
                    if (item.id == updatedDraft.id) updatedDraft else item
                }
            )
        }
        scheduleNoteDraftSave()
    }

    fun openNoteActionsSheet() {
        _uiState.update { it.copy(activeSheet = NoteSheet.NoteActions) }
    }

    fun dismissNoteActionsSheet() {
        _uiState.update { it.copy(activeSheet = NoteSheet.None) }
    }

    fun requestDeleteNote() {
        val noteId = _uiState.value.note?.id ?: return
        _uiState.update { it.copy(activeSheet = NoteSheet.DeleteConfirmation(noteId = noteId)) }
    }

    fun dismissDeleteNoteSheet() {
        _uiState.update { it.copy(activeSheet = NoteSheet.None) }
    }

    fun fetchNotes() {
        viewModelScope.launch {
            if (_uiState.value.notes == null) {
                _uiState.update { it.copy(status = UiStatus.Loading) }
            }

            getNotesUseCase()
                .onSuccess { list ->
                    _uiState.update { state ->
                        state.copy(
                            notes = list,
                            status = UiStatus.Idle
                        )
                    }
                }
                .onError { msg, code ->
                    _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                }
        }
    }

    fun fetchNote(id: String) {
        val currentNote = _uiState.value.note
        if (currentNote?.id != id) {
            _uiState.update { it.copy(status = UiStatus.Loading, note = null, noteDraft = null) }
        }

        viewModelScope.launch {
            getNoteUseCase(id = id)
                .onSuccess { noteData ->
                    _uiState.update { state ->
                        state.copy(
                            note = noteData,
                            noteDraft = noteData,
                            notes = state.notes?.map { if (it.id == noteData.id) noteData else it }
                                ?: listOf(noteData),
                            status = UiStatus.Idle
                        )
                    }
                }
                .onError { msg, code ->
                    _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                }
        }
    }

    suspend fun createNote(title: String): ApiResult<Note> {
        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = createNoteUseCase(title = title.trim())
            .onSuccess { newNote ->
                _uiState.update { state ->
                    state.copy(
                        notes = (state.notes.orEmpty() + newNote),
                        note = newNote,
                        noteDraft = newNote,
                        activeSheet = NoteSheet.None,
                        status = UiStatus.Idle
                    )
                }
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        return result
    }

    suspend fun updateNote(id: String, note: Note): ApiResult<Note> {
        val result = updateNoteUseCase(id = id, note = note)
            .onSuccess { updatedNote ->
                _uiState.update { state ->
                    state.copy(
                        notes = state.notes?.map { item ->
                            if (item.id == id) updatedNote else item
                        },
                        note = updatedNote,
                        noteDraft = updatedNote
                    )
                }
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        return result
    }

    fun confirmDeleteNote(id: String) {
        dismissDeleteNoteSheet()

        _uiState.update { state ->
            state.copy(
                notes = state.notes?.filterNot { it.id == id },
                note = if (state.note?.id == id) null else state.note,
                noteDraft = if (state.noteDraft?.id == id) null else state.noteDraft
            )
        }

        viewModelScope.launch {
            deleteNoteUseCase(id = id)
                .onError { msg, code ->
                    _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                    fetchNotes()
                }
        }
    }

    fun flushDraftSave(content: String? = null) {
        saveNoteDraftJob?.cancel()
        var draft = _uiState.value.noteDraft ?: return
        if (content != null && draft.content != content) {
            draft = draft.copy(
                content = content,
                updatedAt = OffsetDateTime.now().toString()
            )
            _uiState.update { state ->
                state.copy(
                    noteDraft = draft,
                    note = draft,
                    notes = state.notes?.map { item ->
                        if (item.id == draft.id) draft else item
                    }
                )
            }
        }
        viewModelScope.launch {
            updateNote(id = draft.id, note = draft)
        }
    }

    private fun scheduleNoteDraftSave() {
        saveNoteDraftJob?.cancel()
        saveNoteDraftJob = viewModelScope.launch {
            delay(timeMillis = 100)
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
