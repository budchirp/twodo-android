package dev.cankolay.twodo.android.presentation.view.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.view.task.TaskGrid

internal fun LazyListScope.homeTasksSection(
    tasks: List<Task>,
    mutation: ApiResult<Unit>?,
    onRetryToggle: () -> Unit,
    onToggle: (Task) -> Unit,
    onNavigate: (Route) -> Unit,
) {
    homeHeading(key = "tasks", title = R.string.latest_tasks)

    if (mutation is ApiResult.Failure) {
        item(key = "task-mutation-error") {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = mutation.error.message,
                onRefresh = onRetryToggle,
            )
        }
    }

    item(key = "tasks-preview") {
        if (tasks.isEmpty()) {
            SettingRow(
                title = stringResource(id = R.string.tasks_empty_title),
                onClick = { onNavigate(Route.Task()) },
            )
        } else {
            TaskGrid(
                tasks = tasks.take(4),
                enabled = mutation != ApiResult.Loading,
                onTaskClick = { onNavigate(Route.Task(id = it.id)) },
                onToggle = onToggle,
            )
        }
    }

    item(key = "all-tasks") {
        HomeSectionLink(
            label = stringResource(id = R.string.go_to_all_tasks),
            onClick = { onNavigate(Route.Tasks) },
        )
    }
}
