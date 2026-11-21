package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowSizeClass

@Composable
fun AppMainLayout(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (
            currentWindowAdaptiveInfoV2()
                .windowSizeClass
                .isWidthAtLeastBreakpoint(
                    widthDpBreakpoint = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                )
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                AppNavigationRail()

                Box(modifier = Modifier.weight(weight = 1f)) {
                    content()
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .zIndex(zIndex = 0f)) {
                    content()
                }

                AppToolbar()
            }
        }
    }
}
