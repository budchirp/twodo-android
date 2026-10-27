package dev.cankolay.twodo.android.presentation.form

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction

@Composable
fun FormTextField(
    field: FormField<String>,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSubmit: (() -> Unit)? = null,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    TextField(
        modifier = modifier,
        value = field.value,
        label = { Text(text = label) },
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions =
            KeyboardActions(onDone = { if (onSubmit != null) onSubmit() else keyboard?.hide() }),
        isError = field.error != null,
        supportingText = field.error?.let { error -> { Text(text = stringResource(id = error)) } },
    )
}
