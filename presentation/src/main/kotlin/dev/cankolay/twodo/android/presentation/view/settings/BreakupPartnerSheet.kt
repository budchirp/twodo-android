package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BreakupPartnerSheet(
    onDismiss: () -> Unit,
    onLeave: () -> Unit
) {
    DestructiveConfirmationSheet(
        title = stringResource(id = R.string.break_up),
        description = stringResource(id = R.string.break_up_desc),
        confirmText = stringResource(id = R.string.break_up),
        onDismiss = onDismiss,
        onConfirm = onLeave
    )
}
