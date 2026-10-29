package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.MarkdownEditor
import dev.cankolay.twodo.android.presentation.composable.app.SettingCard
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.util.DateUtils
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.canManage
import java.time.LocalDate

internal fun LazyListScope.calendarEntries(
    date: LocalDate,
    entries: List<CalendarEntry>,
    canTrackPeriods: Boolean,
    onClick: (CalendarEntry) -> Unit,
) {
    val (notes, events) = entries.partition { it.type == CalendarEntryType.NOTE }

    item {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text =
                stringResource(
                    id = R.string.selected_day,
                    DateUtils.format(
                        date = date,
                        pattern = stringResource(id = R.string.calendar_date_pattern),
                    ),
                ),
            style = MaterialTheme.typography.titleMedium,
        )
    }

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
                                title = stringResource(id = entry.type.label),
                                description =
                                    parts.joinToString(separator = " · ").ifBlank {
                                        stringResource(id = R.string.no_notes)
                                    },
                                leadingContent = {
                                    Icon(
                                        icon =
                                            if (entry.type == CalendarEntryType.OVULATION)
                                                Icons.Default.AutoAwesome
                                            else Icons.Default.CalendarMonth
                                    )
                                },
                                onClick =
                                    if (entry.canManage(canTrackPeriods = canTrackPeriods))
                                        ({ onClick(entry) })
                                    else null,
                            )
                        },
                )
        }
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
                                    enabled = entry.canManage(canTrackPeriods = canTrackPeriods),
                                    onClick = { onClick(entry) },
                                )
                                .padding(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(space = 4.dp),
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
