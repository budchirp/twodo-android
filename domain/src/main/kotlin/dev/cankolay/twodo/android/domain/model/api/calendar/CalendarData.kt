package dev.cankolay.twodo.android.domain.model.api.calendar

import java.time.LocalDate

data class CalendarData(
    val date: LocalDate,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val entries: List<CalendarItem>,
    val projections: List<CalendarProjection>,
    val prediction: PeriodPrediction,
)

enum class CalendarProjectionType(val value: String) {
    PERIOD("period"),
    OVULATION("ovulation");

    companion object {
        fun fromValue(value: String) = entries.first { it.value == value }
    }
}

data class CalendarProjection(
    val type: CalendarProjectionType,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val label: String,
    val peakDate: LocalDate?,
)
