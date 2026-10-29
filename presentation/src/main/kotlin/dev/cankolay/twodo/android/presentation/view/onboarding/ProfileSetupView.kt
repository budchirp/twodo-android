package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.OnboardingLayout
import dev.cankolay.twodo.android.presentation.composable.app.RadioOptions
import dev.cankolay.twodo.android.presentation.form.FormTextField
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.user.ProfileSetupUiState
import dev.cankolay.twodo.android.presentation.viewmodel.user.ProfileSetupViewModel

@Composable
fun ProfileSetupView(viewModel: ProfileSetupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileSetupContent(
        userState = state,
        onRefresh = viewModel::fetch,
        onNameChange = viewModel::updateName,
        onGenderChange = viewModel::updateGender,
        onSubmit = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileSetupContent(
    userState: ProfileSetupUiState,
    onRefresh: () -> Unit,
    onNameChange: (String) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onSubmit: () -> Unit,
) {
    val form = userState.form

    val isLoading = userState.user == ApiResult.Loading || userState.result == ApiResult.Loading

    val selectedGender = form.gender.value

    OnboardingLayout(
        route = Route.ProfileSetup,
        title = stringResource(id = R.string.profile_setup_title),
        description = stringResource(id = R.string.profile_setup_desc),
        isLoading = isLoading,
        onRefresh = onRefresh,
        status = {
            when (val result = userState.user) {
                is ApiResult.Failure ->
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = result.error.message,
                        onRefresh = onRefresh,
                    )

                else -> Unit
            }

            when (val result = userState.result) {
                is ApiResult.Failure ->
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = result.error.message,
                        onRefresh = onSubmit,
                    )

                else -> Unit
            }
        },
        actions = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && form.canSubmit,
                onClick = onSubmit,
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        lazyContent = {
            item {
                FormTextField(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    field = form.name,
                    onValueChange = onNameChange,
                    enabled = !isLoading,
                    label = stringResource(id = R.string.name),
                    onSubmit = onSubmit,
                )
            }

            item {
                val genders =
                    mapOf(
                        Gender.FEMALE to R.string.female,
                        Gender.MALE to R.string.male,
                    )

                RadioOptions(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = genders.keys.toList(),
                    selected = selectedGender,
                    label = { gender -> stringResource(id = genders.getValue(gender)) },
                    onSelected = onGenderChange,
                    enabled = !isLoading,
                )

                form.gender.error?.let { error ->
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(id = error),
                    )
                }
            }
        },
    )
}
