package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow
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
                shape = MaterialTheme.shapes.large,
                color = if (isExpanded && selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainer,
            ) {
                SettingRow(title = note.title, onClick = onClick)
            }
        }
    }
}
