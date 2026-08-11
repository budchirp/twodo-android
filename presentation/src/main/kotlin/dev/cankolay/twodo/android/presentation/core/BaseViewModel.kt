package dev.cankolay.twodo.android.presentation.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : Any>(initialState: S) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(capacity = BUFFERED)
    val events = _events.receiveAsFlow()

    private val jobs = mutableMapOf<String, Job>()

    protected fun updateState(transform: S.() -> S) {
        _uiState.update { it.transform() }
    }

    protected fun launchOnce(key: String, block: suspend CoroutineScope.() -> Unit) {
        if (jobs[key]?.isActive == true) return
        jobs[key] = viewModelScope.launch(block = block).track(key)
    }

    protected fun launchLatest(key: String, block: suspend CoroutineScope.() -> Unit) {
        jobs[key]?.cancel()
        jobs[key] = viewModelScope.launch(block = block).track(key)
    }

    protected fun cancelJob(key: String) {
        jobs.remove(key)?.cancel()
    }

    protected fun sendEvent(event: UiEvent) {
        _events.trySend(event)
    }

    private fun Job.track(key: String): Job {
        invokeOnCompletion {
            if (jobs[key] === this) jobs.remove(key)
        }
        return this
    }
}
