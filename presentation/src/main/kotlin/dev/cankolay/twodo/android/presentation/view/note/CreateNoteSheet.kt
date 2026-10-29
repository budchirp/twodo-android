package dev.cankolay.twodo.android.presentation.view.note

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppBottomSheet
import dev.cankolay.twodo.android.presentation.form.FormTextField
import dev.cankolay.twodo.android.presentation.viewmodel.note.CreateNoteFormState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateNoteSheet(
    form: CreateNoteFormState,
    result: ApiResult<*>?,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCreate: () -> Unit,
) {
    val isLoading = result == ApiResult.Loading

    AppBottomSheet(
        title = stringResource(id = R.string.create_note),
        onDismiss = onDismiss,
        result = result,
        showLoading = false,
        actions = {
            Button(
                enabled = form.canSubmit && !isLoading,
                onClick = onCreate,
            ) {
                Text(text = stringResource(id = R.string.create))
            }
        },
    ) {
        item {
            FormTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                field = form.title,
                enabled = !isLoading,
                onValueChange = onTitleChange,
                label = stringResource(id = R.string.title),
                onSubmit = onCreate,
            )
        }
    }
}
