package dev.cankolay.twodo.android.data.api.model.response.calendar

import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.CycleHistory
import dev.cankolay.twodo.android.domain.model.api.calendar.CycleRange
import dev.cankolay.twodo.android.domain.model.api.calendar.DateWindow
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodPrediction
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodPredictionReliability
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DateWindowDto(
    val startDate: String,
    val endDate: String
)

@Serializable
data class PeriodPredictionDto(
    val hasEnoughData: Boolean = false,
    val reliability: String? = null,
    val nextPeriodWindow: DateWindowDto? = null,
    val ovulationWindow: DateWindowDto? = null,
    val expectedPeriodStartDate: String? = null,
    val expectedPeriodEndDate: String? = null,
    val cycleLengthDays: Int? = null,
    val periodDurationDays: Int? = null,
    val cycleLengthVariabilityDays: Double? = null,
    val predictionUncertaintyDays: Int? = null,
    val recentIrregularity: Boolean = false,
    val basis: String? = null,
    val disclaimer: String? = null
)

@Serializable
data class CycleRangeDto(
    val startDate: String,
    val endDate: String,
    val durationDays: Int,
    val isComplete: Boolean = true,
    val flowLevels: List<String> = emptyList(),
    val symptoms: List<String> = emptyList()
)

@Serializable
data class CycleHistoryDto(
    val periodStartDate: String,
    val periodEndDate: String,
    val periodDurationDays: Int,
    val cycleLengthDays: Int
)

@Serializable
data class CalendarPredictionSummaryDto(
    val cyclePrediction: PeriodPredictionDto? = null,
    val ranges: List<CycleRangeDto> = emptyList(),
    val cycles: List<CycleHistoryDto> = emptyList()
)

fun DateWindowDto.toDomain(): DateWindow? {
    val start = parseLocalDateSafe(startDate) ?: return null
    val end = parseLocalDateSafe(endDate) ?: return null
    return DateWindow(startDate = start, endDate = end)
}

fun PeriodPredictionDto.toDomain() = PeriodPrediction(
    hasEnoughData = hasEnoughData,
    reliability = reliability?.let { PeriodPredictionReliability.fromValue(it) }
        ?: PeriodPredictionReliability.UNKNOWN,
    nextPeriodWindow = nextPeriodWindow?.toDomain(),
    ovulationWindow = ovulationWindow?.toDomain(),
    expectedPeriodStartDate = expectedPeriodStartDate?.let { parseLocalDateSafe(it) },
    expectedPeriodEndDate = expectedPeriodEndDate?.let { parseLocalDateSafe(it) },
    cycleLengthDays = cycleLengthDays,
    periodDurationDays = periodDurationDays,
    cycleLengthVariabilityDays = cycleLengthVariabilityDays,
    predictionUncertaintyDays = predictionUncertaintyDays,
    recentIrregularity = recentIrregularity,
    basis = basis,
    disclaimer = disclaimer
)

fun CycleRangeDto.toDomain(): CycleRange? {
    val start = parseLocalDateSafe(startDate) ?: return null
    val end = parseLocalDateSafe(endDate) ?: return null
    return CycleRange(
        startDate = start,
        endDate = end,
        durationDays = durationDays,
        isComplete = isComplete,
        flowLevels = flowLevels.map { FlowLevel.fromValue(it) },
        symptoms = symptoms.map { PeriodSymptom.fromValue(it) }
    )
}

fun CycleHistoryDto.toDomain(): CycleHistory? {
    val start = parseLocalDateSafe(periodStartDate) ?: return null
    val end = parseLocalDateSafe(periodEndDate) ?: return null
    return CycleHistory(
        periodStartDate = start,
        periodEndDate = end,
        periodDurationDays = periodDurationDays,
        cycleLengthDays = cycleLengthDays
    )
}

fun CalendarPredictionSummaryDto.toDomain() = CalendarPredictionSummary(
    cyclePrediction = cyclePrediction?.toDomain(),
    ranges = ranges.mapNotNull { it.toDomain() },
    cycles = cycles.mapNotNull { it.toDomain() }
)

private fun parseLocalDateSafe(text: String): LocalDate? {
    return try {
        LocalDate.parse(text)
    } catch (_: Exception) {
        null
    }
}
