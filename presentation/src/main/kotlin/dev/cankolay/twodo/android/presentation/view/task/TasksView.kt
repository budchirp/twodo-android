package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppRefreshableLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.task.TasksUiState
import dev.cankolay.twodo.android.presentation.viewmodel.task.TasksViewModel

@Composable
fun TasksView(
    onTaskClick: (String?) -> Unit,
    viewModel: TasksViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TasksContent(
        state = state,
        onRefresh = { viewModel.fetch(forceRefresh = true) },
        onRetryToggle = viewModel::retryToggle,
        onTaskClick = { task -> onTaskClick(task.id) },
        onCreate = { onTaskClick(null) },
        onToggle = viewModel::toggle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksContent(
    state: TasksUiState,
    onRefresh: () -> Unit,
    onRetryToggle: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onCreate: () -> Unit,
    onToggle: (Task) -> Unit,
) {
    AppLayout(
        route = Route.Tasks,
        topBar = { context ->
            AppTopAppBar(
                context = context,
                trailingContent = {
                    AppIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_task),
                        onClick = onCreate,
                    )
                },
            )
        },
    ) {
        AppRefreshableLazyColumn(
            isLoading = state.load == ApiResult.Loading || state.toggle == ApiResult.Loading,
            onRefresh = onRefresh,
        ) {
            tasksList(
                state = state,
                onRefresh = onRefresh,
                onRetryToggle = onRetryToggle,
                onTaskClick = onTaskClick,
                onCreate = onCreate,
                onToggle = onToggle,
            )
        }
    }
}
