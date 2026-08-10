package dev.cankolay.twodo.android.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
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
import dev.cankolay.twodo.android.domain.usecase.api.calendar.UpdateCalendarEntryUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.parseLocalDate
import dev.cankolay.twodo.android.presentation.form.update
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

data class CalendarUiState(
    val entries: List<CalendarEntry>? = null,
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val entryForm: CalendarEntryFormState? = null,
    val deletingEntry: CalendarEntry? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val errorCode: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val createCalendarEntryUseCase: CreateCalendarEntryUseCase,
    private val getCalendarEntriesUseCase: GetCalendarEntriesUseCase,
    private val updateCalendarEntryUseCase: UpdateCalendarEntryUseCase,
    private val deleteCalendarEntryUseCase: DeleteCalendarEntryUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchCalendar() {
        viewModelScope.launch {
            refreshCalendar()
        }
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
            it.copy(
                entryForm = CalendarEntryFormState.create(date = it.selectedDate),
                deletingEntry = null
            )
        }
    }

    fun openEditEntryForm(entry: CalendarEntry) {
        _uiState.update {
            it.copy(
                entryForm = CalendarEntryFormState.edit(entry = entry),
                deletingEntry = null
            )
        }
    }

    fun dismissEntryForm() {
        _uiState.update { it.copy(entryForm = null) }
    }

    fun requestDeleteEntry() {
        val entry = _uiState.value.entryForm?.entry ?: return
        _uiState.update { it.copy(entryForm = null, deletingEntry = entry) }
    }

    fun dismissDeleteEntry() {
        _uiState.update { it.copy(deletingEntry = null) }
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

    suspend fun submitEntryForm(isFemale: Boolean): Boolean {
        val form = _uiState.value.entryForm ?: return false
        val input = buildEntryInput(form = form) ?: return false
        val result = form.entry?.let { entry ->
            updateEntry(id = entry.id, input = input, isFemale = isFemale)
        } ?: createEntry(input = input, isFemale = isFemale)

        return result is ApiResult.Success
    }

    suspend fun deleteSelectedEntry(isFemale: Boolean): Boolean {
        val entry = _uiState.value.deletingEntry ?: return false
        return deleteEntry(entry = entry, isFemale = isFemale) is ApiResult.Success
    }

    suspend fun createEntry(
        input: CalendarEntryInput,
        isFemale: Boolean
    ): ApiResult<CalendarEntry> {
        validateInput(input = input, isFemale = isFemale)?.let { return it }

        _uiState.update { it.copy(isSaving = true, error = null, errorCode = null) }

        val result = createCalendarEntryUseCase(input = input)
        when (result) {
            is ApiResult.Error -> _uiState.update {
                it.copy(error = result.message, errorCode = result.code)
            }

            is ApiResult.Fatal -> _uiState.update {
                it.copy(error = result.exception.messageOrDefault(), errorCode = null)
            }

            is ApiResult.Success -> {
                val entryMonth = YearMonth.from(result.data.date)
                if (entryMonth == _uiState.value.visibleMonth) {
                    _uiState.update { state ->
                        state.copy(
                            selectedDate = result.data.date,
                            entries = (state.entries.orEmpty() + result.data).sortedBy { it.date },
                            error = null,
                            errorCode = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            visibleMonth = entryMonth,
                            selectedDate = result.data.date,
                            error = null,
                            errorCode = null
                        )
                    }
                    refreshCalendar()
                }
            }

            else -> Unit
        }

        _uiState.update { it.copy(isSaving = false) }
        return result
    }

    suspend fun updateEntry(
        id: String,
        input: CalendarEntryInput,
        isFemale: Boolean
    ): ApiResult<CalendarEntry> {
        validateInput(input = input, isFemale = isFemale)?.let { return it }

        _uiState.update { it.copy(isSaving = true, error = null, errorCode = null) }

        val result = updateCalendarEntryUseCase(id = id, input = input)
        when (result) {
            is ApiResult.Error -> _uiState.update {
                it.copy(error = result.message, errorCode = result.code)
            }

            is ApiResult.Fatal -> _uiState.update {
                it.copy(error = result.exception.messageOrDefault(), errorCode = null)
            }

            is ApiResult.Success -> {
                val entryMonth = YearMonth.from(result.data.date)
                if (entryMonth == _uiState.value.visibleMonth) {
                    _uiState.update { state ->
                        state.copy(
                            selectedDate = result.data.date,
                            entries = state.entries.orEmpty().map { entry ->
                                if (entry.id == id) result.data else entry
                            }.sortedBy { it.date },
                            error = null,
                            errorCode = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            visibleMonth = entryMonth,
                            selectedDate = result.data.date,
                            error = null,
                            errorCode = null
                        )
                    }
                    refreshCalendar()
                }
            }

            else -> Unit
        }

        _uiState.update { it.copy(isSaving = false) }
        return result
    }

    suspend fun deleteEntry(entry: CalendarEntry, isFemale: Boolean): ApiResult<Nothing?> {
        if (entry.type == CalendarEntryType.PERIOD && !isFemale) {
            val error = validationError(message = "Only female users can manage period entries.")
            _uiState.update { it.copy(error = error.message, errorCode = error.code) }
            return error
        }

        _uiState.update { it.copy(isSaving = true, error = null, errorCode = null) }

        val result = deleteCalendarEntryUseCase(id = entry.id)
        when (result) {
            is ApiResult.Error -> _uiState.update {
                it.copy(error = result.message, errorCode = result.code)
            }

            is ApiResult.Fatal -> _uiState.update {
                it.copy(error = result.exception.messageOrDefault(), errorCode = null)
            }

            is ApiResult.Success -> {
                _uiState.update { state ->
                    state.copy(
                        entries = state.entries.orEmpty().filterNot { it.id == entry.id },
                        error = null,
                        errorCode = null
                    )
                }
            }

            else -> Unit
        }

        _uiState.update { it.copy(isSaving = false) }
        return result
    }

    private suspend fun refreshCalendar() {
        val state = _uiState.value
        if (state.entries == null) {
            _uiState.update { it.copy(isLoading = true, error = null, errorCode = null) }
        }

        when (val result = getCalendarEntriesUseCase(
            startDate = state.visibleMonth.atDay(1),
            endDate = state.visibleMonth.atEndOfMonth()
        )) {
            is ApiResult.Error -> _uiState.update {
                it.copy(error = result.message, errorCode = result.code)
            }

            is ApiResult.Fatal -> _uiState.update {
                it.copy(error = result.exception.messageOrDefault(), errorCode = null)
            }

            is ApiResult.Success -> _uiState.update {
                it.copy(
                    entries = result.data.sortedBy { entry -> entry.date },
                    error = null,
                    errorCode = null
                )
            }

            else -> Unit
        }

        _uiState.update { it.copy(isLoading = false) }
    }

    private fun updateEntryForm(update: (CalendarEntryFormState) -> CalendarEntryFormState) {
        _uiState.update { state ->
            state.entryForm?.let { form -> state.copy(entryForm = update(form)) } ?: state
        }
    }

    private fun buildEntryInput(form: CalendarEntryFormState): CalendarEntryInput? {
        val (dateField, date) = form.date.parseLocalDate(error = R.string.invalid_date)
        if (date == null) {
            _uiState.update { it.copy(entryForm = form.copy(date = dateField)) }
            return null
        }

        _uiState.update { it.copy(entryForm = form.copy(date = dateField)) }

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
            _uiState.update { it.copy(error = error.message, errorCode = error.code) }
        }
    }

    private fun validationError(message: String) = ApiResult.Error(
        message = message,
        reason = ErrorReason.CLIENT,
        code = "validation_error"
    )
}

private fun Throwable.messageOrDefault() =
    localizedMessage ?: message ?: "Unexpected error"
