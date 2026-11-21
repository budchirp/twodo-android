package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.cankolay.twodo.android.presentation.composable.app.ItemTransition
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout

@Composable
internal fun NoteTransition(
    id: String,
    detail: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ItemTransition(
        key = "note:$id",
        detail = detail,
        modifier = modifier,
        enabled = LocalNotesLayout.current?.navigation?.isExpanded != true,
        content = content,
    )
}
