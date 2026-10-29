package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role

@Composable
fun SwitchSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    SettingCard(
        modifier =
            modifier
                .clip(shape = MaterialTheme.shapes.large)
                .toggleable(
                    value = checked,
                    enabled = enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ),
        title = title,
        description = description,
        color = color,
        trailingContent = { Switch(checked = checked, enabled = enabled, onCheckedChange = null) },
    )
}
