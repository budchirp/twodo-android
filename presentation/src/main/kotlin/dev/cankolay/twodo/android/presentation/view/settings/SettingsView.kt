package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.Avatar
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.resetTo
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails
import dev.cankolay.twodo.android.presentation.viewmodel.application.AuthViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    userViewModel: UserViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navBackStack = LocalNavBackStack.current

    val userState by userViewModel.uiState.collectAsStateWithLifecycle()
    val user = userState.user
    HandleEvents(viewModel = userViewModel)

    val isLoading = userState.isLoading
    val error = userState.error

    AppLayout(route = Route.Settings) {
        AppLazyColumn {
            item {
                ProfileCard(user = user, isLoading = isLoading, error = error, onLogout = {
                    authViewModel.logout()

                    navBackStack.resetTo(Route.Welcome)
                })
            }

            item {
                val routes = listOf(Route.Couple)

                CardStackList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = routes.map { route ->
                        val details = route.getDetails()

                        CardStackListItem(
                            title = details.title,
                            leadingContent = {
                                Icon(
                                    icon = details.icon.default,
                                )
                            },
                            onClick = {
                                navBackStack.add(element = route)
                            }
                        )
                    }
                )
            }

            item {
                val routes =
                    listOf(Route.Appearance, Route.Languages, Route.About)

                CardStackList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = routes.map { route ->
                        val details = route.getDetails()

                        CardStackListItem(
                            title = details.title,
                            description = details.description,
                            leadingContent = {
                                Icon(
                                    icon = details.icon.default,
                                )
                            },
                            onClick = {
                                navBackStack.add(element = route)
                            },
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileCard(user: User?, isLoading: Boolean, error: String?, onLogout: () -> Unit) {
    CardStackList(
        modifier = Modifier.padding(horizontal = 16.dp),
        items = listOf(
            CardStackListItem(
                title = when (true) {
                    isLoading -> stringResource(id = R.string.loading)
                    (error != null) -> stringResource(id = R.string.api_error)
                    else -> user?.name ?: ""
                },
                leadingContent = {
                    Avatar(picture = user?.picture)
                },
                trailingContent = {
                    Button(onClick = onLogout) {
                        Text(text = stringResource(id = R.string.sign_out))
                    }
                },
                contentPadding = PaddingValues(vertical = 12.dp),
                contentSize = null
            ),
        )
    )
}
