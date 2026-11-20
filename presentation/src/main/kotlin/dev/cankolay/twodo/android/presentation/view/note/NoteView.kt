package dev.cankolay.twodo.android.presentation.view.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPane
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBarType
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout
import dev.cankolay.twodo.android.presentation.core.HandleNavigation
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorUiState
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorViewModel

@Composable
fun NoteView(
    id: String? = null,
    onBack: () -> Unit = {},
    viewModel: NoteEditorViewModel? =
        if (id != null) hiltViewModel<NoteEditorViewModel>() else null,
) {
    val state =
        if (viewModel != null) viewModel.uiState.collectAsStateWithLifecycle().value
        else NoteEditorUiState()

    val navigation = LocalNotesLayout.current?.navigation

    val visible = navigation == null || navigation.id == id

    val close = id != null && visible && navigation?.pending != null

    HandleNavigation(
        result =
            Unit.takeIf { state.close is ApiResult.Success || state.delete is ApiResult.Success }
    ) {
        onBack()
    }

    LaunchedEffect(key1 = id) {
        if (id != null) viewModel?.load(id = id)
    }

    LaunchedEffect(key1 = close) {
        if (close) viewModel?.close()
    }

    LaunchedEffect(key1 = state.close) {
        if (state.close is ApiResult.Failure) navigation?.cancel()
    }

    val content: @Composable () -> Unit = {
        NoteContent(
            id = id,
            state = state,
            visible = visible && id != null,
            onRetry = { if (id != null) viewModel?.load(id = id, forceRefresh = true) },
            onBack = {
                if (navigation?.isExpanded == true && navigation.isFullscreen) navigation.toggle()
                else viewModel?.close()
            },
            isExpanded = navigation?.isExpanded == true,
            isFullscreen = navigation?.isFullscreen == true,
            onToggle = { navigation?.toggle() },
            onTitleChange = { title -> viewModel?.updateTitle(title = title) },
            onContentChange = { content -> viewModel?.updateContent(content = content) },
            onOpenActions = { viewModel?.openActions() },
            onDismissSheet = { viewModel?.dismiss() },
            onRequestDelete = { viewModel?.askDelete() },
            onDelete = { viewModel?.delete() },
            onRetrySave = { viewModel?.save() },
        )
    }

    if (id == null) {
        content()
    } else {
        NoteTransition(
            id = id,
            detail = true,
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
    }
}

@Composable
private fun NoteContent(
    id: String?,
    state: NoteEditorUiState,
    visible: Boolean,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    isExpanded: Boolean,
    isFullscreen: Boolean,
    onToggle: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onOpenActions: () -> Unit,
    onDismissSheet: () -> Unit,
    onRequestDelete: () -> Unit,
    onDelete: () -> Unit,
    onRetrySave: () -> Unit,
) {
    BackHandler(enabled = visible) {
        if (state.showActions || state.showDelete) onDismissSheet()
        else onBack()
    }

    val note = state.draft ?: state.note
    val readOnly = state.close == ApiResult.Loading || state.delete == ApiResult.Loading

    AppPane(modifier = Modifier.fillMaxSize()) {
        if (id == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.select_note),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            AppLayout(
                route = Route.Note(id = id),
                context = { it.copy(scrollBehavior = null) },
                topBar = { context ->
                    AppTopAppBar(
                        type = AppTopAppBarType.Default,
                        context = context,
                        onBackClick = onBack,
                        trailingContent = {
                            if (isExpanded) {
                                IconButton(onClick = onToggle, enabled = !readOnly) {
                                    Icon(
                                        icon = if (isFullscreen) Icons.Default.FullscreenExit
                                        else Icons.Default.Fullscreen,
                                    )
                                }
                            }
                        },
                        title = {
                            if (note != null) {
                                BasicTextField(
                                    value = note.title,
                                    onValueChange = onTitleChange,
                                    enabled = !readOnly,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                Text(text = stringResource(id = R.string.notes))
                            }
                        },
                    )
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
                                .imePadding()
                        ) {
                            val load = state.load
                            if (load is ApiResult.Failure) {
                                ErrorCard(
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 16.dp,
                                        end = 16.dp
                                    ),
                                    error = load.error.message,
                                    onRefresh = onRetry,
                                )
                            }
                            val save = state.save
                            if (save is ApiResult.Failure) {
                                ErrorCard(
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 16.dp,
                                        end = 16.dp
                                    ),
                                    error = save.error.message,
                                    onRefresh = onRetrySave,
                                )
                            }
                            if (note != null) {
                                NoteEditor(
                                    modifier = Modifier
                                        .weight(weight = 1f)
                                        .padding(top = 16.dp),
                                    note = note,
                                    readOnly = readOnly,
                                    onContentChange = onContentChange,
                                    onOpenActions = onOpenActions,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showActions && note != null) {
        NoteActionsSheet(
            title = note.title.ifBlank { stringResource(id = R.string.notes) },
            updatedAt = note.updatedAt,
            onDismiss = onDismissSheet,
            onDelete = onRequestDelete,
        )
    }
    if (state.showDelete) {
        DeleteNoteSheet(
            result = state.delete,
            onDismiss = onDismissSheet,
            onDelete = onDelete,
        )
    }
}
