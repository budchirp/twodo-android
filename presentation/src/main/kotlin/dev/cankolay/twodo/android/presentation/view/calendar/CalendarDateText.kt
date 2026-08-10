package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern(stringResource(id = R.string.calendar_date_pattern)))
