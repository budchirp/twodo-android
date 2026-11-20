package dev.cankolay.twodo.android.presentation.viewmodel.user

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.user.UpdateProfileUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validatePresent
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileFormState(
    val name: FormField<String> = FormField(value = ""),
    val gender: FormField<Gender?> = FormField(value = null),
) {
    val canSubmit: Boolean
        get() = name.value.isNotBlank() && gender.value != null
}

data class ProfileSetupUiState(
    val user: User? = null,
    val load: ApiResult<Unit>? = null,
    val result: ApiResult<Unit>? = null,
    val form: ProfileFormState = ProfileFormState(),
    val formInitialized: Boolean = false,
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val observeSession: ObserveSessionUseCase,
    private val refreshSession: RefreshSessionUseCase,
    private val updateProfile: UpdateProfileUseCase,
) : BaseViewModel<ProfileSetupUiState>(initialState = ProfileSetupUiState()) {

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            observeSession().collect { session ->
                _uiState.update {
                    when (session) {
                        is SessionState.Active -> it.copy(
                            user = session.user,
                            load = if (it.user == null && it.load == ApiResult.Loading)
                                ApiResult.Success(data = Unit) else it.load,
                            form = if (!it.formInitialized) ProfileFormState(
                                name = FormField(value = session.user.name),
                                gender = FormField(value = session.user.gender),
                            ) else it.form,
                            formInitialized = true,
                        )

                        is SessionState.Failed -> it.copy(load = ApiResult.Failure(error = session.error))
                        SessionState.Starting -> ProfileSetupUiState(load = ApiResult.Loading)
                        SessionState.SignedOut -> ProfileSetupUiState()
                        SessionState.ProfileRequired -> it.copy(user = null, load = null)
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(
                form = it.form.copy(name = it.form.name.update(value = name)),
                formInitialized = true,
            )
        }
    }

    fun updateGender(gender: Gender) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(
                form = it.form.copy(gender = it.form.gender.update(value = gender)),
                formInitialized = true,
            )
        }
    }

    fun save() {
        if (uiState.value.result == ApiResult.Loading) return

        val form = uiState.value.form
        val name = form.name.validateRequired(error = R.string.name_required)
        val gender = form.gender.validatePresent(error = R.string.gender_required)

        _uiState.update { it.copy(form = form.copy(name = name, gender = gender)) }

        val selected = gender.value ?: return

        if (name.error != null || gender.error != null) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = updateProfile(name = name.value.trim(), gender = selected)

            _uiState.update { it.copy(result = result.map { }) }
        }
    }

    fun fetch() {
        if (loadJob?.isActive == true) return

        if (uiState.value.load == ApiResult.Loading || uiState.value.result == ApiResult.Loading)
            return

        _uiState.update { it.copy(load = ApiResult.Loading) }

        loadJob = viewModelScope.launch {
            val result = refreshSession()

            _uiState.update { it.copy(load = result.map { }) }
        }
    }
}
