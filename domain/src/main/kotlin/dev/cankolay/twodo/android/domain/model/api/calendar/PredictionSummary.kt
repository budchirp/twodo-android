package dev.cankolay.twodo.android.domain.model.api.calendar

import java.time.LocalDate

enum class PeriodPredictionReliability(val value: String) {
    HIGH("high"),
    MEDIUM("medium"),
    LOW("low"),
    INSUFFICIENT_DATA("insufficient_data"),
    UNKNOWN("unknown");

    companion object {
        fun fromValue(value: String): PeriodPredictionReliability =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

data class DateWindow(
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class CycleRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val durationDays: Int,
    val isComplete: Boolean = true,
    val flowLevels: List<FlowLevel> = emptyList(),
    val symptoms: List<PeriodSymptom> = emptyList()
)

data class CycleHistory(
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate,
    val periodDurationDays: Int,
    val cycleLengthDays: Int
)

data class PeriodPrediction(
    val hasEnoughData: Boolean = false,
    val reliability: PeriodPredictionReliability = PeriodPredictionReliability.UNKNOWN,
    val nextPeriodWindow: DateWindow? = null,
    val ovulationWindow: DateWindow? = null,
    val expectedPeriodStartDate: LocalDate? = null,
    val expectedPeriodEndDate: LocalDate? = null,
    val cycleLengthDays: Int? = null,
    val periodDurationDays: Int? = null,
    val cycleLengthVariabilityDays: Double? = null,
    val predictionUncertaintyDays: Int? = null,
    val recentIrregularity: Boolean = false,
    val basis: String? = null,
    val disclaimer: String? = null
)

data class CalendarPredictionSummary(
    val cyclePrediction: PeriodPrediction? = null,
    val ranges: List<CycleRange> = emptyList(),
    val cycles: List<CycleHistory> = emptyList()
)
