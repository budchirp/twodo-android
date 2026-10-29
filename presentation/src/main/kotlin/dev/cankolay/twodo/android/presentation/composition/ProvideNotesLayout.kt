package dev.cankolay.twodo.android.presentation.composition

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import dev.cankolay.twodo.android.presentation.navigation.notes.NotesNavigation
import dev.cankolay.twodo.android.presentation.navigation.notes.rememberNotesNavigation
import dev.cankolay.twodo.android.presentation.navigation.notes.rememberNotesSceneStrategy

internal class NotesLayout(
    val navigation: NotesNavigation,
    val strategy: SceneStrategy<NavKey>,
    val transition: SharedTransitionScope,
)

internal val LocalNotesLayout = staticCompositionLocalOf<NotesLayout?> { null }

@Composable
fun ProvideNotesLayout(content: @Composable () -> Unit) {
    val navigation = rememberNotesNavigation(stack = LocalNavBackStack.current)

    val strategy = rememberNotesSceneStrategy(navigation = navigation)

    SharedTransitionLayout {
        CompositionLocalProvider(
            value =
                LocalNotesLayout provides
                        NotesLayout(
                            navigation = navigation,
                            strategy = strategy,
                            transition = this@SharedTransitionLayout,
                        ),
            content = content,
        )
    }
}
