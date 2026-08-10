package dev.cankolay.twodo.android.data.api.model.response.calendar

import dev.cankolay.twodo.android.data.api.model.response.user.UserSummaryDto
import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.EjaculationLocation
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodDetails
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.domain.model.api.calendar.ProtectionMethod
import dev.cankolay.twodo.android.domain.model.api.calendar.SexualActivityDetails
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CalendarEntryDto(
    val id: String,
    val date: String,
    val type: String,
    val notes: String? = null,
    val createdBy: UserSummaryDto? = null,
    val period: PeriodDto? = null,
    val sexualActivity: SexualActivityDto? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class PeriodDto(
    val event: String,
    val flowLevel: String,
    val symptoms: List<String> = emptyList()
)

@Serializable
data class SexualActivityDto(
    val sexOccurred: Boolean,
    val protectionMethod: String,
    val ejaculationLocation: String
)

fun CalendarEntryDto.toDomain() = CalendarEntry(
    id = id,
    date = LocalDate.parse(date),
    type = CalendarEntryType.fromValue(value = type),
    notes = notes,
    createdBy = createdBy?.toDomain(),
    period = period?.toDomain(),
    sexualActivity = sexualActivity?.toDomain(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PeriodDto.toDomain() = PeriodDetails(
    event = PeriodEvent.fromValue(value = event),
    flowLevel = FlowLevel.fromValue(value = flowLevel),
    symptoms = symptoms.map { PeriodSymptom.fromValue(value = it) }
)

fun SexualActivityDto.toDomain() = SexualActivityDetails(
    sexOccurred = sexOccurred,
    protectionMethod = ProtectionMethod.fromValue(value = protectionMethod),
    ejaculationLocation = EjaculationLocation.fromValue(value = ejaculationLocation)
)
