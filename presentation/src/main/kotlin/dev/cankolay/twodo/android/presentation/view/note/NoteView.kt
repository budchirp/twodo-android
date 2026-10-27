package dev.cankolay.twodo.android.presentation.view.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBarType
import dev.cankolay.twodo.android.presentation.core.HandleNavigation
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorUiState
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorViewModel

@Composable
fun NoteView(
    id: String,
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HandleNavigation(
        result =
            Unit.takeIf { state.close is ApiResult.Success || state.delete is ApiResult.Success }
    ) {
        onBack()
    }
    LaunchedEffect(key1 = id) { viewModel.load(id = id) }

    NoteContent(
        id = id,
        uiState = state,
        onRetry = { viewModel.load(id = id) },
        onBack = viewModel::close,
        onTitleChange = viewModel::updateTitle,
        onContentChange = viewModel::updateContent,
        onOpenActions = viewModel::openActions,
        onDismissSheet = viewModel::dismiss,
        onRequestDelete = viewModel::askDelete,
        onDelete = viewModel::delete,
        onRetrySave = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteContent(
    id: String,
    uiState: NoteEditorUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onOpenActions: () -> Unit,
    onDismissSheet: () -> Unit,
    onRequestDelete: () -> Unit,
    onDelete: () -> Unit,
    onRetrySave: () -> Unit,
) {
    val note = uiState.draft

    val isLoading = uiState.close == ApiResult.Loading || uiState.delete == ApiResult.Loading

    BackHandler {
        if (uiState.showActions || uiState.showDelete) onDismissSheet() else onBack()
    }

    if (note == null) {
        AppLayout(
            route = Route.Note(id = id),
            topBar = { context ->
                AppTopAppBar(context = context, onBackClick = onBack)
            },
        ) {
            when (val noteState = uiState.note) {
                ApiResult.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                is ApiResult.Failure ->
                    ErrorCard(error = noteState.error.message, onRefresh = onRetry)

                else -> Unit
            }
        }
    }

    note?.let { noteDraft ->
        AppLayout(
            route = Route.Note(id = id),
            context = { context ->
                context.copy(scrollBehavior = null)
            },
            topBar = { context ->
                AppTopAppBar(
                    type = AppTopAppBarType.Default,
                    context = context,
                    onBackClick = onBack,
                    title = {
                        BasicTextField(
                            value = noteDraft.title,
                            onValueChange = onTitleChange,
                            enabled = !isLoading,
                            textStyle =
                                LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                            cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                when (val result = uiState.save) {
                    is ApiResult.Failure ->
                        ErrorCard(error = result.error.message, onRefresh = onRetrySave)

                    else -> Unit
                }

                MarkdownEditor(
                    markdown = noteDraft.content,
                    onContentChange = onContentChange,
                    readOnly = isLoading,
                    modifier =
                        Modifier
                            .weight(weight = 1f)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 64.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledIconButton(onClick = onOpenActions, enabled = !isLoading) {
                            Icon(icon = Icons.Default.MoreVert)
                        }
                    }
                }
            }

            if (uiState.showActions) {
                NoteActionsSheet(
                    title = noteDraft.title.ifBlank { stringResource(id = R.string.notes) },
                    updatedAt = noteDraft.updatedAt,
                    onDismiss = onDismissSheet,
                    onDelete = onRequestDelete,
                )
            }

            if (uiState.showDelete) {
                DeleteNoteSheet(
                    result = uiState.delete,
                    onDismiss = onDismissSheet,
                    onDelete = onDelete,
                )
            }
        }
    }
}
