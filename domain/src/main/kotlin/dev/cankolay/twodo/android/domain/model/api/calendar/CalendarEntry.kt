package dev.cankolay.twodo.android.domain.model.api.calendar

import dev.cankolay.twodo.android.domain.model.api.user.User
import java.time.LocalDate

enum class CalendarEntryType(val value: String) {
    NOTE(value = "note"),
    PERIOD(value = "period"),
    PERIOD_PREDICTION(value = "period_prediction"),
    SEXUAL_ACTIVITY(value = "sexual_activity"),
    OVULATION(value = "ovulation");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value }
                ?: error("Unknown CalendarEntryType: $value")
    }
}

enum class PeriodEvent(val value: String) {
    START(value = "start"),
    DAY(value = "day"),
    END(value = "end");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value }
                ?: error("Unknown PeriodEvent: $value")
    }
}

enum class FlowLevel(val value: String) {
    SPOTTING(value = "spotting"),
    LIGHT(value = "light"),
    MEDIUM(value = "medium"),
    HEAVY(value = "heavy");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value }
                ?: error("Unknown FlowLevel: $value")
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
            entries.firstOrNull { it.value == value }
                ?: error("Unknown PeriodSymptom: $value")
    }
}

enum class ProtectionMethod(val value: String) {
    NONE(value = "none"),
    CONDOM(value = "condom"),
    DIAPHRAGM(value = "diaphragm"),
    EMERGENCY_CONTRACEPTION(value = "emergency_contraception"),
    FERTILITY_AWARENESS(value = "fertility_awareness"),
    HORMONAL(value = "hormonal"),
    IMPLANT(value = "implant"),
    INJECTION(value = "injection"),
    IUD(value = "iud"),
    PATCH(value = "patch"),
    PILL(value = "pill"),
    RING(value = "ring"),
    WITHDRAWAL(value = "withdrawal");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value }
                ?: error("Unknown ProtectionMethod: $value")
    }
}

enum class EjaculationLocation(val value: String) {
    NONE(value = "none"),
    CONDOM(value = "condom"),
    INSIDE_VAGINA(value = "inside_vagina"),
    OUTSIDE_VAGINA(value = "outside_vagina"),
    ANAL(value = "anal"),
    ORAL(value = "oral"),
    OTHER_BODY(value = "other_body");

    companion object {
        fun fromValue(value: String) =
            entries.firstOrNull { it.value == value }
                ?: error("Unknown EjaculationLocation: $value")
    }
}

data class PeriodDetails(
    val event: PeriodEvent,
    val flowLevel: FlowLevel,
    val symptoms: List<PeriodSymptom>
)

data class SexualActivityDetails(
    val sexOccurred: Boolean,
    val protectionMethod: ProtectionMethod,
    val ejaculationLocation: EjaculationLocation
)

data class CalendarEntry(
    val id: String,
    val date: LocalDate,
    val type: CalendarEntryType,
    val notes: String?,
    val createdBy: User? = null,
    val period: PeriodDetails?,
    val sexualActivity: SexualActivityDetails?,
    val createdAt: String,
    val updatedAt: String
)
