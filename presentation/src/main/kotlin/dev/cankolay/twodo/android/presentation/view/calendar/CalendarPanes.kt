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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import dev.cankolay.twodo.android.presentation.composable.app.PullToRefreshLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayoutContext
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPane
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppPaneDefaults
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarPanes(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    topBar: @Composable (AppLayoutContext) -> Unit,
    status: @Composable () -> Unit,
    error: @Composable () -> Unit,
    month: @Composable () -> Unit,
    entries: (@Composable () -> Unit)?,
) {
    val isExpanded =
        currentWindowAdaptiveInfoV2()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(
                widthDpBreakpoint = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
            )

    val calendar: @Composable () -> Unit = {
        AppLayout(route = Route.Calendar, topBar = topBar) {
            status()

            if (!isExpanded) error()

            PullToRefreshLazyColumn(isLoading = isLoading, onRefresh = onRefresh) {
                item(content = { month() })

                if (!isExpanded && entries != null) {
                    item(content = { entries() })
                }
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
                            if (entries != null) {
                                item(content = { entries() })
                            }
                        }
                    }
                }
            }
        }

        else -> calendar()
    }
}
