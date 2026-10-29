package dev.cankolay.twodo.android.presentation.view.note

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
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBarType
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteEditorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteEditor(
    note: Note,
    state: NoteEditorUiState,
    isExpanded: Boolean,
    isFullscreen: Boolean,
    onBack: () -> Unit,
    onToggle: () -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onOpenActions: () -> Unit,
    onDismissSheet: () -> Unit,
    onRequestDelete: () -> Unit,
    onDelete: () -> Unit,
    onRetrySave: () -> Unit,
) {
    val isLoading = state.close == ApiResult.Loading || state.delete == ApiResult.Loading

    AppLayout(
        route = Route.Note(id = note.id),
        context = { context -> context.copy(scrollBehavior = null) },
        topBar = { context ->
            AppTopAppBar(
                type = AppTopAppBarType.Default,
                context = context,
                onBackClick = onBack,
                trailingContent = {
                    if (isExpanded) {
                        IconButton(onClick = onToggle, enabled = !isLoading) {
                            Icon(
                                icon =
                                    if (isFullscreen) Icons.Default.FullscreenExit
                                    else Icons.Default.Fullscreen
                            )
                        }
                    }
                },
                title = {
                    BasicTextField(
                        value = note.title,
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
            when (val result = state.save) {
                is ApiResult.Failure ->
                    ErrorCard(error = result.error.message, onRefresh = onRetrySave)

                else -> Unit
            }

            MarkdownEditor(
                markdown = note.content,
                onContentChange = onContentChange,
                readOnly = isLoading,
                modifier = Modifier
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

        if (state.showActions) {
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
}
