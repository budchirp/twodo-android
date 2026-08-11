package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import dev.cankolay.twodo.android.domain.model.api.invite.InviteStatus
import dev.cankolay.twodo.android.domain.model.api.invite.InviteType
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.OnboardingLayout
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.application.AuthViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.invite.InvitePartnerFormState
import dev.cankolay.twodo.android.presentation.viewmodel.invite.InviteSheet
import dev.cankolay.twodo.android.presentation.viewmodel.invite.InviteViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CoupleSetupView(
    userViewModel: UserViewModel = hiltViewModel(),
    inviteViewModel: InviteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val userState by userViewModel.uiState.collectAsStateWithLifecycle()
    val user = userState.user
    HandleEvents(viewModel = userViewModel)

    val inviteState by inviteViewModel.uiState.collectAsStateWithLifecycle()
    val invites = inviteState.invites
    HandleEvents(viewModel = inviteViewModel) { event ->
        if (event == dev.cankolay.twodo.android.presentation.core.UiEvent.InviteAccepted) {
            userViewModel.fetchUser()
        }
    }

    OnboardingLayout(
        route = Route.CoupleSetup,
        title = stringResource(id = R.string.couple_setup_title),
        description = stringResource(id = R.string.couple_setup_desc),
        isLoading = userState.isLoading || inviteState.isLoading,
        onRefresh = {
            userViewModel.fetchUser()
            if (user != null) {
                inviteViewModel.fetchInvites()
            }
        },
        actions = {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    authViewModel.logout()
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon = Icons.AutoMirrored.Filled.Logout)

                    Text(text = stringResource(id = R.string.sign_out))
                }
            }
        },
        lazyContent = {
            when {
                user == null || userState.error != null -> {
                    item {
                        ErrorCard(
                            title = stringResource(id = R.string.couple_setup_error_title),
                            error = userState.error,
                            onRefresh = {
                                userViewModel.fetchUser()
                            })
                    }
                }

                inviteState.error != null && invites == null -> {
                    item {
                        ErrorCard(
                            title = stringResource(id = R.string.error),
                            error = inviteState.error,
                            onRefresh = { inviteViewModel.fetchInvites() }
                        )
                    }
                }

                else -> {
                    val pendingSent =
                        invites.orEmpty()
                            .filter { invite ->
                                invite.type == InviteType.Sent && invite.status == InviteStatus.PENDING
                            }

                    val pendingReceived =
                        invites.orEmpty()
                            .filter { invite ->
                                invite.type == InviteType.Received && invite.status == InviteStatus.PENDING
                            }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(space = 16.dp)) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                            ) {
                                Text(text = stringResource(id = R.string.received_invites))

                                CardStackList(
                                    items = if (pendingReceived.isEmpty()) listOf(
                                        CardStackListItem(
                                            title = stringResource(id = R.string.no_received_invites),
                                            description = stringResource(id = R.string.no_received_invites_desc),
                                            leadingContent = {
                                                Icon(icon = Icons.Default.Mail)
                                            },
                                        )
                                    ) else pendingReceived.map { invite ->
                                        CardStackListItem(
                                            title = invite.user.name,
                                            contentSize = null,
                                            contentPadding = PaddingValues(
                                                vertical = 12.dp
                                            ),
                                            trailingContent = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(
                                                        space = 8.dp
                                                    )
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            inviteViewModel.handleInvite(
                                                                action = InviteAction.Reject,
                                                                id = invite.id
                                                            )
                                                        },
                                                        enabled = !inviteState.isLoading
                                                    ) {
                                                        Icon(icon = Icons.Default.Close)
                                                    }

                                                    IconButton(
                                                        enabled = !inviteState.isLoading,
                                                        onClick = {
                                                            inviteViewModel.handleInvite(
                                                                action = InviteAction.Accept,
                                                                id = invite.id
                                                            )
                                                        }
                                                    ) {
                                                        Icon(icon = Icons.Default.Check)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                )
                            }

                            CardStackList(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                items = listOf(
                                    if (pendingSent.isNotEmpty()) CardStackListItem(
                                        title = stringResource(
                                            id = R.string.waiting_for,
                                            pendingSent.lastOrNull()?.user?.name ?: ""
                                        ),
                                        description = stringResource(id = R.string.waiting_for_partner_desc),
                                        leadingContent = {
                                            Icon(icon = Icons.Default.HourglassEmpty)
                                        },
                                    ) else
                                        CardStackListItem(
                                            title = stringResource(id = R.string.create_couple),
                                            description = stringResource(id = R.string.create_couple_desc),
                                            leadingContent = {
                                                Icon(icon = Icons.Default.PersonAdd)
                                            },
                                            onClick = {
                                                inviteViewModel.openInvitePartnerSheet()
                                            }
                                        )
                                )
                            )
                        }
                    }
                }
            }
        }) {
        (inviteState.activeSheet as? InviteSheet.InvitePartner)?.let { sheet ->
            InvitePartnerSheet(
                form = sheet.form,
                onDismiss = { inviteViewModel.dismissSheet() },
                onUsernameChange = { inviteViewModel.updateInviteUsername(username = it) },
                onInvite = inviteViewModel::submitInvite
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvitePartnerSheet(
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
