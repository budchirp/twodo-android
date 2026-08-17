package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.viewmodel.note.CreateNoteFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateNoteSheet(
    form: CreateNoteFormState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCreate: () -> Unit
) {
    AppBottomSheet(
        title = stringResource(id = R.string.create_note),
        onDismiss = onDismiss,
        actions = {
            Button(
                enabled = form.canSubmit,
                onClick = onCreate
            ) {
                Text(text = stringResource(id = R.string.create))
            }
        }
    ) {
        item {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.title.value,
                onValueChange = onTitleChange,
                label = { Text(text = stringResource(id = R.string.title)) },
                isError = form.title.error != null,
                supportingText = form.title.error?.let { error ->
                    { Text(text = stringResource(id = error)) }
                }
            )
        }
    }
}
