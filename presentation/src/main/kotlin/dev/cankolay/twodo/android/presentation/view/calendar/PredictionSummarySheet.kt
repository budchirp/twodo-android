package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PredictionSummarySheet(
    summary: CalendarPredictionSummary?,
    onDismiss: () -> Unit
) {
    AppBottomSheet(
        title = stringResource(id = R.string.period_tracker),
        onDismiss = onDismiss
    ) {
        summary?.let { data ->
            val cycle = data.cyclePrediction

            item {
                val items = if (cycle == null || !cycle.hasEnoughData) {
                    listOf(
                        CardStackListItem(
                            title = stringResource(id = R.string.prediction_unavailable),
                            description = cycle?.basis?.takeIf { it.isNotBlank() }
                                ?: stringResource(id = R.string.prediction_unavailable_desc)
                        )
                    )
                } else {
                    val cycleLength = cycle.cycleLengthDays
                    val periodDuration = cycle.periodDurationDays

                    val averagesText = if (
                        cycleLength != null && periodDuration != null
                    ) {
                        stringResource(
                            id = R.string.period_tracker_averages,
                            cycleLength,
                            periodDuration
                        )
                    } else {
                        null
                    }

                    val periodWindow = cycle.nextPeriodWindow
                    val start = cycle.expectedPeriodStartDate
                    val end = cycle.expectedPeriodEndDate

                    listOfNotNull(
                        when {
                            periodWindow != null -> {
                                CardStackListItem(
                                    title = stringResource(id = R.string.next_period_window),
                                    description = "${
                                        DateUtils.format(
                                            periodWindow.startDate,
                                            DateUtils.SUMMARY_DATE_PATTERN
                                        )
                                    } – ${
                                        DateUtils.format(
                                            periodWindow.endDate,
                                            DateUtils.SUMMARY_DATE_PATTERN
                                        )
                                    }"
                                )
                            }

                            start != null -> {
                                CardStackListItem(
                                    title = stringResource(id = R.string.next_period_window),
                                    description = if (end != null) {
                                        "${
                                            DateUtils.format(
                                                start,
                                                DateUtils.SUMMARY_DATE_PATTERN
                                            )
                                        } – ${
                                            DateUtils.format(
                                                end,
                                                DateUtils.SUMMARY_DATE_PATTERN
                                            )
                                        }"
                                    } else {
                                        DateUtils.format(start, DateUtils.SUMMARY_DATE_PATTERN)
                                    }
                                )
                            }

                            else -> null
                        },

                        cycle.ovulationWindow?.let { window ->
                            CardStackListItem(
                                title = stringResource(id = R.string.ovulation_window),
                                description = "${
                                    DateUtils.format(
                                        window.startDate,
                                        DateUtils.SUMMARY_DATE_PATTERN
                                    )
                                } – ${
                                    DateUtils.format(
                                        window.endDate,
                                        DateUtils.SUMMARY_DATE_PATTERN
                                    )
                                }"
                            )
                        },

                        averagesText?.let {
                            CardStackListItem(
                                title = stringResource(id = R.string.period_tracker_desc),
                                description = it
                            )
                        },

                        cycle.basis?.takeIf { it.isNotBlank() }?.let {
                            CardStackListItem(
                                title = stringResource(id = R.string.period_tips_title),
                                description = it
                            )
                        }
                    )
                }

                CardStackList(items = items)
            }

            item {
                val defaultDisclaimer = stringResource(id = R.string.default_medical_disclaimer)

                Text(
                    text = cycle?.disclaimer?.takeIf { it.isNotBlank() } ?: defaultDisclaimer,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}
