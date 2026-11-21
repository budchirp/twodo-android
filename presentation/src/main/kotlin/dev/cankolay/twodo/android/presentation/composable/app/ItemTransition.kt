package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import dev.cankolay.twodo.android.presentation.composable.rememberDeviceCornerRadius
import dev.cankolay.twodo.android.presentation.composition.LocalItemTransitionScope

internal const val ItemTransitionDurationMillis = 300

@Composable
internal fun ItemTransition(
    key: String?,
    detail: Boolean,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scope = LocalItemTransitionScope.current

    if (scope == null || key == null || !enabled) {
        Box(modifier = modifier.rotate(if (detail) 0f else rotation)) { content() }
        return
    }

    val visibility = LocalNavAnimatedContentScope.current
    val screenCorner = rememberDeviceCornerRadius()
    val corner by visibility.transition.animateDp(label = "itemCorner") { state ->
        if ((state == EnterExitState.Visible) == detail) screenCorner else 24.dp
    }
    val angle by visibility.transition.animateFloat(label = "itemRotation") { state ->
        if ((state == EnterExitState.Visible) == detail) 0f else rotation
    }
    val shape = RoundedCornerShape(size = corner)

    with(scope) {
        Box(
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = key),
                    animatedVisibilityScope = visibility,
                    boundsTransform = { _, _ -> tween(durationMillis = ItemTransitionDurationMillis) },
                    clipInOverlayDuringTransition = OverlayClip(clipShape = shape),
                )
                .then(modifier)
                .rotate(angle)
                .clip(shape),
        ) {
            content()
        }
    }
}
