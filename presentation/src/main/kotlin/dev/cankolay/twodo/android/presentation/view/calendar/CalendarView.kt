package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.composition.LocalSnackbarHostState
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.CalendarViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.UserViewModel
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
    val navBackStack = LocalNavBackStack.current
    val snackbarHostState = LocalSnackbarHostState.current

    val userState by userViewModel.uiState.collectAsStateWithLifecycle()
    val isFemale = userState.user?.gender == Gender.FEMALE

    val uiState by calendarViewModel.uiState.collectAsStateWithLifecycle()
    val entries = uiState.entries
    val selectedEntries = entries.orEmpty().filter { it.date == uiState.selectedDate }

    LaunchedEffect(key1 = Unit) {
        calendarViewModel.fetchCalendar()
        calendarViewModel.fetchPeriodTracker()
    }

    LaunchedEffect(key1 = uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(message = it) }
    }

    LaunchedEffect(key1 = uiState.errorCode) {
        if (uiState.errorCode == "error-profile-required") {
            userViewModel.fetchUser()
            navBackStack.add(element = Route.ProfileSetup)
            while (navBackStack.size > 1) {
                navBackStack.removeAt(0)
            }
        }
    }

    AppLayout(route = Route.Calendar, topBar = { context ->
        AppTopAppBar(context = context, trailingContent = {
            IconButton(onClick = { navBackStack.add(element = Route.PeriodTracker) }) {
                Icon(icon = Icons.Default.Info)
            }

            IconButton(onClick = { calendarViewModel.openCreateEntryForm() }) {
                Icon(icon = Icons.Default.Add)
            }
        })
    }) {
        PullToRefreshLazyColumn(
            isLoading = uiState.isLoading,
            onRefresh = {
                calendarViewModel.fetchCalendar()
                calendarViewModel.fetchPeriodTracker()
            }
        ) {
            item {
                MonthCalendarCard(
                    visibleMonth = uiState.visibleMonth,
                    selectedDate = uiState.selectedDate,
                    entries = entries.orEmpty(),
                    predictedPeriodDates = uiState.predictedPeriodDates,
                    onPreviousMonth = { calendarViewModel.moveMonth(months = -1) },
                    onNextMonth = { calendarViewModel.moveMonth(months = 1) },
                    onDateClick = { calendarViewModel.selectDate(date = it) }
                )
            }

            when {
                uiState.error != null && entries == null -> {
                    item {
                        ErrorCard(
                            title = stringResource(id = R.string.calendar_error),
                            error = uiState.error,
                            onRefresh = { calendarViewModel.fetchCalendar() }
                        )
                    }
                }

                entries == null -> Unit

                else -> {
                    item {
                        SelectedDayEntries(
                            date = uiState.selectedDate,
                            entries = selectedEntries,
                            isFemale = isFemale,
                            onEntryClick = { calendarViewModel.openEditEntryForm(entry = it) }
                        )
                    }
                }
            }
        }

        uiState.entryForm?.let { form ->
            CalendarEntrySheet(
                form = form,
                isFemale = isFemale,
                isSaving = uiState.isSaving,
                onDismiss = { calendarViewModel.dismissEntryForm() },
                onDelete = { calendarViewModel.requestDeleteEntry() },
                onSave = { calendarViewModel.submitEntryForm(isFemale = isFemale) },
                onDateChange = { calendarViewModel.updateEntryDate(date = it) },
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

        if (uiState.deletingEntry != null) {
            DeleteCalendarEntrySheet(
                isLoading = uiState.isSaving,
                onDismiss = { calendarViewModel.dismissDeleteEntry() },
                onDelete = { calendarViewModel.deleteSelectedEntry(isFemale = isFemale) }
            )
        }
    }
}

@Composable
private fun MonthCalendarCard(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    entries: List<CalendarEntry>,
    predictedPeriodDates: Set<LocalDate>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (LocalDate) -> Unit
) {
    val entriesByDate = entries.groupBy { it.date }
    val locale = LocalLocale.current.platformLocale

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(icon = Icons.Default.ArrowBack)
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
                Icon(icon = Icons.Default.ArrowForward)
            }
        }

        CalendarWeekHeader()
        CalendarMonthGrid(
            visibleMonth = visibleMonth,
            selectedDate = selectedDate,
            entriesByDate = entriesByDate,
            predictedPeriodDates = predictedPeriodDates,
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
    predictedPeriodDates: Set<LocalDate>,
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
                        CalendarDayCell(
                            modifier = Modifier.weight(weight = 1f),
                            date = date,
                            selected = date == selectedDate,
                            entryCount = dateEntries.size,
                            isPredictedPeriod = date in predictedPeriodDates || hasPeriodPrediction,
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
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        hasOvulation -> MaterialTheme.colorScheme.secondaryContainer
        isPredictedPeriod -> MaterialTheme.colorScheme.tertiaryContainer
        entryCount > 0 -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> MaterialTheme.colorScheme.surfaceContainerLow
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
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            if (entryCount > 0 || isPredictedPeriod || hasOvulation) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (entryCount > 0) {
                        Text(
                            text = entryCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (hasOvulation) {
                        Surface(
                            modifier = Modifier.size(size = 6.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary
                        ) {}
                    }

                    if (isPredictedPeriod) {
                        Surface(
                            modifier = Modifier.size(size = 6.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayEntries(
    date: LocalDate,
    entries: List<CalendarEntry>,
    isFemale: Boolean,
    onEntryClick: (CalendarEntry) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
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
