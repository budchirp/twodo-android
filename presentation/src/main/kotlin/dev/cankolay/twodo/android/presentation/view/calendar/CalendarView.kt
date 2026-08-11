package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.PullToRefreshLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarSheet
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.user.UserViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalendarView(
    userViewModel: UserViewModel = hiltViewModel(),
    calendarViewModel: CalendarViewModel = hiltViewModel()
) {
    val userState by userViewModel.uiState.collectAsStateWithLifecycle()
    val isFemale = userState.user?.gender == Gender.FEMALE

    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    val entries = uiState.entries
    val selectedEntries = uiState.selectedEntries

    HandleEvents(viewModel = calendarViewModel)

    val isWideScreen = currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
        widthDpBreakpoint = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    )

    AppLayout(route = Route.Calendar, topBar = { context ->
        AppTopAppBar(context = context, trailingContent = {
            IconButton(onClick = { calendarViewModel.openPredictionSheet() }) {
                Icon(icon = Icons.Default.AutoAwesome)
            }

            IconButton(onClick = { calendarViewModel.openCreateEntrySheet() }) {
                Icon(icon = Icons.Default.Add)
            }
        })
    }) {
        PullToRefreshLazyColumn(
            isLoading = uiState.isLoading,
            onRefresh = {
                calendarViewModel.fetchEntries()
            }
        ) {
            if (isWideScreen) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(weight = 1f)) {
                            MonthCalendarCard(
                                visibleMonth = uiState.visibleMonth,
                                selectedDate = uiState.selectedDate,
                                entries = entries.orEmpty(),
                                conceptionRiskEvents = uiState.predictionSummary?.conceptionRisk?.relevantEvents.orEmpty()
                                    .toSet(),
                                onPreviousMonth = { calendarViewModel.moveMonth(months = -1) },
                                onNextMonth = { calendarViewModel.moveMonth(months = 1) },
                                onDateClick = { calendarViewModel.selectDate(date = it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(modifier = Modifier.weight(weight = 1f)) {
                            CalendarEntriesContent(
                                date = uiState.selectedDate,
                                entries = entries,
                                selectedEntries = selectedEntries,
                                error = uiState.error,
                                isFemale = isFemale,
                                onEntryClick = { calendarViewModel.openEditEntrySheet(entry = it) },
                                onRefresh = { calendarViewModel.fetchEntries() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                item {
                    MonthCalendarCard(
                        visibleMonth = uiState.visibleMonth,
                        selectedDate = uiState.selectedDate,
                        entries = entries.orEmpty(),
                        conceptionRiskEvents = uiState.predictionSummary?.conceptionRisk?.relevantEvents.orEmpty()
                            .toSet(),
                        onPreviousMonth = { calendarViewModel.moveMonth(months = -1) },
                        onNextMonth = { calendarViewModel.moveMonth(months = 1) },
                        onDateClick = { calendarViewModel.selectDate(date = it) }
                    )
                }

                item {
                    CalendarEntriesContent(
                        date = uiState.selectedDate,
                        entries = entries,
                        selectedEntries = selectedEntries,
                        error = uiState.error,
                        isFemale = isFemale,
                        onEntryClick = { calendarViewModel.openEditEntrySheet(entry = it) },
                        onRefresh = { calendarViewModel.fetchEntries() }
                    )
                }
            }
        }

        when (val sheet = uiState.activeSheet) {
            is CalendarSheet.PredictionSummary -> {
                PredictionSummarySheet(
                    summary = uiState.predictionSummary,
                    onDismiss = { calendarViewModel.dismissSheet() }
                )
            }

            is CalendarSheet.EntryForm -> {
                CalendarEntrySheet(
                    form = sheet.form,
                    isFemale = isFemale,
                    onDismiss = { calendarViewModel.dismissSheet() },
                    onDelete = { calendarViewModel.requestDeleteEntry() },
                    onSave = { calendarViewModel.submitEntry(isFemale = isFemale) },
                    onTypeChange = { calendarViewModel.updateEntryType(type = it) },
                    onNotesChange = { calendarViewModel.updateEntryNotes(notes = it) },
                    onPeriodEventChange = { calendarViewModel.updatePeriodEvent(event = it) },
                    onFlowLevelChange = { calendarViewModel.updateFlowLevel(flowLevel = it) },
                    onSymptomsChange = { calendarViewModel.updateSymptoms(symptoms = it) },
                    onSexOccurredChange = { calendarViewModel.updateSexOccurred(sexOccurred = it) },
                    onProtectionMethodChange = {
                        calendarViewModel.updateProtectionMethod(protectionMethod = it)
                    },
                    onEjaculationLocationChange = {
                        calendarViewModel.updateEjaculationLocation(ejaculationLocation = it)
                    }
                )
            }

            is CalendarSheet.DeleteConfirmation -> {
                DeleteCalendarEntrySheet(
                    onDismiss = { calendarViewModel.dismissSheet() },
                    onDelete = { calendarViewModel.confirmDeleteEntry() }
                )
            }

            CalendarSheet.None -> Unit
        }
    }
}

@Composable
private fun MonthCalendarCard(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    entries: List<CalendarEntry>,
    conceptionRiskEvents: Set<LocalDate>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier.padding(horizontal = 16.dp)
) {
    val entriesByDate = entries.groupBy { it.date }
    val locale = LocalLocale.current.platformLocale

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(icon = Icons.AutoMirrored.Filled.ArrowBack)
            }

            Text(
                text = visibleMonth.month.getDisplayName(
                    TextStyle.FULL,
                    locale
                ) + " " + visibleMonth.year,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = onNextMonth) {
                Icon(icon = Icons.AutoMirrored.Filled.ArrowForward)
            }
        }

        CalendarWeekHeader()
        CalendarMonthGrid(
            visibleMonth = visibleMonth,
            selectedDate = selectedDate,
            entriesByDate = entriesByDate,
            conceptionRiskEvents = conceptionRiskEvents,
            onDateClick = onDateClick
        )
    }
}

@Composable
private fun CalendarWeekHeader() {
    val locale = LocalLocale.current.platformLocale

    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.entries.forEach { day ->
            Text(
                modifier = Modifier.weight(weight = 1f),
                text = day.getDisplayName(TextStyle.SHORT, locale),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    entriesByDate: Map<LocalDate, List<CalendarEntry>>,
    conceptionRiskEvents: Set<LocalDate>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = visibleMonth.atDay(1)
    val leadingBlankDays = firstDay.dayOfWeek.value - 1
    val days = List(size = leadingBlankDays) { null } +
            (1..visibleMonth.lengthOfMonth()).map { day -> visibleMonth.atDay(day) }
    val rows = days.chunked(size = 7)

    Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    if (date == null) {
                        Box(
                            modifier = Modifier
                                .weight(weight = 1f)
                                .aspectRatio(ratio = 1f)
                        )
                    } else {
                        val dateEntries = entriesByDate[date].orEmpty()
                        val hasOvulation =
                            dateEntries.any { it.type == CalendarEntryType.OVULATION }
                        val hasPeriodPrediction =
                            dateEntries.any { it.type == CalendarEntryType.PERIOD_PREDICTION }
                        val isConceptionRiskEvent = date in conceptionRiskEvents
                        CalendarDayCell(
                            modifier = Modifier.weight(weight = 1f),
                            date = date,
                            selected = date == selectedDate,
                            entryCount = dateEntries.size,
                            isPredictedPeriod = hasPeriodPrediction || isConceptionRiskEvent,
                            hasOvulation = hasOvulation,
                            onClick = { onDateClick(date) }
                        )
                    }
                }

                repeat(times = 7 - week.size) {
                    Box(
                        modifier = Modifier
                            .weight(weight = 1f)
                            .aspectRatio(ratio = 1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    modifier: Modifier,
    date: LocalDate,
    selected: Boolean,
    entryCount: Int,
    isPredictedPeriod: Boolean,
    hasOvulation: Boolean = false,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()

    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        hasOvulation -> MaterialTheme.colorScheme.secondaryContainer
        isPredictedPeriod -> MaterialTheme.colorScheme.tertiaryContainer
        entryCount > 0 -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        hasOvulation -> MaterialTheme.colorScheme.onSecondaryContainer
        isPredictedPeriod -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val fontWeight = when {
        isToday -> FontWeight.ExtraBold
        selected -> FontWeight.Bold
        else -> FontWeight.Medium
    }

    Surface(
        modifier = modifier
            .aspectRatio(ratio = 1f)
            .padding(all = 2.dp)
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(all = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = fontWeight),
                color = textColor
            )

            if (entryCount > 0) {
                Text(
                    text = entryCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CalendarEntriesContent(
    date: LocalDate,
    entries: List<CalendarEntry>?,
    selectedEntries: List<CalendarEntry>,
    error: String?,
    isFemale: Boolean,
    onEntryClick: (CalendarEntry) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier.padding(horizontal = 16.dp)
) {
    when {
        error != null && entries == null -> {
            ErrorCard(
                modifier = modifier,
                title = stringResource(id = R.string.calendar_error),
                error = error,
                onRefresh = onRefresh
            )
        }

        entries == null -> Unit

        else -> {
            SelectedDayEntries(
                modifier = modifier,
                date = date,
                entries = selectedEntries,
                isFemale = isFemale,
                onEntryClick = onEntryClick
            )
        }
    }
}

@Composable
private fun SelectedDayEntries(
    date: LocalDate,
    entries: List<CalendarEntry>,
    isFemale: Boolean,
    onEntryClick: (CalendarEntry) -> Unit,
    modifier: Modifier = Modifier.padding(horizontal = 16.dp)
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.selected_day, formatDate(date)),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        CardStackList(
            items = if (entries.isEmpty()) listOf(
                CardStackListItem(
                    title = stringResource(id = R.string.calendar_day_empty_title),
                    description = stringResource(id = R.string.calendar_day_empty_desc),
                    leadingContent = { Icon(icon = Icons.Default.CalendarMonth) }
                )
            ) else entries.map { entry ->
                CardStackListItem(
                    title = entry.type.label(),
                    description = entry.description(),
                    leadingContent = { Icon(icon = entry.type.icon()) },
                    onClick = if (entry.canManage(isFemale = isFemale)) {
                        { onEntryClick(entry) }
                    } else null
                )
            }
        )
    }
}
