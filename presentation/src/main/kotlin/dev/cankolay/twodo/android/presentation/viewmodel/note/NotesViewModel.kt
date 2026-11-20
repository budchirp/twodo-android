package dev.cankolay.twodo.android.presentation.viewmodel.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.usecase.api.note.CreateNoteUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.GetNotesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.ObserveNotesUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateNoteFormState(val title: FormField<String> = FormField(value = "")) {
    val canSubmit: Boolean
        get() = title.value.isNotBlank()
}

data class NotesUiState(
    val notes: List<Note>? = null,
    val load: ApiResult<Unit>? = null,
    val result: ApiResult<Note>? = null,
    val form: CreateNoteFormState = CreateNoteFormState(),
    val showForm: Boolean = false,
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val observeNotes: ObserveNotesUseCase,
    private val getNotes: GetNotesUseCase,
    private val createNote: CreateNoteUseCase,
) : BaseViewModel<NotesUiState>(
    initialState = NotesUiState(notes = observeNotes().value)
) {

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            observeNotes().collect { data ->
                _uiState.update {
                    it.copy(
                        notes = data,
                        load = if (data == null && it.notes != null) null else it.load,
                    )
                }
            }
        }

        fetch()
    }

    fun openForm() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(form = CreateNoteFormState(), showForm = true, result = null)
        }
    }

    fun dismiss() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showForm = false, result = null) }
    }

    fun updateTitle(title: String) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(form = it.form.copy(title = it.form.title.update(value = title)))
        }
    }

    fun create() {
        if (uiState.value.result == ApiResult.Loading) return

        val title = uiState.value.form.title.validateRequired(error = R.string.title_required)

        _uiState.update { it.copy(form = it.form.copy(title = title)) }

        if (title.error != null) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = createNote(title = title.value.trim())

            _uiState.update { it.copy(result = result) }
        }
    }

    fun consume() {
        _uiState.update { it.copy(result = null, showForm = false) }
    }

    fun fetch(forceRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return

        if (uiState.value.load == ApiResult.Loading) return

        _uiState.update { it.copy(load = ApiResult.Loading) }

        loadJob = viewModelScope.launch {
            val result = getNotes(forceRefresh = forceRefresh)

            _uiState.update { it.copy(load = result.map { }) }
        }
    }
}
