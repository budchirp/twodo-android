package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.PullToRefreshLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.SettingCard
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout
import dev.cankolay.twodo.android.presentation.core.HandleNavigation
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.NotesUiState
import dev.cankolay.twodo.android.presentation.viewmodel.note.NotesViewModel

@Composable
fun NotesView(
    onNoteClick: (String) -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val selected = LocalNotesLayout.current?.navigation?.id

    val id =
        when (val result = state.result) {
            is ApiResult.Success -> result.data.id

            else -> null
        }

    HandleNavigation(result = id) { id ->
        viewModel.consume()
        onNoteClick(id)
    }

    NotesContent(
        state = state,
        selected = selected,
        onRefresh = { viewModel.fetch(forceRefresh = true) },
        onCreateClick = viewModel::openForm,
        onDismissSheet = viewModel::dismiss,
        onTitleChange = viewModel::updateTitle,
        onCreate = viewModel::create,
        onNoteClick = onNoteClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesContent(
    state: NotesUiState,
    selected: String?,
    onRefresh: () -> Unit,
    onCreateClick: () -> Unit,
    onDismissSheet: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCreate: () -> Unit,
    onNoteClick: (String) -> Unit,
) {
    AppLayout(
        route = Route.Notes,
        topBar = { context ->
            AppTopAppBar(
                context = context,
                trailingContent = {
                    IconButton(
                        onClick = onCreateClick,
                        enabled = state.result != ApiResult.Loading,
                    ) {
                        Icon(icon = Icons.Default.Add)
                    }
                },
            )
        },
    ) {
        when (val notes = state.notes) {
            ApiResult.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            is ApiResult.Failure ->
                ErrorCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    error = notes.error.message,
                    onRefresh = onRefresh,
                )

            else -> Unit
        }
        PullToRefreshLazyColumn(
            isLoading = state.notes == ApiResult.Loading,
            onRefresh = onRefresh,
        ) {
            when (val result = state.notes) {
                is ApiResult.Success -> {
                    val notes = result.data
                    if (notes.isEmpty()) {
                        item {
                            SettingCard(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                title = stringResource(id = R.string.notes_empty_title),
                                description = stringResource(id = R.string.notes_empty_desc),
                                onClick = onCreateClick,
                                leadingContent = { Icon(icon = Icons.Default.Edit) },
                            )
                        }
                    } else {
                        items(items = notes) { note ->
                            NoteItem(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                note = note,
                                selected = selected == note.id,
                                onClick = { onNoteClick(note.id) },
                            )
                        }
                    }
                }

                else -> Unit
            }
        }
        if (state.showForm) {
            CreateNoteSheet(
                form = state.form,
                result = state.result,
                onDismiss = onDismissSheet,
                onTitleChange = onTitleChange,
                onCreate = onCreate,
            )
        }
    }
}
