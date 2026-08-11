package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.Avatar
import dev.cankolay.twodo.android.presentation.composable.app.Card
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.DestructiveConfirmationSheet
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.core.HandleEvents
import dev.cankolay.twodo.android.presentation.navigation.resetTo
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.user.UserSheet
import dev.cankolay.twodo.android.presentation.viewmodel.user.UserViewModel
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoupleView(userViewModel: UserViewModel = hiltViewModel()) {
    val navBackStack = LocalNavBackStack.current

    val userState by userViewModel.uiState.collectAsStateWithLifecycle()
    val user = userState.user
    HandleEvents(viewModel = userViewModel)

    val isLoading = userState.isLoading
    val error = userState.error

    AppLayout(route = Route.Couple) {
        if (user == null) {
            AppLazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isLoading && error == null) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    error?.let { message ->
                        item {
                            Card(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )

                                    Button(onClick = {
                                        userViewModel.fetchUser()
                                    }) {
                                        Text(text = stringResource(id = R.string.try_again))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val couple = user?.couple
        AnimatedVisibility(
            visible = couple != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (couple != null) {
                AppLazyColumn {
                    item {
                        val first = couple.users.firstOrNull()
                        val second = couple.users.drop(n = 1).firstOrNull()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.width(width = 128.dp)
                            ) {
                                Avatar(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .zIndex(zIndex = 1f),
                                    picture = first?.picture,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    size = 80.dp
                                )

                                Avatar(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd),
                                    picture = second?.picture,
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    size = 80.dp
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = couple.users.joinToString(separator = " & ") { it.name },
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )

                                val daysTogether = runCatching {
                                    ChronoUnit.DAYS.between(
                                        OffsetDateTime.parse(couple.createdAt),
                                        OffsetDateTime.now()
                                    ).toString()
                                }.getOrDefault(defaultValue = "0")

                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    Text(
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 4.dp
                                        ),
                                        style = LocalTextStyle.current.copy(fontWeight = FontWeight.Medium),
                                        text = stringResource(
                                            id = R.string.days_together,
                                            daysTogether,
                                        )
                                    )
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                    }

                    item {
                        Button(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            onClick = {
                                userViewModel.openLeaveCoupleSheet()
                            }) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon = Icons.Default.HeartBroken)

                                Text(
                                    text = stringResource(id = R.string.break_up)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = user != null && user.couple == null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AppLazyColumn {
                item {
                    CardStackList(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items = listOf(
                            CardStackListItem(
                                title = stringResource(id = R.string.couple_setup),
                                description = stringResource(id = R.string.couple_setup_desc),
                                leadingContent = {
                                    Icon(icon = Icons.Default.PersonAdd)
                                },
                                onClick = {
                                    navBackStack.resetTo(Route.CoupleSetup)
                                }
                            )
                        )
                    )
                }
            }
        }

        if (userState.activeSheet is UserSheet.LeaveCouple) {
            BreakupPartnerSheet(
                onDismiss = { userViewModel.dismissSheet() },
                onLeave = userViewModel::leaveCouple
            )
        }
    }
}

@Composable
fun BreakupPartnerSheet(
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
