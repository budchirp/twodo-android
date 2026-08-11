package dev.cankolay.twodo.android.presentation.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.cankolay.twodo.android.presentation.navigation.route.Route

fun NavBackStack<NavKey>.resetTo(route: Route) {
    add(route)
    while (size > 1) removeAt(0)
}
