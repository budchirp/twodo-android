package dev.cankolay.twodo.android.presentation.composition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import dev.cankolay.twodo.android.presentation.navigation.resetTo
import dev.cankolay.twodo.android.presentation.navigation.route.Route

val LocalNavBackStack =
    staticCompositionLocalOf<NavBackStack<NavKey>> { error("Not provided") }

@Composable
fun ProvideNavBackStack(startRoute: Route, content: @Composable () -> Unit) {
    val navBackStack = rememberNavBackStack(startRoute)

    LaunchedEffect(key1 = startRoute) {
        if (navBackStack.lastOrNull().shouldReplaceWith(route = startRoute)) {
            navBackStack.resetTo(startRoute)
        }
    }

    CompositionLocalProvider(
        value = LocalNavBackStack provides navBackStack,
    ) {
        content()
    }
}

private fun NavKey?.shouldReplaceWith(route: Route) =
    when (route) {
        Route.Welcome -> this != Route.Welcome
        Route.StartupError -> this != Route.StartupError
        Route.ProfileSetup -> this != Route.ProfileSetup
        Route.CoupleSetup -> this != Route.CoupleSetup
        Route.Notes -> this == null ||
                this == Route.Welcome ||
                this == Route.StartupError ||
                this == Route.ProfileSetup ||
                this == Route.CoupleSetup

        else -> this != route
    }
