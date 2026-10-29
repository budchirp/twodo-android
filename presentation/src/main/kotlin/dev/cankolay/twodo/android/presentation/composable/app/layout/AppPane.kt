package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.composable.rememberDeviceCornerRadius

internal object AppPaneDefaults {
    val Width = 360.dp

    val Spacing = 16.dp
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun currentPaneDirective(): PaneScaffoldDirective =
    calculatePaneScaffoldDirective(windowAdaptiveInfo = currentWindowAdaptiveInfoV2())
        .copy(
            maxVerticalPartitions = 1,
            horizontalPartitionSpacerSize = AppPaneDefaults.Spacing,
            defaultPanePreferredWidth = AppPaneDefaults.Width,
        )

@Composable
internal fun AppPane(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(size = rememberDeviceCornerRadius()),
        color = MaterialTheme.colorScheme.surface,
        content = content,
    )
}
