package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.usecase.api.couple.LeaveCoupleUseCase
import dev.cankolay.twodo.android.domain.usecase.api.user.GetUserUseCase
import dev.cankolay.twodo.android.domain.usecase.api.user.UpdateProfileUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validatePresent
import dev.cankolay.twodo.android.presentation.form.validateRequired
import dev.cankolay.twodo.android.presentation.state.UiStatus
import dev.cankolay.twodo.android.presentation.state.errorCode
import dev.cankolay.twodo.android.presentation.state.errorMessage
import dev.cankolay.twodo.android.presentation.state.isLoading
import dev.cankolay.twodo.android.presentation.state.onError
import dev.cankolay.twodo.android.presentation.state.onSuccess
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
    val user: User? = null,
    val profileForm: ProfileFormState = ProfileFormState(),
    val activeSheet: UserSheet = UserSheet.None,
    val status: UiStatus = UiStatus.Idle,
    val isFatalError: Boolean = false,
    val isInitialized: Boolean = false
) {
    val isLoading: Boolean get() = status.isLoading
    val error: String? get() = status.errorMessage
    val errorCode: String? get() = status.errorCode
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
            _uiState.update { it.copy(status = UiStatus.Loading) }

            getUserUseCase()
                .onSuccess { userData ->
                    _uiState.update {
                        it.copy(
                            user = userData,
                            profileForm = userData.toProfileFormState(),
                            status = UiStatus.Idle,
                            isFatalError = false,
                            isInitialized = true
                        )
                    }
                }
                .onError { msg, code ->
                    _uiState.update {
                        it.copy(
                            user = null,
                            status = UiStatus.Error(msg, code),
                            isFatalError = false,
                            isInitialized = true
                        )
                    }
                }
        }
    }

    suspend fun leaveCouple(): ApiResult<Nothing?> {
        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = leaveCoupleUseCase()
            .onSuccess {
                _uiState.update { state ->
                    state.copy(
                        user = state.user?.copy(couple = null),
                        activeSheet = UserSheet.None,
                        status = UiStatus.Idle
                    )
                }
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        return result
    }

    suspend fun updateProfile(name: String, gender: Gender): ApiResult<User> {
        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = updateProfileUseCase(name = name, gender = gender)
            .onSuccess { updatedUser ->
                val refreshedUser = when (val refreshed = getUserUseCase()) {
                    is ApiResult.Success -> refreshed.data
                    else -> updatedUser
                }

                _uiState.update {
                    it.copy(
                        user = refreshedUser,
                        profileForm = refreshedUser.toProfileFormState(),
                        status = UiStatus.Idle,
                        isFatalError = false,
                        isInitialized = true
                    )
                }
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        return result
    }

    fun clearUser() {
        fetchUserJob?.cancel()
        _uiState.value = UserUiState()
    }

    private fun validationError(message: String) = ApiResult.Error(
        message = message,
        reason = ErrorReason.CLIENT,
        code = "validation_error"
    )
}

private fun User.toProfileFormState() = ProfileFormState(
    name = FormField(value = name),
    gender = FormField(value = gender)
)
