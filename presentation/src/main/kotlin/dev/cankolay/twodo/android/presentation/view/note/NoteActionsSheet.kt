package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteActionsSheet(
    title: String,
    updatedAt: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AppBottomSheet(
        title = title,
        onDismiss = onDismiss,
    ) {
        item {
            SettingsGroup(
                modifier = Modifier.padding(horizontal = 16.dp),
                items =
                    listOf(
                        SettingItem(
                            title =
                                stringResource(
                                    id = R.string.edited_at,
                                    DateUtils.format(
                                        value = updatedAt,
                                        pattern = DateUtils.DATE_TIME_PATTERN,
                                    ),
                                ),
                            leadingContent = { Icon(icon = Icons.Default.Update) },
                        ),
                        SettingItem(
                            title = stringResource(id = R.string.delete),
                            onClick = onDelete,
                            leadingContent = { Icon(icon = Icons.Default.Delete) },
                        ),
                    ),
            )
        }
    }
}
