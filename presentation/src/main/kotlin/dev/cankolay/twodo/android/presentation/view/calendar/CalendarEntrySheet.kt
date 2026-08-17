package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardRadioList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarEntryFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarEntrySheet(
    form: CalendarEntryFormState,
    isFemale: Boolean,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: () -> Unit,
    onTypeChange: (CalendarEntryType) -> Unit,
    onNotesChange: (String) -> Unit,
    onPeriodEventChange: (PeriodEvent) -> Unit,
    onFlowLevelChange: (FlowLevel) -> Unit,
    onSymptomsChange: (Set<PeriodSymptom>) -> Unit
) {
    AppBottomSheet(
        title = stringResource(
            id = if (form.isEditing) R.string.edit_calendar_entry else R.string.create_calendar_entry
        ),
        onDismiss = onDismiss,
        actions = {
            if (form.isEditing && onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text(text = stringResource(id = R.string.delete))
                }
            }

            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }

            Button(onClick = onSave) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    ) {
        item {
            EntryTypeSelector(
                selected = form.type,
                isFemale = isFemale,
                onSelected = onTypeChange
            )
        }

        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.notes,
                onValueChange = onNotesChange,
                label = { Text(text = stringResource(id = R.string.notes_field)) },
                minLines = 3
            )
        }

        when (form.type) {
            CalendarEntryType.NOTE,
            CalendarEntryType.OVULATION,
            CalendarEntryType.PERIOD_PREDICTION -> Unit

            CalendarEntryType.PERIOD -> periodFields(
                periodEvent = form.periodEvent,
                onPeriodEventChange = onPeriodEventChange,
                flowLevel = form.flowLevel,
                onFlowLevelChange = onFlowLevelChange,
                symptoms = form.symptoms,
                onSymptomsChange = onSymptomsChange
            )
        }
    }
}

@Composable
private fun EntryTypeSelector(
    selected: CalendarEntryType,
    isFemale: Boolean,
    onSelected: (CalendarEntryType) -> Unit
) {
    val types = CalendarEntryType.entries.filter { type ->
        type != CalendarEntryType.OVULATION &&
                type != CalendarEntryType.PERIOD_PREDICTION &&
                (type != CalendarEntryType.PERIOD || isFemale)
    }

    CardRadioList(
        items = types,
        selected = selected,
        label = { type ->
            stringResource(
                id = when (type) {
                    CalendarEntryType.NOTE -> R.string.calendar_type_note
                    CalendarEntryType.PERIOD -> R.string.calendar_type_period
                    CalendarEntryType.PERIOD_PREDICTION -> R.string.expected_period
                    CalendarEntryType.OVULATION -> R.string.calendar_type_ovulation
                }
            )
        },
        onSelected = onSelected
    )
}

private fun androidx.compose.foundation.lazy.LazyListScope.periodFields(
    periodEvent: PeriodEvent,
    onPeriodEventChange: (PeriodEvent) -> Unit,
    flowLevel: FlowLevel,
    onFlowLevelChange: (FlowLevel) -> Unit,
    symptoms: Set<PeriodSymptom>,
    onSymptomsChange: (Set<PeriodSymptom>) -> Unit
) {
    item {
        EnumRadioList(
            title = stringResource(id = R.string.period_event),
            values = PeriodEvent.entries,
            selected = periodEvent,
            label = { event ->
                stringResource(
                    id = when (event) {
                        PeriodEvent.START -> R.string.period_event_start
                        PeriodEvent.DAY -> R.string.period_event_day
                        PeriodEvent.END -> R.string.period_event_end
                    }
                )
            },
            onSelected = onPeriodEventChange
        )
    }

    item {
        EnumRadioList(
            title = stringResource(id = R.string.flow_level),
            values = FlowLevel.entries,
            selected = flowLevel,
            label = { flowLevel ->
                stringResource(
                    id = when (flowLevel) {
                        FlowLevel.SPOTTING -> R.string.flow_spotting
                        FlowLevel.LIGHT -> R.string.flow_light
                        FlowLevel.MEDIUM -> R.string.flow_medium
                        FlowLevel.HEAVY -> R.string.flow_heavy
                    }
                )
            },
            onSelected = onFlowLevelChange
        )
    }

    item {
        Text(
            text = stringResource(id = R.string.symptoms),
            style = MaterialTheme.typography.titleMedium
        )
    }

    item {
        CardStackList(
            items = PeriodSymptom.entries.map { symptom ->
                val checked = symptom in symptoms
                val onClick = {
                    onSymptomsChange(
                        if (checked) symptoms - symptom else symptoms + symptom
                    )
                }
                CardStackListItem(
                    title = stringResource(
                        id = when (symptom) {
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
                    ),
                    onClick = onClick,
                    leadingContent = {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onClick() }
                        )
                    }
                )
            }
        )
    }
}

@Composable
private fun <T> EnumRadioList(
    title: String,
    values: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        CardRadioList(
            items = values,
            selected = selected,
            label = label,
            onSelected = onSelected
        )
    }
}
