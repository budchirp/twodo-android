package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditorBottomBar(
    saveLabel: String,
    onSave: () -> Unit,
    actionsLabel: String,
    onOpenActions: () -> Unit,
    enabled: Boolean = true,
    showActions: Boolean = true,
    actionsEnabled: Boolean = enabled,
) {
    Surface(
        modifier = Modifier
            .imePadding(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onSave, enabled = enabled) {
                Text(text = saveLabel)
            }

            Spacer(modifier = Modifier.weight(weight = 1f))

            if (showActions) AppIconButton(
                icon = Icons.Default.MoreVert,
                contentDescription = actionsLabel,
                onClick = onOpenActions,
                enabled = actionsEnabled,
            )
        }
    }
}
