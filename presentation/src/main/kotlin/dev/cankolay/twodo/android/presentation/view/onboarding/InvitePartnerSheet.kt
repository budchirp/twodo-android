package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.form.FormTextField
import dev.cankolay.twodo.android.presentation.viewmodel.invite.CoupleSetupUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InvitePartnerSheet(state: CoupleSetupUiState, actions: CoupleSetupActions) {

    val isLoading = state.result == ApiResult.Loading

    AppBottomSheet(
        title = stringResource(id = R.string.invite_partner),
        onDismiss = actions.onDismissSheet,
        result = state.result,
        onRefresh = actions.onInvite,
        actions = {
            Button(enabled = state.form.canSubmit && !isLoading, onClick = actions.onInvite) {
                Text(text = stringResource(id = R.string.invite))
            }
        },
    ) {
        item {
            FormTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                field = state.form.username,
                enabled = !isLoading,
                onValueChange = actions.onUsernameChange,
                label = stringResource(id = R.string.username),
                onSubmit = actions.onInvite,
            )
        }
    }
}
