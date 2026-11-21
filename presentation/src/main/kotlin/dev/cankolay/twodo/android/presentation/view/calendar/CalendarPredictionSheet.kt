package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodPrediction
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.util.DateUtils
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarUiState

@Composable
internal fun CalendarPredictionSheet(state: CalendarUiState, actions: CalendarActions) {

    AppBottomSheet(
        title = stringResource(id = R.string.period_tracker),
        onDismiss = actions.onDismiss,
        result = state.predictionLoad,
        onRefresh = actions.onRefreshPrediction,
        content =
            if (state.prediction != null) {
                {
                    val cycle = state.prediction.prediction

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
                                cycle.disclaimer?.takeIf { it.isNotBlank() }
                                    ?: stringResource(id = R.string.default_medical_disclaimer),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            } else null,
    )
}

@Composable
private fun predictionItems(cycle: PeriodPrediction): List<SettingItem> {
    if (!cycle.available) {
        return listOf(
            SettingItem(
                title = stringResource(id = R.string.prediction_unavailable),
                description = stringResource(id = R.string.prediction_unavailable_desc),
            )
        )
    }

    return buildList {
        val start = cycle.nextPeriodWindow?.startDate
        val end = cycle.nextPeriodWindow?.endDate

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
                            stringResource(id = R.string.period_tracker_averages, length, duration),
                    )
            )
        }
    }
}
