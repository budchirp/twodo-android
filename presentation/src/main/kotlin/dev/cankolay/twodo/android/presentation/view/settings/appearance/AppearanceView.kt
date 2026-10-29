package dev.cankolay.twodo.android.presentation.view.settings.appearance

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails

@Composable
fun AppearanceView(onNavigate: (Route) -> Unit) {
    AppearanceContent(onNavigate = onNavigate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceContent(onNavigate: (Route) -> Unit) {
    AppLayout(route = Route.Appearance) {
        AppLazyColumn {
            item {
                val routes =
                    listOf(
                        Route.Theme,
                        Route.MaterialYou,
                    )

                SettingsGroup(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items =
                        routes.map { route ->
                            val details = route.getDetails()

                            SettingItem(
                                title = details.title,
                                description = details.description,
                                onClick = {
                                    onNavigate(route)
                                },
                                leadingContent = {
                                    Icon(icon = details.icon.default)
                                },
                            )
                        },
                )
            }
        }
    }
}
