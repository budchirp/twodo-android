package dev.cankolay.twodo.android.presentation.view.task

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.ItemTransition
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet
import dev.cankolay.twodo.android.presentation.composable.app.layout.EditorLayout
import dev.cankolay.twodo.android.presentation.core.HandleNavigation
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskUiState
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskViewModel

@Composable
fun TaskView(
    id: String?,
    onBack: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val style = id?.let { taskCardStyle(id = it) }
    var showDelete by rememberSaveable { mutableStateOf(value = false) }
    var showActions by rememberSaveable { mutableStateOf(value = false) }
    val mutating =
        state.editor.save == ApiResult.Loading || state.editor.delete == ApiResult.Loading

    LaunchedEffect(id) { viewModel.initialize(id = id) }
    HandleNavigation(result = true.takeIf { state.finished }) { onBack() }
    BackHandler(enabled = mutating) { }

    TaskTheme(background = style?.background) {
        ItemTransition(
            key = id?.let { "task:$it" },
            detail = true,
            modifier = Modifier.fillMaxSize(),
            rotation = style?.rotation ?: 0f,
        ) {
            TaskContent(
                id = id,
                state = state,
                onBack = { if (!mutating) onBack() },
                onTitle = viewModel::updateTitle,
                onDescription = viewModel::updateDescription,
                onRetry = viewModel::load,
                onSave = viewModel::save,
                onOpenActions = { showActions = true },
            )
        }

        if (showActions) {
            TaskActionsSheet(
                state = state,
                enabled = !state.editor.busy && (id == null || state.editor.original != null),
                onDismiss = { showActions = false },
                onRetryMembers = viewModel::loadMembers,
                onDate = viewModel::updateDueDate,
                onAssignee = viewModel::updateAssignee,
                onCompleted = viewModel::updateCompleted,
                onDelete = {
                    showActions = false
                    showDelete = true
                },
            )
        }

        if (showDelete) {
            DestructiveConfirmationSheet(
                title = stringResource(id = R.string.delete_task),
                description = stringResource(id = R.string.delete_task_description),
                confirmText = stringResource(id = R.string.delete),
                result = state.editor.delete,
                onConfirm = viewModel::delete,
                onDismiss = {
                    if (!mutating) {
                        showDelete = false
                        viewModel.clearDeleteError()
                    }
                },
            )
        }
    }
}

@Composable
private fun TaskContent(
    id: String?,
    state: TaskUiState,
    onBack: () -> Unit,
    onTitle: (String) -> Unit,
    onDescription: (String) -> Unit,
    onRetry: () -> Unit,
    onSave: () -> Unit,
    onOpenActions: () -> Unit,
) {
    val editor = state.editor
    val canEdit = !editor.busy && (id == null || editor.original != null)
    val inset = Modifier.padding(horizontal = 16.dp)

    EditorLayout(
        route = Route.Task(id = id),
        title = {
            TaskEditorTitle(state = editor, onTitle = onTitle, enabled = canEdit)
        },
        onBack = onBack,
        onSave = onSave,
        saveLabel = stringResource(id = if (id == null) R.string.create else R.string.save),
        actionsLabel = stringResource(id = R.string.task_actions),
        onOpenActions = onOpenActions,
        canEdit = canEdit,
        isLoading = editor.load == ApiResult.Loading || editor.save == ApiResult.Loading,
    ) {
        AppLazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            val load = editor.load
            val save = editor.save

            if (load is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = inset,
                        error = load.error.message,
                        onRefresh = onRetry,
                    )
                }
            }
            if (save is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = inset,
                        error = save.error.message,
                        onRefresh = onSave,
                    )
                }
            }

            item {
                Column(modifier = inset) {
                    TaskEditorDescription(
                        state = editor,
                        onDescription = onDescription,
                        enabled = canEdit,
                    )
                }
            }
        }
    }
}
