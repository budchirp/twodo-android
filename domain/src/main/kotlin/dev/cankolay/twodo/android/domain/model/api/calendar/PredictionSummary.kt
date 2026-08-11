package dev.cankolay.twodo.android.domain.model.api.calendar

import java.time.LocalDate

enum class ConceptionRiskLevel(val value: String) {
    NONE("none"),
    LOW("low"),
    MODERATE("moderate"),
    HIGH("high"),
    UNKNOWN("unknown");

    companion object {
        fun fromValue(value: String): ConceptionRiskLevel =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class PregnancyAssessmentStatus(val value: String) {
    NOT_DUE("not_due"),
    PERIOD_DUE("period_due"),
    PERIOD_LATE("period_late"),
    POSSIBLE_PREGNANCY("possible_pregnancy"),
    UNKNOWN("unknown");

    companion object {
        fun fromValue(value: String): PregnancyAssessmentStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: UNKNOWN
    }
}

enum class PeriodPredictionReliability(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    companion object {
        fun fromValue(value: String): PeriodPredictionReliability =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: LOW
    }
}

data class PeriodPrediction(
    val expectedPeriodStartDate: LocalDate? = null,
    val expectedPeriodEndDate: LocalDate? = null,
    val averageCycleLengthDays: Int? = null,
    val averagePeriodLengthDays: Int? = null,
    val confidence: String? = null,
    val reliability: PeriodPredictionReliability? = null,
    val disclaimer: String? = null
)

data class FertilityWindowEstimate(
    val ovulationDate: LocalDate? = null,
    val fertileWindowStartDate: LocalDate? = null,
    val fertileWindowEndDate: LocalDate? = null,
    val uncertaintyDays: Int = 0,
    val reliability: PeriodPredictionReliability = PeriodPredictionReliability.LOW,
    val hasEnoughData: Boolean = false,
    val explanation: String = ""
)

data class ConceptionRiskAssessment(
    val level: ConceptionRiskLevel = ConceptionRiskLevel.UNKNOWN,
    val confidence: String = "low",
    val relevantEvents: List<LocalDate> = emptyList(),
    val fertileWindowOverlap: Boolean = false,
    val explanation: String = ""
)

data class PregnancyAssessment(
    val status: PregnancyAssessmentStatus = PregnancyAssessmentStatus.UNKNOWN,
    val confidence: String = "low",
    val expectedPeriodDate: LocalDate? = null,
    val daysLate: Int = 0,
    val conceptionRisk: ConceptionRiskAssessment = ConceptionRiskAssessment(),
    val needsPregnancyTest: Boolean = false,
    val explanation: String = ""
)

data class CalendarPredictionSummary(
    val cyclePrediction: PeriodPrediction? = null,
    val fertilityWindow: FertilityWindowEstimate = FertilityWindowEstimate(),
    val conceptionRisk: ConceptionRiskAssessment = ConceptionRiskAssessment(),
    val pregnancyAssessment: PregnancyAssessment = PregnancyAssessment()
)
