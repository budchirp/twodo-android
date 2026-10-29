package dev.cankolay.twodo.android.presentation.view

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet

@Composable
internal fun LoginErrorSheet(@StringRes error: Int, onDismiss: () -> Unit) {
    AppBottomSheet(
        title = stringResource(id = R.string.error),
        onDismiss = onDismiss,
        status = {
            ErrorCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                error = stringResource(id = error),
            )
        },
        actions = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.close))
            }
        },
    )
}
