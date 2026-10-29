package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import dev.cankolay.twodo.android.presentation.composable.rememberDeviceCornerRadius
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout

private data class NoteKey(val id: String)

@Composable
internal fun NoteTransition(
    id: String,
    detail: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val layout = LocalNotesLayout.current

    if (layout == null || layout.navigation.isExpanded) {
        Box(modifier = modifier, content = { content() })

        return
    }

    val visibility = LocalNavAnimatedContentScope.current

    val screenCorner = rememberDeviceCornerRadius()

    val corner by
    visibility.transition.animateDp(label = "noteCorner") { state ->
        when {
            (state == EnterExitState.Visible) == detail -> screenCorner

            else -> 24.dp
        }
    }

    val shape = RoundedCornerShape(size = corner)

    with(receiver = layout.transition) {
        Box(
            modifier =
                Modifier
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = NoteKey(id = id)),
                        animatedVisibilityScope = visibility,
                        boundsTransform = { _, _ -> tween(durationMillis = 300) },
                        clipInOverlayDuringTransition = OverlayClip(clipShape = shape),
                    )
                    .then(other = modifier)
                    .clip(shape = shape),
            content = { content() },
        )
    }
}
