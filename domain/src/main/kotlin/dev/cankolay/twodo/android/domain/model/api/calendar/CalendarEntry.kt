package dev.cankolay.twodo.android.domain.model.api.calendar

import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.domain.model.api.user.User
import java.time.LocalDate

enum class CalendarEntryType(val value: String) {
    NOTE(value = "note"),
    PERIOD(value = "period"),
    EVENT(value = "event");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value } ?: error("Unknown CalendarEntryType: $value")
    }
}

enum class PeriodEvent(val value: String) {
    START(value = "start"),
    DAY(value = "day"),
    END(value = "end");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value } ?: error("Unknown PeriodEvent: $value")
    }
}

enum class FlowLevel(val value: String) {
    SPOTTING(value = "spotting"),
    LIGHT(value = "light"),
    MEDIUM(value = "medium"),
    HEAVY(value = "heavy");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value } ?: error("Unknown FlowLevel: $value")
    }
}

enum class PeriodSymptom(val value: String) {
    ACNE(value = "acne"),
    BACK_PAIN(value = "back_pain"),
    BLOATING(value = "bloating"),
    BREAST_TENDERNESS(value = "breast_tenderness"),
    CRAMPS(value = "cramps"),
    FATIGUE(value = "fatigue"),
    HEADACHE(value = "headache"),
    MOOD_CHANGES(value = "mood_changes"),
    NAUSEA(value = "nausea");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value } ?: error("Unknown PeriodSymptom: $value")
    }
}

data class PeriodDetails(
    val event: PeriodEvent,
    val flowLevel: FlowLevel,
    val symptoms: List<PeriodSymptom>,
)

sealed interface CalendarItem {
    val date: LocalDate
    val key: String
}

data class CalendarTask(override val date: LocalDate, val task: Task) : CalendarItem {
    override val key: String
        get() = "task:${task.id}"
}

data class CalendarEntry(
    val id: String,
    override val date: LocalDate,
    val type: CalendarEntryType,
    val notes: String?,
    val createdBy: User? = null,
    val period: PeriodDetails?,
    val createdAt: String,
    val updatedAt: String,
    val title: String? = null,
) : CalendarItem {
    override val key: String
        get() = "calendar:$id"
}
