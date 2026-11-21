package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppCard
import dev.cankolay.twodo.android.presentation.composable.app.AppCardDefaults
import dev.cankolay.twodo.android.presentation.composable.app.DateSetting
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow
import dev.cankolay.twodo.android.presentation.util.DateUtils
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskUiState
import sv.lib.squircleshape.SquircleShape
import java.time.LocalDate

@Composable
internal fun TaskDetailsCard(
    state: TaskUiState,
    enabled: Boolean,
    onDate: (LocalDate?) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editor = state.editor

    AppCard(
        modifier = modifier,
        color = Color.Transparent,
        contentPadding = AppCardDefaults.NoPadding,
        verticalArrangement = Arrangement.spacedBy(space = AppCardDefaults.GroupSpacing),
    ) {
        TaskDetailItem {
            DateSetting(
                title = stringResource(id = R.string.task_due_date),
                value = editor.dueDate,
                enabled = enabled,
                onChange = onDate,
                leadingContent = { Icon(icon = Icons.Default.CalendarMonth) },
            )
        }

        val errors = (editor.save as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()
            .filter { it.field == "dueDate" }
        if (errors.isNotEmpty()) {
            TaskDetailItem {
                Text(
                    modifier = Modifier.padding(all = 16.dp),
                    text = errors.flatMap { it.messages }.joinToString(separator = "\n"),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        editor.original?.let { task ->
            TaskDetailItem {
                SettingRow(
                    title = stringResource(id = R.string.created_at),
                    description = DateUtils.format(
                        value = task.createdAt,
                        pattern = DateUtils.DATE_TIME_PATTERN,
                    ),
                    leadingContent = { Icon(icon = Icons.Default.Schedule) },
                )
            }

            TaskDetailItem {
                SettingRow(
                    title = stringResource(id = R.string.updated_at),
                    description = DateUtils.format(
                        value = task.updatedAt,
                        pattern = DateUtils.DATE_TIME_PATTERN,
                    ),
                    leadingContent = { Icon(icon = Icons.Default.Update) },
                )
            }

            TaskDetailItem {
                SettingRow(
                    title = stringResource(id = R.string.delete),
                    onClick = onDelete,
                    enabled = enabled,
                    leadingContent = { Icon(icon = Icons.Default.Delete) },
                )
            }
        }
    }
}

@Composable
internal fun TaskDetailItem(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = SquircleShape(radius = 4.dp),
        content = content,
    )
}
