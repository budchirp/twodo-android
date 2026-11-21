package dev.cankolay.twodo.android.presentation.viewmodel.calendar

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntry
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryInput
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarEntryType
import dev.cankolay.twodo.android.domain.model.api.calendar.CalendarProjection
import dev.cankolay.twodo.android.domain.model.api.calendar.FlowLevel
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodEvent
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodInput
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSummary
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSymptom
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.calendar.CreateCalendarEntryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.DeleteCalendarEntryUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.GetCalendarUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.GetPeriodsUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.ObserveCalendarUseCase
import dev.cankolay.twodo.android.domain.usecase.api.calendar.UpdateCalendarEntryUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
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
    val isImportant: Boolean = false,
) {
    val editing: Boolean
        get() = entry != null
}

data class CalendarUiState(
    val entries: List<CalendarEntry>? = null,
    val projections: List<CalendarProjection> = emptyList(),
    val load: ApiResult<Unit>? = null,
    val prediction: PeriodSummary? = null,
    val predictionLoad: ApiResult<Unit>? = null,
    val result: ApiResult<Unit>? = null,
    val month: YearMonth = YearMonth.now(),
    val date: LocalDate = LocalDate.now(),
    val form: CalendarEntryFormState = CalendarEntryFormState(date = LocalDate.now()),
    val showPrediction: Boolean = false,
    val showForm: Boolean = false,
    val showDelete: Boolean = false,
    val canTrackPeriods: Boolean = false,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val observeEntries: ObserveCalendarUseCase,
    private val getEntries: GetCalendarUseCase,
    private val getPrediction: GetPeriodsUseCase,
    private val createEntry: CreateCalendarEntryUseCase,
    private val updateEntry: UpdateCalendarEntryUseCase,
    private val deleteEntry: DeleteCalendarEntryUseCase,
    private val observeSession: ObserveSessionUseCase,
) : BaseViewModel<CalendarUiState>(
    initialState = CalendarUiState()
) {
    private var loadJob: Job? = null
    private var predictionJob: Job? = null

    init {
        viewModelScope.launch {
            observeSession().collect { state ->
                _uiState.update {
                    it.copy(
                        canTrackPeriods =
                            (state as? SessionState.Active)?.user?.gender == Gender.FEMALE
                    )
                }
            }
        }

        viewModelScope.launch {
            observeEntries().collect { data ->
                _uiState.update {
                    when {
                        data == null -> it.copy(
                            entries = null,
                            projections = emptyList(),
                            load = if (it.entries != null) null else it.load,
                            prediction = null,
                            predictionLoad = if (it.entries != null) null else it.predictionLoad,
                        )

                        data.startDate == it.month.atDay(1) && data.endDate == it.month.atEndOfMonth() ->
                            it.copy(entries = data.entries, projections = data.projections)

                        else -> it
                    }
                }
            }
        }

        fetch()
    }

    fun openPrediction(forceRefresh: Boolean = false) {
        if (predictionJob?.isActive == true) return

        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showPrediction = true) }

        if (uiState.value.predictionLoad == ApiResult.Loading) return

        _uiState.update { it.copy(predictionLoad = ApiResult.Loading) }

        predictionJob = viewModelScope.launch {
            val result = getPrediction(date = LocalDate.now(), forceRefresh = forceRefresh)

            _uiState.update {
                it.copy(
                    prediction = if (result is ApiResult.Success) result.data else it.prediction,
                    predictionLoad = result.map { },
                )
            }
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
                        isImportant = entry.isImportant,
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
        val monthChanged = uiState.value.month != YearMonth.from(date)
        _uiState.update { it.copy(date = date, month = YearMonth.from(date)) }
        if (monthChanged) {
            loadJob?.cancel()
            fetch()
        }
    }

    fun move(months: Int) {
        _uiState.update {
            val month = it.month.plusMonths(months.toLong())

            it.copy(
                month = month,
                date = month.atDay(minOf(it.date.dayOfMonth, month.lengthOfMonth())),
            )
        }
        loadJob?.cancel()
        fetch()
    }

    fun fetch(forceRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return

        val month = uiState.value.month
        _uiState.update { it.copy(load = ApiResult.Loading) }

        loadJob = viewModelScope.launch {
            val result = getEntries(
                date = LocalDate.now(),
                startDate = month.atDay(1),
                endDate = month.atEndOfMonth(),
                forceRefresh = forceRefresh,
            )

            _uiState.update { it.copy(load = result.map { }) }
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
                isImportant = form.isImportant,
                period =
                    if (form.type == CalendarEntryType.PERIOD)
                        PeriodInput(
                            event = form.periodEvent,
                            flowLevel = form.flowLevel,
                            symptoms = form.symptoms.toList(),
                        )
                    else null,
            )

        submit {
            if (form.entry != null) updateEntry(id = form.entry.id, input = input)
            else createEntry(input = input)
        }
    }

    fun delete() {
        val entry = uiState.value.form.entry ?: return

        if (!entry.canManage(canTrackPeriods = uiState.value.canTrackPeriods)) return

        submit {
            deleteEntry(id = entry.id)
        }
    }

    private fun submit(block: suspend () -> ApiResult<*>) {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = block()

            _uiState.update {
                it.copy(
                    result = result.map { },
                    prediction = if (result is ApiResult.Success) null else it.prediction,
                    predictionLoad = if (result is ApiResult.Success) null else it.predictionLoad,
                )
            }

            if (result is ApiResult.Success) {
                loadJob?.cancel()
                fetch(forceRefresh = true)
            }
        }
    }
}

fun CalendarEntryType.canCreate(canTrackPeriods: Boolean): Boolean =
    when (this) {
        CalendarEntryType.NOTE -> true

        CalendarEntryType.PERIOD -> canTrackPeriods

        CalendarEntryType.OVULATION -> false
    }

fun CalendarEntry.canManage(canTrackPeriods: Boolean): Boolean =
    createdBy != null && type.canCreate(canTrackPeriods = canTrackPeriods)
