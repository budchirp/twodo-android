package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import sv.lib.squircleshape.SquircleShape

data class SettingItem(
    val title: String,
    val description: String? = null,
    val onClick: (() -> Unit)? = null,
    val enabled: Boolean = onClick != null,
    val leadingContent: @Composable (() -> Unit)? = null,
    val trailingContent: @Composable (() -> Unit)? = null,
    val rowSize: SettingRowSize = SettingRowSize.forContent(
        description = description,
        hasTrailingContent = trailingContent != null,
    ),
    val modifier: Modifier = Modifier,
)

@Composable
fun SettingsGroup(
    items: List<SettingItem>,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    if (items.isEmpty()) return

    AppCard(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        contentPadding = AppCardDefaults.NoPadding,
        verticalArrangement = Arrangement.spacedBy(space = AppCardDefaults.GroupSpacing),
    ) {
        items.forEach { item ->
            Surface(color = color, shape = SquircleShape(radius = 4.dp)) {
                SettingRow(
                    modifier = item.modifier,
                    title = item.title,
                    description = item.description,
                    onClick = item.onClick,
                    enabled = item.enabled,
                    leadingContent = item.leadingContent,
                    trailingContent = item.trailingContent,
                    rowSize = item.rowSize,
                )
            }
        }
    }
}
