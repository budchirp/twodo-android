package dev.cankolay.twodo.android.presentation.view.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.EditorTextInput
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPane
import dev.cankolay.twodo.android.presentation.composable.app.layout.EditorLayout
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout
import dev.cankolay.twodo.android.presentation.core.HandleNavigation
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorUiState
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorViewModel

@Composable
fun NoteView(
    id: String? = null,
    creating: Boolean = false,
    onBack: () -> Unit = {},
    viewModel: NoteEditorViewModel? =
        if (id != null || creating) hiltViewModel<NoteEditorViewModel>() else null,
) {
    val state = viewModel?.uiState?.collectAsStateWithLifecycle()?.value ?: NoteEditorUiState()
    val navigation = LocalNotesLayout.current?.navigation
    val visible = navigation == null || if (creating) navigation.isCreating else navigation.id == id
    val close = (id != null || creating) && visible && navigation?.hasPending == true

    HandleNavigation(
        result =
            Unit.takeIf { state.close is ApiResult.Success || state.delete is ApiResult.Success }
    ) {
        onBack()
    }

    LaunchedEffect(id, creating) {
        if (creating) viewModel?.createDraft() else if (id != null) viewModel?.load(id = id)
    }

    LaunchedEffect(close) { if (close) viewModel?.close() }

    LaunchedEffect(state.close, state.validate) {
        if (
            state.close is ApiResult.Failure ||
            (state.validate && state.draft?.title.isNullOrBlank())
        )
            navigation?.cancel()
    }

    NoteContent(
        state = state,
        actions =
            NoteActions(
                onRetry = { if (id != null) viewModel?.load(id = id, forceRefresh = true) },
                onBack = {
                    if (
                        navigation?.isExpanded == true &&
                        navigation.isFullscreen &&
                        navigation.hasListPane
                    ) {
                        navigation.toggle()
                    } else viewModel?.close()
                },
                onToggle = { navigation?.toggle() },
                onTitleChange = { viewModel?.updateTitle(title = it) },
                onContentChange = { viewModel?.updateContent(content = it) },
                onOpenActions = { viewModel?.openActions() },
                onDismissSheet = { viewModel?.dismiss() },
                onRequestDelete = { viewModel?.askDelete() },
                onDelete = { viewModel?.delete() },
                onSave = { viewModel?.submit() },
            ),
        layout =
            NoteLayout(
                id = id,
                creating = creating,
                visible = visible && (id != null || creating),
                isExpanded = navigation?.isExpanded == true,
                isFullscreen = navigation?.isFullscreen == true,
            ),
    )
}

@Composable
private fun NoteContent(state: NoteEditorUiState, layout: NoteLayout, actions: NoteActions) {
    NoteTransition(id = layout.id, detail = true, modifier = Modifier.fillMaxSize()) {
        NoteEditorContent(state = state, layout = layout, actions = actions)
    }
}

@Composable
private fun NoteEditorContent(state: NoteEditorUiState, layout: NoteLayout, actions: NoteActions) {
    val draft = state.draft ?: state.note
    val readOnly =
        state.close == ApiResult.Loading ||
                state.delete == ApiResult.Loading ||
                (state.creating && state.save == ApiResult.Loading)
    val canEdit = !readOnly && draft != null
    val focus = LocalFocusManager.current
    val fields = (state.save as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()
    val titleError =
        if (state.validate && draft?.title.isNullOrBlank()) {
            stringResource(id = R.string.title_required)
        } else fields.firstOrNull { it.field == "title" }?.messages?.joinToString(separator = "\n")

    BackHandler(enabled = layout.visible) {
        if (state.showActions || state.showDelete) actions.onDismissSheet() else actions.onBack()
    }

    AppPane(modifier = Modifier.fillMaxSize()) {
        if (layout.id == null && !layout.creating) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.select_note),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            EditorLayout(
                route =
                    if (layout.creating) Route.CreateNote
                    else Route.Note(id = requireNotNull(layout.id)),
                title = {
                    EditorTextInput(
                        value = draft?.title.orEmpty(),
                        onValueChange = actions.onTitleChange,
                        label = stringResource(id = R.string.title),
                        style = MaterialTheme.typography.headlineSmall,
                        enabled = canEdit,
                        errorMessage = titleError,
                        minLines = 1,
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions =
                            KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    )
                },
                onBack = actions.onBack,
                onSave = actions.onSave,
                saveLabel =
                    stringResource(id = if (layout.creating) R.string.create else R.string.save),
                actionsLabel = stringResource(id = R.string.note_actions),
                onOpenActions = actions.onOpenActions,
                canEdit = canEdit && state.save != ApiResult.Loading,
                isLoading =
                    state.load == ApiResult.Loading ||
                            state.save == ApiResult.Loading ||
                            state.close == ApiResult.Loading,
                showActions = state.note != null,
                trailingContent = {
                    if (layout.isExpanded) {
                        AppIconButton(
                            icon =
                                if (layout.isFullscreen) Icons.Default.FullscreenExit
                                else Icons.Default.Fullscreen,
                            contentDescription =
                                stringResource(
                                    id =
                                        if (layout.isFullscreen) R.string.exit_fullscreen
                                        else R.string.enter_fullscreen
                                ),
                            onClick = actions.onToggle,
                            enabled = !readOnly,
                        )
                    }
                },
            ) {
                AppLazyColumn(
                    contentPadding = PaddingValues(all = 0.dp),
                    userScrollEnabled = false,
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                        ) {
                            val load = state.load
                            if (load is ApiResult.Failure) {
                                ErrorCard(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    error = load.error.message,
                                    onRefresh = actions.onRetry,
                                )
                            }

                            val save = state.save
                            if (save is ApiResult.Failure) {
                                ErrorCard(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    error = save.error.message,
                                    onRefresh = actions.onSave,
                                )
                            }

                            if (draft != null) {
                                MarkdownEditor(
                                    modifier =
                                        Modifier
                                            .weight(weight = 1f)
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp),
                                    markdown = draft.content,
                                    readOnly = !canEdit,
                                    onContentChange = actions.onContentChange,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showActions && state.note != null) {
        NoteActionsSheet(state = state, actions = actions)
    }

    if (state.showDelete) {
        DeleteNoteSheet(result = state.delete, actions = actions)
    }
}

internal data class NoteActions(
    val onRetry: () -> Unit,
    val onBack: () -> Unit,
    val onToggle: () -> Unit,
    val onTitleChange: (String) -> Unit,
    val onContentChange: (String) -> Unit,
    val onOpenActions: () -> Unit,
    val onDismissSheet: () -> Unit,
    val onRequestDelete: () -> Unit,
    val onDelete: () -> Unit,
    val onSave: () -> Unit,
)

internal data class NoteLayout(
    val id: String?,
    val creating: Boolean,
    val visible: Boolean,
    val isExpanded: Boolean,
    val isFullscreen: Boolean,
)
