package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MarkdownEditor(
    markdown: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    onContentChange: ((String) -> Unit)? = null
) {
    val state = rememberRichTextState()
    val latestOnContentChange by rememberUpdatedState(newValue = onContentChange)

    state.config.linkColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(
        readOnly,
        markdown.takeIf { readOnly }
    ) {
        state.setMarkdown(markdown = markdown)
        if (!readOnly) {
            state.selection = TextRange(index = markdown.length)
        }
    }

    LaunchedEffect(state, readOnly, onContentChange != null) {
        if (readOnly || onContentChange == null) return@LaunchedEffect

        snapshotFlow { state.toMarkdown() }
            .collectLatest { content ->
                latestOnContentChange?.invoke(content)
            }
    }

    BasicRichTextEditor(
        state = state,
        modifier = modifier,
        readOnly = readOnly,
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary)
    )
}
