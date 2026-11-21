package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import dev.cankolay.twodo.android.presentation.composable.rememberDeviceCornerRadius
import dev.cankolay.twodo.android.presentation.composition.LocalItemTransitionScope

internal const val ItemTransitionDurationMillis = 420
private const val ItemReturnDurationMillis = 380
private val ItemExpandEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val ItemReturnEasing = CubicBezierEasing(0.2f, 0f, 0.2f, 1f)

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
    val corner by
    visibility.transition.animateDp(
        transitionSpec = {
            itemMorphSpec(expanding = (targetState == EnterExitState.Visible) == detail)
        },
        label = "itemCorner",
    ) { state ->
        if ((state == EnterExitState.Visible) == detail) screenCorner else 24.dp
    }
    val angle by
    visibility.transition.animateFloat(
        transitionSpec = {
            itemMorphSpec(expanding = (targetState == EnterExitState.Visible) == detail)
        },
        label = "itemRotation",
    ) { state ->
        if ((state == EnterExitState.Visible) == detail) 0f else rotation
    }
    val shape = RoundedCornerShape(size = corner)

    with(scope) {
        Box(
            modifier =
                Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = key),
                        animatedVisibilityScope = visibility,
                        enter =
                            fadeIn(
                                animationSpec =
                                    tween(
                                        durationMillis = if (detail) 260 else 160,
                                        delayMillis = if (detail) 60 else 140,
                                        easing = LinearOutSlowInEasing,
                                    )
                            ),
                        exit =
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis = 180,
                                        delayMillis = if (detail) 100 else 0,
                                        easing = FastOutLinearInEasing,
                                    )
                            ),
                        boundsTransform = { initial, target ->
                            itemMorphSpec(
                                expanding =
                                    target.width * target.height >= initial.width * initial.height
                            )
                        },
                        resizeMode =
                            scaleToBounds(
                                contentScale = ContentScale.FillWidth,
                                alignment = Alignment.TopStart,
                            ),
                        clipInOverlayDuringTransition = OverlayClip(clipShape = shape),
                    )
                    .then(modifier)
                    .rotate(angle)
                    .clip(shape)
        ) {
            content()
        }
    }
}

private fun <T> itemMorphSpec(expanding: Boolean): TweenSpec<T> =
    tween(
        durationMillis = if (expanding) ItemTransitionDurationMillis else ItemReturnDurationMillis,
        easing = if (expanding) ItemExpandEasing else ItemReturnEasing,
    )
