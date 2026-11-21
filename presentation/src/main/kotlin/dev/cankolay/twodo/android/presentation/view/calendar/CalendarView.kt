package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarEntryFormState
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarUiState
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarViewModel
import java.time.LocalDate

@Composable
fun CalendarView(viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.result) {
        if (state.result is ApiResult.Success) viewModel.dismiss()
    }

    CalendarContent(
        state = state,
        onPrediction = { viewModel.openPrediction() },
        onRefreshPrediction = { viewModel.openPrediction(forceRefresh = true) },
        onCreate = { viewModel.openForm() },
        onEdit = viewModel::openForm,
        onRefresh = { viewModel.fetch(forceRefresh = true) },
        onMove = viewModel::move,
        onSelect = viewModel::select,
        onDismiss = viewModel::dismiss,
        onDelete = viewModel::delete,
        onAskDelete = viewModel::askDelete,
        onSave = viewModel::save,
        onFormChange = viewModel::updateForm,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(
    state: CalendarUiState,
    onPrediction: () -> Unit,
    onRefreshPrediction: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (CalendarEntry) -> Unit,
    onRefresh: () -> Unit,
    onMove: (Int) -> Unit,
    onSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onAskDelete: () -> Unit,
    onSave: () -> Unit,
    onFormChange: (CalendarEntryFormState) -> Unit,
) {
    val isLoading = state.result == ApiResult.Loading
    val entries = state.entries

    CalendarPanes(
        isLoading = state.load == ApiResult.Loading,
        onRefresh = onRefresh,
        topBar = { context ->
            AppTopAppBar(
                context = context,
                trailingContent = {
                    AppIconButton(
                        icon = Icons.Default.AutoAwesome,
                        contentDescription = stringResource(id = R.string.period_tracker),
                        onClick = onPrediction,
                        enabled = !isLoading,
                    )

                    AppIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.create_calendar_entry),
                        onClick = onCreate,
                        enabled = !isLoading,
                    )
                },
            )
        },
        error = (state.load as? ApiResult.Failure)?.error,
        month = {
            CalendarGrid(
                modifier = Modifier.padding(horizontal = 16.dp),
                visibleMonth = state.month,
                selectedDate = state.date,
                entries = entries.orEmpty(),
                projections = state.projections,
                canTrackPeriods = state.canTrackPeriods,
                onPreviousMonth = { onMove(-1) },
                onNextMonth = { onMove(1) },
                onDateClick = onSelect,
            )
        },
        entries =
            if (entries != null) {
                {
                    calendarEntries(
                        date = state.date,
                        entries = entries.filter { it.date == state.date },
                        projections = state.projections.filter { state.date in it.startDate..it.endDate },
                        canTrackPeriods = state.canTrackPeriods,
                        onClick = onEdit,
                    )
                }
            } else null,
    )

    if (state.showPrediction) {
        CalendarPredictionSheet(
            prediction = state.prediction,
            load = state.predictionLoad,
            onDismiss = onDismiss,
            onRefresh = onRefreshPrediction,
        )
    }

    if (state.showForm) {
        CalendarEntrySheet(
            form = state.form,
            result = state.result,
            canTrackPeriods = state.canTrackPeriods,
            onDismiss = onDismiss,
            onDelete = onAskDelete,
            onSave = onSave,
            onFormChange = onFormChange,
        )
    }

    if (state.showDelete) {
        DeleteCalendarEntrySheet(
            result = state.result,
            onDismiss = onDismiss,
            onDelete = onDelete,
        )
    }
}
