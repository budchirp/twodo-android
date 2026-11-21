package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet

@Composable
internal fun LogoutSheet(actions: ProfileActions) {
    DestructiveConfirmationSheet(
        title = stringResource(id = R.string.sign_out),
        description = stringResource(id = R.string.sign_out_desc),
        confirmText = stringResource(id = R.string.sign_out),
        onDismiss = actions.onDismissLogout,
        onConfirm = actions.onLogout,
    )
}
