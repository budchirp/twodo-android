package dev.cankolay.twodo.android.presentation.core

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.composition.LocalSnackbarHostState
import dev.cankolay.twodo.android.presentation.navigation.resetTo

@Composable
fun HandleEvents(
    viewModel: BaseViewModel<*>,
    onEvent: (UiEvent) -> Unit = {}
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val navBackStack = LocalNavBackStack.current
    val eventHandler = remember(viewModel, snackbarHostState, navBackStack, onEvent) {
        EventHandler(snackbarHostState, navBackStack, onEvent)
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.events.collect { event -> eventHandler.handle(event) }
    }
}

private class EventHandler(
    private val snackbarHostState: SnackbarHostState,
    private val navBackStack: NavBackStack<NavKey>,
    private val onEvent: (UiEvent) -> Unit
) {
    suspend fun handle(event: UiEvent) {
        when (event) {
            is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(message = event.message)
            is UiEvent.NavigateTo -> navBackStack.add(element = event.route)
            is UiEvent.ResetTo -> navBackStack.resetTo(route = event.route)
            UiEvent.NavigateBack -> navBackStack.removeLastOrNull()
            UiEvent.InviteAccepted -> onEvent(event)
        }
    }
}
