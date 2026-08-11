package dev.cankolay.twodo.android.presentation.viewmodel.user

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.api.onSuccess
import dev.cankolay.twodo.android.domain.model.api.successResult
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.usecase.api.couple.LeaveCoupleUseCase
import dev.cankolay.twodo.android.domain.usecase.api.user.GetUserUseCase
import dev.cankolay.twodo.android.domain.usecase.api.user.UpdateProfileUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.UiEvent
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.form.validatePresent
import dev.cankolay.twodo.android.presentation.form.validateRequired
import javax.inject.Inject

data class ProfileFormState(
    val name: FormField<String> = FormField(""),
    val gender: FormField<Gender?> = FormField(null)
) {
    val canSubmit = name.value.isNotBlank()
}

sealed interface UserSheet {
    data object None : UserSheet
    data object LeaveCouple : UserSheet
}

data class UserUiState(
    val userResult: ApiResult<User> = ApiResult.Loading,
    val profileForm: ProfileFormState = ProfileFormState(),
    val activeSheet: UserSheet = UserSheet.None
) {
    val user: User? get() = userResult.dataOrNull
    val isLoading: Boolean get() = userResult.isLoading
    val isInitialized: Boolean get() = userResult !is ApiResult.Loading
    val error: String? get() = userResult.errorMessage
    val errorCode: String? get() = userResult.errorCode
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val leaveCoupleUseCase: LeaveCoupleUseCase
) : BaseViewModel<UserUiState>(UserUiState()) {

    fun updateProfileName(name: String) {
        updateState { copy(profileForm = profileForm.copy(name = profileForm.name.update(name))) }
    }

    fun updateProfileGender(gender: Gender) {
        updateState { copy(profileForm = profileForm.copy(gender = profileForm.gender.update(gender))) }
    }

    fun submitProfile() {
        val form = uiState.value.profileForm
        val name = form.name.validateRequired(error = R.string.name_required)
        val gender = form.gender.validatePresent(error = R.string.gender_required)
        val selectedGender = gender.value
        updateState { copy(profileForm = form.copy(name = name, gender = gender)) }
        if (name.error != null || gender.error != null || selectedGender == null) return

        launchOnce("update-profile") {
            updateProfileUseCase(name.value.trim(), selectedGender)
                .onSuccess { updatedUser ->
                    val refreshedUser = getUserUseCase().dataOrNull ?: updatedUser
                    updateState {
                        copy(
                            userResult = successResult(refreshedUser, message = "Updated"),
                            profileForm = refreshedUser.toProfileFormState()
                        )
                    }
                }
                .onError { message, _ -> sendEvent(UiEvent.ShowSnackbar(message)) }
        }
    }

    fun openLeaveCoupleSheet() {
        updateState { copy(activeSheet = UserSheet.LeaveCouple) }
    }

    fun dismissSheet() {
        updateState { copy(activeSheet = UserSheet.None) }
    }

    fun fetchUser() {
        launchOnce("user") {
            updateState { copy(userResult = ApiResult.Loading) }
            val result = getUserUseCase()
            updateState {
                copy(
                    userResult = result,
                    profileForm = result.dataOrNull?.toProfileFormState() ?: profileForm
                )
            }
        }
    }

    fun leaveCouple() {
        val currentUser = uiState.value.user
        dismissSheet()
        launchOnce("leave-couple") {
            leaveCoupleUseCase()
                .onSuccess {
                    if (currentUser != null) {
                        updateState {
                            copy(
                                userResult = successResult(
                                    currentUser.copy(couple = null),
                                    "Updated"
                                )
                            )
                        }
                    }
                }
                .onError { message, _ ->
                    sendEvent(UiEvent.ShowSnackbar(message))
                    fetchUser()
                }
        }
    }

    fun clearUser() {
        cancelJob("user")
        updateState { UserUiState() }
    }
}

private fun User.toProfileFormState() = ProfileFormState(
    name = FormField(name),
    gender = FormField(gender)
)
