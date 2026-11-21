package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.ItemTransition
import dev.cankolay.twodo.android.presentation.composable.app.SettingCard
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppRefreshableLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.task.TasksUiState
import dev.cankolay.twodo.android.presentation.viewmodel.task.TasksViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TasksView(onTaskClick: (String?) -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TasksContent(
        state = state,
        actions =
            TasksActions(
                onRefresh = { viewModel.fetch(forceRefresh = true) },
                onRetryToggle = viewModel::retryToggle,
                onTaskClick = { task -> onTaskClick(task.id) },
                onCreate = { onTaskClick(null) },
                onToggle = viewModel::toggle,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksContent(state: TasksUiState, actions: TasksActions) {
    AppLayout(
        route = Route.Tasks,
        topBar = { context ->
            AppTopAppBar(
                context = context,
                trailingContent = {
                    AppIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_task),
                        onClick = actions.onCreate,
                    )
                },
            )
        },
    ) {
        AppRefreshableLazyColumn(
            isLoading = state.load == ApiResult.Loading || state.toggle == ApiResult.Loading,
            onRefresh = actions.onRefresh,
        ) {
            tasksList(state = state, actions = actions)
        }
    }
}

private fun LazyListScope.tasksList(state: TasksUiState, actions: TasksActions) {
    val load = state.load
    val toggle = state.toggle
    val tasks = state.tasks

    if (load is ApiResult.Failure) {
        item {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = load.error.message,
                onRefresh = actions.onRefresh,
            )
        }
    }

    if (toggle is ApiResult.Failure) {
        item {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = toggle.error.message,
                onRefresh = actions.onRetryToggle,
            )
        }
    }

    if (tasks != null) {
        item {
            if (tasks.isEmpty()) {
                SettingCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.tasks_empty_title),
                    description = stringResource(id = R.string.tasks_empty_description),
                    onClick = actions.onCreate,
                )
            } else {
                TaskGrid(
                    tasks = tasks,
                    enabled = toggle != ApiResult.Loading,
                    actions =
                        TaskItemActions(
                            onTaskClick = actions.onTaskClick,
                            onToggle = actions.onToggle,
                        ),
                )
            }
        }
    }
}

@Composable
internal fun TaskGrid(tasks: List<Task>, enabled: Boolean, actions: TaskItemActions) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 300))
                .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        tasks.chunked(2).forEach { row ->
            key(row.first().id) { TaskRow(tasks = row, enabled = enabled, actions = actions) }
        }
    }
}

@Composable
private fun TaskRow(tasks: List<Task>, enabled: Boolean, actions: TaskItemActions) {
    Row(
        modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        tasks.forEach { task ->
            key(task.id) {
                TaskCard(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .fillMaxHeight(),
                    task = task,
                    enabled = enabled,
                    actions = actions,
                )
            }
        }

        if (tasks.size == 1) {
            Spacer(modifier = Modifier.weight(weight = 1f))
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    actions: TaskItemActions,
) {
    val style = taskCardStyle(id = task.id)

    ItemTransition(
        key = "task:${task.id}",
        detail = false,
        modifier = modifier,
        rotation = style.rotation,
    ) {
        TaskCardContent(task = task, style = style, enabled = enabled, actions = actions)
    }
}

@Composable
private fun TaskCardContent(
    task: Task,
    style: TaskCardStyle,
    enabled: Boolean,
    actions: TaskItemActions,
) {
    val completionLabel =
        stringResource(
            id = if (task.completed) R.string.reopen_task else R.string.complete_task,
            task.title,
        )
    val completionState =
        stringResource(
            id = if (task.completed) R.string.task_completed else R.string.task_incomplete
        )

    Card(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(shape = MaterialTheme.shapes.large)
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Spacebar) {
                        if (enabled && event.type == KeyEventType.KeyUp) actions.onToggle(task)
                        true
                    } else {
                        false
                    }
                }
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = stringResource(id = R.string.edit_task),
                    onClick = { actions.onTaskClick(task) },
                    onDoubleClick = { if (enabled) actions.onToggle(task) },
                )
                .semantics {
                    stateDescription = completionState
                    customActions =
                        if (enabled) {
                            listOf(
                                CustomAccessibilityAction(label = completionLabel) {
                                    actions.onToggle(task)
                                    true
                                }
                            )
                        } else {
                            emptyList()
                        }
                },
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = style.background,
                contentColor = style.foreground,
            ),
    ) {
        TaskCardInfo(task = task, style = style)
    }
}

@Composable
private fun TaskCardInfo(task: Task, style: TaskCardStyle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium,
            textDecoration = if (task.completed) TextDecoration.LineThrough else null,
            color = style.foreground,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        if (task.description.isNotBlank()) {
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge,
                color = style.foreground.copy(alpha = 0.85f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        task.dueDate?.let { due ->
            val today = LocalDate.now()
            val formatted =
                due.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(LocalLocale.current.platformLocale)
                )
            val label =
                when {
                    !task.completed && due < today ->
                        stringResource(id = R.string.task_overdue_date, formatted)

                    !task.completed && due == today -> stringResource(id = R.string.task_due_today)
                    else -> formatted
                }
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
internal fun taskCardStyle(id: String): TaskCardStyle {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    return remember(id, dark) {
        val colors =
            if (dark) {
                listOf(
                    0xFF70501A,
                    0xFF71364D,
                    0xFF255E42,
                    0xFF2A527E,
                    0xFF5B3E80,
                    0xFF783F23,
                    0xFF7E3042,
                    0xFF1C6364,
                )
            } else {
                listOf(
                    0xFFFFCF56,
                    0xFFFF9ABC,
                    0xFF9AE6AD,
                    0xFF91C8FF,
                    0xFFC8A0FF,
                    0xFFFFAB70,
                    0xFFFF8F9E,
                    0xFF6ADCD1,
                )
            }
        val seed = id.hashCode().toUInt()

        TaskCardStyle(
            background = Color(colors[(seed % colors.size.toUInt()).toInt()]),
            foreground = if (dark) Color(0xFFF9F3EA) else Color(0xFF292725),
            rotation = (if (seed % 2u == 0u) 1f else -1f) * (0.6f + (seed / 7u % 5u).toInt() * 0.2f),
        )
    }
}

internal data class TasksActions(
    val onRefresh: () -> Unit,
    val onRetryToggle: () -> Unit,
    val onTaskClick: (Task) -> Unit,
    val onCreate: () -> Unit,
    val onToggle: (Task) -> Unit,
)

internal data class TaskItemActions(val onTaskClick: (Task) -> Unit, val onToggle: (Task) -> Unit)

internal data class TaskCardStyle(val background: Color, val foreground: Color, val rotation: Float)
