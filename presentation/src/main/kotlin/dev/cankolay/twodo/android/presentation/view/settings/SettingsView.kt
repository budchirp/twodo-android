package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Avatar
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails
import dev.cankolay.twodo.android.presentation.viewmodel.user.AccountViewModel

@Composable
fun SettingsView(
    onLogout: () -> Unit,
    onNavigate: (Route) -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val user by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        user = user,
        onRefresh = viewModel::refresh,
        onLogout = onLogout,
        onNavigate = onNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    user: ApiResult<User>?,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (Route) -> Unit,
) {
    var showLogout by rememberSaveable { mutableStateOf(value = false) }

    AppLayout(route = Route.Settings) {
        when (user) {
            ApiResult.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            is ApiResult.Failure -> ErrorCard(error = user.error.message, onRefresh = onRefresh)

            else -> Unit
        }
        AppLazyColumn {
            item {
                when (user) {
                    is ApiResult.Success ->
                        ProfileCard(user = user.data, onLogout = { showLogout = true })

                    else -> Unit
                }
            }

            item {
                val routes = listOf(Route.Couple)

                CardStackList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items =
                        routes.map { route ->
                            val details = route.getDetails()

                            CardStackListItem(
                                title = details.title,
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

            item {
                val routes = listOf(Route.Appearance, Route.Languages, Route.About)

                CardStackList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items =
                        routes.map { route ->
                            val details = route.getDetails()

                            CardStackListItem(
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

        if (showLogout) {
            DestructiveConfirmationSheet(
                title = stringResource(id = R.string.sign_out),
                description = stringResource(id = R.string.sign_out_desc),
                confirmText = stringResource(id = R.string.sign_out),
                onDismiss = { showLogout = false },
                onConfirm = {
                    showLogout = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun ProfileCard(user: User, onLogout: () -> Unit) {
    CardStackList(
        modifier = Modifier.padding(horizontal = 16.dp),
        items =
            listOf(
                CardStackListItem(
                    title = user.name,
                    leadingContent = {
                        Avatar(picture = user.picture)
                    },
                    trailingContent = {
                        Button(onClick = onLogout) {
                            Text(text = stringResource(id = R.string.sign_out))
                        }
                    },
                    contentPadding = PaddingValues(vertical = 12.dp),
                    contentSize = null,
                )
            ),
    )
}
