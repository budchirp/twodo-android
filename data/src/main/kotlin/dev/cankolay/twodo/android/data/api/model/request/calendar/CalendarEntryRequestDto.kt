package dev.cankolay.twodo.android.data.api.model.request.calendar

import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun CalendarEntryInput.toDto(creating: Boolean) = buildJsonObject {
    put("date", date.toString())

    if (creating || type != CalendarEntryType.EVENT) put("type", type.value)

    if (type == CalendarEntryType.EVENT) {
        put("title", requireNotNull(title).trim())
    } else {
        put("notes", notes.orEmpty())

        if (type == CalendarEntryType.PERIOD) {
            period?.let {
                put(
                    "period",
                    buildJsonObject {
                        put("event", it.event.value)
                        put("flowLevel", it.flowLevel.value)
                        put(
                            "symptoms",
                            JsonArray(it.symptoms.map { symptom -> JsonPrimitive(symptom.value) }),
                        )
                    },
                )
            }
        }
    }
}
