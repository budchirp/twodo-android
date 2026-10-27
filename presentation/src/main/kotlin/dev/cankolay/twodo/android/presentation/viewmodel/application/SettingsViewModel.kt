package dev.cankolay.twodo.android.presentation.viewmodel.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.application.SettingsState
import dev.cankolay.twodo.android.domain.repository.application.SettingsStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settings: SettingsState = SettingsState(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settings: SettingsStateRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settings.state.collect { state ->
                _uiState.value = SettingsUiState(settings = state, isLoading = false)
            }
        }
    }

    fun update(state: SettingsState) {
        viewModelScope.launch { settings.update(state = state) }
    }
}
