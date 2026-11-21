package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.util.DateUtils
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteActionsSheet(state: NoteEditorUiState, actions: NoteActions) {
    val note = state.note ?: return

    AppBottomSheet(
        title = (state.draft ?: note).title.ifBlank { stringResource(id = R.string.notes) },
        onDismiss = actions.onDismissSheet,
    ) {
        item {
            SettingsGroup(
                modifier = Modifier.padding(horizontal = 16.dp),
                items =
                    listOf(
                        SettingItem(
                            title = stringResource(id = R.string.created_at),
                            description =
                                DateUtils.format(
                                    value = note.createdAt,
                                    pattern = DateUtils.DATE_TIME_PATTERN,
                                ),
                            leadingContent = { Icon(icon = Icons.Default.Schedule) },
                        ),
                        SettingItem(
                            title = stringResource(id = R.string.updated_at),
                            description =
                                DateUtils.format(
                                    value = note.updatedAt,
                                    pattern = DateUtils.DATE_TIME_PATTERN,
                                ),
                            leadingContent = { Icon(icon = Icons.Default.Update) },
                        ),
                        SettingItem(
                            title = stringResource(id = R.string.delete),
                            onClick = actions.onRequestDelete,
                            leadingContent = { Icon(icon = Icons.Default.Delete) },
                        ),
                    ),
            )
        }
    }
}
