package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingCard
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails
import dev.cankolay.twodo.android.presentation.viewmodel.user.AccountUiState
import dev.cankolay.twodo.android.presentation.viewmodel.user.AccountViewModel

@Composable
fun SettingsView(
    onNavigate: (Route) -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        onRefresh = viewModel::refresh,
        onNavigate = onNavigate,
    )
}

@Composable
private fun SettingsContent(
    state: AccountUiState,
    onRefresh: () -> Unit,
    onNavigate: (Route) -> Unit,
) {
    AppLayout(route = Route.Settings) {
        AppLinearProgressIndicator(isLoading = state.load == ApiResult.Loading)

        AppLazyColumn(contentPadding = PaddingValues(vertical = 16.dp)) {
            val load = state.load
            if (load is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = load.error.message,
                        onRefresh = onRefresh,
                    )
                }
            }
            state.user?.let { user ->
                item {
                    SettingCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        title = user.name,
                        description = user.username,
                        leadingContent = { Icon(icon = Icons.Default.Person) },
                        onClick = { onNavigate(Route.Profile) },
                    )
                }
            }

            item {
                val details = Route.Couple.getDetails()

                SettingCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = details.title,
                    leadingContent = { Icon(icon = details.icon.default) },
                    onClick = { onNavigate(Route.Couple) },
                )
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            }
            
            item {
                val routes = listOf(Route.Appearance, Route.Languages, Route.About)

                SettingsGroup(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items =
                        routes.map { route ->
                            val details = route.getDetails()

                            SettingItem(
                                title = details.title,
                                description = details.description,
                                leadingContent = {
                                    Icon(icon = details.icon.default)
                                },
                                onClick = {
                                    onNavigate(route)
                                },
                            )
                        },
                )
            }
        }
    }
}
