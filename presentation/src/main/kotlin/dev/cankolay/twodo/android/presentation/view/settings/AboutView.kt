package dev.cankolay.twodo.android.presentation.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import dev.cankolay.twodo.android.presentation.composable.app.CardStack
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.ListItem
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
            item {
                AppCard()
            }

            item {
                DevCard()
            }
        }
    }
}

@Composable
private fun AppCard() {
    val context = LocalContext.current

    val uriHandler = LocalUriHandler.current

    CardStack(
        modifier = Modifier.padding(horizontal = 16.dp),
        items =
            listOf(
                {
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
                                    context.packageManager
                                        .getPackageInfo(
                                            context.packageName,
                                            0,
                                        )
                                        ?.versionName ?: "1.0.0",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                },
                {
                    val appGithubUrl: String = stringResource(id = R.string.app_github)
                    ListItem(
                        title = stringResource(id = R.string.about_view_on, "Github"),
                        description =
                            stringResource(
                                id = R.string.about_view_on_desc,
                                "Github",
                            ),
                        onClick = {
                            uriHandler.openUri(uri = appGithubUrl)
                        },
                        leadingContent = {
                            Icon(icon = painterResource(id = R.drawable.ic_github))
                        },
                    )
                },
            ),
    )
}

@Composable
private fun DevCard() {
    val uriHandler = LocalUriHandler.current

    CardStack(
        modifier = Modifier.padding(horizontal = 16.dp),
        items =
            listOf(
                {
                    val devWebsiteUrl: String = stringResource(id = R.string.dev_website)
                    ListItem(
                        title = stringResource(id = R.string.dev_name),
                        description = "@" + stringResource(id = R.string.dev_username),
                        onClick = {
                            uriHandler.openUri(uri = devWebsiteUrl)
                        },
                        leadingContent = {
                            Icon(icon = Icons.Default.Person)
                        },
                    )
                },
                {
                    val devGithubUrl: String = stringResource(id = R.string.dev_github)
                    ListItem(
                        title = stringResource(id = R.string.about_follow_on, "Github"),
                        description =
                            stringResource(
                                id = R.string.about_follow_on_desc,
                                "Github",
                            ),
                        onClick = {
                            uriHandler.openUri(uri = devGithubUrl)
                        },
                        leadingContent = {
                            Icon(icon = painterResource(id = R.drawable.ic_github))
                        },
                    )
                },
            ),
    )
}
