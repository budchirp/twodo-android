package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SettingCard(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = onClick != null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    contentSize: Dp? = 24.dp,
) {
    AppCard(modifier = modifier, color = color, contentPadding = PaddingValues(all = 0.dp)) {
        SettingRow(
            title = title,
            description = description,
            onClick = onClick,
            enabled = enabled,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            contentPadding = contentPadding,
            contentSize = contentSize,
        )
    }
}
