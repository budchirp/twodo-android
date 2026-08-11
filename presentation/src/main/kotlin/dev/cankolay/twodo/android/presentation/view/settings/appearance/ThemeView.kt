package dev.cankolay.twodo.android.presentation.view.settings.appearance

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.application.Theme
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardList
import dev.cankolay.twodo.android.presentation.composable.app.CardRadioList
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.application.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeView(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    AppLayout(route = Route.Theme) {
        uiState.settingsState?.let { state ->
            AppLazyColumn {
                item {
                    CardRadioList(
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp),
                        items = Theme.entries,
                        selected = state.theme,
                        label = { theme ->
                            stringResource(
                                id = when (theme) {
                                    Theme.SYSTEM -> R.string.system
                                    Theme.DARK -> R.string.dark
                                    Theme.LIGHT -> R.string.light
                                }
                            )
                        },
                        onSelected = { theme ->
                            settingsViewModel.updateSettings(state.copy(theme = theme))
                        }
                    )
                }

                item {
                    val onClick = { isAmoled: Boolean ->
                        settingsViewModel.updateSettings(
                            settingsState = state.copy(
                                isAmoled = isAmoled
                            )
                        )
                    }

                    CardList(
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp),
                        title = stringResource(id = R.string.amoled),
                        enabled = state.theme != Theme.LIGHT,
                        onClick = { onClick(!state.isAmoled) },
                        trailingContent = {
                            Switch(
                                checked = state.theme != Theme.LIGHT && state.isAmoled,
                                enabled = state.theme != Theme.LIGHT,
                                onCheckedChange = onClick,
                            )
                        },
                    )
                }
            }
        }
    }
}
