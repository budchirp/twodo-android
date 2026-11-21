package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SettingRowSize(val minHeight: Dp, val verticalPadding: Dp) {
    Compact(minHeight = 64.dp, verticalPadding = 12.dp),
    Comfortable(minHeight = 80.dp, verticalPadding = 16.dp);

    companion object {
        fun forContent(description: String?, hasTrailingContent: Boolean): SettingRowSize =
            if (description.isNullOrEmpty() && !hasTrailingContent) Compact else Comfortable
    }
}

@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                other = if (onClick != null) Modifier.clickable(
                    enabled = enabled,
                    onClick = onClick
                )
                else Modifier
            )
            .heightIn(min = rowSize.minHeight)
            .padding(
                horizontal = AppCardDefaults.Spacing,
                vertical = rowSize.verticalPadding,
            ),
        horizontalArrangement = Arrangement.spacedBy(space = AppCardDefaults.Spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            Box(contentAlignment = Alignment.Center) {
                CompositionLocalProvider(
                    value = LocalContentColor provides MaterialTheme.colorScheme.primary
                ) {
                    leadingContent()
                }
            }
        }

        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(
                space = AppCardDefaults.TextSpacing,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!description.isNullOrEmpty()) {
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (trailingContent != null) {
            Box(contentAlignment = Alignment.Center) {
                trailingContent()
            }
        }
    }
}
