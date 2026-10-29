package dev.cankolay.twodo.android.presentation.composable.app

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PullToRefreshLazyColumn(
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null,
    isLoading: Boolean = false,
    content: LazyListScope.() -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = modifier,
        enabled = onRefresh != null,
        onRefresh = onRefresh ?: {},
        isRefreshing = isLoading,
        state = pullToRefreshState,
        indicator = {
            if (!isLoading && pullToRefreshState.distanceFraction > 0f) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
    ) {
        AppLazyColumn(content = content)
    }
}
