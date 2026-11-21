package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.SettingCard
import dev.cankolay.twodo.android.presentation.viewmodel.task.TasksUiState

internal fun LazyListScope.tasksList(
    state: TasksUiState,
    onRefresh: () -> Unit,
    onRetryToggle: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onCreate: () -> Unit,
    onToggle: (Task) -> Unit,
) {
    val load = state.load
    val toggle = state.toggle
    val tasks = state.tasks

    if (load is ApiResult.Failure) {
        item(key = "tasks-load-error") {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = load.error.message,
                onRefresh = onRefresh,
            )
        }
    }

    if (toggle is ApiResult.Failure) {
        item(key = "tasks-toggle-error") {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = toggle.error.message,
                onRefresh = onRetryToggle,
            )
        }
    }

    if (tasks != null) {
        item(key = "tasks") {
            if (tasks.isEmpty()) {
                SettingCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.tasks_empty_title),
                    description = stringResource(id = R.string.tasks_empty_description),
                    onClick = onCreate,
                )
            } else {
                TaskGrid(
                    tasks = tasks,
                    enabled = toggle != ApiResult.Loading,
                    onTaskClick = onTaskClick,
                    onToggle = onToggle,
                )
            }
        }
    }
}
