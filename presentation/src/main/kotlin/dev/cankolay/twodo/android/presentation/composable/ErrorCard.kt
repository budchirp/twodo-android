package dev.cankolay.twodo.android.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardList
import dev.cankolay.twodo.android.presentation.composable.app.Icon

@Composable
fun ErrorCard(
    modifier: Modifier = Modifier.padding(horizontal = 16.dp),
    title: String = stringResource(id = R.string.error),
    error: String?,
    onRefresh: (() -> Unit)? = null,
) {
    val visibility = remember { MutableTransitionState(initialState = false) }
    visibility.targetState = error != null
    var lastError by remember { mutableStateOf(error) }
    LaunchedEffect(error) { if (error != null) lastError = error }
    AnimatedVisibility(
        visibleState = visibility,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        CardList(
            modifier = modifier,
            color = MaterialTheme.colorScheme.errorContainer,
            title = title,
            description = error ?: lastError,
            contentSize = null,
            trailingContent =
                if (onRefresh != null) {
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
}
