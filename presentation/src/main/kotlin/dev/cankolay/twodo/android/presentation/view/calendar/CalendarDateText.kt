package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
internal fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern(stringResource(id = R.string.calendar_date_pattern)))

internal fun formatSummaryDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))

internal fun formatNoteDateTime(value: String): String =
    OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd EEE yyyy HH:mm"))
