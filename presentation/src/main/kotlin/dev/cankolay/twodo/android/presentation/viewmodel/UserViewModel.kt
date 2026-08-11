package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.getOrNull
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.api.onSuccess
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.api.validationError
import dev.cankolay.twodo.android.domain.usecase.api.couple.LeaveCoupleUseCase
import dev.cankolay.twodo.android.domain.usecase.api.user.GetUserUseCase
import dev.cankolay.twodo.android.domain.usecase.api.user.UpdateProfileUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validatePresent
import dev.cankolay.twodo.android.presentation.form.validateRequired
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileFormState(
    val name: FormField<String> = FormField(value = ""),
    val gender: FormField<Gender?> = FormField(value = null)
) {
    val canSubmit = name.value.isNotBlank() && gender.value != null
}

sealed interface UserSheet {
    data object None : UserSheet
    data object LeaveCouple : UserSheet
}

data class UserUiState(
    val userResult: ApiResult<User> = ApiResult.Loading,
    val profileForm: ProfileFormState = ProfileFormState(),
    val activeSheet: UserSheet = UserSheet.None,
    val actionResult: ApiResult<*>? = null
) {
    val user: User? get() = userResult.getOrNull()
    val isLoading: Boolean get() = userResult.isLoading || actionResult?.isLoading == true
    val isInitialized: Boolean get() = userResult !is ApiResult.Loading
    val error: String? get() = actionResult?.errorMessage ?: userResult.errorMessage
    val errorCode: String? get() = actionResult?.errorCode ?: userResult.errorCode
    val isLeaveCoupleSheetVisible: Boolean get() = activeSheet is UserSheet.LeaveCouple
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val leaveCoupleUseCase: LeaveCoupleUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()
    private var fetchUserJob: Job? = null

    fun updateProfileName(name: String) {
        _uiState.update { state ->
            state.copy(
                profileForm = state.profileForm.copy(
                    name = state.profileForm.name.update(value = name)
                )
            )
        }
    }

    fun updateProfileGender(gender: Gender) {
        _uiState.update { state ->
            state.copy(
                profileForm = state.profileForm.copy(
                    gender = state.profileForm.gender.update(value = gender)
                )
            )
        }
    }

    suspend fun submitProfile(): ApiResult<User> {
        val form = _uiState.value.profileForm
        val name = form.name.validateRequired(error = R.string.name_required)
        val gender = form.gender.validatePresent(error = R.string.gender_required)
        val selectedGender = gender.value
        if (name.error != null || gender.error != null || selectedGender == null) {
            _uiState.update { it.copy(profileForm = form.copy(name = name, gender = gender)) }
            return validationError(message = "Profile name and gender are required.")
        }

        _uiState.update { it.copy(profileForm = form.copy(name = name, gender = gender)) }
        return updateProfile(name = name.value.trim(), gender = selectedGender)
    }

    fun openLeaveCoupleSheet() {
        _uiState.update { it.copy(activeSheet = UserSheet.LeaveCouple) }
    }

    fun dismissLeaveCoupleSheet() {
        _uiState.update { it.copy(activeSheet = UserSheet.None) }
    }

    fun fetchUser() {
        if (fetchUserJob?.isActive == true) return

        fetchUserJob = viewModelScope.launch {
            _uiState.update { it.copy(userResult = ApiResult.Loading) }

            val result = getUserUseCase()
            _uiState.update { state ->
                state.copy(
                    userResult = result,
                    profileForm = result.getOrNull()?.toProfileFormState() ?: state.profileForm
                )
            }
        }
    }

    suspend fun leaveCouple(): ApiResult<Nothing?> {
        _uiState.update { it.copy(actionResult = ApiResult.Loading) }

        val result = leaveCoupleUseCase()
        result.onSuccess {
            _uiState.update { state ->
                val currentUser = state.user
                state.copy(
                    userResult = if (currentUser != null) ApiResult.Success(
                        message = "Updated",
                        data = currentUser.copy(couple = null)
                    ) else state.userResult,
                    activeSheet = UserSheet.None,
                    actionResult = null
                )
            }
        }.onError { _, _ ->
            _uiState.update { it.copy(actionResult = result) }
        }

        return result
    }

    suspend fun updateProfile(name: String, gender: Gender): ApiResult<User> {
        _uiState.update { it.copy(actionResult = ApiResult.Loading) }

        val result = updateProfileUseCase(name = name, gender = gender)
        result.onSuccess { updatedUser ->
            val refreshed = (getUserUseCase().getOrNull() ?: updatedUser)
            _uiState.update { state ->
                state.copy(
                    userResult = ApiResult.Success(message = "Updated", data = refreshed),
                    profileForm = refreshed.toProfileFormState(),
                    actionResult = null
                )
            }
        }.onError { _, _ ->
            _uiState.update { it.copy(actionResult = result) }
        }

        return result
    }

    fun clearUser() {
        fetchUserJob?.cancel()
        _uiState.value = UserUiState()
    }
}

private fun User.toProfileFormState() = ProfileFormState(
    name = FormField(value = name),
    gender = FormField(value = gender)
)
