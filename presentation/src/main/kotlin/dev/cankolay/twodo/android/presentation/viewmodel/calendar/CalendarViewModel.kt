package dev.cankolay.twodo.android.presentation.viewmodel.calendar

import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarPredictionSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodInput
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.domain.model.api.onError
import dev.cankolay.twodo.android.domain.model.api.onSuccess
import dev.cankolay.twodo.android.domain.usecase.api.calendar.CreateCalendarEntryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.DeleteCalendarEntryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.GetCalendarEntriesUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.GetCalendarPredictionSummaryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.UpdateCalendarEntryUseCase
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import dev.cankolay.twodo.android.presentation.core.UiEvent
import dev.cankolay.twodo.android.presentation.form.FormField
import dev.cankolay.twodo.android.presentation.form.parseLocalDate
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
    val symptoms: Set<PeriodSymptom> = emptySet()
) {
    val isEditing = entry != null

    companion object {
        fun create(date: LocalDate) = CalendarEntryFormState(date = FormField(date.toString()))

        fun edit(entry: CalendarEntry) = CalendarEntryFormState(
            entry = entry,
            date = FormField(value = entry.date.toString()),
            type = entry.type,
            notes = entry.notes.orEmpty(),
            periodEvent = entry.period?.event ?: PeriodEvent.DAY,
            flowLevel = entry.period?.flowLevel ?: FlowLevel.MEDIUM,
            symptoms = entry.period?.symptoms.orEmpty().toSet()
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
    val entriesResult: ApiResult<List<CalendarEntry>> = ApiResult.Loading,
    val cachedEntries: List<CalendarEntry>? = null,
    val predictionSummary: CalendarPredictionSummary? = null,
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val activeSheet: CalendarSheet = CalendarSheet.None
) {
    val entries: List<CalendarEntry>? get() = cachedEntries ?: entriesResult.dataOrNull
    val selectedEntries: List<CalendarEntry>
        get() = entries.orEmpty().filter { it.date == selectedDate }
    val isLoading: Boolean get() = entriesResult.isLoading
    val error: String? get() = entriesResult.errorMessage
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val createCalendarEntryUseCase: CreateCalendarEntryUseCase,
    private val getCalendarEntriesUseCase: GetCalendarEntriesUseCase,
    private val updateCalendarEntryUseCase: UpdateCalendarEntryUseCase,
    private val deleteCalendarEntryUseCase: DeleteCalendarEntryUseCase,
    private val getCalendarPredictionSummaryUseCase: GetCalendarPredictionSummaryUseCase
) : BaseViewModel<CalendarUiState>(CalendarUiState()) {

    init {
        fetchEntries()
    }

    fun openPredictionSheet() {
        updateState { copy(activeSheet = CalendarSheet.PredictionSummary) }
        loadPredictionSummary(showError = true)
    }

    fun openCreateEntrySheet(date: LocalDate = uiState.value.selectedDate) {
        updateState {
            copy(
                selectedDate = date,
                activeSheet = CalendarSheet.EntryForm(CalendarEntryFormState.create(date))
            )
        }
    }

    fun openEditEntrySheet(entry: CalendarEntry) {
        updateState {
            copy(
                selectedDate = entry.date,
                activeSheet = CalendarSheet.EntryForm(CalendarEntryFormState.edit(entry))
            )
        }
    }

    fun dismissSheet() {
        updateState { copy(activeSheet = CalendarSheet.None) }
    }

    fun updateEntryType(type: CalendarEntryType) = updateForm { copy(type = type) }

    fun updateEntryNotes(notes: String) = updateForm { copy(notes = notes) }

    fun updatePeriodEvent(event: PeriodEvent) = updateForm { copy(periodEvent = event) }

    fun updateFlowLevel(flowLevel: FlowLevel) = updateForm { copy(flowLevel = flowLevel) }

    fun updateSymptoms(symptoms: Set<PeriodSymptom>) = updateForm { copy(symptoms = symptoms) }

    fun requestDeleteEntry() {
        val form = (uiState.value.activeSheet as? CalendarSheet.EntryForm)?.form ?: return
        val entry = form.entry ?: return
        updateState { copy(activeSheet = CalendarSheet.DeleteConfirmation(entry)) }
    }

    fun selectDate(date: LocalDate) {
        updateState { copy(selectedDate = date, visibleMonth = YearMonth.from(date)) }
    }

    fun moveMonth(months: Int) {
        updateState {
            val newMonth = visibleMonth.plusMonths(months.toLong())
            val newDay = minOf(selectedDate.dayOfMonth, newMonth.lengthOfMonth())
            copy(
                visibleMonth = newMonth,
                selectedDate = newMonth.atDay(newDay)
            )
        }
    }

    fun fetchEntries() {
        launchOnce("entries") {
            updateState { copy(entriesResult = ApiResult.Loading) }
            val result = getCalendarEntriesUseCase()
            updateState {
                copy(
                    entriesResult = result,
                    cachedEntries = result.dataOrNull ?: cachedEntries
                )
            }
            loadPredictionSummary(showError = false)
        }
    }

    fun submitEntry(isFemale: Boolean) {
        val form = (uiState.value.activeSheet as? CalendarSheet.EntryForm)?.form ?: return
        val (dateField, date) = form.date.parseLocalDate(error = R.string.invalid_date)
        if (dateField.error != null || date == null) {
            updateState { copy(activeSheet = CalendarSheet.EntryForm(form.copy(date = dateField))) }
            return
        }

        val input = buildInput(form, date, isFemale)
        dismissSheet()
        launchOnce("save-entry") {
            val result = if (form.isEditing && form.entry != null) {
                updateCalendarEntryUseCase(form.entry.id, input)
            } else {
                createCalendarEntryUseCase(input)
            }
            result.onSuccess { fetchEntries() }
                .onError { message, _ ->
                    sendEvent(UiEvent.ShowSnackbar(message))
                    fetchEntries()
                }
        }
    }

    fun confirmDeleteEntry() {
        val entry =
            (uiState.value.activeSheet as? CalendarSheet.DeleteConfirmation)?.entry ?: return
        dismissSheet()
        updateState { copy(cachedEntries = entries?.filterNot { it.id == entry.id }) }

        launchOnce("delete-entry:${entry.id}") {
            deleteCalendarEntryUseCase(entry.id)
                .onSuccess { fetchEntries() }
                .onError { message, _ ->
                    sendEvent(UiEvent.ShowSnackbar(message))
                    fetchEntries()
                }
        }
    }

    private fun loadPredictionSummary(showError: Boolean) {
        launchLatest("prediction") {
            getCalendarPredictionSummaryUseCase()
                .onSuccess { summary -> updateState { copy(predictionSummary = summary) } }
                .onError { message, _ ->
                    if (showError) sendEvent(UiEvent.ShowSnackbar(message))
                }
        }
    }

    private inline fun updateForm(crossinline transform: CalendarEntryFormState.() -> CalendarEntryFormState) {
        updateState {
            val form = activeSheet as? CalendarSheet.EntryForm ?: return@updateState this
            copy(activeSheet = CalendarSheet.EntryForm(form.form.run(transform)))
        }
    }

    private fun buildInput(
        form: CalendarEntryFormState,
        date: LocalDate,
        isFemale: Boolean
    ): CalendarEntryInput {
        val periodInput = if (isFemale && form.type == CalendarEntryType.PERIOD) {
            PeriodInput(form.periodEvent, form.flowLevel, form.symptoms.toList())
        } else {
            null
        }

        return CalendarEntryInput(
            date = date,
            type = form.type,
            notes = form.notes.ifBlank { null },
            period = periodInput
        )
    }
}
