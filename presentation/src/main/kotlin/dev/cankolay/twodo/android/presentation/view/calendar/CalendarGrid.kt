package dev.cankolay.twodo.android.presentation.view.calendar

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarProjection
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarProjectionType
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields

@Composable
internal fun CalendarGrid(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    entries: List<CalendarEntry>,
    projections: List<CalendarProjection>,
    canTrackPeriods: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entriesByDate = remember(key1 = entries) { entries.groupBy { it.date } }
    val locale = LocalLocale.current.platformLocale
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    val weekDays =
        remember(key1 = firstDayOfWeek) {
            List(size = 7) { firstDayOfWeek.plus(it.toLong()) }
        }
    val weeks =
        remember(key1 = visibleMonth, key2 = firstDayOfWeek) {
            val offset = (visibleMonth.atDay(1).dayOfWeek.value - firstDayOfWeek.value + 7) % 7
            val cellCount = ((offset + visibleMonth.lengthOfMonth() + 6) / 7) * 7
            List(size = cellCount) { index ->
                (index - offset + 1)
                    .takeIf { it in 1..visibleMonth.lengthOfMonth() }
                    ?.let { visibleMonth.atDay(it) }
            }
                .chunked(size = 7)
        }
    val today = LocalDate.now()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.previous_month),
                onClick = onPreviousMonth,
            )
            Text(
                text = visibleMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            AppIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(id = R.string.next_month),
                onClick = onNextMonth,
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    modifier = Modifier.weight(weight = 1f),
                    text = day.getDisplayName(TextStyle.SHORT, locale),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        val modifier = Modifier
                            .weight(weight = 1f)
                            .aspectRatio(ratio = 1f)
                        if (date == null) Box(modifier = modifier)
                        else
                            DayCell(
                                modifier = modifier,
                                date = date,
                                selected = date == selectedDate,
                                isToday = date == today,
                                entries = entriesByDate[date].orEmpty(),
                                projections = projections.filter { date in it.startDate..it.endDate },
                                onClick = { onDateClick(date) },
                            )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    entries: List<CalendarEntry>,
    projections: List<CalendarProjection>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val types = entries.map { it.type }.toSet()
    val estimatedTypes = projections.map { it.type }.toSet()
    val estimateLabel = stringResource(id = R.string.calendar_projection_estimate)
    val estimatedDescription =
        projections.joinToString(separator = ", ") { "$estimateLabel · ${it.label}" }
    val containerColor =
        when {
            selected -> colors.primaryContainer

            CalendarEntryType.OVULATION in types -> colors.secondaryContainer

            CalendarEntryType.PERIOD in types -> colors.tertiaryContainer

            CalendarProjectionType.PERIOD in estimatedTypes ->
                colors.tertiaryContainer.copy(alpha = 0.5f)

            CalendarProjectionType.OVULATION in estimatedTypes ->
                colors.secondaryContainer.copy(alpha = 0.5f)

            entries.isNotEmpty() -> colors.surfaceContainerHighest

            else -> colors.surfaceContainerLow
        }
    val textColor =
        when {
            selected -> colors.onPrimaryContainer

            isToday -> colors.primary

            CalendarEntryType.OVULATION in types -> colors.onSecondaryContainer

            CalendarEntryType.PERIOD in types || CalendarProjectionType.PERIOD in estimatedTypes ->
                colors.onTertiaryContainer

            CalendarProjectionType.OVULATION in estimatedTypes -> colors.onSecondaryContainer

            else -> colors.onSurface
        }
    Surface(
        modifier = modifier
            .padding(all = 2.dp)
            .semantics {
                if (estimatedDescription.isNotEmpty()) stateDescription = estimatedDescription
            },
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(all = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight =
                    when {
                        isToday -> FontWeight.ExtraBold

                        selected -> FontWeight.Bold

                        else -> FontWeight.Medium
                    },
                color = textColor,
            )
            if (entries.isNotEmpty()) {
                Text(
                    text = entries.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                )
            }
        }
    }
}
