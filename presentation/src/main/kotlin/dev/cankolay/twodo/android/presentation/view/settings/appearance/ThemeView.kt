package dev.cankolay.twodo.android.presentation.view.settings.appearance

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.cankolay.twodo.android.domain.model.application.SettingsState
import dev.cankolay.twodo.android.domain.model.application.Theme
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.CardRadioList
import dev.cankolay.twodo.android.presentation.composable.app.SwitchCard
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.application.SettingsViewModel

@Composable
fun ThemeView(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    ThemeContent(state = uiState.settings, onChange = settingsViewModel::update)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeContent(state: SettingsState, onChange: (SettingsState) -> Unit) {
    AppLayout(route = Route.Theme) {
        AppLazyColumn {
            item {
                CardRadioList(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = Theme.entries,
                    selected = state.theme,
                    label = { theme ->
                        stringResource(
                            id =
                                when (theme) {
                                    Theme.SYSTEM -> R.string.system

                                    Theme.DARK -> R.string.dark

                                    Theme.LIGHT -> R.string.light
                                }
                        )
                    },
                    onSelected = { theme ->
                        onChange(state.copy(theme = theme))
                    },
                )
            }

            item {
                val onClick = { isAmoled: Boolean ->
                    onChange(state.copy(isAmoled = isAmoled))
                }

                SwitchCard(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(id = R.string.amoled),
                    enabled = state.theme != Theme.LIGHT,
                    checked = state.theme != Theme.LIGHT && state.isAmoled,
                    onCheckedChange = onClick,
                )
            }
        }
    }
}
