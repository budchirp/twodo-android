package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.IconButton
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
        when (state.result) {
            is ApiResult.Success -> viewModel.dismiss()

            else -> Unit
        }
    }

    CoupleSetupContent(
        state = state,
        onLogout = onLogout,
        onRefresh = viewModel::refresh,
        onOpenInvite = viewModel::openForm,
        onDismissSheet = viewModel::dismiss,
        onUsernameChange = viewModel::updateUsername,
        onInvite = viewModel::create,
        onHandleInvite = viewModel::handle,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CoupleSetupContent(
    state: CoupleSetupUiState,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onOpenInvite: () -> Unit,
    onDismissSheet: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onInvite: () -> Unit,
    onHandleInvite: (String, InviteAction) -> Unit,
) {
    OnboardingLayout(
        route = Route.CoupleSetup,
        title = stringResource(id = R.string.couple_setup_title),
        description = stringResource(id = R.string.couple_setup_desc),
        isLoading =
            state.user == ApiResult.Loading ||
                    state.invites == ApiResult.Loading ||
                    state.action == ApiResult.Loading,
        status = {
            when (val result = state.user) {
                is ApiResult.Failure ->
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = result.error.message,
                        onRefresh = onRefresh,
                    )

                else -> Unit
            }

            when (val result = state.invites) {
                is ApiResult.Failure ->
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = result.error.message,
                        onRefresh = onRefresh,
                    )

                else -> Unit
            }

            when (val result = state.action) {
                is ApiResult.Failure ->
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = result.error.message,
                        onRefresh = onRefresh,
                    )

                else -> Unit
            }
        },
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
            if (state.user is ApiResult.Success && state.invites is ApiResult.Success) {
                val sent =
                    state.invites.data.filter {
                        it.status == InviteStatus.PENDING && it.type == InviteType.Sent
                    }

                val received =
                    state.invites.data.filter {
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
                                        contentSize = null,
                                        contentPadding = PaddingValues(vertical = 12.dp),
                                        trailingContent = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement =
                                                    Arrangement.spacedBy(space = 8.dp),
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        onHandleInvite(
                                                            invite.id,
                                                            InviteAction.Reject,
                                                        )
                                                    },
                                                    enabled = state.action != ApiResult.Loading,
                                                ) {
                                                    Icon(icon = Icons.Default.Close)
                                                }

                                                IconButton(
                                                    enabled = state.action != ApiResult.Loading,
                                                    onClick = {
                                                        onHandleInvite(
                                                            invite.id,
                                                            InviteAction.Accept,
                                                        )
                                                    },
                                                ) {
                                                    Icon(icon = Icons.Default.Check)
                                                }
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
