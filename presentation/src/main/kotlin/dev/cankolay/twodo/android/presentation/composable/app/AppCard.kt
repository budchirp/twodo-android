package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object AppCardDefaults {
    val Spacing = 16.dp
    val TextSpacing = 2.dp
    val GroupSpacing = 2.dp
    val ImageSize = 48.dp
    val ContentPadding = PaddingValues(all = Spacing)
    val NoPadding = PaddingValues(all = 0.dp)
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentPadding: PaddingValues = AppCardDefaults.ContentPadding,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(space = AppCardDefaults.Spacing),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = shape)
                .then(
                    other = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
                ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues = contentPadding),
            verticalArrangement = verticalArrangement,
        ) {
            content()
        }
    }
}
