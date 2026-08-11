package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.navigation.resetTo
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails
import dev.cankolay.twodo.android.presentation.navigation.route.navigationRoutes

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigationRail() {
    val navBackStack = LocalNavBackStack.current

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        windowInsets = WindowInsets.systemBars
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            navigationRoutes.forEach { route ->
                val details = route.getDetails()

                val isSelected = navBackStack.lastOrNull() == route

                NavigationRailItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navBackStack.resetTo(route)
                        }
                    },
                    icon = {
                        Icon(
                            icon = if (isSelected) details.icon.default else details.icon.outlined
                                ?: details.icon.default
                        )
                    }
                )
            }
        }
    }
}
