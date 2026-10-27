package dev.cankolay.twodo.android.presentation.viewmodel.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateNoteFormState(val title: FormField<String> = FormField(value = "")) {
    val canSubmit: Boolean
        get() = title.value.isNotBlank()
}

data class NotesUiState(
    val notes: ApiResult<List<Note>>? = null,
    val result: ApiResult<Note>? = null,
    val form: CreateNoteFormState = CreateNoteFormState(),
    val showForm: Boolean = false,
)

@HiltViewModel
class NotesViewModel @Inject constructor(private val notes: NoteRepository) :
    BaseViewModel<NotesUiState>(initialState = NotesUiState()) {

    init {
        viewModelScope.launch {
            notes.notes.collect { data ->
                if (_uiState.value.notes != ApiResult.Loading) {
                    _uiState.update { it.copy(notes = data?.let { ApiResult.Success(data = it) }) }
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
            val result = notes.create(title = title.value.trim())

            _uiState.update { it.copy(result = result) }
        }
    }

    fun consume() {
        _uiState.update { it.copy(result = null, showForm = false) }
    }

    fun fetch(forceRefresh: Boolean = false) {
        if (uiState.value.notes == ApiResult.Loading) return

        _uiState.update { it.copy(notes = ApiResult.Loading) }

        viewModelScope.launch {
            val result = notes.getAll(forceRefresh = forceRefresh)

            _uiState.update { it.copy(notes = result) }
        }
    }
}
