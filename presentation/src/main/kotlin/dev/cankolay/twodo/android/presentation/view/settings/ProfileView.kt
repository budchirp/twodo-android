package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator
import dev.cankolay.twodo.android.presentation.composable.app.Avatar
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.user.ProfileUiState
import dev.cankolay.twodo.android.presentation.viewmodel.user.ProfileViewModel

@Composable
fun ProfileView(onLogout: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileContent(state = state, onRefresh = viewModel::refresh, onLogout = onLogout)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(state: ProfileUiState, onRefresh: () -> Unit, onLogout: () -> Unit) {
    var showLogout by rememberSaveable { mutableStateOf(value = false) }

    AppLayout(route = Route.Profile) {
        AppLinearProgressIndicator(isLoading = state.load == ApiResult.Loading)

        state.user?.let { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(picture = user.picture, size = 80.dp)

                Column(
                    modifier = Modifier.weight(weight = 1f),
                    verticalArrangement = Arrangement.spacedBy(space = 2.dp),
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

        AppLazyColumn(contentPadding = PaddingValues(vertical = 16.dp)) {
            val failure = state.load as? ApiResult.Failure
            if (failure != null) {
                item {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = failure.error.message,
                        onRefresh = onRefresh,
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.user != null && state.load != ApiResult.Loading,
                        onClick = onRefresh,
                    ) {
                        Text(text = stringResource(id = R.string.refresh_profile))
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                        onClick = { showLogout = true },
                    ) {
                        Text(text = stringResource(id = R.string.sign_out))
                    }
                }
            }
        }

        if (showLogout) {
            LogoutSheet(
                onDismiss = { showLogout = false },
                onLogout = {
                    showLogout = false
                    onLogout()
                },
            )
        }
    }
}
