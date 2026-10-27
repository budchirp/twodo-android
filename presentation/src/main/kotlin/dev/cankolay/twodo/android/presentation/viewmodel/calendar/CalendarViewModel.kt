package dev.cankolay.twodo.android.presentation.viewmodel.calendar

import androidx.lifecycle.viewModelScope
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
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarEntryFormState(
    val entry: CalendarEntry? = null,
    val date: LocalDate,
    val type: CalendarEntryType = CalendarEntryType.NOTE,
    val notes: String = "",
    val periodEvent: PeriodEvent = PeriodEvent.DAY,
    val flowLevel: FlowLevel = FlowLevel.MEDIUM,
    val symptoms: Set<PeriodSymptom> = emptySet(),
) {
    val editing: Boolean
        get() = entry != null
}

data class CalendarUiState(
    val entries: ApiResult<List<CalendarEntry>>? = null,
    val prediction: ApiResult<CalendarPredictionSummary>? = null,
    val result: ApiResult<*>? = null,
    val month: YearMonth = YearMonth.now(),
    val date: LocalDate = LocalDate.now(),
    val form: CalendarEntryFormState = CalendarEntryFormState(date = LocalDate.now()),
    val showPrediction: Boolean = false,
    val showForm: Boolean = false,
    val showDelete: Boolean = false,
    val canTrackPeriods: Boolean = false,
)

@HiltViewModel
class CalendarViewModel
@Inject
constructor(
    private val calendar: CalendarRepository,
    session: SessionRepository,
) : BaseViewModel<CalendarUiState>(initialState = CalendarUiState()) {
    init {
        viewModelScope.launch {
            session.state.collect { state ->
                _uiState.update {
                    it.copy(
                        canTrackPeriods =
                            (state as? SessionState.Active)?.user?.gender == Gender.FEMALE
                    )
                }
            }
        }

        viewModelScope.launch {
            calendar.entries.collect { entries ->
                if (uiState.value.entries != ApiResult.Loading) {
                    _uiState.update {
                        it.copy(entries = entries?.let { ApiResult.Success(data = it) })
                    }
                }
            }
        }

        fetch()
    }

    fun openPrediction(forceRefresh: Boolean = false) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showPrediction = true) }

        if (uiState.value.prediction == ApiResult.Loading) return

        _uiState.update { it.copy(prediction = ApiResult.Loading) }

        viewModelScope.launch {
            val result = calendar.getPredictionSummary(forceRefresh = forceRefresh)

            _uiState.update { it.copy(prediction = result) }
        }
    }

    fun openForm() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(
                form = CalendarEntryFormState(date = it.date),
                showForm = true,
                result = null,
            )
        }
    }

    fun openForm(entry: CalendarEntry) {
        if (
            uiState.value.result == ApiResult.Loading ||
            !entry.canManage(canTrackPeriods = uiState.value.canTrackPeriods)
        )
            return

        _uiState.update {
            it.copy(
                date = entry.date,
                result = null,
                showForm = true,
                form =
                    CalendarEntryFormState(
                        entry = entry,
                        date = entry.date,
                        type = entry.type,
                        notes = entry.notes.orEmpty(),
                        periodEvent = entry.period?.event ?: PeriodEvent.DAY,
                        flowLevel = entry.period?.flowLevel ?: FlowLevel.MEDIUM,
                        symptoms = entry.period?.symptoms.orEmpty().toSet(),
                    ),
            )
        }
    }

    fun updateForm(form: CalendarEntryFormState) {
        if (uiState.value.result == ApiResult.Loading || !uiState.value.showForm) return

        _uiState.update { it.copy(form = form) }
    }

    fun dismiss() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update {
            it.copy(showForm = false, showDelete = false, showPrediction = false, result = null)
        }
    }

    fun askDelete() {
        if (uiState.value.result == ApiResult.Loading || uiState.value.form.entry == null) return

        _uiState.update {
            it.copy(
                showForm = false,
                showDelete = true,
                result = null,
            )
        }
    }

    fun select(date: LocalDate) {
        _uiState.update { it.copy(date = date, month = YearMonth.from(date)) }
    }

    fun move(months: Int) {
        _uiState.update {
            val month = it.month.plusMonths(months.toLong())

            it.copy(
                month = month,
                date = month.atDay(minOf(it.date.dayOfMonth, month.lengthOfMonth())),
            )
        }
    }

    fun fetch(forceRefresh: Boolean = false) {
        if (uiState.value.entries == ApiResult.Loading) return

        _uiState.update { it.copy(entries = ApiResult.Loading) }

        viewModelScope.launch {
            val result = calendar.getAll(forceRefresh = forceRefresh)

            _uiState.update { it.copy(entries = result, prediction = null) }
        }
    }

    fun save() {
        if (!uiState.value.showForm) return

        val form = uiState.value.form

        if (!form.type.canCreate(canTrackPeriods = uiState.value.canTrackPeriods)) return

        val input =
            CalendarEntryInput(
                date = form.date,
                type = form.type,
                notes = form.notes.ifBlank { null },
                period =
                    if (form.type == CalendarEntryType.PERIOD)
                        PeriodInput(
                            event = form.periodEvent,
                            flowLevel = form.flowLevel,
                            symptoms = form.symptoms.toList(),
                        )
                    else null,
            )

        submit(
            refreshEntries =
                form.type == CalendarEntryType.PERIOD ||
                        form.entry?.type == CalendarEntryType.PERIOD
        ) {
            if (form.entry != null) calendar.update(id = form.entry.id, input = input)
            else calendar.create(input = input)
        }
    }

    fun delete() {
        val entry = uiState.value.form.entry ?: return

        if (!entry.canManage(canTrackPeriods = uiState.value.canTrackPeriods)) return

        submit(refreshEntries = entry.type == CalendarEntryType.PERIOD) {
            calendar.delete(id = entry.id)
        }
    }

    private fun submit(refreshEntries: Boolean, block: suspend () -> ApiResult<*>) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = block()

            _uiState.update { it.copy(result = result, prediction = null) }

            if (result is ApiResult.Success && refreshEntries) fetch(forceRefresh = true)
        }
    }
}

fun CalendarEntryType.canCreate(canTrackPeriods: Boolean): Boolean =
    when (this) {
        CalendarEntryType.NOTE -> true

        CalendarEntryType.PERIOD -> canTrackPeriods

        CalendarEntryType.OVULATION,
        CalendarEntryType.PERIOD_PREDICTION -> false
    }

fun CalendarEntry.canManage(canTrackPeriods: Boolean): Boolean =
    createdBy != null && type.canCreate(canTrackPeriods = canTrackPeriods)
