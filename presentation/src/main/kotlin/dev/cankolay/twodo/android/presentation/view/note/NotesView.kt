package dev.cankolay.twodo.android.presentation.view.note

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.ItemTransition
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppRefreshableLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.note.NotesUiState
import dev.cankolay.twodo.android.presentation.viewmodel.note.NotesViewModel

@Composable
fun NotesView(
    onNoteClick: (String) -> Unit,
    onCreate: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selected = LocalNotesLayout.current?.navigation?.id
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    NotesContent(
        state = state,
        selected = selected,
        actions =
            NotesActions(
                onBack = { backDispatcher?.onBackPressed() },
                onRefresh = { viewModel.fetch(forceRefresh = true) },
                onCreateClick = onCreate,
                onNoteClick = onNoteClick,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesContent(state: NotesUiState, selected: String?, actions: NotesActions) {
    AppLayout(
        route = Route.Notes,
        topBar = { context ->
            AppTopAppBar(
                context = context,
                onBackClick = actions.onBack,
                trailingContent = {
                    AppIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_note),
                        onClick = actions.onCreateClick,
                    )
                },
            )
        },
    ) {
        AppRefreshableLazyColumn(
            isLoading = state.load == ApiResult.Loading,
            onRefresh = actions.onRefresh,
        ) {
            notesList(
                notes = state.notes,
                load = state.load,
                selectedId = selected,
                actions = actions,
            )
        }
    }
}

internal fun LazyListScope.notesList(
    notes: List<Note>?,
    load: ApiResult<Unit>?,
    selectedId: String? = null,
    actions: NotesActions,
) {
    if (load is ApiResult.Failure) {
        item {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = load.error.message,
                onRefresh = actions.onRefresh,
            )
        }
    }

    if (notes != null) {
        if (notes.isEmpty()) {
            item {
                SettingRow(
                    title = stringResource(id = R.string.notes_empty_title),
                    leadingContent = { Icon(icon = Icons.Default.Edit) },
                    onClick = actions.onCreateClick,
                )
            }
        } else {
            items(items = notes) { note ->
                val isExpanded = LocalNotesLayout.current?.navigation?.isExpanded != false
                NoteTransition(
                    id = note.id,
                    detail = false,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color =
                            if (isExpanded && selectedId == note.id)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        SettingRow(title = note.title, onClick = { actions.onNoteClick(note.id) })
                    }
                }
            }
        }
    }
}

@Composable
internal fun NoteTransition(
    id: String?,
    detail: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ItemTransition(
        key = id?.let { "note:$it" },
        detail = detail,
        modifier = modifier,
        enabled = LocalNotesLayout.current?.navigation?.isExpanded != true,
        content = content,
    )
}

internal data class NotesActions(
    val onBack: () -> Unit = {},
    val onRefresh: () -> Unit,
    val onCreateClick: () -> Unit,
    val onNoteClick: (String) -> Unit,
)
