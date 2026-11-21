package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow

internal fun LazyListScope.notesList(
    notes: List<Note>?,
    load: ApiResult<Unit>?,
    onRefresh: (() -> Unit)? = null,
    onNoteClick: (String) -> Unit,
    selectedId: String? = null,
    onCreate: (() -> Unit)? = null,
) {
    if (load is ApiResult.Failure) {
        item(key = "notes-error") {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = load.error.message,
                onRefresh = onRefresh,
            )
        }
    }

    if (notes != null) {
        if (notes.isEmpty()) {
            item(key = "notes-empty") {
                SettingRow(
                    title = stringResource(id = R.string.notes_empty_title),
                    leadingContent = { Icon(icon = Icons.Default.Edit) },
                    onClick = onCreate,
                )
            }
        } else {
            items(items = notes, key = { "note-${it.id}" }) { note ->
                NoteItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    note = note,
                    selected = selectedId == note.id,
                    onClick = { onNoteClick(note.id) },
                )
            }
        }
    }
}
