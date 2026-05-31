package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import dev.cankolay.twodo.android.presentation.composition.LocalSnackbarHostState

@Composable
fun AppMainLayout(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
                widthDpBreakpoint = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
            )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AppNavigationRail()

                content()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                AppToolbar()

                content()
            }
        }

        SnackbarHost(
            modifier = Modifier.align(alignment = Alignment.BottomCenter),
            hostState = LocalSnackbarHostState.current
        )
    }
}