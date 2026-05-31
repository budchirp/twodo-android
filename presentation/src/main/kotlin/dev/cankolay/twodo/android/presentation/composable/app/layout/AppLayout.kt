package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import dev.cankolay.twodo.android.presentation.composable.rememberDeviceCornerRadius
import dev.cankolay.twodo.android.presentation.navigation.route.Route

data class AppLayoutContext @OptIn(ExperimentalMaterial3Api::class) constructor(
    val route: Route,
    val background: Color,
    val scrollBehavior: TopAppBarScrollBehavior?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLayout(
    route: Route,
    context: @Composable (default: AppLayoutContext) -> AppLayoutContext = { context ->
        context
    },
    topBar: (@Composable (context: AppLayoutContext) -> Unit) = { context ->
        AppTopAppBar(
            context = context
        )
    },
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val context = context(
        AppLayoutContext(
            route = route,
            background = MaterialTheme.colorScheme.surface,
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        )
    )

    Scaffold(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(shape = RoundedCornerShape(size = rememberDeviceCornerRadius()))
                .then(
                    other = if (context.scrollBehavior == null) Modifier else Modifier.nestedScroll(
                        connection = context.scrollBehavior.nestedScrollConnection
                    )
                ),
        topBar = {
            topBar(context)
        },
        bottomBar = { bottomBar() },
        floatingActionButton = { floatingActionButton() },
        contentWindowInsets = WindowInsets.systemBars.only(sides = WindowInsetsSides.Top),
        containerColor = context.background,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues = innerPadding),
        ) {
            content()
        }
    }
}