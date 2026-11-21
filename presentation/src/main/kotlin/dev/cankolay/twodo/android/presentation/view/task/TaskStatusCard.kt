package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppCard
import dev.cankolay.twodo.android.presentation.composable.app.AppCardDefaults
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow
import dev.cankolay.twodo.android.presentation.composable.app.SettingRowSize
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskUiState

@Composable
internal fun TaskStatusCard(
    state: TaskUiState,
    enabled: Boolean,
    onAssignee: (String?) -> Unit,
    onCompleted: (Boolean) -> Unit,
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
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon = Icons.Default.Groups, tint = MaterialTheme.colorScheme.primary)

                    Text(
                        text = stringResource(id = R.string.task_responsibility),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                val choices = listOf("") + state.members.map { it.id }
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    choices.forEachIndexed { index, memberId ->
                        SegmentedButton(
                            selected = editor.assignedToUserId.orEmpty() == memberId,
                            enabled = enabled && state.membersLoad != ApiResult.Loading,
                            onClick = { onAssignee(memberId.ifEmpty { null }) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = choices.size
                            ),
                        ) {
                            val label = when (memberId) {
                                "" -> stringResource(id = R.string.task_shared)
                                state.userId -> stringResource(id = R.string.task_me)
                                else -> state.members.firstOrNull { it.id == memberId }?.name.orEmpty()
                            }

                            Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                val errors = (editor.save as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()
                    .filter { it.field == "assignedToUserId" }
                if (errors.isNotEmpty()) {
                    Text(
                        text = errors.flatMap { it.messages }.joinToString(separator = "\n"),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        if (editor.original != null) {
            TaskDetailItem {
                SettingRow(
                    modifier = Modifier.toggleable(
                        value = editor.completed,
                        enabled = enabled,
                        role = Role.Switch,
                        onValueChange = onCompleted,
                    ),
                    title = stringResource(id = R.string.task_completed),
                    enabled = enabled,
                    rowSize = SettingRowSize.Compact,
                    leadingContent = { Icon(icon = Icons.Default.TaskAlt) },
                    trailingContent = {
                        Switch(
                            checked = editor.completed,
                            enabled = enabled,
                            onCheckedChange = null
                        )
                    },
                )
            }
        }
    }
}
