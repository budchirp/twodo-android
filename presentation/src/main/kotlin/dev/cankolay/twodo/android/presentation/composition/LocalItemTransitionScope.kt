package dev.cankolay.twodo.android.presentation.composition

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalItemTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
