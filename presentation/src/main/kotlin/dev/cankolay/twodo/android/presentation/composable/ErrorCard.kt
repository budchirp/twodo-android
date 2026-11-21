package dev.cankolay.twodo.android.presentation.composable

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingCard

@Composable
fun ErrorCard(
    error: String,
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.error),
    onRefresh: (() -> Unit)? = null,
) {
    SettingCard(
        modifier = modifier,
        color = MaterialTheme.colorScheme.errorContainer,
        title = title,
        description = error,
        trailingContent = if (onRefresh != null) {
            {
                Button(onClick = onRefresh) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        } else null,
        leadingContent = {
            Icon(
                icon = Icons.Default.ErrorOutline,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
        },
    )
}
