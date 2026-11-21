package dev.cankolay.twodo.android.presentation.view.note

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
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
        onBack = { backDispatcher?.onBackPressed() },
        onRefresh = { viewModel.fetch(forceRefresh = true) },
        onCreateClick = onCreate,
        onNoteClick = onNoteClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesContent(
    state: NotesUiState,
    selected: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCreateClick: () -> Unit,
    onNoteClick: (String) -> Unit,
) {
    AppLayout(
        route = Route.Notes,
        topBar = { context ->
            AppTopAppBar(
                context = context,
                onBackClick = onBack,
                trailingContent = {
                    AppIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_note),
                        onClick = onCreateClick,
                    )
                },
            )
        },
    ) {
        AppRefreshableLazyColumn(
            isLoading = state.load == ApiResult.Loading,
            onRefresh = onRefresh,
        ) {
            notesList(
                notes = state.notes,
                load = state.load,
                selectedId = selected,
                onRefresh = onRefresh,
                onNoteClick = onNoteClick,
                onCreate = onCreateClick,
            )
        }
    }
}
