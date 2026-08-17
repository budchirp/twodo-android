package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.viewmodel.invite.InvitePartnerFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InvitePartnerSheet(
    form: InvitePartnerFormState,
    onDismiss: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onInvite: () -> Unit
) {
    AppBottomSheet(
        title = stringResource(id = R.string.invite_partner),
        onDismiss = onDismiss,
        actions = {
            Button(
                enabled = form.canSubmit,
                onClick = onInvite
            ) {
                Text(text = stringResource(id = R.string.invite))
            }
        }
    ) {
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.username.value,
                onValueChange = onUsernameChange,
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.username)) },
                isError = form.username.error != null,
                supportingText = form.username.error?.let { error ->
                    { Text(text = stringResource(id = error)) }
                }
            )
        }
    }
}
