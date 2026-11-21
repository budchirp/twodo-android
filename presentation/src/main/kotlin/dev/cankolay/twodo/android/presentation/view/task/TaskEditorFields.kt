package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.TaskLimits
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.EditorTextInput
import dev.cankolay.twodo.android.presentation.viewmodel.task.TaskEditorState

@Composable
internal fun TaskEditorTitle(
    state: TaskEditorState,
    onTitle: (String) -> Unit,
    enabled: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val fields = (state.save as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()
    val titleError = when {
        state.validate && state.title.isBlank() -> stringResource(id = R.string.title_required)

        state.titleLength > TaskLimits.TITLE -> stringResource(id = R.string.task_title_limit)

        else -> fields.firstOrNull { it.field == "title" }?.messages?.joinToString(separator = "\n")
    }

    EditorTextInput(
        value = state.title,
        onValueChange = onTitle,
        label = stringResource(id = R.string.title),
        style = MaterialTheme.typography.headlineSmall,
        enabled = enabled,
        errorMessage = titleError,
        minLines = 1,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
    )
}

@Composable
internal fun TaskEditorDescription(
    state: TaskEditorState,
    onDescription: (String) -> Unit,
    enabled: Boolean,
) {
    val fields = (state.save as? ApiResult.Failure)?.error?.fieldErrors.orEmpty()
    val descriptionError = if (state.descriptionLength > TaskLimits.DESCRIPTION) {
        stringResource(id = R.string.task_description_limit)
    } else {
        fields.firstOrNull { it.field == "description" }?.messages?.joinToString(separator = "\n")
    }

    EditorTextInput(
        value = state.description,
        onValueChange = onDescription,
        label = stringResource(id = R.string.task_description),
        style = MaterialTheme.typography.bodyLarge,
        enabled = enabled,
        errorMessage = descriptionError,
        minLines = 1,
        maxLines = 12,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
    )
}
