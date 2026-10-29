package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.motion.slideInY
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails
import dev.cankolay.twodo.android.presentation.navigation.route.navigationRoutes
import dev.cankolay.twodo.android.presentation.navigation.route.resetTo

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.AppToolbar() {
    val navBackStack = LocalNavBackStack.current

    val route = navBackStack.lastOrNull() ?: Route.Notes
    val isVisible = navigationRoutes.any {
        it == route
    }

    AnimatedVisibility(
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -ScreenOffset - 16.dp)
                .zIndex(1f),
        visible = isVisible,
        enter = fadeIn() + scaleIn() + slideInY(),
        exit = fadeOut(),
    ) {
        HorizontalFloatingToolbar(
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            expanded = false,
        ) {
            val appRowRoutes = navigationRoutes.map { it to it.getDetails() }

            AppBarRow {
                appRowRoutes.forEach { (destination, details) ->
                    val isSelected = navBackStack.lastOrNull() == destination

                    clickableItem(
                        label = details.title,
                        icon = {
                            Icon(icon = details.icon.forSelection(selected = isSelected))
                        },
                        onClick = {
                            if (!isSelected) {
                                navBackStack.resetTo(route = destination)
                            }
                        },
                    )
                }
            }
        }
    }
}
