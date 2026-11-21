package dev.cankolay.twodo.android.presentation.view.task

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.task.Task

@Composable
internal fun TaskGrid(
    tasks: List<Task>,
    enabled: Boolean,
    onTaskClick: (Task) -> Unit,
    onToggle: (Task) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 300))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        tasks.chunked(2).forEach { row ->
            key(row.first().id) {
                TaskRow(
                    tasks = row,
                    enabled = enabled,
                    onTaskClick = onTaskClick,
                    onToggle = onToggle,
                )
            }
        }
    }
}

@Composable
private fun TaskRow(
    tasks: List<Task>,
    enabled: Boolean,
    onTaskClick: (Task) -> Unit,
    onToggle: (Task) -> Unit,
) {
    Row(
        modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        tasks.forEach { task ->
            key(task.id) {
                TaskCard(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .fillMaxHeight(),
                    task = task,
                    enabled = enabled,
                    onClick = { onTaskClick(task) },
                    onToggle = { onToggle(task) },
                )
            }
        }

        if (tasks.size == 1) {
            Spacer(modifier = Modifier.weight(weight = 1f))
        }
    }
}
