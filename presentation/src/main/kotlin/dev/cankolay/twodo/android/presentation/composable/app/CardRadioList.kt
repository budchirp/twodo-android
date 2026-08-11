package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> CardRadioList(
    items: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    CardStackList(
        modifier = modifier,
        items = items.map { item ->
            val onClick = { onSelected(item) }
            CardStackListItem(
                title = label(item),
                onClick = onClick,
                leadingContent = {
                    androidx.compose.material3.RadioButton(
                        selected = selected == item,
                        onClick = onClick
                    )
                }
            )
        }
    )
}
