package dev.cankolay.twodo.android.presentation.navigation.notes

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.window.core.layout.WindowSizeClass
import dev.cankolay.twodo.android.presentation.navigation.route.Route

internal class NotesNavigation(
    private val stack: NavBackStack<NavKey>,
    fullscreen: MutableState<Boolean>,
    pending: MutableState<String?>,
    val isExpanded: Boolean,
) {
    var isFullscreen by fullscreen
        private set

    var pending by pending
        private set

    val id: String?
        get() = (stack.lastOrNull() as? Route.Note)?.id

    fun select(id: String) {
        if (id == this.id) return

        if (this.id == null) {
            pending = null
            isFullscreen = false

            stack.add(element = Route.Note(id = id))
        } else {
            pending = id
        }
    }

    fun close() {
        val next = pending

        pending = null
        isFullscreen = false

        if (stack.lastOrNull() is Route.Note) stack.removeLastOrNull()

        if (next != null) stack.add(element = Route.Note(id = next))
    }

    fun cancel() {
        pending = null
    }

    fun toggle() {
        isFullscreen = !isFullscreen
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberNotesNavigation(stack: NavBackStack<NavKey>): NotesNavigation {
    val fullscreen = rememberSaveable { mutableStateOf(value = false) }

    val pending = rememberSaveable { mutableStateOf<String?>(value = null) }

    val info = currentWindowAdaptiveInfoV2()

    val directive = calculatePaneScaffoldDirective(windowAdaptiveInfo = info)

    val isExpanded =
        info.windowSizeClass.isWidthAtLeastBreakpoint(
            widthDpBreakpoint = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
        ) && directive.maxHorizontalPartitions > 1

    return remember(key1 = stack, key2 = isExpanded) {
        NotesNavigation(
            stack = stack,
            fullscreen = fullscreen,
            pending = pending,
            isExpanded = isExpanded,
        )
    }
}
