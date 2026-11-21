package dev.cankolay.twodo.android.presentation.viewmodel.home

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.overview.Overview
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.overview.GetOverviewUseCase
import dev.cankolay.twodo.android.domain.usecase.api.overview.ObserveOverviewRevisionUseCase
import dev.cankolay.twodo.android.domain.usecase.api.overview.ObserveOverviewUseCase
import dev.cankolay.twodo.android.domain.usecase.api.task.UpdateTaskUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class OverviewUiState(
    val overview: Overview? = null,
    val load: ApiResult<Unit>? = null,
    val toggle: ApiResult<Unit>? = null,
)

private data class HomeRequest(val date: LocalDate, val serial: Long, val active: Boolean)

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val getOverview: GetOverviewUseCase,
    observeOverview: ObserveOverviewUseCase,
    observeRevision: ObserveOverviewRevisionUseCase,
    observeSession: ObserveSessionUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val refreshSession: RefreshSessionUseCase,
) : BaseViewModel<OverviewUiState>(
    initialState = OverviewUiState(
        overview = observeOverview().value?.takeIf { it.date == LocalDate.now() },
    )
) {
    private val request = MutableStateFlow<HomeRequest?>(value = null)
    private var toggleJob: Job? = null
    private var toggleInput: Pair<String, Boolean>? = null
    private var ownerKey: Triple<String, String?, List<String>>? = null

    init {
        viewModelScope.launch {
            observeOverview().collect { data ->
                val date = request.value?.date ?: LocalDate.now()
                _uiState.update { it.copy(overview = data?.takeIf { overview -> overview.date == date }) }
            }
        }

        viewModelScope.launch {
            val owner = observeSession().map { session ->
                (session as? SessionState.Active)?.user
            }.distinctUntilChanged()

            combine(request, observeRevision(), owner) { page, revision, account ->
                Triple(
                    page,
                    revision,
                    account
                )
            }
                .collectLatest { (page, _, account) ->
                    if (account == null) {
                        toggleJob?.cancel()
                        toggleInput = null
                        ownerKey = null
                        _uiState.value = OverviewUiState()
                        return@collectLatest
                    }
                    val nextOwner = Triple(
                        account.id,
                        account.couple?.id,
                        account.couple?.users.orEmpty().map { it.id }.sorted()
                    )
                    if (nextOwner != ownerKey) {
                        toggleJob?.cancel()
                        toggleInput = null
                        ownerKey = nextOwner
                        _uiState.update { it.copy(toggle = null) }
                    }
                    if (page == null || !page.active) return@collectLatest

                    _uiState.update {
                        it.copy(
                            overview = it.overview?.takeIf { data -> data.date == page.date },
                            load = ApiResult.Loading
                        )
                    }
                    val result = getOverview(date = page.date)
                    _uiState.update { it.copy(load = result.map { }) }

                    if (result is ApiResult.Success) {
                        val couple = result.data.couple
                        val currentMembers = account.couple?.users?.map { it.id }?.toSet()
                        val overviewMembers = couple?.memberIds
                        if (couple?.id != account.couple?.id || currentMembers != overviewMembers) {
                            refreshSession()
                        }
                    } else {
                        reconcileMembership(result = result)
                    }
                }
        }
    }

    fun refresh(date: LocalDate) {
        val current = request.value
        if (current?.active == true && current.date == date && uiState.value.load == ApiResult.Loading) return
        request.value =
            HomeRequest(date = date, serial = (request.value?.serial ?: 0) + 1, active = true)
    }

    fun pause() {
        request.update { it?.copy(active = false) }
    }

    fun toggle(task: Task) = toggle(id = task.id, completed = !task.completed)

    fun retryToggle() {
        toggleInput?.let { (id, completed) -> toggle(id = id, completed = completed) }
    }

    private fun toggle(id: String, completed: Boolean) {
        if (toggleJob?.isActive == true) return
        toggleInput = id to completed
        _uiState.update { it.copy(toggle = ApiResult.Loading) }

        toggleJob = viewModelScope.launch {
            val result = updateTask(id = id, completed = completed)
            _uiState.update { it.copy(toggle = result.map { }) }
            if (result is ApiResult.Success) toggleInput = null
            reconcileMembership(result = result)
        }
    }

    private suspend fun reconcileMembership(result: ApiResult<*>) {
        val code = (result as? ApiResult.Failure)?.error?.code?.replace('.', '_')
        if (code == "error_profile_required" || code == "error_user_no_couple") {
            refreshSession()
        }
    }
}
