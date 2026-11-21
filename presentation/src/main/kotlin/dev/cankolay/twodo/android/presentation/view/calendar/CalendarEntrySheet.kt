package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.RadioOptions
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingRowSize
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.CalendarUiState
import dev.cankolay.twodo.android.presentation.viewmodel.calendar.canCreate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarEntrySheet(state: CalendarUiState, actions: CalendarActions) {

    val isLoading = state.result == ApiResult.Loading
    val fields = (state.result as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()

    AppBottomSheet(
        title =
            stringResource(
                id =
                    if (state.form.editing) R.string.edit_calendar_entry
                    else R.string.create_calendar_entry
            ),
        onDismiss = actions.onDismiss,
        result = state.result,
        onRefresh = actions.onSave,
        actions = {
            if (state.form.editing)
                TextButton(onClick = actions.onAskDelete, enabled = !isLoading) {
                    Text(text = stringResource(id = R.string.delete))
                }
            TextButton(onClick = actions.onDismiss, enabled = !isLoading) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(onClick = actions.onSave, enabled = !isLoading) {
                Text(text = stringResource(id = R.string.save))
            }
        },
    ) {
        if (!state.form.editing)
            item {
                RadioOptions(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items =
                        CalendarEntryType.entries.filter {
                            it.canCreate(canTrackPeriods = state.canTrackPeriods)
                        },
                    selected = state.form.type,
                    enabled = !isLoading,
                    label = { stringResource(id = it.label) },
                    onSelected = { actions.onFormChange(state.form.copy(type = it)) },
                )
            }
        item {
            val event = state.form.type == CalendarEntryType.EVENT
            val error =
                when {
                    event && state.form.submitted && state.form.title.isBlank() ->
                        stringResource(id = R.string.title_required)

                    event && state.form.titleLength > 1000 ->
                        stringResource(id = R.string.calendar_title_limit)

                    !event && state.form.notesLength > 1000 ->
                        stringResource(id = R.string.calendar_notes_limit)

                    else ->
                        fields
                            .firstOrNull { it.field == if (event) "title" else "notes" }
                            ?.messages
                            ?.joinToString(separator = "\n")
                }

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                value = if (event) state.form.title else state.form.notes,
                enabled = !isLoading,
                onValueChange = {
                    actions.onFormChange(
                        if (event) state.form.copy(title = it) else state.form.copy(notes = it)
                    )
                },
                label = {
                    Text(
                        text =
                            stringResource(id = if (event) R.string.title else R.string.notes_field)
                    )
                },
                minLines = if (event) 1 else 3,
                isError = error != null,
                supportingText = if (error != null) ({ Text(text = error) }) else null,
            )
        }
        if (state.form.type == CalendarEntryType.PERIOD) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(id = R.string.period_event),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    RadioOptions(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items = PeriodEvent.entries,
                        selected = state.form.periodEvent,
                        enabled = !isLoading,
                        label = { stringResource(id = it.label) },
                        onSelected = { actions.onFormChange(state.form.copy(periodEvent = it)) },
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(id = R.string.flow_level),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    RadioOptions(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        items = FlowLevel.entries,
                        selected = state.form.flowLevel,
                        enabled = !isLoading,
                        label = { stringResource(id = it.label) },
                        onSelected = { actions.onFormChange(state.form.copy(flowLevel = it)) },
                    )
                }
            }
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(id = R.string.symptoms),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            item {
                SettingsGroup(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items =
                        PeriodSymptom.entries.map { symptom ->
                            val checked = symptom in state.form.symptoms
                            SettingItem(
                                title = stringResource(id = symptom.label),
                                rowSize = SettingRowSize.Compact,
                                modifier =
                                    Modifier.toggleable(
                                        value = checked,
                                        enabled = !isLoading,
                                        role = Role.Checkbox,
                                    ) { selected ->
                                        actions.onFormChange(
                                            state.form.copy(
                                                symptoms =
                                                    if (selected) state.form.symptoms + symptom
                                                    else state.form.symptoms - symptom
                                            )
                                        )
                                    },
                                leadingContent = {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = null,
                                        enabled = !isLoading,
                                    )
                                },
                            )
                        },
                )
            }
        }
    }
}

@get:StringRes
internal val CalendarEntryType.label: Int
    get() =
        when (this) {
            CalendarEntryType.NOTE -> R.string.calendar_type_note

            CalendarEntryType.PERIOD -> R.string.calendar_type_period

            CalendarEntryType.EVENT -> R.string.calendar_type_event
        }

@get:StringRes
internal val PeriodEvent.label: Int
    get() =
        when (this) {
            PeriodEvent.START -> R.string.period_event_start

            PeriodEvent.DAY -> R.string.period_event_day

            PeriodEvent.END -> R.string.period_event_end
        }

@get:StringRes
internal val FlowLevel.label: Int
    get() =
        when (this) {
            FlowLevel.SPOTTING -> R.string.flow_spotting

            FlowLevel.LIGHT -> R.string.flow_light

            FlowLevel.MEDIUM -> R.string.flow_medium

            FlowLevel.HEAVY -> R.string.flow_heavy
        }

@get:StringRes
internal val PeriodSymptom.label: Int
    get() =
        when (this) {
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
