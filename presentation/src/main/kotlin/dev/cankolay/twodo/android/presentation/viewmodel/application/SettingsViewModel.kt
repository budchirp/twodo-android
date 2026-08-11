package dev.cankolay.twodo.android.presentation.viewmodel.application

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.application.SettingsState
import dev.cankolay.twodo.android.domain.usecase.application.settings.GetSettingsStateUseCase
import dev.cankolay.twodo.android.domain.usecase.application.settings.UpdateSettingsStateUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val settingsState: SettingsState = SettingsState()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getSettingsStateUseCase: GetSettingsStateUseCase,
    private val updateSettingsStateUseCase: UpdateSettingsStateUseCase
) : BaseViewModel<SettingsUiState>(SettingsUiState()) {
    init {
        viewModelScope.launch {
            getSettingsStateUseCase()
                .catch { emit(SettingsState()) }
                .collect { settingsState ->
                updateState { copy(settingsState = settingsState) }
            }
        }
    }

    fun updateSettings(settingsState: SettingsState) {
        viewModelScope.launch {
            updateSettingsStateUseCase(settingsState)
        }
    }
}
