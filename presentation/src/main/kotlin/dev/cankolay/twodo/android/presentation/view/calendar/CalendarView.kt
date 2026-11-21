package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor
import dev.cankolay.twodo.android.presentation.composable.app.SettingCard
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPane
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPaneDefaults
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.composable.app.layout.currentPaneDirective
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.util.DateUtils
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarEntryFormState
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarUiState
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.canManage
import java.time.LocalDate

@Composable
fun CalendarView(viewModel: CalendarViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.result) {
        if (state.result is ApiResult.Success) viewModel.dismiss()
    }

    CalendarContent(
        state = state,
        actions =
            CalendarActions(
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
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(state: CalendarUiState, actions: CalendarActions) {

    CalendarPanes(
        state = state,
        actions = actions,
        month = {
            CalendarGrid(
                state = state,
                actions = actions,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        },
        entries =
            if (state.entries != null) {
                { calendarEntries(state = state, actions = actions) }
            } else null,
    )

    if (state.showPrediction) {
        CalendarPredictionSheet(state = state, actions = actions)
    }

    if (state.showForm) {
        CalendarEntrySheet(state = state, actions = actions)
    }

    if (state.showDelete) {
        DeleteCalendarEntrySheet(result = state.result, actions = actions)
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
private fun CalendarPanes(
    state: CalendarUiState,
    actions: CalendarActions,
    month: @Composable () -> Unit,
    entries: (LazyListScope.() -> Unit)?,
) {
    val error = (state.load as? ApiResult.Failure)?.error
    val isLoading = state.result == ApiResult.Loading

    val isExpanded = currentPaneDirective().maxHorizontalPartitions > 1

    val calendar: @Composable () -> Unit = {
        AppLayout(
            route = Route.Calendar,
            topBar = { context ->
                AppTopAppBar(
                    context = context,
                    trailingContent = {
                        AppIconButton(
                            icon = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(id = R.string.period_tracker),
                            onClick = actions.onPrediction,
                            enabled = !isLoading,
                        )

                        AppIconButton(
                            icon = Icons.Default.Add,
                            contentDescription =
                                stringResource(id = R.string.create_calendar_entry),
                            onClick = actions.onCreate,
                            enabled = !isLoading,
                        )
                    },
                )
            },
        ) {
            AppLinearProgressIndicator(isLoading = (state.load == ApiResult.Loading))

            PullToRefreshBox(
                isRefreshing = (state.load == ApiResult.Loading),
                onRefresh = actions.onRefresh,
                indicator = {},
            ) {
                AppLazyColumn {
                    if (!isExpanded && error != null) {
                        item {
                            ErrorCard(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                error = error.message,
                                onRefresh = actions.onRefresh,
                            )
                        }
                    }
                    item(content = { month() })

                    if (!isExpanded) entries?.invoke(this)
                }
            }
        }
    }

    if (isExpanded) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer),
            horizontalArrangement = Arrangement.spacedBy(space = AppPaneDefaults.Spacing),
        ) {
            Box(modifier = Modifier.weight(weight = 1f), content = { calendar() })

            AppPane(
                modifier = Modifier
                    .width(width = AppPaneDefaults.Width)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(
                                insets = WindowInsets.systemBars.only(sides = WindowInsetsSides.Top)
                            )
                ) {
                    AppLazyColumn(modifier = Modifier.weight(weight = 1f)) {
                        if (error != null) {
                            item {
                                ErrorCard(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    error = error.message,
                                    onRefresh = actions.onRefresh,
                                )
                            }
                        }
                        entries?.invoke(this)
                    }
                }
            }
        }
    } else {
        calendar()
    }
}

private fun LazyListScope.calendarEntries(state: CalendarUiState, actions: CalendarActions) {
    val entries = state.entries.orEmpty().filter { it.date == state.date }
    val projections = state.projections.filter { state.date in it.startDate..it.endDate }

    val (notes, events) = entries.partition { it.type == CalendarEntryType.NOTE }

    item {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text =
                stringResource(
                    id = R.string.selected_day,
                    DateUtils.format(
                        date = state.date,
                        pattern = stringResource(id = R.string.calendar_date_pattern),
                    ),
                ),
            style = MaterialTheme.typography.titleMedium,
        )
    }

    if (events.isNotEmpty() || (entries.isEmpty() && projections.isEmpty())) {
        item {
            when {
                events.isEmpty() ->
                    SettingCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        title = stringResource(id = R.string.calendar_day_empty_title),
                        description = stringResource(id = R.string.calendar_day_empty_desc),
                        leadingContent = { Icon(icon = Icons.Default.CalendarMonth) },
                    )

                else ->
                    SettingsGroup(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items =
                            events.map { entry ->
                                val parts =
                                    when (entry.type) {
                                        CalendarEntryType.PERIOD ->
                                            listOfNotNull(
                                                entry.period?.event?.let {
                                                    stringResource(id = it.label)
                                                },
                                                entry.period?.flowLevel?.let {
                                                    stringResource(id = it.label)
                                                },
                                                entry.period
                                                    ?.symptoms
                                                    ?.takeIf { it.isNotEmpty() }
                                                    ?.map { stringResource(id = it.label) }
                                                    ?.joinToString(),
                                                entry.notes?.takeIf { it.isNotBlank() },
                                            )

                                        else -> listOfNotNull(entry.notes)
                                    }

                                SettingItem(
                                    title =
                                        if (entry.isImportant)
                                            stringResource(id = R.string.important_date) +
                                                    " · " +
                                                    stringResource(id = entry.type.label)
                                        else stringResource(id = entry.type.label),
                                    description =
                                        parts.joinToString(separator = " · ").ifBlank {
                                            stringResource(id = R.string.no_notes)
                                        },
                                    leadingContent = {
                                        Icon(
                                            icon =
                                                if (entry.isImportant) Icons.Default.Star
                                                else if (entry.type == CalendarEntryType.OVULATION)
                                                    Icons.Default.AutoAwesome
                                                else Icons.Default.CalendarMonth
                                        )
                                    },
                                    onClick =
                                        if (
                                            entry.canManage(canTrackPeriods = state.canTrackPeriods)
                                        )
                                            ({ actions.onEdit(entry) })
                                        else null,
                                )
                            },
                    )
            }
        }
    }

    items(items = projections) { projection ->
        SettingCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = projection.label,
            description =
                stringResource(id = R.string.calendar_projection_estimate) +
                        " · " +
                        DateUtils.formatRange(
                            start = projection.startDate,
                            end = projection.endDate
                        ),
            leadingContent = { Icon(icon = Icons.Default.AutoAwesome) },
        )
    }

    if (notes.isNotEmpty()) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

                notes.forEachIndexed { index, entry ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled =
                                        entry.canManage(canTrackPeriods = state.canTrackPeriods),
                                    onClick = { actions.onEdit(entry) },
                                )
                                .padding(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                    ) {
                        Text(
                            text =
                                DateUtils.format(
                                    value = entry.createdAt,
                                    pattern = DateUtils.TIME_PATTERN,
                                ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        if (entry.isImportant) {
                            Text(
                                text = stringResource(id = R.string.important_date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        MarkdownEditor(markdown = entry.notes.orEmpty(), readOnly = true)
                    }

                    if (index < notes.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                    }
                }
            }
        }
    }
}

internal data class CalendarActions(
    val onPrediction: () -> Unit,
    val onRefreshPrediction: () -> Unit,
    val onCreate: () -> Unit,
    val onEdit: (CalendarEntry) -> Unit,
    val onRefresh: () -> Unit,
    val onMove: (Int) -> Unit,
    val onSelect: (LocalDate) -> Unit,
    val onDismiss: () -> Unit,
    val onDelete: () -> Unit,
    val onAskDelete: () -> Unit,
    val onSave: () -> Unit,
    val onFormChange: (CalendarEntryFormState) -> Unit,
)
