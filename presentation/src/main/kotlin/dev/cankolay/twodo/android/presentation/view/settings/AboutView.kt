package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.AppCard
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@Composable
fun AboutView() {
    AboutContent()
}

@OptIn(ExperimentalMaterial3Api::class)
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

    AppCard(modifier = modifier, contentPadding = PaddingValues(all = 0.dp)) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            AsyncImage(
                model = context.packageManager.getApplicationIcon(context.packageName),
                contentDescription = null,
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .size(size = 48.dp),
            )

            Column {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text =
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                            ?: "1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        SettingRow(
            title = stringResource(id = R.string.about_view_on, "Github"),
            description = stringResource(id = R.string.about_view_on_desc, "Github"),
            onClick = { handler.openUri(uri = url) },
            leadingContent = { Icon(icon = painterResource(id = R.drawable.ic_github)) },
        )
    }
}

@Composable
private fun DeveloperCard(modifier: Modifier) {
    val handler = LocalUriHandler.current
    val website = stringResource(id = R.string.dev_website)
    val github = stringResource(id = R.string.dev_github)

    SettingsGroup(
        modifier = modifier,
        items =
            listOf(
                SettingItem(
                    title = stringResource(id = R.string.dev_name),
                    description = "@" + stringResource(id = R.string.dev_username),
                    onClick = { handler.openUri(uri = website) },
                    leadingContent = { Icon(icon = Icons.Default.Person) },
                ),
                SettingItem(
                    title = stringResource(id = R.string.about_follow_on, "Github"),
                    description = stringResource(id = R.string.about_follow_on_desc, "Github"),
                    onClick = { handler.openUri(uri = github) },
                    leadingContent = { Icon(icon = painterResource(id = R.drawable.ic_github)) },
                ),
            ),
    )
}
