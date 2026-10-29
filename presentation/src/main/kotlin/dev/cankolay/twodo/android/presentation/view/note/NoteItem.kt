package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout

@Composable
internal fun NoteItem(
    note: Note,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isExpanded = LocalNotesLayout.current?.navigation?.isExpanded != false

    Box(modifier = modifier) {
        NoteTransition(id = note.id, detail = false, modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = if (isExpanded) MaterialTheme.shapes.large else RectangleShape,
                color =
                    when {
                        isExpanded && selected -> MaterialTheme.colorScheme.secondaryContainer

                        else -> MaterialTheme.colorScheme.surfaceContainer
                    },
            ) {
                Text(
                    text = note.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                )
            }
        }
    }
}
