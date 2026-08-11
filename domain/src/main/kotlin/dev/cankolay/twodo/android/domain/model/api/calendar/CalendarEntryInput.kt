package dev.cankolay.twodo.android.domain.model.api.calendar

import java.time.LocalDate

typealias PeriodInput = PeriodDetails
typealias SexualActivityInput = SexualActivityDetails

data class CalendarEntryInput(
    val date: LocalDate,
    val type: CalendarEntryType,
    val notes: String?,
    val period: PeriodInput?,
    val sexualActivity: SexualActivityInput?
)
