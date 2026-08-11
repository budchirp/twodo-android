package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.EjaculationLocation
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodInput
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.domain.model.api.calendar.ProtectionMethod
import dev.cankolay.twodo.android.domain.model.api.calendar.SexualActivityInput
import dev.cankolay.twodo.android.domain.usecase.api.calendar.CreateCalendarEntryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.DeleteCalendarEntryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.GetCalendarEntriesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.GetCalendarPredictionSummaryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.UpdateCalendarEntryUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.parseLocalDate
import dev.cankolay.twodo.android.presentation.form.update
import dev.cankolay.twodo.android.presentation.state.UiStatus
import dev.cankolay.twodo.android.presentation.state.errorMessage
import dev.cankolay.twodo.android.presentation.state.isLoading
import dev.cankolay.twodo.android.presentation.state.onError
import dev.cankolay.twodo.android.presentation.state.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarEntryFormState(
    val entry: CalendarEntry? = null,
    val date: FormField<String>,
    val type: CalendarEntryType = CalendarEntryType.NOTE,
    val notes: String = "",
    val periodEvent: PeriodEvent = PeriodEvent.DAY,
    val flowLevel: FlowLevel = FlowLevel.MEDIUM,
    val symptoms: Set<PeriodSymptom> = emptySet(),
    val sexOccurred: Boolean = true,
    val protectionMethod: ProtectionMethod = ProtectionMethod.NONE,
    val ejaculationLocation: EjaculationLocation = EjaculationLocation.NONE
) {
    val isEditing = entry != null

    companion object {
        fun create(date: LocalDate) = CalendarEntryFormState(
            date = FormField(value = date.toString())
        )

        fun edit(entry: CalendarEntry) = CalendarEntryFormState(
            entry = entry,
            date = FormField(value = entry.date.toString()),
            type = entry.type,
            notes = entry.notes.orEmpty(),
            periodEvent = entry.period?.event ?: PeriodEvent.DAY,
            flowLevel = entry.period?.flowLevel ?: FlowLevel.MEDIUM,
            symptoms = entry.period?.symptoms.orEmpty().toSet(),
            sexOccurred = entry.sexualActivity?.sexOccurred ?: true,
            protectionMethod = entry.sexualActivity?.protectionMethod ?: ProtectionMethod.NONE,
            ejaculationLocation = entry.sexualActivity?.ejaculationLocation
                ?: EjaculationLocation.NONE
        )
    }
}

sealed interface CalendarSheet {
    data object None : CalendarSheet
    data object PredictionSummary : CalendarSheet
    data class EntryForm(val form: CalendarEntryFormState) : CalendarSheet
    data class DeleteConfirmation(val entry: CalendarEntry) : CalendarSheet
}

data class CalendarUiState(
    val entries: List<CalendarEntry>? = null,
    val predictionSummary: CalendarPredictionSummary? = null,
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val activeSheet: CalendarSheet = CalendarSheet.None,
    val status: UiStatus = UiStatus.Idle
) {
    val selectedEntries: List<CalendarEntry>
        get() = entries.orEmpty().filter { it.date == selectedDate }
    val isLoading: Boolean get() = status.isLoading
    val error: String? get() = status.errorMessage
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val createCalendarEntryUseCase: CreateCalendarEntryUseCase,
    private val getCalendarEntriesUseCase: GetCalendarEntriesUseCase,
    private val updateCalendarEntryUseCase: UpdateCalendarEntryUseCase,
    private val deleteCalendarEntryUseCase: DeleteCalendarEntryUseCase,
    private val getCalendarPredictionSummaryUseCase: GetCalendarPredictionSummaryUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    fun openPredictionSheet() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.PredictionSummary) }
        fetchPredictionSummary()
    }

    fun dismissPredictionSheet() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.None) }
    }

    fun openCreateEntryForm(date: LocalDate = _uiState.value.selectedDate) {
        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                activeSheet = CalendarSheet.EntryForm(CalendarEntryFormState.create(date = date))
            )
        }
    }

    fun openEditEntryForm(entry: CalendarEntry) {
        _uiState.update { state ->
            state.copy(
                selectedDate = entry.date,
                activeSheet = CalendarSheet.EntryForm(CalendarEntryFormState.edit(entry = entry))
            )
        }
    }

    fun dismissEntryForm() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.None) }
    }

    fun updateEntryType(type: CalendarEntryType) {
        updateForm { copy(type = type) }
    }

    fun updateEntryDate(date: String) {
        updateForm { copy(date = this.date.update(value = date)) }
    }

    fun updateEntryNotes(notes: String) {
        updateForm { copy(notes = notes) }
    }

    fun updatePeriodEvent(event: PeriodEvent) {
        updateForm { copy(periodEvent = event) }
    }

    fun updateFlowLevel(flowLevel: FlowLevel) {
        updateForm { copy(flowLevel = flowLevel) }
    }

    fun updateSymptoms(symptoms: Set<PeriodSymptom>) {
        updateForm { copy(symptoms = symptoms) }
    }

    fun toggleEntryFormSymptom(symptom: PeriodSymptom) {
        updateForm {
            val updated = if (symptom in symptoms) symptoms - symptom else symptoms + symptom
            copy(symptoms = updated)
        }
    }

    fun updateSexOccurred(sexOccurred: Boolean) {
        updateForm { copy(sexOccurred = sexOccurred) }
    }

    fun updateProtectionMethod(protectionMethod: ProtectionMethod) {
        updateForm { copy(protectionMethod = protectionMethod) }
    }

    fun updateEjaculationLocation(ejaculationLocation: EjaculationLocation) {
        updateForm { copy(ejaculationLocation = ejaculationLocation) }
    }

    fun requestDeleteEntry() {
        val form = (_uiState.value.activeSheet as? CalendarSheet.EntryForm)?.form ?: return
        val entry = form.entry ?: return
        _uiState.update { it.copy(activeSheet = CalendarSheet.DeleteConfirmation(entry = entry)) }
    }

    fun dismissDeleteEntry() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.None) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                selectedDate = date,
                visibleMonth = YearMonth.from(date)
            )
        }
    }

    fun moveMonth(months: Int) {
        _uiState.update { state ->
            val newMonth =
                if (months > 0) state.visibleMonth.plusMonths(months.toLong()) else state.visibleMonth.minusMonths(
                    -months.toLong()
                )
            val updatedDate =
                state.selectedDate.withMonth(newMonth.monthValue).withYear(newMonth.year)
            state.copy(visibleMonth = newMonth, selectedDate = updatedDate)
        }
    }

    fun fetchCalendar() {
        viewModelScope.launch {
            if (_uiState.value.entries == null) {
                _uiState.update { it.copy(status = UiStatus.Loading) }
            }

            getCalendarEntriesUseCase()
                .onSuccess { entries ->
                    _uiState.update { state ->
                        state.copy(
                            entries = entries,
                            status = UiStatus.Idle
                        )
                    }
                }
                .onError { msg, code ->
                    _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                }

            fetchPredictionSummarySilently()
        }
    }

    fun fetchPredictionSummary() {
        viewModelScope.launch {
            getCalendarPredictionSummaryUseCase()
                .onSuccess { summary ->
                    _uiState.update { it.copy(predictionSummary = summary) }
                }
                .onError { msg, code ->
                    _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                }
        }
    }

    private fun fetchPredictionSummarySilently() {
        viewModelScope.launch {
            getCalendarPredictionSummaryUseCase()
                .onSuccess { summary ->
                    _uiState.update { it.copy(predictionSummary = summary) }
                }
        }
    }

    fun submitEntryForm(isFemale: Boolean) {
        val form = (_uiState.value.activeSheet as? CalendarSheet.EntryForm)?.form ?: return
        val (field, parsedDate) = form.date.parseLocalDate(error = R.string.invalid_date)
        if (field.error != null || parsedDate == null) {
            _uiState.update { state ->
                state.copy(activeSheet = CalendarSheet.EntryForm(form.copy(date = field)))
            }
            return
        }

        val input = buildInput(form = form, date = parsedDate, isFemale = isFemale)
        dismissEntryForm()

        viewModelScope.launch {
            val result = if (form.isEditing && form.entry != null) {
                updateCalendarEntryUseCase(id = form.entry.id, input = input)
            } else {
                createCalendarEntryUseCase(input = input)
            }

            result.onSuccess {
                fetchCalendar()
            }.onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                fetchCalendar()
            }
        }
    }

    fun deleteSelectedEntry(isFemale: Boolean) {
        val entry =
            (_uiState.value.activeSheet as? CalendarSheet.DeleteConfirmation)?.entry ?: return
        dismissDeleteEntry()

        _uiState.update { state ->
            state.copy(entries = state.entries?.filterNot { it.id == entry.id })
        }

        viewModelScope.launch {
            deleteCalendarEntryUseCase(id = entry.id)
                .onSuccess {
                    fetchCalendar()
                }
                .onError { msg, code ->
                    _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                    fetchCalendar()
                }
        }
    }

    private inline fun updateForm(crossinline block: CalendarEntryFormState.() -> CalendarEntryFormState) {
        _uiState.update { state ->
            val form = (state.activeSheet as? CalendarSheet.EntryForm)?.form ?: return@update state
            state.copy(activeSheet = CalendarSheet.EntryForm(form.block()))
        }
    }

    private fun buildInput(
        form: CalendarEntryFormState,
        date: LocalDate,
        isFemale: Boolean
    ): CalendarEntryInput {
        val periodInput = if (isFemale && form.type == CalendarEntryType.PERIOD) {
            PeriodInput(
                event = form.periodEvent,
                flowLevel = form.flowLevel,
                symptoms = form.symptoms.toList()
            )
        } else null

        val sexInput = if (form.type == CalendarEntryType.SEXUAL_ACTIVITY) {
            SexualActivityInput(
                sexOccurred = form.sexOccurred,
                protectionMethod = form.protectionMethod,
                ejaculationLocation = form.ejaculationLocation
            )
        } else null

        return CalendarEntryInput(
            date = date,
            type = form.type,
            notes = form.notes.ifBlank { null },
            period = periodInput,
            sexualActivity = sexInput
        )
    }
}
