package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.OnboardingLayout
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.UserViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.application.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileSetupView(
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()

    val userState by userViewModel.uiState.collectAsStateWithLifecycle()
    val profileForm = userState.profileForm
    LaunchedEffect(key1 = Unit) {
        if (!userState.isInitialized && !userState.isLoading) {
            userViewModel.fetchUser()
        }
    }

    val isLoading = userState.isLoading
    val selectedGender = profileForm.gender.value

    OnboardingLayout(
        route = Route.ProfileSetup,
        title = stringResource(id = R.string.profile_setup_title),
        description = stringResource(id = R.string.profile_setup_desc),
        isLoading = isLoading,
        onRefresh = { userViewModel.fetchUser() },
        actions = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && profileForm.canSubmit,
                onClick = {
                    scope.launch {
                        userViewModel.submitProfile()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        lazyContent = {
            if (userState.error != null) {
                item {
                    ErrorCard(
                        title = stringResource(id = R.string.profile_setup_error_title),
                        error = userState.error,
                        onRefresh = { userViewModel.fetchUser() }
                    )
                }
            }

            item {
                TextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    value = profileForm.name.value,
                    onValueChange = { userViewModel.updateProfileName(name = it) },
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.name)) },
                    isError = profileForm.name.error != null,
                    supportingText = if (profileForm.name.value.isBlank() || profileForm.name.error != null) {
                        {
                            Text(
                                text = profileForm.name.error?.let { stringResource(id = it) }
                                    ?: stringResource(id = R.string.name_required)
                            )
                        }
                    } else null
                )
            }

            item {
                val genders = mapOf(
                    Gender.FEMALE to R.string.female,
                    Gender.MALE to R.string.male
                )

                CardStackList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = genders.map { item ->
                        val onClick = { userViewModel.updateProfileGender(gender = item.key) }

                        CardStackListItem(
                            title = stringResource(id = item.value),
                            onClick = onClick,
                            leadingContent = {
                                RadioButton(
                                    selected = selectedGender == item.key,
                                    onClick = onClick
                                )
                            }
                        )
                    }
                )
            }
        }
    )
}
