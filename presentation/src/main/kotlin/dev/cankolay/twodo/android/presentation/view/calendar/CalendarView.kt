package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarEntryFormState
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarUiState
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarViewModel
import java.time.LocalDate

@Composable
fun CalendarView(viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.result) {
        when (state.result) {
            is ApiResult.Success -> viewModel.dismiss()

            else -> Unit
        }
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

    CalendarPanes(
        isLoading = state.entries == ApiResult.Loading,
        onRefresh = onRefresh,
        topBar = { context ->
            AppTopAppBar(
                context = context,
                trailingContent = {
                    IconButton(onClick = onPrediction, enabled = !isLoading) {
                        Icon(icon = Icons.Default.AutoAwesome)
                    }

                    IconButton(onClick = onCreate, enabled = !isLoading) {
                        Icon(icon = Icons.Default.Add)
                    }
                },
            )
        },
        status = {
            when (val result = state.entries) {
                ApiResult.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                else -> Unit
            }
        },
        error = {
            when (val result = state.entries) {
                is ApiResult.Failure ->
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = result.error.message,
                        onRefresh = onRefresh,
                    )

                else -> Unit
            }
        },
        month = {
            CalendarGrid(
                modifier = Modifier.padding(horizontal = 16.dp),
                visibleMonth = state.month,
                selectedDate = state.date,
                entries =
                    when (val result = state.entries) {
                        is ApiResult.Success -> result.data

                        else -> emptyList()
                    },
                canTrackPeriods = state.canTrackPeriods,
                onPreviousMonth = { onMove(-1) },
                onNextMonth = { onMove(1) },
                onDateClick = onSelect,
            )
        },
        entries =
            when (val result = state.entries) {
                is ApiResult.Success -> {
                    {
                        calendarEntries(
                            date = state.date,
                            entries = result.data.filter { it.date == state.date },
                            canTrackPeriods = state.canTrackPeriods,
                            onClick = onEdit,
                        )
                    }
                }

                else -> null
            },
    )

    if (state.showPrediction) {
        CalendarPredictionSheet(
            state = state.prediction,
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
