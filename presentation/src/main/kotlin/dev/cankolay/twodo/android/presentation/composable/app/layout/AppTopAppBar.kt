package dev.cankolay.twodo.android.presentation.composable.app.layout

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.navigation.route.getDetails

private val routes = listOf(
    Route.Welcome, Route.CoupleSetup, Route.Notes, Route.Calendar, Route.Settings
)

enum class AppTopAppBarType {
    Large, Default
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    type: AppTopAppBarType = AppTopAppBarType.Large,
    context: AppLayoutContext,
    title: (@Composable () -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    leadingContent: @Composable () -> Unit = {},
    trailingContent: @Composable () -> Unit = {},
) {
    val navBackStack = LocalNavBackStack.current

    val title = @Composable {
        if (title != null) title() else
            Text(
                text = context.route.getDetails().title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
    }

    val actions: @Composable RowScope.() -> Unit = {
        trailingContent()
    }

    val navigationIcon = @Composable {
        leadingContent()

        if (!routes.any {
                it == context.route
            }) {
            FilledIconButton(
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                onClick = {
                    if (onBackClick != null) {
                        onBackClick()
                    } else if (navBackStack.size > 1) {
                        navBackStack.removeLastOrNull()
                    }
                }) {
                Icon(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                )
            }
        }
    }

    val windowInsets = WindowInsets.systemBars.only(sides = WindowInsetsSides.Top)

    val colors =
        TopAppBarDefaults.topAppBarColors(
            containerColor = context.background,
            scrolledContainerColor = context.background
        )

    when (type) {
        AppTopAppBarType.Large -> LargeTopAppBar(
            title = title,
            actions = actions,
            navigationIcon = navigationIcon,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = context.scrollBehavior
        )

        AppTopAppBarType.Default -> TopAppBar(
            title = title,
            actions = actions,
            navigationIcon = navigationIcon,
            windowInsets = windowInsets,
            colors = colors
        )
    }
}
