package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskUiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskActionsSheet(
    state: TaskUiState,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onRetryMembers: () -> Unit,
    onDate: (LocalDate?) -> Unit,
    onAssignee: (String?) -> Unit,
    onCompleted: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    AppBottomSheet(
        title = state.editor.title.ifBlank { stringResource(id = R.string.task_actions) },
        onDismiss = onDismiss,
        result = state.membersLoad,
        onRefresh = onRetryMembers,
    ) {
        item {
            TaskStatusCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                state = state,
                enabled = enabled,
                onAssignee = onAssignee,
                onCompleted = onCompleted,
            )
        }

        item {
            TaskDetailsCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                state = state,
                enabled = enabled,
                onDate = onDate,
                onDelete = onDelete,
            )
        }
    }
}
