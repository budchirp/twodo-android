package dev.cankolay.twodo.android.presentation.view.task

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.TaskLimits
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.EditorTextInput
import dev.cankolay.twodo.android.presentation.composable.app.ItemTransition
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.EditorLayout
import dev.cankolay.twodo.android.presentation.core.HandleNavigation
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskEditorState
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskUiState
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskViewModel
import java.time.LocalDate

@Composable
fun TaskView(id: String?, onBack: () -> Unit, viewModel: TaskViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDelete by rememberSaveable { mutableStateOf(value = false) }
    var showActions by rememberSaveable { mutableStateOf(value = false) }
    val mutating =
        state.editor.save == ApiResult.Loading || state.editor.delete == ApiResult.Loading

    LaunchedEffect(id) { viewModel.initialize(id = id) }
    HandleNavigation(result = true.takeIf { state.finished }) { onBack() }
    BackHandler(enabled = mutating) {}

    TaskContent(
        state = state,
        layout = TaskLayout(id = id, showActions = showActions, showDelete = showDelete),
        actions =
            TaskActions(
                onBack = { if (!mutating) onBack() },
                onTitle = viewModel::updateTitle,
                onDescription = viewModel::updateDescription,
                onRetry = viewModel::load,
                onSave = viewModel::save,
                onOpenActions = { showActions = true },
                onDismissActions = { showActions = false },
                onRetryMembers = viewModel::loadMembers,
                onDate = viewModel::updateDueDate,
                onAssignee = viewModel::updateAssignee,
                onCompleted = viewModel::updateCompleted,
                onRequestDelete = {
                    showActions = false
                    showDelete = true
                },
                onDelete = viewModel::delete,
                onDismissDelete = {
                    if (!mutating) {
                        showDelete = false
                        viewModel.clearDeleteError()
                    }
                },
            ),
    )
}

@Composable
private fun TaskContent(state: TaskUiState, layout: TaskLayout, actions: TaskActions) {
    val style = layout.id?.let { taskCardStyle(id = it) }
    TaskTheme(background = style?.background) {
        ItemTransition(
            key = layout.id?.let { "task:$it" },
            detail = true,
            modifier = Modifier.fillMaxSize(),
            rotation = style?.rotation ?: 0f,
        ) {
            TaskEditorContent(state = state, layout = layout, actions = actions)
        }

        if (layout.showActions) {
            TaskActionsSheet(
                state = state,
                enabled =
                    !state.editor.busy && (layout.id == null || state.editor.original != null),
                actions = actions,
            )
        }

        if (layout.showDelete) {
            DeleteTaskSheet(result = state.editor.delete, actions = actions)
        }
    }
}

@Composable
private fun TaskEditorContent(state: TaskUiState, layout: TaskLayout, actions: TaskActions) {
    val editor = state.editor
    val canEdit = !editor.busy && (layout.id == null || editor.original != null)
    val inset = Modifier.padding(horizontal = 16.dp)

    EditorLayout(
        route = Route.Task(id = layout.id),
        title = { TaskEditorTitle(state = editor, onTitle = actions.onTitle, enabled = canEdit) },
        onBack = actions.onBack,
        onSave = actions.onSave,
        saveLabel = stringResource(id = if (layout.id == null) R.string.create else R.string.save),
        actionsLabel = stringResource(id = R.string.task_actions),
        onOpenActions = actions.onOpenActions,
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
                        onRefresh = actions.onRetry,
                    )
                }
            }
            if (save is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = inset,
                        error = save.error.message,
                        onRefresh = actions.onSave,
                    )
                }
            }

            item {
                TaskEditorDescription(
                    modifier = inset,
                    state = editor,
                    onDescription = actions.onDescription,
                    enabled = canEdit,
                )
            }
        }
    }
}

@Composable
internal fun TaskEditorTitle(state: TaskEditorState, onTitle: (String) -> Unit, enabled: Boolean) {
    val focusManager = LocalFocusManager.current
    val fields = (state.save as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()
    val titleError =
        when {
            state.validate && state.title.isBlank() -> stringResource(id = R.string.title_required)

            state.titleLength > TaskLimits.TITLE -> stringResource(id = R.string.task_title_limit)

            else ->
                fields.firstOrNull { it.field == "title" }?.messages?.joinToString(separator = "\n")
        }

    EditorTextInput(
        value = state.title,
        onValueChange = onTitle,
        label = stringResource(id = R.string.title),
        style = MaterialTheme.typography.headlineSmall,
        enabled = enabled,
        errorMessage = titleError,
        minLines = 1,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
    )
}

@Composable
internal fun TaskEditorDescription(
    state: TaskEditorState,
    onDescription: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val fields = (state.save as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()
    val descriptionError =
        if (state.descriptionLength > TaskLimits.DESCRIPTION) {
            stringResource(id = R.string.task_description_limit)
        } else {
            fields
                .firstOrNull { it.field == "description" }
                ?.messages
                ?.joinToString(separator = "\n")
        }

    EditorTextInput(
        modifier = modifier,
        value = state.description,
        onValueChange = onDescription,
        label = stringResource(id = R.string.task_description),
        style = MaterialTheme.typography.bodyLarge,
        enabled = enabled,
        errorMessage = descriptionError,
        minLines = 1,
        maxLines = 12,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
    )
}

@Composable
internal fun TaskTheme(background: Color?, content: @Composable () -> Unit) {
    val parent = MaterialTheme.colorScheme
    val scheme =
        rememberDynamicColorScheme(
            seedColor = background ?: parent.primary,
            isDark = parent.surface.luminance() < 0.5f,
            style = PaletteStyle.Expressive,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
        )

    MaterialTheme(
        colorScheme =
            if (background == null) scheme
            else scheme.copy(background = background, surface = background),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}

internal data class TaskLayout(val id: String?, val showActions: Boolean, val showDelete: Boolean)

internal data class TaskActions(
    val onBack: () -> Unit,
    val onTitle: (String) -> Unit,
    val onDescription: (String) -> Unit,
    val onRetry: () -> Unit,
    val onSave: () -> Unit,
    val onOpenActions: () -> Unit,
    val onDismissActions: () -> Unit,
    val onRetryMembers: () -> Unit,
    val onDate: (LocalDate?) -> Unit,
    val onAssignee: (String?) -> Unit,
    val onCompleted: (Boolean) -> Unit,
    val onRequestDelete: () -> Unit,
    val onDelete: () -> Unit,
    val onDismissDelete: () -> Unit,
)
