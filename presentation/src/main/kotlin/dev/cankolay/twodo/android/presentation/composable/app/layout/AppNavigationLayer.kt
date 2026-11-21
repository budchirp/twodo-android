package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

private const val NavigationZIndex = 1000f

internal fun Modifier.navigationLayer(): Modifier = zIndex(zIndex = NavigationZIndex)
