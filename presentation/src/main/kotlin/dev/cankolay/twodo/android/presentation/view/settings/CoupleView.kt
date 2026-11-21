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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
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
import dev.cankolay.twodo.android.presentation.composable.app.AppCard
import dev.cankolay.twodo.android.presentation.composable.app.AppCardDefaults
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator
import dev.cankolay.twodo.android.presentation.composable.app.Avatar
import dev.cankolay.twodo.android.presentation.composable.app.DateSetting
import dev.cankolay.twodo.android.presentation.composable.app.DateSettingState
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.rememberDateSettingState
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.user.CoupleUiState
import dev.cankolay.twodo.android.presentation.viewmodel.user.CoupleViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun CoupleView(viewModel: CoupleViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CoupleContent(
        state = state,
        actions =
            CoupleActions(
                onRefresh = viewModel::refresh,
                onDate = viewModel::updateRelationshipDate,
                onRetryDate = viewModel::retryDate,
                onRequestLeave = viewModel::confirm,
                onDismissSheet = viewModel::dismiss,
                onLeave = viewModel::leave,
            ),
        pickerState = rememberDateSettingState(allowFuture = false),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoupleContent(
    state: CoupleUiState,
    actions: CoupleActions,
    pickerState: DateSettingState,
) {
    AppLayout(route = Route.Couple) {
        AppLinearProgressIndicator(
            isLoading = state.load == ApiResult.Loading || state.dateResult == ApiResult.Loading
        )

        AppLazyColumn {
            val load = state.load
            if (load is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = load.error.message,
                        onRefresh = actions.onRefresh,
                    )
                }
            }

            state.couple?.let { couple ->
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
                                text = couple.users.joinToString(separator = " & ") { it.name },
                                textAlign = TextAlign.Center,
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                            )

                            val daysTogether =
                                couple.relationshipStartDate?.let {
                                    ChronoUnit.DAYS.between(it, LocalDate.now()).toInt()
                                }

                            if (daysTogether != null)
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.extraLarge,
                                ) {
                                    Text(
                                        modifier =
                                            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                        style =
                                            LocalTextStyle.current.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                        text =
                                            pluralStringResource(
                                                R.plurals.days_together,
                                                daysTogether,
                                                daysTogether,
                                            ),
                                    )
                                }
                        }
                    }
                }

                item {
                    AppCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentPadding = AppCardDefaults.NoPadding,
                    ) {
                        DateSetting(
                            pickerState = pickerState,
                            title = stringResource(id = R.string.relationship_start_date),
                            value =
                                if (state.hasDateDraft) state.dateDraft
                                else couple.relationshipStartDate,
                            allowFuture = false,
                            enabled = state.dateResult != ApiResult.Loading,
                            onChange = actions.onDate,
                        )
                    }
                }

                val dateResult = state.dateResult
                if (dateResult is ApiResult.Failure) {
                    item {
                        ErrorCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            error = dateResult.error.message,
                            onRefresh = actions.onRetryDate,
                        )
                    }
                }

                item { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer) }

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
                        enabled = state.result != ApiResult.Loading,
                        onClick = actions.onRequestLeave,
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

        if (state.showLeave) {
            LeaveCoupleSheet(result = state.result, actions = actions)
        }
    }
}

internal data class CoupleActions(
    val onRefresh: () -> Unit,
    val onDate: (LocalDate?) -> Unit,
    val onRetryDate: () -> Unit,
    val onRequestLeave: () -> Unit,
    val onDismissSheet: () -> Unit,
    val onLeave: () -> Unit,
)
