package dev.cankolay.twodo.android.presentation.core

import dev.cankolay.twodo.android.presentation.navigation.route.Route

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class NavigateTo(val route: Route) : UiEvent
    data class ResetTo(val route: Route) : UiEvent
    data object NavigateBack : UiEvent
    data object InviteAccepted : UiEvent
}
