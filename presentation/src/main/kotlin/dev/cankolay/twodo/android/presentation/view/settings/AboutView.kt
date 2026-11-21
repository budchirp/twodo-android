package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppCardDefaults
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@Composable
fun AboutView() {
    AboutContent()
}

@Composable
private fun AboutContent() {
    AppLayout(route = Route.About) {
        AppLazyColumn {
            item { AppInfoCard(modifier = Modifier.padding(horizontal = 16.dp)) }

            item { DeveloperCard(modifier = Modifier.padding(horizontal = 16.dp)) }
        }
    }
}

@Composable
private fun AppInfoCard(modifier: Modifier) {
    val context = LocalContext.current
    val handler = LocalUriHandler.current
    val url = stringResource(id = R.string.app_github)

    val appIcon = remember(context) {
        context.packageManager.getApplicationIcon(context.packageName)
    }
    val version = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
    }

    SettingsGroup(
        modifier = modifier,
        items = listOf(
            SettingItem(
                title = stringResource(id = R.string.app_name),
                description = version,
                leadingContent = {
                    AsyncImage(
                        model = appIcon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(size = AppCardDefaults.ImageSize)
                            .clip(shape = CircleShape),
                    )
                },
            ),
            SettingItem(
                title = stringResource(id = R.string.about_view_on, "GitHub"),
                description = stringResource(id = R.string.about_view_on_desc, "GitHub"),
                onClick = { handler.openUri(uri = url) },
                leadingContent = { Icon(icon = painterResource(id = R.drawable.ic_github)) },
            ),
        ),
    )
}

@Composable
private fun DeveloperCard(modifier: Modifier) {
    val handler = LocalUriHandler.current
    val website = stringResource(id = R.string.dev_website)
    val github = stringResource(id = R.string.dev_github)

    SettingsGroup(
        modifier = modifier,
        items = listOf(
            SettingItem(
                title = stringResource(id = R.string.dev_name),
                description = "@" + stringResource(id = R.string.dev_username),
                onClick = { handler.openUri(uri = website) },
                leadingContent = { Icon(icon = Icons.Default.Person) },
            ),
            SettingItem(
                title = stringResource(id = R.string.about_follow_on, "GitHub"),
                description = stringResource(id = R.string.about_follow_on_desc, "GitHub"),
                onClick = { handler.openUri(uri = github) },
                leadingContent = { Icon(icon = painterResource(id = R.drawable.ic_github)) },
            ),
        ),
    )
}
