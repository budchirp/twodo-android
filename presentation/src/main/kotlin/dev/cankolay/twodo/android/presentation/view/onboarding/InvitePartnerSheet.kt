package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.form.FormTextField
import dev.cankolay.twodo.android.presentation.viewmodel.invite.InvitePartnerFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InvitePartnerSheet(
    form: InvitePartnerFormState,
    result: ApiResult<*>?,
    onDismiss: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onInvite: () -> Unit,
) {
    val isLoading = result == ApiResult.Loading

    AppBottomSheet(
        title = stringResource(id = R.string.invite_partner),
        onDismiss = onDismiss,
        result = result,
        showLoading = false,
        actions = {
            Button(
                enabled = form.canSubmit && !isLoading,
                onClick = onInvite,
            ) {
                Text(text = stringResource(id = R.string.invite))
            }
        },
    ) {
        item {
            FormTextField(
                modifier = Modifier.fillMaxWidth(),
                field = form.username,
                enabled = !isLoading,
                onValueChange = onUsernameChange,
                label = stringResource(id = R.string.username),
                onSubmit = onInvite,
            )
        }
    }
}
