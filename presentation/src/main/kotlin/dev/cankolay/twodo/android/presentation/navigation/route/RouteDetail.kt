package dev.cankolay.twodo.android.presentation.navigation.route

import androidx.compose.ui.graphics.vector.ImageVector

data class RouteDetailIcon(
    val default: ImageVector,
    val outlined: ImageVector? = null,
) {
    fun forSelection(selected: Boolean): ImageVector =
        if (selected) default else outlined ?: default
}

data class RouteDetail(
    val title: String,
    val description: String = title,
    val icon: RouteDetailIcon,
)
