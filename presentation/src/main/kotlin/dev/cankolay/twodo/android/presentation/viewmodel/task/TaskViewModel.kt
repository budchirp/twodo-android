package dev.cankolay.twodo.android.presentation.viewmodel.task

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.task.FieldChange
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.couple.GetCoupleUseCase
import dev.cankolay.twodo.android.domain.usecase.api.task.CreateTaskUseCase
import dev.cankolay.twodo.android.domain.usecase.api.task.DeleteTaskUseCase
import dev.cankolay.twodo.android.domain.usecase.api.task.GetTaskUseCase
import dev.cankolay.twodo.android.domain.usecase.api.task.UpdateTaskUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTask: GetTaskUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val deleteTask: DeleteTaskUseCase,
    private val getCouple: GetCoupleUseCase,
    private val refreshSession: RefreshSessionUseCase,
    observeSession: ObserveSessionUseCase,
) : BaseViewModel<TaskUiState>(initialState = TaskUiState()) {
    private var initialized = false
    private var id: String? = null
    private var loadJob: Job? = null
    private var membersJob: Job? = null
    private var mutationJob: Job? = null
    private var owner: Triple<String, String?, List<String>>? = null

    init {
        viewModelScope.launch {
            observeSession().collect { session ->
                val user = (session as? SessionState.Active)?.user
                val next = user?.let {
                    Triple(
                        it.id,
                        it.couple?.id,
                        it.couple?.users.orEmpty().map { member -> member.id }.sorted()
                    )
                }

                if (owner != null && next != owner) {
                    loadJob?.cancel()
                    membersJob?.cancel()
                    mutationJob?.cancel()
                    _uiState.value = TaskUiState(finished = true)
                } else {
                    _uiState.update {
                        it.copy(
                            userId = user?.id,
                            members = user?.couple?.users.orEmpty()
                        )
                    }
                }

                owner = next
            }
        }
    }

    fun initialize(id: String?) {
        if (initialized) return
        initialized = true
        this.id = id
        if (id != null) load()
        loadMembers()
    }

    fun loadMembers() {
        if (membersJob?.isActive == true) return
        _uiState.update { it.copy(membersLoad = ApiResult.Loading) }

        membersJob = viewModelScope.launch {
            val result = getCouple()
            _uiState.update {
                it.copy(
                    members = if (result is ApiResult.Success) result.data.users else it.members,
                    membersLoad = result.map { },
                )
            }
            reconcileMembership(result = result)
        }
    }

    fun load() {
        val taskId = id ?: return
        if (loadJob?.isActive == true || uiState.value.editor.busy) return
        _uiState.update { it.copy(editor = it.editor.copy(load = ApiResult.Loading)) }

        loadJob = viewModelScope.launch {
            val before = uiState.value.editor
            val result = getTask(id = taskId)

            _uiState.update { state ->
                val current = state.editor
                state.copy(
                    editor = if (result is ApiResult.Success) {
                        val task = result.data
                        current.copy(
                            original = task,
                            title = if (current.title != before.original?.title.orEmpty()) current.title else task.title,
                            description = if (current.description != before.original?.description.orEmpty()) current.description else task.description,
                            dueDate = if (current.dueDate != before.original?.dueDate) current.dueDate else task.dueDate,
                            assignedToUserId = if (current.assignedToUserId != before.original?.assignedToUserId) current.assignedToUserId else task.assignedToUserId,
                            completed = if (current.completed != (before.original?.completed
                                    ?: false)
                            ) current.completed else task.completed,
                            load = ApiResult.Success(data = Unit),
                        )
                    } else current.copy(load = result.map { })
                )
            }
            reconcileMembership(result = result)
        }
    }

    fun updateTitle(value: String) = draft { it.copy(title = value, save = null) }

    fun updateDescription(value: String) = draft { it.copy(description = value, save = null) }

    fun updateDueDate(value: LocalDate?) = draft { it.copy(dueDate = value, save = null) }

    fun updateAssignee(value: String?) = draft { it.copy(assignedToUserId = value, save = null) }

    fun updateCompleted(value: Boolean) = draft { it.copy(completed = value, save = null) }

    private fun draft(block: (TaskEditorState) -> TaskEditorState) {
        _uiState.update { if (it.editor.busy) it else it.copy(editor = block(it.editor)) }
    }

    fun save() {
        val editor = uiState.value.editor
        if (editor.busy || mutationJob?.isActive == true || (id != null && editor.original == null)) return

        if (!editor.valid) {
            _uiState.update { it.copy(editor = editor.copy(validate = true)) }
            return
        }

        if (!editor.changed) {
            _uiState.update { it.copy(finished = true) }
            return
        }

        _uiState.update { it.copy(editor = editor.copy(save = ApiResult.Loading)) }

        mutationJob = viewModelScope.launch {
            val original = editor.original
            val result = if (original == null) {
                createTask(
                    title = editor.title,
                    description = editor.description,
                    dueDate = editor.dueDate,
                    assignedToUserId = editor.assignedToUserId,
                )
            } else {
                updateTask(
                    id = original.id,
                    title = editor.title.trim().takeIf { it != original.title },
                    description = editor.description.takeIf { it != original.description },
                    completed = editor.completed.takeIf { it != original.completed },
                    dueDate = if (editor.dueDate != original.dueDate) FieldChange(editor.dueDate) else null,
                    assignedToUserId = if (editor.assignedToUserId != original.assignedToUserId) FieldChange(
                        editor.assignedToUserId
                    ) else null,
                )
            }

            _uiState.update {
                it.copy(
                    editor = it.editor.copy(save = result.map { }),
                    finished = result is ApiResult.Success
                )
            }
            reconcileMembership(result = result)
        }
    }

    fun delete() {
        val taskId = id ?: return
        if (uiState.value.editor.busy || mutationJob?.isActive == true) return
        _uiState.update { it.copy(editor = it.editor.copy(delete = ApiResult.Loading)) }

        mutationJob = viewModelScope.launch {
            val result = deleteTask(id = taskId)
            _uiState.update {
                it.copy(
                    editor = it.editor.copy(delete = result),
                    finished = result is ApiResult.Success
                )
            }
            reconcileMembership(result = result)
        }
    }

    fun clearDeleteError() = draft { it.copy(delete = null) }

    private suspend fun reconcileMembership(result: ApiResult<*>) {
        val code = (result as? ApiResult.Failure)?.error?.code?.replace('.', '_')
        if (code == "error_profile_required" || code == "error_user_no_couple") {
            refreshSession()
        }
    }
}
