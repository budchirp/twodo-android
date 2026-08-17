package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteActionsSheet(
    title: String,
    updatedAt: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AppBottomSheet(
        title = title,
        onDismiss = onDismiss
    ) {
        item {
            CardStackList(
                items = listOf(
                    CardStackListItem(
                        title = stringResource(
                            id = R.string.edited_at,
                            DateUtils.format(updatedAt, DateUtils.DATE_TIME_PATTERN)
                        ),
                        leadingContent = { Icon(icon = Icons.Default.Update) }
                    ),
                    CardStackListItem(
                        title = stringResource(id = R.string.delete),
                        onClick = onDelete,
                        leadingContent = { Icon(icon = Icons.Default.Delete) }
                    )
                )
            )
        }
    }
}
