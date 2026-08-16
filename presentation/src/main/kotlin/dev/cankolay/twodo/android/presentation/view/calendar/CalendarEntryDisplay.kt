package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.presentation.R

@Composable
internal fun CalendarEntry.description(): String {
    val details = when (type) {
        CalendarEntryType.NOTE -> notes.orEmpty()
        CalendarEntryType.PERIOD -> listOfNotNull(
            period?.event?.label(),
            period?.flowLevel?.label(),
            period?.symptoms?.takeIf { it.isNotEmpty() }?.map { symptom -> symptom.label() }
                ?.joinToString(),
            notes
        ).joinToString(separator = " · ")

        CalendarEntryType.OVULATION -> notes.orEmpty()
        CalendarEntryType.PERIOD_PREDICTION -> notes.orEmpty()
    }

    return details.ifBlank { stringResource(id = R.string.no_notes) }
}

internal fun CalendarEntry.canManage(isFemale: Boolean) =
    createdBy != null &&
            type != CalendarEntryType.OVULATION &&
            type != CalendarEntryType.PERIOD_PREDICTION &&
            (type != CalendarEntryType.PERIOD || isFemale)

@Composable
internal fun CalendarEntryType.icon(): ImageVector = when (this) {
    CalendarEntryType.NOTE -> Icons.Default.Edit
    CalendarEntryType.PERIOD -> Icons.Default.CalendarMonth
    CalendarEntryType.PERIOD_PREDICTION -> Icons.Default.CalendarMonth
    CalendarEntryType.OVULATION -> Icons.Default.AutoAwesome
}
