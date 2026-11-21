package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

@Composable
fun SettingCard(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    shape: Shape = MaterialTheme.shapes.large,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = onClick != null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    rowSize: SettingRowSize = SettingRowSize.forContent(
        description = description,
        hasTrailingContent = trailingContent != null,
    ),
) {
    AppCard(
        modifier = modifier,
        color = color,
        shape = shape,
        contentPadding = AppCardDefaults.NoPadding,
    ) {
        SettingRow(
            title = title,
            description = description,
            onClick = onClick,
            enabled = enabled,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            rowSize = rowSize,
        )
    }
}
