package dev.cankolay.twodo.android.presentation.viewmodel.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.usecase.api.note.GetNotesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.note.ObserveNotesUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note>? = null,
    val load: ApiResult<Unit>? = null,
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val observeNotes: ObserveNotesUseCase,
    private val getNotes: GetNotesUseCase,
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
