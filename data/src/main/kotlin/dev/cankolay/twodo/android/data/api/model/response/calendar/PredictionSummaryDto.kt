package dev.cankolay.twodo.android.data.api.model.response.calendar

import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.ConceptionRiskAssessment
import dev.cankolay.twodo.android.domain.model.api.calendar.ConceptionRiskLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.FertilityWindowEstimate
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodPrediction
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodPredictionReliability
import dev.cankolay.twodo.android.domain.model.api.calendar.PregnancyAssessment
import dev.cankolay.twodo.android.domain.model.api.calendar.PregnancyAssessmentStatus
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class PeriodPredictionDto(
    val expectedPeriodStartDate: String? = null,
    val expectedPeriodEndDate: String? = null,
    val averageCycleLengthDays: Int? = null,
    val averagePeriodLengthDays: Int? = null,
    val confidence: String? = null,
    val reliability: String? = null,
    val disclaimer: String? = null
)

@Serializable
data class FertilityWindowEstimateDto(
    val ovulationDate: String? = null,
    val fertileWindowStartDate: String? = null,
    val fertileWindowEndDate: String? = null,
    val uncertaintyDays: Int = 0,
    val reliability: String? = null,
    val hasEnoughData: Boolean = false,
    val explanation: String = ""
)

@Serializable
data class ConceptionRiskAssessmentDto(
    val level: String = "unknown",
    val confidence: String = "low",
    val relevantEvents: List<String> = emptyList(),
    val fertileWindowOverlap: Boolean = false,
    val explanation: String = ""
)

@Serializable
data class PregnancyAssessmentDto(
    val status: String = "unknown",
    val confidence: String = "low",
    val expectedPeriodDate: String? = null,
    val daysLate: Int = 0,
    val conceptionRisk: ConceptionRiskAssessmentDto? = null,
    val needsPregnancyTest: Boolean = false,
    val explanation: String = ""
)

@Serializable
data class CalendarPredictionSummaryDto(
    val cyclePrediction: PeriodPredictionDto? = null,
    val fertilityWindow: FertilityWindowEstimateDto? = null,
    val conceptionRisk: ConceptionRiskAssessmentDto? = null,
    val pregnancyAssessment: PregnancyAssessmentDto? = null
)

fun PeriodPredictionDto.toDomain() = PeriodPrediction(
    expectedPeriodStartDate = expectedPeriodStartDate?.let { parseLocalDateSafe(it) },
    expectedPeriodEndDate = expectedPeriodEndDate?.let { parseLocalDateSafe(it) },
    averageCycleLengthDays = averageCycleLengthDays,
    averagePeriodLengthDays = averagePeriodLengthDays,
    confidence = confidence,
    reliability = reliability?.let { PeriodPredictionReliability.fromValue(it) },
    disclaimer = disclaimer
)

fun FertilityWindowEstimateDto.toDomain() = FertilityWindowEstimate(
    ovulationDate = ovulationDate?.let { parseLocalDateSafe(it) },
    fertileWindowStartDate = fertileWindowStartDate?.let { parseLocalDateSafe(it) },
    fertileWindowEndDate = fertileWindowEndDate?.let { parseLocalDateSafe(it) },
    uncertaintyDays = uncertaintyDays,
    reliability = reliability?.let { PeriodPredictionReliability.fromValue(it) }
        ?: PeriodPredictionReliability.LOW,
    hasEnoughData = hasEnoughData,
    explanation = explanation
)

fun ConceptionRiskAssessmentDto.toDomain() = ConceptionRiskAssessment(
    level = ConceptionRiskLevel.fromValue(level),
    confidence = confidence,
    relevantEvents = relevantEvents.mapNotNull { parseLocalDateSafe(it) },
    fertileWindowOverlap = fertileWindowOverlap,
    explanation = explanation
)

fun PregnancyAssessmentDto.toDomain() = PregnancyAssessment(
    status = PregnancyAssessmentStatus.fromValue(status),
    confidence = confidence,
    expectedPeriodDate = expectedPeriodDate?.let { parseLocalDateSafe(it) },
    daysLate = daysLate,
    conceptionRisk = conceptionRisk?.toDomain() ?: ConceptionRiskAssessment(),
    needsPregnancyTest = needsPregnancyTest,
    explanation = explanation
)

fun CalendarPredictionSummaryDto.toDomain() = CalendarPredictionSummary(
    cyclePrediction = cyclePrediction?.toDomain(),
    fertilityWindow = fertilityWindow?.toDomain() ?: FertilityWindowEstimate(),
    conceptionRisk = conceptionRisk?.toDomain() ?: ConceptionRiskAssessment(),
    pregnancyAssessment = pregnancyAssessment?.toDomain() ?: PregnancyAssessment()
)

private fun parseLocalDateSafe(text: String): LocalDate? {
    return try {
        LocalDate.parse(text)
    } catch (_: Exception) {
        null
    }
}
