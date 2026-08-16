package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.presentation.R

@Composable
internal fun CalendarEntryType.label() = stringResource(
    id = when (this) {
        CalendarEntryType.NOTE -> R.string.calendar_type_note
        CalendarEntryType.PERIOD -> R.string.calendar_type_period
        CalendarEntryType.PERIOD_PREDICTION -> R.string.expected_period
        CalendarEntryType.OVULATION -> R.string.calendar_type_ovulation
    }
)

@Composable
internal fun PeriodEvent.label() = stringResource(
    id = when (this) {
        PeriodEvent.START -> R.string.period_event_start
        PeriodEvent.DAY -> R.string.period_event_day
        PeriodEvent.END -> R.string.period_event_end
    }
)

@Composable
internal fun FlowLevel.label() = stringResource(
    id = when (this) {
        FlowLevel.SPOTTING -> R.string.flow_spotting
        FlowLevel.LIGHT -> R.string.flow_light
        FlowLevel.MEDIUM -> R.string.flow_medium
        FlowLevel.HEAVY -> R.string.flow_heavy
    }
)

@Composable
internal fun PeriodSymptom.label() = stringResource(
    id = when (this) {
        PeriodSymptom.ACNE -> R.string.symptom_acne
        PeriodSymptom.BACK_PAIN -> R.string.symptom_back_pain
        PeriodSymptom.BLOATING -> R.string.symptom_bloating
        PeriodSymptom.BREAST_TENDERNESS -> R.string.symptom_breast_tenderness
        PeriodSymptom.CRAMPS -> R.string.symptom_cramps
        PeriodSymptom.FATIGUE -> R.string.symptom_fatigue
        PeriodSymptom.HEADACHE -> R.string.symptom_headache
        PeriodSymptom.MOOD_CHANGES -> R.string.symptom_mood_changes
        PeriodSymptom.NAUSEA -> R.string.symptom_nausea
    }
)
