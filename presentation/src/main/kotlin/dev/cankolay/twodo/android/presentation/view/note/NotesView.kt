package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.PullToRefreshLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.CreateNoteFormState
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteSheet
import dev.cankolay.twodo.android.presentation.viewmodel.note.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotesView(
    noteViewModel: NoteViewModel = hiltViewModel(),
) {
    val navBackStack = LocalNavBackStack.current

    val state by noteViewModel.uiState.collectAsStateWithLifecycle()
    val notes = state.notes
    HandleEvents(viewModel = noteViewModel)

    val isLoading = state.isLoading
    val error = state.error

    AppLayout(route = Route.Notes, topBar = { context ->
        AppTopAppBar(context = context, trailingContent = {
            IconButton(onClick = {
                noteViewModel.openCreateNoteSheet()
            }) {
                Icon(icon = Icons.Default.Add)
            }
        })
    }) {
        PullToRefreshLazyColumn(
            isLoading = isLoading,
            onRefresh = { noteViewModel.fetchNotes() },
        ) {
            when {
                error != null && notes == null -> {
                    item {
                        ErrorCard(
                            title = stringResource(id = R.string.notes_error),
                            error = error,
                            onRefresh = { noteViewModel.fetchNotes() })
                    }
                }

                notes == null -> Unit

                notes.isEmpty() -> {
                    item {
                        CardStackList(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            items = listOf(
                                CardStackListItem(
                                    title = stringResource(id = R.string.notes_empty_title),
                                    description = stringResource(id = R.string.notes_empty_desc),
                                    leadingContent = {
                                        Icon(
                                            icon = Icons.Default.Edit,
                                        )
                                    }
                                )
                            ))
                    }
                }

                else -> {
                    item {
                        CardStackList(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            items = notes.map { note ->
                                CardStackListItem(
                                    title = note.title,
                                    onClick = {
                                        navBackStack.add(element = Route.Note(id = note.id))
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }

        (state.activeSheet as? NoteSheet.CreateNote)?.let { sheet ->
            CreateNoteSheet(
                form = sheet.form,
                onDismiss = { noteViewModel.dismissSheet() },
                onTitleChange = { noteViewModel.updateCreateNoteTitle(title = it) },
                onCreate = { noteViewModel.submitNote() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteSheet(
    form: CreateNoteFormState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    AppBottomSheet(
        title = stringResource(id = R.string.create_note),
        onDismiss = onDismiss,
        actions = {
            Button(
                enabled = form.canSubmit,
                onClick = onCreate
            ) {
                Text(text = stringResource(id = R.string.create))
            }
        }
    ) {
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.title.value,
                onValueChange = onTitleChange,
                label = { Text(text = stringResource(id = R.string.title)) },
                isError = form.title.error != null,
                supportingText = form.title.error?.let { error ->
                    { Text(text = stringResource(id = error)) }
                }
            )
        }
    }
}
