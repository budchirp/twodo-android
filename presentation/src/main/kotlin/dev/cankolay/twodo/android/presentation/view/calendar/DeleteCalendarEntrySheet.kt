package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet

@Composable
internal fun DeleteCalendarEntrySheet(
    result: ApiResult<*>?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    DestructiveConfirmationSheet(
        title = stringResource(id = R.string.delete_calendar_entry),
        description = stringResource(id = R.string.delete_calendar_entry_desc),
        confirmText = stringResource(id = R.string.delete),
        result = result,
        onDismiss = onDismiss,
        onConfirm = onDelete,
    )
}
