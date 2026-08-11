package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.ConceptionRiskLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PregnancyAssessmentStatus
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionSummarySheet(
    summary: CalendarPredictionSummary?,
    onDismiss: () -> Unit
) {
    val titleText = stringResource(id = R.string.fertile_window_title)
    val ovulationLabel = stringResource(id = R.string.estimated_ovulation)
    val fertileWindowLabel = stringResource(id = R.string.fertile_window)
    val conceptionRiskLabel = stringResource(id = R.string.conception_risk)
    val pregnancyTestText = stringResource(id = R.string.pregnancy_test_recommended)
    val defaultDisclaimer = stringResource(id = R.string.default_medical_disclaimer)

    AppBottomSheet(
        title = titleText,
        onDismiss = onDismiss
    ) {
        summary?.let { data ->
            val fertility = data.fertilityWindow
            val pregnancy = data.pregnancyAssessment
            val risk = data.conceptionRisk
            val cycle = data.cyclePrediction

            if (pregnancy.status == PregnancyAssessmentStatus.POSSIBLE_PREGNANCY || pregnancy.needsPregnancyTest) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = pregnancyTestText,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                if (pregnancy.explanation.isNotBlank()) {
                                    Text(
                                        text = pregnancy.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (pregnancy.status == PregnancyAssessmentStatus.PERIOD_LATE && pregnancy.daysLate > 0) {
                item {
                    val lateText =
                        stringResource(id = R.string.period_late_days, pregnancy.daysLate)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = lateText,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            val items = buildList {
                fertility.ovulationDate?.let { date ->
                    add(
                        CardStackListItem(
                            title = ovulationLabel,
                            description = formatSummaryDate(date)
                        )
                    )
                }

                val startDate = fertility.fertileWindowStartDate
                val endDate = fertility.fertileWindowEndDate
                if (startDate != null && endDate != null) {
                    add(
                        CardStackListItem(
                            title = fertileWindowLabel,
                            description = "${formatSummaryDate(startDate)} – ${
                                formatSummaryDate(
                                    endDate
                                )
                            }"
                        )
                    )
                }

                if (risk.level != ConceptionRiskLevel.UNKNOWN && risk.level != ConceptionRiskLevel.NONE) {
                    add(
                        CardStackListItem(
                            title = conceptionRiskLabel,
                            description = if (risk.explanation.isNotBlank()) "${risk.level.name} (${risk.explanation})" else risk.level.name
                        )
                    )
                }
            }

            if (items.isNotEmpty()) {
                item {
                    CardStackList(items = items)
                }
            }

            item {
                Text(
                    text = cycle?.disclaimer ?: defaultDisclaimer,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatSummaryDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
}
