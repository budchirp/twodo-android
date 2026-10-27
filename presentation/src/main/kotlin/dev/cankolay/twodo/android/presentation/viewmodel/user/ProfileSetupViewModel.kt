package dev.cankolay.twodo.android.presentation.viewmodel.user

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.userResult
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validatePresent
import dev.cankolay.twodo.android.presentation.form.validateRequired
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
    val user: ApiResult<User>? = null,
    val result: ApiResult<User>? = null,
    val form: ProfileFormState = ProfileFormState(),
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(private val session: SessionRepository) :
    BaseViewModel<ProfileSetupUiState>(initialState = ProfileSetupUiState()) {

    init {
        viewModelScope.launch {
            session.state.collect { state ->
                val result = state.userResult()

                _uiState.update {
                    val previous = it.user as? ApiResult.Success

                    it.copy(
                        user = result,
                        form =
                            when {
                                result is ApiResult.Success &&
                                        previous?.data?.id != result.data.id ->
                                    ProfileFormState(
                                        name = FormField(value = result.data.name),
                                        gender = FormField(value = result.data.gender),
                                    )

                                else -> it.form
                            },
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(form = it.form.copy(name = it.form.name.update(value = name)))
        }
    }

    fun updateGender(gender: Gender) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(form = it.form.copy(gender = it.form.gender.update(value = gender)))
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
            val result = session.updateProfile(name = name.value.trim(), gender = selected)

            _uiState.update { it.copy(result = result) }
        }
    }

    fun fetch() {
        if (uiState.value.user == ApiResult.Loading || uiState.value.result == ApiResult.Loading)
            return

        _uiState.update { it.copy(user = ApiResult.Loading) }

        viewModelScope.launch {
            val result = session.refresh()

            _uiState.update { it.copy(user = result) }
        }
    }
}
