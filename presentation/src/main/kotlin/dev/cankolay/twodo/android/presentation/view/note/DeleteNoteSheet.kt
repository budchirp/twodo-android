package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeleteNoteSheet(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    DestructiveConfirmationSheet(
        title = stringResource(id = R.string.delete_note),
        description = stringResource(id = R.string.delete_note_desc),
        confirmText = stringResource(id = R.string.delete),
        onDismiss = onDismiss,
        onConfirm = onDelete
    )
}
