package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
fun <T> RadioOptions(
    items: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SettingsGroup(
        modifier = modifier.selectableGroup(),
        items =
            items.map { item ->
                val onClick = { onSelected(item) }
                SettingItem(
                    title = label(item),
                    modifier =
                        Modifier.selectable(
                            selected = selected == item,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = onClick,
                        ),
                    leadingContent = {
                        androidx.compose.material3.RadioButton(
                            selected = selected == item,
                            onClick = null,
                            enabled = enabled,
                        )
                    },
                )
            },
    )
}
