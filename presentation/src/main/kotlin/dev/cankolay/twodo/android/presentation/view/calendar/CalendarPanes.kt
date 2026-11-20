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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.ApiFailure
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.AppLinearProgressIndicator
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayoutContext
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPane
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPaneDefaults
import dev.cankolay.twodo.android.presentation.composable.app.layout.currentPaneDirective
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
@Composable
internal fun CalendarPanes(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    topBar: @Composable (AppLayoutContext) -> Unit,
    error: ApiFailure?,
    month: @Composable () -> Unit,
    entries: (LazyListScope.() -> Unit)?,
) {
    val isExpanded = currentPaneDirective().maxHorizontalPartitions > 1

    val calendar: @Composable () -> Unit = {
        AppLayout(route = Route.Calendar, topBar = topBar) {
            AppLinearProgressIndicator(isLoading = isLoading)

            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                indicator = {},
            ) {
                AppLazyColumn {
                    if (!isExpanded && error != null) {
                        item {
                            ErrorCard(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                error = error.message,
                                onRefresh = onRefresh,
                            )
                        }
                    }
                    item(content = { month() })

                    if (!isExpanded) entries?.invoke(this)
                }
            }
        }
    }

    if (isExpanded) {
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
                    AppLazyColumn(modifier = Modifier.weight(weight = 1f)) {
                        if (error != null) {
                            item {
                                ErrorCard(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    error = error.message,
                                    onRefresh = onRefresh,
                                )
                            }
                        }
                        entries?.invoke(this)
                    }
                }
            }
        }
    } else {
        calendar()
    }
}
