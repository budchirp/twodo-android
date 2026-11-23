package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.motion.slideInY
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails
import dev.cankolay.twodo.android.presentation.navigation.route.navigationRoutes
import dev.cankolay.twodo.android.presentation.navigation.route.resetTo
import kotlin.math.roundToInt

private const val IslandExitDurationMillis = 360
private const val IslandFadeDelayMillis = 140

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.AppToolbar() {
    val navBackStack = LocalNavBackStack.current

    val route = navBackStack.lastOrNull() ?: Route.Home

    AnimatedVisibility(
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -ScreenOffset - 16.dp)
                .navigationLayer(),
        visible = route in navigationRoutes,
        enter = fadeIn() + scaleIn() + slideInY(),
        exit =
            slideOutVertically(
                animationSpec = tween(
                    durationMillis = IslandExitDurationMillis,
                    easing = LinearEasing
                ),
                targetOffsetY = { (it * 0.4f).roundToInt() },
            ) +
                    scaleOut(
                        animationSpec = tween(
                            durationMillis = IslandExitDurationMillis,
                            easing = FastOutSlowInEasing
                        ),
                        targetScale = 0.82f,
                        transformOrigin = TransformOrigin(
                            pivotFractionX = 0.5f,
                            pivotFractionY = 1f
                        ),
                    ) +
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = IslandExitDurationMillis - IslandFadeDelayMillis,
                            delayMillis = IslandFadeDelayMillis,
                        ),
                    ),
    ) {
        HorizontalFloatingToolbar(
            colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
            expanded = false,
        ) {
            val appRowRoutes = navigationRoutes.map { it to it.getDetails() }

            AppBarRow {
                appRowRoutes.forEach { (destination, details) ->
                    val isSelected =
                        navBackStack.lastOrNull { it in navigationRoutes } == destination

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
