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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor
import dev.cankolay.twodo.android.presentation.composable.app.PullToRefreshLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppTopAppBar
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.util.DateUtils
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
            item {
                Calendar(
                    visibleMonth = uiState.visibleMonth,
                    selectedDate = uiState.selectedDate,
                    entries = entries.orEmpty(),
                    onPreviousMonth = { calendarViewModel.moveMonth(months = -1) },
                    onNextMonth = { calendarViewModel.moveMonth(months = 1) },
                    onDateClick = { calendarViewModel.selectDate(date = it) }
                )
            }

            item {
                Entries(
                    date = uiState.selectedDate,
                    entries = entries,
                    selected = selectedEntries,
                    error = uiState.error,
                    isFemale = isFemale,
                    onClick = { calendarViewModel.openEditEntrySheet(entry = it) },
                    onRefresh = { calendarViewModel.fetchEntries() }
                )
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
                    onSymptomsChange = { calendarViewModel.updateSymptoms(symptoms = it) }
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
private fun Calendar(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    entries: List<CalendarEntry>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
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

        MonthGrid(
            visibleMonth = visibleMonth,
            selectedDate = selectedDate,
            entriesByDate = entriesByDate,
            onDateClick = onDateClick
        )
    }
}

@Composable
private fun MonthGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    entriesByDate: Map<LocalDate, List<CalendarEntry>>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = visibleMonth.atDay(1)
    val days = List(size = firstDay.dayOfWeek.value - 1) { null } +
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
                        DayCell(
                            modifier = Modifier.weight(weight = 1f),
                            date = date,
                            selected = date == selectedDate,
                            entryCount = dateEntries.size,
                            hasPeriod = hasPeriodPrediction,
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
private fun DayCell(
    modifier: Modifier,
    date: LocalDate,
    selected: Boolean,
    entryCount: Int,
    hasPeriod: Boolean,
    hasOvulation: Boolean = false,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()

    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        hasOvulation -> MaterialTheme.colorScheme.secondaryContainer
        hasPeriod -> MaterialTheme.colorScheme.tertiaryContainer
        entryCount > 0 -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        hasOvulation -> MaterialTheme.colorScheme.onSecondaryContainer
        hasPeriod -> MaterialTheme.colorScheme.onTertiaryContainer
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
private fun Entries(
    date: LocalDate,
    entries: List<CalendarEntry>?,
    selected: List<CalendarEntry>,
    error: String?,
    isFemale: Boolean,
    onClick: (CalendarEntry) -> Unit,
    onRefresh: () -> Unit,
) {
    when {
        error != null && entries == null -> {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = stringResource(id = R.string.calendar_error),
                error = error,
                onRefresh = onRefresh
            )
        }

        entries == null -> Unit

        else -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(space = 16.dp)
            ) {
                EntriesCard(
                    date = date,
                    entries = selected.filterNot { it.type == CalendarEntryType.NOTE },
                    isFemale = isFemale,
                    onClick = onClick
                )
                Notes(
                    entries = selected.filter { it.type == CalendarEntryType.NOTE },
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun EntriesCard(
    date: LocalDate,
    entries: List<CalendarEntry>,
    isFemale: Boolean,
    onClick: (CalendarEntry) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(
                id = R.string.selected_day,
                DateUtils.format(
                    date = date,
                    pattern = stringResource(id = R.string.calendar_date_pattern)
                )
            ),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )

        if (entries.isEmpty()) {
            CardStackList(
                items = listOf(
                    CardStackListItem(
                        title = stringResource(id = R.string.calendar_day_empty_title),
                        description = stringResource(id = R.string.calendar_day_empty_desc),
                        leadingContent = { Icon(icon = Icons.Default.CalendarMonth) }
                    )
                )
            )
        } else {
            CardStackList(
                items = entries.map { entry ->
                    val title = stringResource(
                        id = when (entry.type) {
                            CalendarEntryType.NOTE -> R.string.calendar_type_note
                            CalendarEntryType.PERIOD -> R.string.calendar_type_period
                            CalendarEntryType.PERIOD_PREDICTION -> R.string.expected_period
                            CalendarEntryType.OVULATION -> R.string.calendar_type_ovulation
                        }
                    )

                    val description = when (entry.type) {
                        CalendarEntryType.NOTE -> entry.notes.orEmpty()
                        CalendarEntryType.PERIOD -> listOfNotNull(
                            entry.period?.event?.let { event ->
                                stringResource(
                                    id = when (event) {
                                        PeriodEvent.START -> R.string.period_event_start
                                        PeriodEvent.DAY -> R.string.period_event_day
                                        PeriodEvent.END -> R.string.period_event_end
                                    }
                                )
                            },
                            entry.period?.flowLevel?.let { flowLevel ->
                                stringResource(
                                    id = when (flowLevel) {
                                        FlowLevel.SPOTTING -> R.string.flow_spotting
                                        FlowLevel.LIGHT -> R.string.flow_light
                                        FlowLevel.MEDIUM -> R.string.flow_medium
                                        FlowLevel.HEAVY -> R.string.flow_heavy
                                    }
                                )
                            },
                            entry.period?.symptoms
                                ?.takeIf { it.isNotEmpty() }
                                ?.map { symptom ->
                                    stringResource(
                                        id = when (symptom) {
                                            PeriodSymptom.ACNE -> R.string.symptom_acne
                                            PeriodSymptom.BACK_PAIN -> R.string.symptom_back_pain
                                            PeriodSymptom.BLOATING -> R.string.symptom_bloating
                                            PeriodSymptom.BREAST_TENDERNESS -> R.string.symptom_breast_tenderness
                                            PeriodSymptom.CRAMPS -> R.string.symptom_cramps
                                            PeriodSymptom.FATIGUE -> R.string.symptom_fatigue
                                            PeriodSymptom.HEADACHE -> R.string.symptom_headache
                                            PeriodSymptom.MOOD_CHANGES -> R.string.symptom_mood_changes
                                            PeriodSymptom.NAUSEA -> R.string.symptom_nausea
                                        }
                                    )
                                }
                                ?.joinToString(),
                            entry.notes
                        ).joinToString(separator = " · ")

                        CalendarEntryType.OVULATION,
                        CalendarEntryType.PERIOD_PREDICTION -> entry.notes.orEmpty()
                    }.ifBlank { stringResource(id = R.string.no_notes) }

                    val icon = when (entry.type) {
                        CalendarEntryType.NOTE -> Icons.Default.Edit
                        CalendarEntryType.PERIOD,
                        CalendarEntryType.PERIOD_PREDICTION -> Icons.Default.CalendarMonth

                        CalendarEntryType.OVULATION -> Icons.Default.AutoAwesome
                    }

                    val canManage = entry.createdBy != null &&
                            entry.type != CalendarEntryType.OVULATION &&
                            entry.type != CalendarEntryType.PERIOD_PREDICTION &&
                            (entry.type != CalendarEntryType.PERIOD || isFemale)

                    CardStackListItem(
                        title = title,
                        description = description,
                        leadingContent = { Icon(icon = icon) },
                        onClick = if (canManage) {
                            { onClick(entry) }
                        } else null
                    )
                }
            )
        }
    }
}

@Composable
private fun Notes(
    entries: List<CalendarEntry>,
    onClick: (CalendarEntry) -> Unit
) {
    if (entries.isEmpty()) return

    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)

        entries.forEachIndexed { index, entry ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(entry) }
                    .padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 4.dp)
            ) {
                Text(
                    text = DateUtils.format(value = entry.createdAt, DateUtils.TIME_PATTERN),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                MarkdownEditor(
                    markdown = entry.notes.orEmpty(),
                    readOnly = true
                )
            }

            if (index < entries.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            }
        }
    }
}
