package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodPrediction
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.util.DateUtils

@Composable
internal fun CalendarPredictionSheet(
    state: ApiResult<CalendarPredictionSummary>?,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
) {
    AppBottomSheet(
        title = stringResource(id = R.string.period_tracker),
        onDismiss = onDismiss,
        status = {
            when (state) {
                ApiResult.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ApiResult.Failure ->
                    ErrorCard(
                        modifier = Modifier.padding(all = 16.dp),
                        error = state.error.message,
                        onRefresh = onRefresh,
                    )

                else -> Unit
            }
        },
        content =
            when (state) {
                is ApiResult.Success -> {
                    {
                        val cycle = state.data.cyclePrediction

                        item {
                            SettingsGroup(
                                items = predictionItems(cycle = cycle),
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }

                        item {
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text =
                                    cycle?.disclaimer?.takeIf { it.isNotBlank() }
                                        ?: stringResource(id = R.string.default_medical_disclaimer),
                                style = MaterialTheme.typography.labelSmall,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                    }
                }

                else -> null
            },
    )
}

@Composable
private fun predictionItems(cycle: PeriodPrediction?): List<SettingItem> {
    if (cycle == null || !cycle.hasEnoughData) {
        return listOf(
            SettingItem(
                title = stringResource(id = R.string.prediction_unavailable),
                description =
                    cycle?.basis?.takeIf { it.isNotBlank() }
                        ?: stringResource(id = R.string.prediction_unavailable_desc),
            )
        )
    }

    return buildList {
        val start = cycle.nextPeriodWindow?.startDate ?: cycle.expectedPeriodStartDate
        val end = cycle.nextPeriodWindow?.endDate ?: cycle.expectedPeriodEndDate

        if (start != null) {
            add(
                element =
                    SettingItem(
                        title = stringResource(id = R.string.next_period_window),
                        description = DateUtils.formatRange(start = start, end = end),
                    )
            )
        }

        cycle.ovulationWindow?.let { window ->
            add(
                element =
                    SettingItem(
                        title = stringResource(id = R.string.ovulation_window),
                        description =
                            DateUtils.formatRange(start = window.startDate, end = window.endDate),
                    )
            )
        }

        val length = cycle.cycleLengthDays
        val duration = cycle.periodDurationDays

        if (length != null && duration != null) {
            add(
                element =
                    SettingItem(
                        title = stringResource(id = R.string.period_tracker_desc),
                        description =
                            stringResource(
                                id = R.string.period_tracker_averages,
                                length,
                                duration,
                            ),
                    )
            )
        }

        cycle.basis
            ?.takeIf { it.isNotBlank() }
            ?.let { basis ->
                add(
                    element =
                        SettingItem(
                            title = stringResource(id = R.string.period_tips_title),
                            description = basis,
                        )
                )
            }
    }
}
