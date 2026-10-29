package dev.cankolay.twodo.android.presentation.view.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPane
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout
import dev.cankolay.twodo.android.presentation.core.HandleNavigation
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorUiState
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorViewModel

@Composable
fun NoteView(
    id: String? = null,
    onBack: () -> Unit = {},
    viewModel: NoteEditorViewModel? =
        when (id) {
            null -> null

            else -> hiltViewModel<NoteEditorViewModel>()
        },
) {
    val state =
        when (viewModel) {
            null -> NoteEditorUiState()

            else -> viewModel.uiState.collectAsStateWithLifecycle().value
        }

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
        when (id) {
            null -> Unit

            else -> viewModel?.load(id = id)
        }
    }

    LaunchedEffect(key1 = close) {
        if (close) viewModel?.close()
    }

    LaunchedEffect(key1 = state.close) {
        when (state.close) {
            is ApiResult.Failure -> navigation?.cancel()

            else -> Unit
        }
    }

    val content: @Composable () -> Unit = {
        NoteContent(
            state = state,
            visible = visible && id != null,
            onRetry = { if (id != null) viewModel?.load(id = id) },
            onBack = {
                when {
                    navigation?.isExpanded == true && navigation.isFullscreen -> navigation.toggle()

                    else -> viewModel?.close()
                }
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

    when (id) {
        null -> content()

        else ->
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
        when {
            state.showActions || state.showDelete -> onDismissSheet()

            else -> onBack()
        }
    }

    AppPane(modifier = Modifier.fillMaxSize()) {
        when (val note = state.note) {
            null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(id = R.string.select_note),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            ApiResult.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is ApiResult.Failure -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorCard(error = note.error.message, onRefresh = onRetry)
                }
            }

            is ApiResult.Success -> {
                NoteEditor(
                    note = state.draft ?: note.data,
                    state = state,
                    isExpanded = isExpanded,
                    isFullscreen = isFullscreen,
                    onBack = onBack,
                    onToggle = onToggle,
                    onTitleChange = onTitleChange,
                    onContentChange = onContentChange,
                    onOpenActions = onOpenActions,
                    onDismissSheet = onDismissSheet,
                    onRequestDelete = onRequestDelete,
                    onDelete = onDelete,
                    onRetrySave = onRetrySave,
                )
            }
        }
    }
}
