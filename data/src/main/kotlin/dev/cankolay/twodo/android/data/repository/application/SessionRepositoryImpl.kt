package dev.cankolay.twodo.android.data.repository.application

import dev.cankolay.twodo.android.data.di.module.ApplicationScope
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import dev.cankolay.twodo.android.domain.model.application.AuthState
import dev.cankolay.twodo.android.domain.model.application.SessionState
import dev.cankolay.twodo.android.domain.repository.api.CalendarRepository
import dev.cankolay.twodo.android.domain.repository.api.CoupleRepository
import dev.cankolay.twodo.android.domain.repository.api.InviteRepository
import dev.cankolay.twodo.android.domain.repository.api.NoteRepository
import dev.cankolay.twodo.android.domain.repository.api.OverviewRepository
import dev.cankolay.twodo.android.domain.repository.api.TaskRepository
import dev.cankolay.twodo.android.domain.repository.api.UserRepository
import dev.cankolay.twodo.android.domain.repository.application.AuthStateRepository
import dev.cankolay.twodo.android.domain.repository.application.SessionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val credentials: AuthStateRepository,
    private val users: UserRepository,
    private val couples: CoupleRepository,
    private val notes: NoteRepository,
    private val tasks: TaskRepository,
    private val overview: OverviewRepository,
    private val calendar: CalendarRepository,
    private val invites: InviteRepository,
    @ApplicationScope scope: CoroutineScope
) : SessionRepository {
    private val _state = MutableStateFlow<SessionState>(SessionState.Starting)
    override val state = _state.asStateFlow()
    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized = _isInitialized.asStateFlow()
    private val credentialLock = Mutex()
    private val userLock = Mutex()
    private var sessionScope: CoroutineScope? = null
    private var needsInitialization = false

    init {
        scope.launch {
            credentials.state.map { it.token }.distinctUntilChanged().collectLatest { token ->
                sessionScope = null
                _isInitialized.value = true
                if (token.isBlank()) {
                    needsInitialization = false
                    _state.value = SessionState.SignedOut
                    clearData()
                } else coroutineScope {
                    _state.value = SessionState.Starting
                    sessionScope = this
                    loadUser()
                    awaitCancellation()
                }
            }
        }
    }

    override suspend fun authenticate(token: String) = credentialLock.withLock {
        if (credentials.state.first().token == token) return@withLock
        endSession()
        needsInitialization = true
        credentials.update(state = AuthState(token = token))
    }

    override suspend fun logout() = credentialLock.withLock {
        endSession()
        needsInitialization = false
        credentials.update(state = AuthState())
        _state.value = SessionState.SignedOut
    }

    override suspend fun refresh(): ApiResult<User> = inSession {
        loadUser()
    }

    override suspend fun refreshProfile(): ApiResult<User> = inSession {
        userLock.withLock {
            users.refreshProfile().also { result ->
                if (result is ApiResult.Success) applyUser(user = result.data)
            }
        }
    }

    override suspend fun updateProfile(name: String, gender: Gender): ApiResult<User> = inSession {
        userLock.withLock {
            users.updateProfile(name = name, gender = gender).also { result ->
                if (result is ApiResult.Success) applyUser(user = result.data)
            }
        }
    }

    override suspend fun updateRelationshipDate(date: LocalDate?) = inSession {
        userLock.withLock {
            couples.updateRelationshipDate(date).also { result ->
                currentCoroutineContext().ensureActive()
                val active = _state.value as? SessionState.Active
                if (result is ApiResult.Success && active != null) {
                    applyUser(user = active.user.copy(couple = result.data))
                }
            }
        }
    }

    override suspend fun leaveCouple(): ApiResult<Unit> = inSession {
        userLock.withLock {
            couples.leave().also { result ->
                currentCoroutineContext().ensureActive()
                if (result is ApiResult.Success) {
                    val active = _state.value as? SessionState.Active
                    clearData()
                    currentCoroutineContext().ensureActive()
                    if (active != null) _state.value =
                        SessionState.Active(user = active.user.copy(couple = null))
                }
            }
        }
    }

    private suspend fun loadUser(): ApiResult<User> = userLock.withLock {
        val result = if (needsInitialization) users.initialize() else users.get()
        currentCoroutineContext().ensureActive()
        when (result) {
            ApiResult.Loading -> Unit

            is ApiResult.Success -> {
                applyUser(user = result.data)
            }

            is ApiResult.Failure -> if (_state.value !is SessionState.Active) {
                val code = result.error.code?.replace('.', '_')?.replace('-', '_')
                _state.value = if (code == "error_profile_required") {
                    SessionState.ProfileRequired
                } else SessionState.Failed(error = result.error)
            }
        }
        result
    }

    private suspend fun applyUser(user: User) {
        currentCoroutineContext().ensureActive()
        needsInitialization = false
        val previous = _state.value as? SessionState.Active
        val previousMembers = previous?.user?.couple?.users?.map { it.id }?.toSet()
        val currentMembers = user.couple?.users?.map { it.id }?.toSet()
        if (previous != null && (previous.user.couple?.id != user.couple?.id || previousMembers != currentMembers)) {
            clearData()
        }
        currentCoroutineContext().ensureActive()
        _state.value = SessionState.Active(user = user)
    }

    private suspend fun endSession() {
        sessionScope?.cancel()
        sessionScope = null
        _state.value = SessionState.Starting
        clearData()
    }

    private suspend fun clearData() {
        overview.clear()
        tasks.clear()
        notes.clear()
        calendar.clear()
        invites.clear()
    }

    private suspend fun <T> inSession(block: suspend () -> T): T {
        val scope = sessionScope ?: throw CancellationException("No active session")
        return scope.async { block() }.await()
    }
}
