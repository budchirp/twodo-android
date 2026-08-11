package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.EjaculationLocation
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodDetails
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.domain.model.api.calendar.ProtectionMethod
import dev.cankolay.twodo.android.domain.model.api.calendar.SexualActivityDetails
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
import dev.cankolay.twodo.android.presentation.state.errorCode
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
    val errorCode: String? get() = status.errorCode
    val entryForm: CalendarEntryFormState? get() = (activeSheet as? CalendarSheet.EntryForm)?.form
    val deletingEntry: CalendarEntry? get() = (activeSheet as? CalendarSheet.DeleteConfirmation)?.entry
    val isPredictionSheetVisible: Boolean get() = activeSheet is CalendarSheet.PredictionSummary
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

    fun fetchCalendar() {
        viewModelScope.launch {
            refreshCalendar()
        }
    }

    fun openPredictionSheet() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.PredictionSummary) }
    }

    fun dismissPredictionSheet() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.None) }
    }

    fun moveMonth(months: Long) {
        _uiState.update {
            val visibleMonth = it.visibleMonth.plusMonths(months)
            it.copy(
                visibleMonth = visibleMonth,
                selectedDate = visibleMonth.atDay(1)
            )
        }
        fetchCalendar()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun openCreateEntryForm() {
        _uiState.update {
            it.copy(activeSheet = CalendarSheet.EntryForm(CalendarEntryFormState.create(date = it.selectedDate)))
        }
    }

    fun openEditEntryForm(entry: CalendarEntry) {
        _uiState.update {
            it.copy(activeSheet = CalendarSheet.EntryForm(CalendarEntryFormState.edit(entry = entry)))
        }
    }

    fun dismissEntryForm() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.None) }
    }

    fun requestDeleteEntry() {
        val form = (_uiState.value.activeSheet as? CalendarSheet.EntryForm)?.form ?: return
        val entry = form.entry ?: return
        _uiState.update { it.copy(activeSheet = CalendarSheet.DeleteConfirmation(entry = entry)) }
    }

    fun dismissDeleteEntry() {
        _uiState.update { it.copy(activeSheet = CalendarSheet.None) }
    }

    fun updateEntryDate(date: String) {
        updateEntryForm { it.copy(date = it.date.update(value = date.trim())) }
    }

    fun updateEntryType(type: CalendarEntryType) {
        updateEntryForm { it.copy(type = type) }
    }

    fun updateEntryNotes(notes: String) {
        updateEntryForm { it.copy(notes = notes) }
    }

    fun updatePeriodEvent(event: PeriodEvent) {
        updateEntryForm { it.copy(periodEvent = event) }
    }

    fun updateFlowLevel(flowLevel: FlowLevel) {
        updateEntryForm { it.copy(flowLevel = flowLevel) }
    }

    fun updateSymptoms(symptoms: Set<PeriodSymptom>) {
        updateEntryForm { it.copy(symptoms = symptoms) }
    }

    fun updateSexOccurred(sexOccurred: Boolean) {
        updateEntryForm { it.copy(sexOccurred = sexOccurred) }
    }

    fun updateProtectionMethod(protectionMethod: ProtectionMethod) {
        updateEntryForm { it.copy(protectionMethod = protectionMethod) }
    }

    fun updateEjaculationLocation(ejaculationLocation: EjaculationLocation) {
        updateEntryForm { it.copy(ejaculationLocation = ejaculationLocation) }
    }

    fun submitEntryForm(isFemale: Boolean) {
        val form = (_uiState.value.activeSheet as? CalendarSheet.EntryForm)?.form ?: return
        val input = buildEntryInput(form = form) ?: return
        dismissEntryForm()

        viewModelScope.launch {
            if (form.entry != null) {
                updateEntry(id = form.entry.id, input = input, isFemale = isFemale)
            } else {
                createEntry(input = input, isFemale = isFemale)
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
                    refreshPredictionSummary()
                }
                .onError { msg, code ->
                    _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
                    refreshCalendar()
                }
        }
    }

    suspend fun createEntry(
        input: CalendarEntryInput,
        isFemale: Boolean
    ): ApiResult<CalendarEntry> {
        validateInput(input = input, isFemale = isFemale)?.let { return it }

        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = createCalendarEntryUseCase(input = input)
            .onSuccess { newEntry ->
                val entryMonth = YearMonth.from(newEntry.date)
                if (entryMonth == _uiState.value.visibleMonth) {
                    _uiState.update { state ->
                        state.copy(
                            selectedDate = newEntry.date,
                            entries = (state.entries.orEmpty() + newEntry).sortedBy { it.date },
                            status = UiStatus.Idle
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            visibleMonth = entryMonth,
                            selectedDate = newEntry.date,
                            status = UiStatus.Idle
                        )
                    }
                    refreshCalendar()
                }
                refreshPredictionSummary()
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        return result
    }

    suspend fun updateEntry(
        id: String,
        input: CalendarEntryInput,
        isFemale: Boolean
    ): ApiResult<CalendarEntry> {
        validateInput(input = input, isFemale = isFemale)?.let { return it }

        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = updateCalendarEntryUseCase(id = id, input = input)
            .onSuccess { updatedEntry ->
                val entryMonth = YearMonth.from(updatedEntry.date)
                if (entryMonth == _uiState.value.visibleMonth) {
                    _uiState.update { state ->
                        state.copy(
                            selectedDate = updatedEntry.date,
                            entries = state.entries.orEmpty().map { entry ->
                                if (entry.id == id) updatedEntry else entry
                            }.sortedBy { it.date },
                            status = UiStatus.Idle
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            visibleMonth = entryMonth,
                            selectedDate = updatedEntry.date,
                            status = UiStatus.Idle
                        )
                    }
                    refreshCalendar()
                }
                refreshPredictionSummary()
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        return result
    }

    suspend fun deleteEntry(entry: CalendarEntry, isFemale: Boolean): ApiResult<Nothing?> {
        if (entry.type == CalendarEntryType.PERIOD && !isFemale) {
            val error = validationError(message = "Only female users can manage period entries.")
            _uiState.update { it.copy(status = UiStatus.Error(error.message, error.code)) }
            return error
        }

        _uiState.update { it.copy(status = UiStatus.Loading) }

        val result = deleteCalendarEntryUseCase(id = entry.id)
            .onSuccess {
                _uiState.update { state ->
                    state.copy(
                        entries = state.entries.orEmpty().filterNot { it.id == entry.id },
                        status = UiStatus.Idle
                    )
                }
                refreshPredictionSummary()
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        return result
    }

    private suspend fun refreshCalendar() {
        val state = _uiState.value
        if (state.entries == null) {
            _uiState.update { it.copy(status = UiStatus.Loading) }
        }

        getCalendarEntriesUseCase(
            startDate = state.visibleMonth.atDay(1),
            endDate = state.visibleMonth.atEndOfMonth()
        )
            .onSuccess { list ->
                _uiState.update {
                    it.copy(
                        entries = list.sortedBy { entry -> entry.date },
                        status = UiStatus.Idle
                    )
                }
            }
            .onError { msg, code ->
                _uiState.update { it.copy(status = UiStatus.Error(msg, code)) }
            }

        refreshPredictionSummary()
    }

    private suspend fun refreshPredictionSummary() {
        getCalendarPredictionSummaryUseCase()
            .onSuccess { summary ->
                _uiState.update { it.copy(predictionSummary = summary) }
            }
    }

    private fun updateEntryForm(update: (CalendarEntryFormState) -> CalendarEntryFormState) {
        _uiState.update { state ->
            val form = (state.activeSheet as? CalendarSheet.EntryForm)?.form ?: return@update state
            state.copy(activeSheet = CalendarSheet.EntryForm(form = update(form)))
        }
    }

    private fun buildEntryInput(form: CalendarEntryFormState): CalendarEntryInput? {
        val (dateField, date) = form.date.parseLocalDate(error = R.string.invalid_date)
        if (date == null) {
            _uiState.update { it.copy(activeSheet = CalendarSheet.EntryForm(form = form.copy(date = dateField))) }
            return null
        }

        _uiState.update { it.copy(activeSheet = CalendarSheet.EntryForm(form = form.copy(date = dateField))) }

        return CalendarEntryInput(
            date = date,
            type = form.type,
            notes = form.notes.trim().ifBlank { null },
            period = if (form.type == CalendarEntryType.PERIOD) {
                PeriodDetails(
                    event = form.periodEvent,
                    flowLevel = form.flowLevel,
                    symptoms = form.symptoms.toList().sortedBy { it.ordinal }
                )
            } else null,
            sexualActivity = if (form.type == CalendarEntryType.SEXUAL_ACTIVITY) {
                SexualActivityDetails(
                    sexOccurred = form.sexOccurred,
                    protectionMethod = form.protectionMethod,
                    ejaculationLocation = form.ejaculationLocation
                )
            } else null
        )
    }

    private fun validateInput(input: CalendarEntryInput, isFemale: Boolean): ApiResult.Error? {
        return when (input.type) {
            CalendarEntryType.NOTE -> when {
                input.period != null || input.sexualActivity != null ->
                    validationError(message = "Notes cannot include period or sexual activity details.")

                else -> null
            }

            CalendarEntryType.PERIOD -> when {
                !isFemale -> validationError(message = "Only female users can manage period entries.")
                input.period == null -> validationError(message = "Period entries require period details.")
                input.sexualActivity != null -> validationError(message = "Period entries cannot include sexual activity details.")
                else -> null
            }

            CalendarEntryType.SEXUAL_ACTIVITY -> when {
                input.sexualActivity == null -> validationError(message = "Sexual activity entries require sexual activity details.")
                input.period != null -> validationError(message = "Sexual activity entries cannot include period details.")
                else -> null
            }

            CalendarEntryType.OVULATION, CalendarEntryType.PERIOD_PREDICTION -> null
        }?.also { error ->
            _uiState.update { it.copy(status = UiStatus.Error(error.message, error.code)) }
        }
    }

    private fun validationError(message: String) = ApiResult.Error(
        message = message,
        reason = ErrorReason.CLIENT,
        code = "validation_error"
    )
}
