package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRefreshableLazyColumn(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val hasNavigationRail = currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
        widthDpBreakpoint = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND,
    )

    Column(modifier = modifier.fillMaxSize()) {
        AppLinearProgressIndicator(isLoading = isLoading)

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            indicator = {},
        ) {
            AppLazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = if (hasNavigationRail) 16.dp else 112.dp,
                ),
                content = content,
            )
        }
    }
}
