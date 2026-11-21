package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.invite.InviteStatus
import dev.cankolay.twodo.android.domain.model.api.invite.InviteType
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.OnboardingLayout
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.invite.CoupleSetupUiState
import dev.cankolay.twodo.android.presentation.viewmodel.invite.CoupleSetupViewModel

@Composable
fun CoupleSetupView(onLogout: () -> Unit, viewModel: CoupleSetupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state.result) {
        if (state.result is ApiResult.Success) viewModel.dismiss()
    }

    CoupleSetupContent(
        state = state,
        onLogout = onLogout,
        onRefresh = viewModel::refresh,
        onRefreshUser = viewModel::refreshUser,
        onRefreshInvites = { viewModel.fetch(forceRefresh = true) },
        onOpenInvite = viewModel::openForm,
        onDismissSheet = viewModel::dismiss,
        onUsernameChange = viewModel::updateUsername,
        onInvite = viewModel::create,
        onHandleInvite = viewModel::handle,
        onRetryAction = viewModel::retryAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CoupleSetupContent(
    state: CoupleSetupUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshUser: () -> Unit,
    onRefreshInvites: () -> Unit,
    onOpenInvite: () -> Unit,
    onDismissSheet: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onInvite: () -> Unit,
    onHandleInvite: (String, InviteAction) -> Unit,
    onRetryAction: () -> Unit,
) {
    OnboardingLayout(
        route = Route.CoupleSetup,
        title = stringResource(id = R.string.couple_setup_title),
        description = stringResource(id = R.string.couple_setup_desc),
        isLoading =
            state.userLoad == ApiResult.Loading ||
                    state.invitesLoad == ApiResult.Loading ||
                    state.action == ApiResult.Loading,
        onRefresh = onRefresh,
        actions = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogout,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(icon = Icons.AutoMirrored.Filled.Logout)

                    Text(text = stringResource(id = R.string.sign_out))
                }
            }
        },
        lazyContent = {
            val userLoad = state.userLoad
            if (userLoad is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = userLoad.error.message,
                        onRefresh = onRefreshUser,
                    )
                }
            }
            val invitesLoad = state.invitesLoad
            if (invitesLoad is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = invitesLoad.error.message,
                        onRefresh = onRefreshInvites,
                    )
                }
            }
            val action = state.action
            if (action is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = action.error.message,
                        onRefresh = onRetryAction,
                    )
                }
            }
            val invites = state.invites
            if (state.user != null && invites != null) {
                val sent =
                    invites.filter {
                        it.status == InviteStatus.PENDING && it.type == InviteType.Sent
                    }

                val received =
                    invites.filter {
                        it.status == InviteStatus.PENDING && it.type == InviteType.Received
                    }

                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(id = R.string.received_invites),
                    )
                }

                item {
                    SettingsGroup(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items =
                            if (received.isEmpty())
                                listOf(
                                    SettingItem(
                                        title = stringResource(id = R.string.no_received_invites),
                                        description =
                                            stringResource(id = R.string.no_received_invites_desc),
                                        leadingContent = {
                                            Icon(icon = Icons.Default.Mail)
                                        },
                                    )
                                )
                            else
                                received.map { invite ->
                                    SettingItem(
                                        title = invite.user.name,
                                        trailingContent = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(space = 8.dp),
                                            ) {
                                                AppIconButton(
                                                    icon = Icons.Default.Close,
                                                    contentDescription = stringResource(id = R.string.reject_invite),
                                                    onClick = {
                                                        onHandleInvite(
                                                            invite.id,
                                                            InviteAction.Reject,
                                                        )
                                                    },
                                                    enabled = state.action != ApiResult.Loading,
                                                )

                                                AppIconButton(
                                                    icon = Icons.Default.Check,
                                                    contentDescription = stringResource(id = R.string.accept_invite),
                                                    enabled = state.action != ApiResult.Loading,
                                                    onClick = {
                                                        onHandleInvite(
                                                            invite.id,
                                                            InviteAction.Accept,
                                                        )
                                                    },
                                                )
                                            }
                                        },
                                    )
                                },
                    )
                }

                item {
                    SettingsGroup(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items =
                            listOf(
                                if (sent.isNotEmpty())
                                    SettingItem(
                                        title =
                                            stringResource(
                                                id = R.string.waiting_for,
                                                sent.lastOrNull()?.user?.name ?: "",
                                            ),
                                        description =
                                            stringResource(id = R.string.waiting_for_partner_desc),
                                        leadingContent = {
                                            Icon(icon = Icons.Default.HourglassEmpty)
                                        },
                                    )
                                else
                                    SettingItem(
                                        title = stringResource(id = R.string.create_couple),
                                        description =
                                            stringResource(id = R.string.create_couple_desc),
                                        leadingContent = {
                                            Icon(icon = Icons.Default.PersonAdd)
                                        },
                                        onClick = onOpenInvite,
                                    )
                            ),
                    )
                }
            }
        },
    ) {
        if (state.showForm) {
            InvitePartnerSheet(
                form = state.form,
                result = state.result,
                onDismiss = onDismissSheet,
                onUsernameChange = onUsernameChange,
                onInvite = onInvite,
            )
        }
    }
}
