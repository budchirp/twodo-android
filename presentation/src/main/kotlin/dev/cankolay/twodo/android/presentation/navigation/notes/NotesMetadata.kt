package dev.cankolay.twodo.android.presentation.navigation.notes

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPaneDefaults
import dev.cankolay.twodo.android.presentation.motion.TransitionType
import dev.cankolay.twodo.android.presentation.motion.navigationTransition
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.view.note.NoteView

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun notesListMetadata(): Map<String, Any> =
    navigationTransition(type = TransitionType.FADE) +
            ListDetailSceneStrategy.listPane(
                sceneKey = Route.Notes,
                detailPlaceholder = { NoteView() },
            ) +
            ListDetailSceneStrategy.preferredPaneSize(width = AppPaneDefaults.Width) +
            ListDetailSceneStrategy.paneAnimation(
                enterTransition = EnterTransition.None,
                exitTransition = ExitTransition.None,
            )

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun noteDetailMetadata(): Map<String, Any> =
    navigationTransition(type = TransitionType.FADE) +
            ListDetailSceneStrategy.detailPane(sceneKey = Route.Notes) +
            ListDetailSceneStrategy.paneAnimation(
                enterTransition = EnterTransition.None,
                exitTransition = ExitTransition.None,
            )
