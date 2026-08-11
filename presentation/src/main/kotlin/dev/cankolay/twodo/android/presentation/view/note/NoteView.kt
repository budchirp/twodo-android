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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBarType
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.view.calendar.formatNoteDateTime
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteSheet
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteViewModel
import kotlinx.coroutines.FlowPreview

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NoteView(
    id: String,
    noteViewModel: NoteViewModel = hiltViewModel(),
) {
    val navBackStack = LocalNavBackStack.current

    val uiState by noteViewModel.uiState.collectAsStateWithLifecycle()
    val note = uiState.note

    HandleEvents(viewModel = noteViewModel)

    LaunchedEffect(key1 = id) {
        noteViewModel.fetchNote(id = id)
    }

    val richTextState = rememberRichTextState()
    richTextState.config.linkColor = MaterialTheme.colorScheme.primary

    val performBackSave = {
        noteViewModel.flushDraftSave(content = richTextState.toMarkdown())
        if (navBackStack.size > 1) {
            navBackStack.removeLastOrNull()
        }
    }

    BackHandler {
        performBackSave()
    }

    if (note == null) {
        AppLayout(route = Route.Note(id = id)) {
            uiState.error?.let { message ->
                ErrorCard(
                    title = stringResource(id = R.string.notes_error),
                    error = message,
                    onRefresh = { noteViewModel.fetchNote(id) }
                )
            }
        }
    }

    note?.let {
        val noteDraft = uiState.noteDraft ?: return@let

        DisposableEffect(key1 = id) {
            onDispose {
                noteViewModel.flushDraftSave()
            }
        }

        LaunchedEffect(key1 = id) {
            val initialContent = noteDraft.content
            richTextState.setMarkdown(markdown = initialContent)
            richTextState.selection = TextRange(index = initialContent.length)
            snapshotFlow { richTextState.toMarkdown() }
                .collect { content ->
                    noteViewModel.updateNoteDraftContent(content = content)
                }
        }

        AppLayout(
            route = Route.Note(id = id),
            context = { context ->
                context.copy(
                    scrollBehavior = null
                )
            },
            topBar = { context ->
                AppTopAppBar(
                    type = AppTopAppBarType.Default,
                    context = context,
                    onBackClick = { performBackSave() },
                    title = {
                        BasicTextField(
                            value = noteDraft.title,
                            onValueChange = { title ->
                                noteViewModel.updateNoteDraftTitle(title = title)
                            },
                            textStyle = LocalTextStyle.current.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                BasicRichTextEditor(
                    state = richTextState,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .weight(weight = 1f)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 64.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        }

                        FilledIconButton(onClick = { noteViewModel.openNoteActionsSheet() }) {
                            Icon(icon = Icons.Default.MoreVert)
                        }
                    }
                }
            }

            when (val sheet = uiState.activeSheet) {
                is NoteSheet.NoteActions -> {
                    val sheetTitle =
                        noteDraft.title.ifBlank { stringResource(id = R.string.notes) }

                    AppBottomSheet(
                        title = sheetTitle,
                        onDismiss = { noteViewModel.dismissSheet() }
                    ) {
                        item {
                            CardStackList(
                                items = listOf(
                                    CardStackListItem(
                                        title = stringResource(
                                            id = R.string.edited_at,
                                            formatNoteDateTime(noteDraft.updatedAt)
                                        ),
                                        leadingContent = {
                                            Icon(icon = Icons.Default.Update)
                                        }
                                    ),
                                    CardStackListItem(
                                        title = stringResource(id = R.string.delete),
                                        onClick = {
                                            noteViewModel.requestDeleteNote()
                                        },
                                        leadingContent = {
                                            Icon(icon = Icons.Default.Delete)
                                        }
                                    )
                                )
                            )
                        }
                    }
                }

                is NoteSheet.DeleteConfirmation -> {
                    DeleteNoteSheet(
                        onDismiss = { noteViewModel.dismissSheet() },
                        onDelete = {
                            noteViewModel.confirmDeleteNote(id = sheet.noteId)
                        }
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
fun DeleteNoteSheet(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    DestructiveConfirmationSheet(
        title = stringResource(id = R.string.delete_note),
        description = stringResource(id = R.string.delete_note_desc),
        confirmText = stringResource(id = R.string.delete),
        onDismiss = onDismiss,
        onConfirm = onDelete
    )
}
