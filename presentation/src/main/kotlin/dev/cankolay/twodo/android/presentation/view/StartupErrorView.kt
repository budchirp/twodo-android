package dev.cankolay.twodo.android.presentation.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.OnboardingLayout
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StartupErrorView(
    userViewModel: UserViewModel = hiltViewModel()
) {
    val userState by userViewModel.uiState.collectAsStateWithLifecycle()

    OnboardingLayout(
        route = Route.StartupError,
        title = stringResource(id = R.string.startup_error_title),
        description = stringResource(id = R.string.startup_error_desc),
        isLoading = userState.isLoading,
        onRefresh = { userViewModel.fetchUser() },
        actions = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !userState.isLoading,
                onClick = { userViewModel.fetchUser() }
            ) {
                Text(text = stringResource(id = R.string.try_again))
            }
        },
        lazyContent = {
            item {
                ErrorCard(
                    title = stringResource(id = R.string.startup_error_card_title),
                    error = userState.error
                        ?: stringResource(id = R.string.startup_error_unknown),
                )
            }
        }
    )
}
