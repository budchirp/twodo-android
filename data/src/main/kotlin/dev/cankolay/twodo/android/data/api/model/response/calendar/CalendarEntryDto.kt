package dev.cankolay.twodo.android.data.api.model.response.calendar

import dev.cankolay.twodo.android.data.api.model.response.task.TaskDto
import dev.cankolay.twodo.android.data.api.model.response.task.toDomain
import dev.cankolay.twodo.android.data.api.model.response.user.UserSummaryDto
import dev.cankolay.twodo.android.data.api.model.response.user.toDomain
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarItem
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarTask
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodDetails
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate

@Serializable(with = CalendarItemSerializer::class)
sealed interface CalendarItemDto

object CalendarItemSerializer :
    JsonContentPolymorphicSerializer<CalendarItemDto>(CalendarItemDto::class) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<CalendarItemDto> =
        when (element.jsonObject["type"]?.jsonPrimitive?.content) {
            "task" -> CalendarTaskDto.serializer()
            "note",
            "period",
            "event" -> CalendarEntryDto.serializer()

            else -> error("Unknown calendar item type")
        }
}

@Serializable
data class CalendarTaskDto(val type: String, val date: String, val task: TaskDto) : CalendarItemDto

@Serializable
data class CalendarEntryDto(
    val id: String,
    val date: String,
    val type: String,
    val notes: String? = null,
    val createdBy: UserSummaryDto? = null,
    val period: PeriodDto? = null,
    val createdAt: String,
    val updatedAt: String,
    val title: String? = null,
) : CalendarItemDto

fun CalendarItemDto.toDomain(): CalendarItem =
    when (this) {
        is CalendarEntryDto -> toDomain()
        is CalendarTaskDto -> CalendarTask(date = LocalDate.parse(date), task = task.toDomain())
    }

@Serializable
data class PeriodDto(
    val event: String,
    val flowLevel: String? = null,
    val symptoms: List<String> = emptyList(),
)

fun CalendarEntryDto.toDomain() =
    CalendarEntry(
        id = id,
        date = LocalDate.parse(date),
        type = CalendarEntryType.fromValue(value = type),
        notes = notes,
        createdBy = createdBy?.toDomain(),
        period = period?.toDomain(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        title = title,
    )

fun PeriodDto.toDomain() =
    PeriodDetails(
        event = PeriodEvent.fromValue(value = event),
        flowLevel = flowLevel?.let { FlowLevel.fromValue(value = it) } ?: FlowLevel.MEDIUM,
        symptoms = symptoms.map { PeriodSymptom.fromValue(value = it) },
    )
