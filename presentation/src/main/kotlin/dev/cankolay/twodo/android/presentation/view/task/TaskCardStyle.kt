package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

internal data class TaskCardStyle(
    val background: Color,
    val foreground: Color,
    val rotation: Float,
)

@Composable
internal fun taskCardStyle(id: String): TaskCardStyle {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    return remember(id, dark) {
        val colors = if (dark) {
            listOf(
                0xFF70501A,
                0xFF71364D,
                0xFF255E42,
                0xFF2A527E,
                0xFF5B3E80,
                0xFF783F23,
                0xFF7E3042,
                0xFF1C6364
            )
        } else {
            listOf(
                0xFFFFCF56,
                0xFFFF9ABC,
                0xFF9AE6AD,
                0xFF91C8FF,
                0xFFC8A0FF,
                0xFFFFAB70,
                0xFFFF8F9E,
                0xFF6ADCD1
            )
        }
        val seed = id.hashCode().toUInt()

        TaskCardStyle(
            background = Color(colors[(seed % colors.size.toUInt()).toInt()]),
            foreground = if (dark) Color(0xFFF9F3EA) else Color(0xFF292725),
            rotation = (if (seed % 2u == 0u) 1f else -1f) * (0.6f + (seed / 7u % 5u).toInt() * 0.2f),
        )
    }
}
