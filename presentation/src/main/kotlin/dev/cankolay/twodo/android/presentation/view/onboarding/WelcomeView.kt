package dev.cankolay.twodo.android.presentation.view.onboarding

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.OnboardingLayout
import dev.cankolay.twodo.android.presentation.composable.app.Icon
import dev.cankolay.twodo.android.presentation.composable.app.SettingItem
import dev.cankolay.twodo.android.presentation.composable.app.SettingsGroup
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@Composable
fun WelcomeView(authUrl: String) {
    val context = LocalContext.current

    WelcomeContent(
        actions =
            WelcomeActions(
                onLogin = {
                    val intent = CustomTabsIntent.Builder().setShowTitle(true).build()

                    intent.launchUrl(context, authUrl.toUri())
                }
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomeContent(actions: WelcomeActions) {
    OnboardingLayout(
        route = Route.Welcome,
        title = stringResource(id = R.string.welcome),
        description = stringResource(id = R.string.welcome_desc),
        actions = {
            Button(modifier = Modifier.fillMaxWidth(), onClick = actions.onLogin) {
                Text(text = stringResource(id = R.string.continue_with_trash))
            }
        },
        lazyContent = {
            item {
                SettingsGroup(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items =
                        listOf(
                            SettingItem(
                                title = stringResource(id = R.string.feature_1),
                                description = stringResource(id = R.string.feature_1_desc),
                                leadingContent = { Icon(icon = Icons.Default.Check) },
                            ),
                            SettingItem(
                                title = stringResource(id = R.string.feature_2),
                                description = stringResource(id = R.string.feature_2_desc),
                                leadingContent = {
                                    Icon(
                                        icon =
                                            ImageVector.vectorResource(
                                                id = R.drawable.wand_stars_24px
                                            )
                                    )
                                },
                            ),
                            SettingItem(
                                title = stringResource(id = R.string.feature_3),
                                description = stringResource(id = R.string.feature_3_desc),
                                leadingContent = {
                                    Icon(
                                        icon =
                                            ImageVector.vectorResource(
                                                id = R.drawable.partner_heart_24px
                                            )
                                    )
                                },
                            ),
                        ),
                )
            }
        },
    )
}

internal data class WelcomeActions(val onLogin: () -> Unit)
