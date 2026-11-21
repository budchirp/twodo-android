package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.ItemTransition
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun TaskCard(
    task: Task,
    enabled: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    date: LocalDate? = task.dueDate,
) {
    val style = taskCardStyle(id = task.id)

    ItemTransition(
        key = "task:${task.id}",
        detail = false,
        modifier = modifier,
        rotation = style.rotation,
    ) {
        TaskCardContent(
            task = task,
            style = style,
            date = date,
            enabled = enabled,
            onClick = onClick,
            onToggle = onToggle,
        )
    }
}

@Composable
private fun TaskCardContent(
    task: Task,
    style: TaskCardStyle,
    date: LocalDate?,
    enabled: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
) {
    val completionLabel = stringResource(
        id = if (task.completed) R.string.reopen_task else R.string.complete_task,
        task.title,
    )
    val completionState = stringResource(
        id = if (task.completed) R.string.task_completed else R.string.task_incomplete,
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(shape = MaterialTheme.shapes.large)
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Spacebar) {
                    if (enabled && event.type == KeyEventType.KeyUp) onToggle()
                    true
                } else {
                    false
                }
            }
            .combinedClickable(
                role = Role.Button,
                onClickLabel = stringResource(id = R.string.edit_task),
                onClick = onClick,
                onDoubleClick = { if (enabled) onToggle() },
            )
            .semantics {
                stateDescription = completionState
                customActions = if (enabled) {
                    listOf(
                        CustomAccessibilityAction(label = completionLabel) {
                            onToggle()
                            true
                        },
                    )
                } else {
                    emptyList()
                }
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = style.background,
            contentColor = style.foreground,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 112.dp)
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 4.dp),
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                color = style.foreground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = style.foreground.copy(alpha = 0.85f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            date?.let { due ->
                val today = LocalDate.now()
                val formatted = due.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(LocalLocale.current.platformLocale)
                )
                val label = when {
                    !task.completed && due < today -> stringResource(
                        id = R.string.task_overdue_date,
                        formatted
                    )

                    !task.completed && due == today -> stringResource(id = R.string.task_due_today)
                    else -> formatted
                }
                Text(text = label, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
