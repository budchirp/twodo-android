package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor

@Composable
internal fun NoteEditor(
    note: Note,
    readOnly: Boolean,
    onContentChange: (String) -> Unit,
    onOpenActions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        MarkdownEditor(
            markdown = note.content,
            onContentChange = onContentChange,
            readOnly = readOnly,
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 64.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(onClick = onOpenActions, enabled = !readOnly) {
                    Icon(icon = Icons.Default.MoreVert)
                }
            }
        }
    }
}
