package dev.cankolay.twodo.android.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.cankolay.twodo.android.presentation.composable.app.PullToRefreshLazyColumn
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@Composable
fun OnboardingLayout(
    route: Route,
    title: String,
    description: String,
    isLoading: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    lazyContent: LazyListScope.() -> Unit,
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current

    AppLayout(
        route = route,
        topBar = {},
        bottomBar = if (actions != null) {
            {
                NavigationBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        actions()
                    }
                }
            }
        } else {
            {}
        }
    ) {
        PullToRefreshLazyColumn(
            isLoading = isLoading,
            onRefresh = onRefresh,
            contentPadding = PaddingValues(top = 64.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 32.dp)
                ) {
                    AsyncImage(
                        model = context.packageManager.getApplicationIcon(context.packageName),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(size = 64.dp)
                                .clip(shape = CircleShape),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Text(
                            text = description,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            lazyContent()
        }

        content()
    }
}