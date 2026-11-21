package dev.cankolay.twodo.android.data.api.model.response.calendar

import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarData
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarProjection
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarProjectionType
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CalendarDataDto(
    val date: String,
    val entries: List<CalendarEntryDto>,
    val projections: List<CalendarProjectionDto>,
    val prediction: PeriodPredictionDto,
)

@Serializable
data class CalendarProjectionDto(
    val type: String,
    val startDate: String,
    val endDate: String,
    val label: String,
    val peakDate: String? = null,
)

fun CalendarDataDto.toDomain(startDate: LocalDate, endDate: LocalDate) = CalendarData(
    date = LocalDate.parse(date),
    startDate = startDate,
    endDate = endDate,
    entries = entries.map { it.toDomain() },
    projections = projections.map {
        CalendarProjection(
            type = CalendarProjectionType.fromValue(it.type),
            startDate = LocalDate.parse(it.startDate),
            endDate = LocalDate.parse(it.endDate),
            label = it.label,
            peakDate = it.peakDate?.let(LocalDate::parse),
        )
    },
    prediction = prediction.toDomain(),
)
