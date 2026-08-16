package dev.cankolay.twodo.android.data.api.model.request.calendar

import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEntryRequestDto(
    val date: String,
    val type: String,
    val notes: String? = null,
    val period: PeriodRequestDto? = null
)

@Serializable
data class PeriodRequestDto(
    val event: String,
    val flowLevel: String,
    val symptoms: List<String>,
    val endDate: String? = null
)

fun CalendarEntryInput.toDto() = CalendarEntryRequestDto(
    date = date.toString(),
    type = type.value,
    notes = notes,
    period = period?.let { period ->
        PeriodRequestDto(
            event = period.event.value,
            flowLevel = period.flowLevel.value,
            symptoms = period.symptoms.map { it.value }
        )
    }
)
