package dev.cankolay.twodo.android.presentation.viewmodel.note

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.api.onSuccess
import dev.cankolay.twodo.android.domain.usecase.api.note.CreateNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.DeleteNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.GetNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.GetNotesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.UpdateNoteUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.UiEvent
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import kotlinx.coroutines.delay
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
    val notesResult: ApiResult<List<Note>> = ApiResult.Loading,
    val noteResult: ApiResult<Note> = ApiResult.Loading,
    val cachedNotes: List<Note>? = null,
    val noteDraft: Note? = null,
    val activeSheet: NoteSheet = NoteSheet.None
) {
    val notes: List<Note>? get() = cachedNotes ?: notesResult.dataOrNull
    val note: Note? get() = noteDraft ?: noteResult.dataOrNull
    val isLoading: Boolean get() = notesResult.isLoading || noteResult.isLoading
    val error: String? get() = noteResult.errorMessage ?: notesResult.errorMessage
}

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val getNoteUseCase: GetNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : BaseViewModel<NoteUiState>(NoteUiState()) {

    init {
        fetchNotes()
    }

    fun openCreateNoteSheet() {
        updateState {
            copy(activeSheet = NoteSheet.CreateNote(CreateNoteFormState()))
        }
    }

    fun openNoteActionsSheet() {
        updateState { copy(activeSheet = NoteSheet.NoteActions) }
    }

    fun requestDeleteNote() {
        val noteId = uiState.value.note?.id ?: return
        updateState { copy(activeSheet = NoteSheet.DeleteConfirmation(noteId)) }
    }

    fun dismissSheet() {
        updateState { copy(activeSheet = NoteSheet.None) }
    }

    fun updateCreateNoteTitle(title: String) {
        updateState {
            val sheet = activeSheet as? NoteSheet.CreateNote ?: return@updateState this
            copy(
                activeSheet = NoteSheet.CreateNote(
                    sheet.form.copy(
                        title = sheet.form.title.update(
                            title
                        )
                    )
                )
            )
        }
    }

    fun submitNote() {
        val form = (uiState.value.activeSheet as? NoteSheet.CreateNote)?.form ?: return
        val title = form.title.validateRequired(error = R.string.title_required)
        if (title.error != null) {
            updateState {
                copy(activeSheet = NoteSheet.CreateNote(form.copy(title = title)))
            }
            return
        }

        launchOnce("create-note") {
            updateState { copy(activeSheet = NoteSheet.None) }
            val result = createNoteUseCase(title = title.value.trim())
            result.onSuccess { newNote ->
                updateState {
                    copy(
                        cachedNotes = notes.orEmpty() + newNote,
                        noteResult = result,
                        noteDraft = newNote
                    )
                }
                sendEvent(UiEvent.NavigateTo(Route.Note(newNote.id)))
            }.onError { message, _ ->
                sendEvent(UiEvent.ShowSnackbar(message))
            }
        }
    }

    fun updateNoteDraftTitle(title: String) {
        updateDraft { copy(title = title) }
    }

    fun updateNoteDraftContent(content: String) {
        updateDraft { copy(content = content) }
    }

    fun fetchNotes() {
        launchOnce("notes") {
            updateState { copy(notesResult = ApiResult.Loading) }
            val result = getNotesUseCase()
            updateState {
                copy(
                    notesResult = result,
                    cachedNotes = result.dataOrNull ?: cachedNotes
                )
            }
        }
    }

    fun fetchNote(id: String) {
        if (uiState.value.note?.id != id) {
            updateState { copy(noteResult = ApiResult.Loading, noteDraft = null) }
        }

        launchLatest("note") {
            val result = getNoteUseCase(id)
            val fetchedNote = result.dataOrNull
            updateState {
                copy(
                    noteResult = result,
                    noteDraft = fetchedNote ?: noteDraft,
                    cachedNotes = fetchedNote?.let { note ->
                        notes.orEmpty().let { notes ->
                            if (notes.any { it.id == note.id }) {
                                notes.map { if (it.id == note.id) note else it }
                            } else {
                                notes + note
                            }
                        }
                    } ?: cachedNotes
                )
            }
        }
    }

    fun confirmDeleteNote(id: String) {
        dismissSheet()
        updateState {
            copy(
                cachedNotes = notes?.filterNot { it.id == id },
                noteDraft = noteDraft?.takeUnless { it.id == id }
            )
        }
        sendEvent(UiEvent.ResetTo(Route.Notes))

        launchOnce("delete-note:$id") {
            val result = deleteNoteUseCase(id)
            result.onError { message, _ ->
                sendEvent(UiEvent.ShowSnackbar(message))
                fetchNotes()
            }
        }
    }

    fun flushDraftSave(content: String? = null) {
        cancelJob("draft-save")
        val currentDraft = uiState.value.noteDraft ?: return
        val draft = if (content != null && currentDraft.content != content) {
            currentDraft.copy(content = content, updatedAt = OffsetDateTime.now().toString())
        } else {
            currentDraft
        }

        if (draft != currentDraft) updateStateWithDraft(draft)
        launchLatest("draft-save") { saveDraft(draft) }
    }

    private fun updateDraft(transform: Note.() -> Note) {
        val draft = uiState.value.noteDraft ?: uiState.value.note ?: return
        val updatedDraft = draft.transform().copy(updatedAt = OffsetDateTime.now().toString())
        if (updatedDraft == draft) return
        updateStateWithDraft(updatedDraft)
        launchLatest("draft-save") {
            delay(100)
            val latestDraft = uiState.value.noteDraft ?: return@launchLatest
            saveDraft(latestDraft)
        }
    }

    private suspend fun saveDraft(draft: Note) {
        updateNoteUseCase(id = draft.id, note = draft)
            .onSuccess { updatedNote -> updateStateWithDraft(updatedNote) }
            .onError { message, _ -> sendEvent(UiEvent.ShowSnackbar(message)) }
    }

    private fun updateStateWithDraft(draft: Note) {
        updateState {
            copy(
                noteDraft = draft,
                cachedNotes = notes?.map { if (it.id == draft.id) draft else it }
            )
        }
    }
}
