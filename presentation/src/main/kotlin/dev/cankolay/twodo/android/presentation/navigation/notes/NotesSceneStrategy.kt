package dev.cankolay.twodo.android.presentation.navigation.notes

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPaneDefaults

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberNotesSceneStrategy(navigation: NotesNavigation): SceneStrategy<NavKey> {
    val directive =
        calculatePaneScaffoldDirective(windowAdaptiveInfo = currentWindowAdaptiveInfoV2())

    return rememberListDetailSceneStrategy(
        shouldHandleSinglePaneLayout = false,
        directive =
            directive.copy(
                maxVerticalPartitions = 1,
                horizontalPartitionSpacerSize = AppPaneDefaults.Spacing,
                maxHorizontalPartitions =
                    when {
                        !navigation.isExpanded || navigation.isFullscreen -> 1

                        else -> directive.maxHorizontalPartitions
                    },
            ),
    )
}
