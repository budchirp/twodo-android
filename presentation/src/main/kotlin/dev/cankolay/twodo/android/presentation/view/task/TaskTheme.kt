package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

@Composable
internal fun TaskTheme(
    background: Color?,
    content: @Composable () -> Unit,
) {
    val parent = MaterialTheme.colorScheme
    val scheme = rememberDynamicColorScheme(
        seedColor = background ?: parent.primary,
        isDark = parent.surface.luminance() < 0.5f,
        style = PaletteStyle.Expressive,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
    )

    MaterialTheme(
        colorScheme = if (background == null) scheme else scheme.copy(
            background = background,
            surface = background,
        ),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
