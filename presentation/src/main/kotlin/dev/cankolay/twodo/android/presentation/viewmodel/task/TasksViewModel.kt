package dev.cankolay.twodo.android.presentation.viewmodel.task

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.task.GetTasksUseCase
import dev.cankolay.twodo.android.domain.usecase.api.task.ObserveTasksUseCase
import dev.cankolay.twodo.android.domain.usecase.api.task.UpdateTaskUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    observeTasks: ObserveTasksUseCase,
    observeSession: ObserveSessionUseCase,
    private val getTasks: GetTasksUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val refreshSession: RefreshSessionUseCase,
) : BaseViewModel<TasksUiState>(initialState = TasksUiState(tasks = observeTasks().value)) {
    private var loadJob: Job? = null
    private var toggleJob: Job? = null
    private var toggleInput: Pair<String, Boolean>? = null

    init {
        viewModelScope.launch {
            observeTasks().collect { tasks ->
                if (tasks == null && uiState.value.tasks != null) {
                    cancelRequests()
                    _uiState.value = TasksUiState()
                } else {
                    _uiState.update { it.copy(tasks = tasks) }
                }
            }
        }

        viewModelScope.launch {
            observeSession()
                .map { session ->
                    val user = (session as? SessionState.Active)?.user
                    val couple = user?.couple

                    if (user?.profileCompleted == true && couple != null) {
                        Triple(user.id, couple.id, couple.users.map { it.id }.sorted())
                    } else {
                        null
                    }
                }
                .distinctUntilChanged()
                .collect { owner ->
                    cancelRequests()
                    _uiState.value =
                        TasksUiState(tasks = if (owner != null) observeTasks().value else null)

                    if (owner != null) fetch(forceRefresh = true)
                }
        }
    }

    fun fetch(forceRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return

        _uiState.update { it.copy(load = ApiResult.Loading) }

        loadJob = viewModelScope.launch {
            val result = getTasks(forceRefresh = forceRefresh)

            _uiState.update { it.copy(load = result.map { }) }
            reconcileMembership(result = result)
        }
    }

    fun toggle(task: Task) {
        toggle(id = task.id, completed = !task.completed)
    }

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

    private fun cancelRequests() {
        loadJob?.cancel()
        toggleJob?.cancel()
        toggleInput = null
    }

    private suspend fun reconcileMembership(result: ApiResult<*>) {
        val code = (result as? ApiResult.Failure)?.error?.code
            ?.replace('.', '_')
            ?.replace('-', '_')

        if (code == "error_profile_required" || code == "error_user_no_couple") {
            refreshSession()
        }
    }
}
