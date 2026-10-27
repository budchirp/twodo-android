package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
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
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.Avatar
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.util.DateUtils
import dev.cankolay.twodo.android.presentation.viewmodel.user.CoupleUiState
import dev.cankolay.twodo.android.presentation.viewmodel.user.CoupleViewModel

@Composable
fun CoupleView(viewModel: CoupleViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoupleContent(
        userState = state,
        onRefresh = viewModel::refresh,
        onRequestLeave = viewModel::confirm,
        onDismissSheet = viewModel::dismiss,
        onLeave = viewModel::leave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoupleContent(
    userState: CoupleUiState,
    onRefresh: () -> Unit,
    onRequestLeave: () -> Unit,
    onDismissSheet: () -> Unit,
    onLeave: () -> Unit,
) {
    AppLayout(route = Route.Couple) {
        when (val user = userState.user) {
            ApiResult.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            is ApiResult.Failure -> ErrorCard(error = user.error.message, onRefresh = onRefresh)

            is ApiResult.Success -> {
                user.data.couple?.let { couple ->
                    AppLazyColumn {
                        item {
                            val first = couple.users.firstOrNull()
                            val second = couple.users.drop(n = 1).firstOrNull()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(modifier = Modifier.width(width = 128.dp)) {
                                    Avatar(
                                        modifier =
                                            Modifier
                                                .align(Alignment.CenterStart)
                                                .zIndex(zIndex = 1f),
                                        picture = first?.picture,
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        size = 80.dp,
                                    )

                                    Avatar(
                                        modifier = Modifier.align(Alignment.CenterEnd),
                                        picture = second?.picture,
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        size = 80.dp,
                                    )
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text =
                                            couple.users.joinToString(separator = " & ") {
                                                it.name
                                            },
                                        textAlign = TextAlign.Center,
                                        style =
                                            MaterialTheme.typography.titleLarge.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                    )

                                    val daysTogether =
                                        DateUtils.daysSince(couple.createdAt).toString()

                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = MaterialTheme.shapes.extraLarge,
                                    ) {
                                        Text(
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 16.dp,
                                                    vertical = 4.dp,
                                                ),
                                            style =
                                                LocalTextStyle.current.copy(
                                                    fontWeight = FontWeight.Medium
                                                ),
                                            text =
                                                stringResource(
                                                    id = R.string.days_together,
                                                    daysTogether,
                                                ),
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
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    ),
                                enabled = userState.result != ApiResult.Loading,
                                onClick = onRequestLeave,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(icon = Icons.Default.HeartBroken)

                                    Text(text = stringResource(id = R.string.break_up))
                                }
                            }
                        }
                    }
                }
            }

            null -> Unit
        }

        if (userState.showLeave) {
            BreakupPartnerSheet(
                result = userState.result,
                onDismiss = onDismissSheet,
                onLeave = onLeave,
            )
        }
    }
}
