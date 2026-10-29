package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppLazyColumn(
    modifier: Modifier = Modifier,
    fill: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 16.dp),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = if (fill) modifier.fillMaxSize() else modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}
