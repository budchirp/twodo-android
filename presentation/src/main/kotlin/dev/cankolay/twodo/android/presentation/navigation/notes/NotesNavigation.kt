package dev.cankolay.twodo.android.presentation.navigation.notes

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.cankolay.twodo.android.presentation.composable.app.layout.currentPaneDirective
import dev.cankolay.twodo.android.presentation.navigation.route.Route

internal class NotesNavigation(
    private val stack: NavBackStack<NavKey>,
    fullscreen: MutableState<Boolean>,
    pending: MutableState<String?>,
    pendingCreate: MutableState<Boolean>,
    val isExpanded: Boolean,
) {
    var isFullscreen by fullscreen
        private set

    var pending by pending
        private set

    private var pendingCreate by pendingCreate

    val isCreating: Boolean
        get() = stack.lastOrNull() == Route.CreateNote

    val hasPending: Boolean
        get() = pending != null || pendingCreate

    private val hasDetail: Boolean
        get() = id != null || isCreating

    val id: String?
        get() = (stack.lastOrNull() as? Route.Note)?.id

    val hasListPane: Boolean
        get() = stack.any { it == Route.Notes }

    fun select(id: String, fullscreen: Boolean = false) {
        if (id == this.id && !isCreating) return

        isFullscreen = fullscreen
        pendingCreate = false
        if (!hasDetail) {
            pending = null

            stack.add(element = Route.Note(id = id))
        } else {
            pending = id
        }
    }

    fun create() {
        if (isCreating) return
        isFullscreen = false
        pending = null
        if (hasDetail) pendingCreate = true
        else stack.add(element = Route.CreateNote)
    }

    fun close() {
        val next = if (pendingCreate) Route.CreateNote else pending?.let { Route.Note(id = it) }

        pending = null
        pendingCreate = false
        isFullscreen = next != null && isFullscreen

        if (hasDetail) stack.removeLastOrNull()

        if (next != null) stack.add(element = next)
    }

    fun cancel() {
        pending = null
        pendingCreate = false
    }

    fun toggle() {
        if (!isExpanded || !hasDetail) return
        if (isFullscreen && !hasListPane) {
            // Home opens a standalone detail; add the list only when leaving full-screen.
            stack.add(index = stack.lastIndex, element = Route.Notes)
        }
        isFullscreen = !isFullscreen
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberNotesNavigation(stack: NavBackStack<NavKey>): NotesNavigation {
    val fullscreen = rememberSaveable { mutableStateOf(value = false) }

    val pending = rememberSaveable { mutableStateOf<String?>(value = null) }
    val pendingCreate = rememberSaveable { mutableStateOf(value = false) }

    val isExpanded = currentPaneDirective().maxHorizontalPartitions > 1

    return remember(key1 = stack, key2 = isExpanded) {
        NotesNavigation(
            stack = stack,
            fullscreen = fullscreen,
            pending = pending,
            pendingCreate = pendingCreate,
            isExpanded = isExpanded,
        )
    }
}
