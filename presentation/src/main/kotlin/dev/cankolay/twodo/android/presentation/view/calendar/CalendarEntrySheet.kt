package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.EjaculationLocation
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.domain.model.api.calendar.ProtectionMethod
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardStackList
import dev.cankolay.twodo.android.presentation.composable.app.CardStackListItem
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.viewmodel.CalendarEntryFormState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarEntrySheet(
    form: CalendarEntryFormState,
    isFemale: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: suspend () -> Boolean,
    onDateChange: (String) -> Unit,
    onTypeChange: (CalendarEntryType) -> Unit,
    onNotesChange: (String) -> Unit,
    onPeriodEventChange: (PeriodEvent) -> Unit,
    onFlowLevelChange: (FlowLevel) -> Unit,
    onSymptomsChange: (Set<PeriodSymptom>) -> Unit,
    onSexOccurredChange: (Boolean) -> Unit,
    onProtectionMethodChange: (ProtectionMethod) -> Unit,
    onEjaculationLocationChange: (EjaculationLocation) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    AppBottomSheet(
        title = stringResource(
            id = if (form.isEditing) R.string.edit_calendar_entry else R.string.create_calendar_entry
        ),
        onDismiss = onDismiss,
        sheetState = sheetState,
        actions = {
            if (form.isEditing && onDelete != null) {
                TextButton(
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            onDelete()
                        }
                    }
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            }

            TextButton(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }

            Button(
                enabled = !isSaving,
                onClick = {
                    scope.launch {
                        if (onSave()) {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                }
            ) {
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
            CalendarEntryType.NOTE, CalendarEntryType.OVULATION, CalendarEntryType.PERIOD_PREDICTION -> Unit
            CalendarEntryType.PERIOD -> periodFields(
                periodEvent = form.periodEvent,
                onPeriodEventChange = onPeriodEventChange,
                flowLevel = form.flowLevel,
                onFlowLevelChange = onFlowLevelChange,
                symptoms = form.symptoms,
                onSymptomsChange = onSymptomsChange
            )

            CalendarEntryType.SEXUAL_ACTIVITY -> sexualActivityFields(
                sexOccurred = form.sexOccurred,
                onSexOccurredChange = onSexOccurredChange,
                protectionMethod = form.protectionMethod,
                onProtectionMethodChange = onProtectionMethodChange,
                ejaculationLocation = form.ejaculationLocation,
                onEjaculationLocationChange = onEjaculationLocationChange
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

    CardStackList(
        items = types.map { type ->
            val onClick = { onSelected(type) }
            CardStackListItem(
                title = type.label(),
                onClick = onClick,
                leadingContent = {
                    RadioButton(
                        selected = selected == type,
                        onClick = onClick
                    )
                }
            )
        }
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
            label = { it.label() },
            onSelected = onPeriodEventChange
        )
    }

    item {
        EnumRadioList(
            title = stringResource(id = R.string.flow_level),
            values = FlowLevel.entries,
            selected = flowLevel,
            label = { it.label() },
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
                    title = symptom.label(),
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

private fun androidx.compose.foundation.lazy.LazyListScope.sexualActivityFields(
    sexOccurred: Boolean,
    onSexOccurredChange: (Boolean) -> Unit,
    protectionMethod: ProtectionMethod,
    onProtectionMethodChange: (ProtectionMethod) -> Unit,
    ejaculationLocation: EjaculationLocation,
    onEjaculationLocationChange: (EjaculationLocation) -> Unit
) {
    item {
        CardStackList(
            items = listOf(
                CardStackListItem(
                    title = stringResource(id = R.string.sex_occurred),
                    onClick = { onSexOccurredChange(!sexOccurred) },
                    trailingContent = {
                        Switch(
                            checked = sexOccurred,
                            onCheckedChange = onSexOccurredChange
                        )
                    }
                )
            )
        )
    }

    item {
        EnumRadioList(
            title = stringResource(id = R.string.protection_method),
            values = ProtectionMethod.entries,
            selected = protectionMethod,
            label = { it.label() },
            onSelected = onProtectionMethodChange
        )
    }

    item {
        EnumRadioList(
            title = stringResource(id = R.string.ejaculation_location),
            values = EjaculationLocation.entries,
            selected = ejaculationLocation,
            label = { it.label() },
            onSelected = onEjaculationLocationChange
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

        CardStackList(
            items = values.map { value ->
                val onClick = { onSelected(value) }
                CardStackListItem(
                    title = label(value),
                    onClick = onClick,
                    leadingContent = {
                        RadioButton(
                            selected = selected == value,
                            onClick = onClick
                        )
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeleteCalendarEntrySheet(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onDelete: suspend () -> Boolean
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    AppBottomSheet(
        title = stringResource(id = R.string.delete_calendar_entry),
        description = stringResource(id = R.string.delete_calendar_entry_desc),
        onDismiss = onDismiss,
        sheetState = sheetState,
        actions = {
            TextButton(
                onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        onDismiss()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }

            Button(
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = {
                    scope.launch {
                        if (onDelete()) {
                            sheetState.hide()
                            onDismiss()
                        }
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.delete))
            }
        }
    )
}
