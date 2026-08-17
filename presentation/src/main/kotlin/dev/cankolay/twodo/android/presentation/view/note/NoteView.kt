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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBarType
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteSheet
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    var editorContent by remember(key1 = id) { mutableStateOf<String?>(null) }

    val performBackSave = {
        noteViewModel.flushDraftSave(content = editorContent)
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
                MarkdownEditor(
                    markdown = noteDraft.content,
                    onContentChange = { content ->
                        editorContent = content
                        noteViewModel.updateNoteDraftContent(content = content)
                    },
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
                    NoteActionsSheet(
                        title = noteDraft.title.ifBlank { stringResource(id = R.string.notes) },
                        updatedAt = noteDraft.updatedAt,
                        onDismiss = { noteViewModel.dismissSheet() },
                        onDelete = { noteViewModel.requestDeleteNote() }
                    )
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
