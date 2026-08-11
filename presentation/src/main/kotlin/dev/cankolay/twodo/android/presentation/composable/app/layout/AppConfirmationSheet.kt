package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestructiveConfirmationSheet(
    title: String,
    description: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AppBottomSheet(
        title = title,
        description = description,
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = onConfirm
            ) {
                Text(text = confirmText)
            }
        }
    )
}
