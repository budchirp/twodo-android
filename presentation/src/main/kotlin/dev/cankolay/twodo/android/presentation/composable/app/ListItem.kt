package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = onClick != null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues? = null,
    contentSize: Dp? = 24.dp
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                modifier
                    .then(other = if (enabled) Modifier.clickable { onClick?.invoke() } else Modifier)
                    .padding(
                        paddingValues = contentPadding ?: PaddingValues(
                            vertical = if (description == null) 24.dp else 16.dp
                        )
                    ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.let {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .then(other = if (contentSize != null) Modifier.size(size = contentSize) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.primary) {
                        leadingContent()
                    }
                }
            }

            Column(
                modifier =
                    Modifier
                        .padding(
                            start = if (leadingContent == null) 16.dp else 0.dp,
                            end = if (trailingContent == null) 16.dp else 0.dp
                        )
                        .weight(weight = 1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 2)

                if (!description.isNullOrEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            trailingContent?.let {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .then(other = if (contentSize != null) Modifier.height(height = contentSize) else Modifier),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    trailingContent()
                }
            }
        }

    }
}
