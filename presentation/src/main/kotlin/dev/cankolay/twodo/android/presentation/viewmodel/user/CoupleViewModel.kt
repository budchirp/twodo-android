package dev.cankolay.twodo.android.presentation.viewmodel.user

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.map
import dev.cankolay.twodo.android.domain.model.api.user.Couple
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.usecase.api.couple.GetCoupleUseCase
import dev.cankolay.twodo.android.domain.usecase.api.couple.LeaveCoupleUseCase
import dev.cankolay.twodo.android.domain.usecase.api.couple.UpdateRelationshipDateUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.ObserveSessionUseCase
import dev.cankolay.twodo.android.domain.usecase.application.session.RefreshSessionUseCase
import dev.cankolay.twodo.android.presentation.core.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CoupleUiState(
    val user: User? = null,
    val couple: Couple? = null,
    val dateResult: ApiResult<Unit>? = null,
    val dateDraft: LocalDate? = null,
    val hasDateDraft: Boolean = false,
    val load: ApiResult<Unit>? = null,
    val result: ApiResult<Unit>? = null,
    val showLeave: Boolean = false,
)

@HiltViewModel
class CoupleViewModel @Inject constructor(
    private val observeSession: ObserveSessionUseCase,
    private val refreshSession: RefreshSessionUseCase,
    private val leaveCouple: LeaveCoupleUseCase,
    private val getCouple: GetCoupleUseCase,
    private val updateDate: UpdateRelationshipDateUseCase,
) : BaseViewModel<CoupleUiState>(
    initialState = CoupleUiState(user = (observeSession().value as? SessionState.Active)?.user)
) {

    private var loadJob: Job? = null
    private var dateJob: Job? = null
    private var pendingDate: LocalDate? = null
    private var owner: Triple<String, String?, List<String>>? = null

    init {
        viewModelScope.launch {
            observeSession().collect { session ->
                val user = (session as? SessionState.Active)?.user
                val nextOwner = user?.let {
                    Triple(
                        it.id,
                        it.couple?.id,
                        it.couple?.users.orEmpty().map { member -> member.id }.sorted()
                    )
                }
                if (nextOwner != owner) {
                    loadJob?.cancel()
                    dateJob?.cancel()
                    if (owner != null) _uiState.value = CoupleUiState()
                    owner = nextOwner
                }
                _uiState.update {
                    when (session) {
                        is SessionState.Active -> it.copy(
                            user = session.user,
                            couple = session.user.couple,
                            load = if (it.user == null && it.load == ApiResult.Loading)
                                ApiResult.Success(data = Unit) else it.load,
                        )

                        is SessionState.Failed -> it.copy(load = ApiResult.Failure(error = session.error))
                        SessionState.Starting -> CoupleUiState(load = ApiResult.Loading)
                        SessionState.SignedOut -> CoupleUiState()
                        SessionState.ProfileRequired -> it.copy(
                            user = null,
                            couple = null,
                            load = null
                        )
                    }
                }
            }
        }
        refresh()
    }

    fun confirm() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showLeave = true, result = null) }
    }

    fun dismiss() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(showLeave = false, result = null) }
    }

    fun refresh() {
        if (loadJob?.isActive == true) return

        if (uiState.value.load == ApiResult.Loading) return

        _uiState.update { it.copy(load = ApiResult.Loading) }

        loadJob = viewModelScope.launch {
            val result = getCouple()
            _uiState.update {
                it.copy(
                    couple = if (result is ApiResult.Success) result.data else it.couple,
                    load = result.map { })
            }
            if (result is ApiResult.Failure && result.error.statusCode == 403) refreshSession()
        }
    }

    fun updateRelationshipDate(date: LocalDate?) {
        if (dateJob?.isActive == true || date?.isAfter(LocalDate.now()) == true) return
        pendingDate = date
        _uiState.update {
            it.copy(dateResult = ApiResult.Loading, dateDraft = date, hasDateDraft = true)
        }

        dateJob = viewModelScope.launch {
            val result = updateDate(date = date)
            _uiState.update {
                it.copy(
                    couple = if (result is ApiResult.Success) result.data else it.couple,
                    dateResult = result.map { },
                    hasDateDraft = result !is ApiResult.Success,
                )
            }
        }
    }

    fun retryDate() = updateRelationshipDate(date = pendingDate)

    fun leave() {
        if (uiState.value.result == ApiResult.Loading) return

        _uiState.update { it.copy(result = ApiResult.Loading) }

        viewModelScope.launch {
            val result = leaveCouple()

            _uiState.update { it.copy(result = result) }
        }
    }
}
