package dev.cankolay.twodo.android.presentation.view.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.cankolay.twodo.android.presentation.composable.app.PullToRefreshLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayoutContext
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPane
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPaneDefaults
import dev.cankolay.twodo.android.presentation.composable.app.layout.currentPaneDirective
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun CalendarPanes(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    topBar: @Composable (AppLayoutContext) -> Unit,
    status: @Composable () -> Unit,
    error: @Composable () -> Unit,
    month: @Composable () -> Unit,
    entries: (LazyListScope.() -> Unit)?,
) {
    val isExpanded = currentPaneDirective().maxHorizontalPartitions > 1

    val calendar: @Composable () -> Unit = {
        AppLayout(route = Route.Calendar, topBar = topBar) {
            status()

            if (!isExpanded) error()

            PullToRefreshLazyColumn(isLoading = isLoading, onRefresh = onRefresh) {
                item(content = { month() })

                if (!isExpanded) entries?.invoke(this)
            }
        }
    }

    when {
        isExpanded -> {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surfaceContainer),
                horizontalArrangement = Arrangement.spacedBy(space = AppPaneDefaults.Spacing),
            ) {
                Box(modifier = Modifier.weight(weight = 1f), content = { calendar() })

                AppPane(
                    modifier = Modifier
                        .width(width = AppPaneDefaults.Width)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .windowInsetsPadding(
                                    insets =
                                        WindowInsets.systemBars.only(sides = WindowInsetsSides.Top)
                                )
                    ) {
                        error()

                        AppLazyColumn(modifier = Modifier.weight(weight = 1f)) {
                            entries?.invoke(this)
                        }
                    }
                }
            }
        }

        else -> calendar()
    }
}
