package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics

@Composable
fun AppLinearProgressIndicator(isLoading: Boolean, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .loadingOpacity(isLoading = isLoading)
    )
}

@Composable
fun AppCircularProgressIndicator(isLoading: Boolean, modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier.loadingOpacity(isLoading = isLoading))
}

@Composable
private fun Modifier.loadingOpacity(isLoading: Boolean): Modifier {
    val opacity by animateFloatAsState(
        targetValue = if (isLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "Loading indicator opacity",
    )

    return graphicsLayer { alpha = opacity }
        .then(other = if (isLoading) Modifier else Modifier.clearAndSetSemantics {})
}
