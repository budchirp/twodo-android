package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet

@Composable
internal fun DeleteTaskSheet(result: ApiResult<*>?, actions: TaskActions) {
    DestructiveConfirmationSheet(
        title = stringResource(id = R.string.delete_task),
        description = stringResource(id = R.string.delete_task_description),
        confirmText = stringResource(id = R.string.delete),
        result = result,
        onConfirm = actions.onDelete,
        onDismiss = actions.onDismissDelete,
    )
}
