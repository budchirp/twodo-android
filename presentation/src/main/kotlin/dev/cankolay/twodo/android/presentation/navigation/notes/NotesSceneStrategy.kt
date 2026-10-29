package dev.cankolay.twodo.android.presentation.navigation.notes

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import dev.cankolay.twodo.android.presentation.composable.app.layout.currentPaneDirective

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberNotesSceneStrategy(navigation: NotesNavigation): SceneStrategy<NavKey> {
    val directive = currentPaneDirective()

    return rememberListDetailSceneStrategy(
        shouldHandleSinglePaneLayout = false,
        directive =
            directive.copy(
                maxHorizontalPartitions =
                    when {
                        !navigation.isExpanded || navigation.isFullscreen -> 1

                        else -> directive.maxHorizontalPartitions
                    }
            ),
    )
}
