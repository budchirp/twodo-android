package dev.cankolay.twodo.android.presentation.view.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.app.RadioOptions
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route

@Composable
fun LanguagesView() {
    val currentLanguage =
        AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "system" }

    LanguagesContent(
        language = currentLanguage,
        onChange = { language ->
            AppCompatDelegate.setApplicationLocales(
                if (language == "system") LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(language)
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagesContent(language: String, onChange: (String) -> Unit) {
    AppLayout(route = Route.Languages) {
        AppLazyColumn {
            item {
                val languages =
                    mapOf(
                        "system" to R.string.system,
                        "en" to R.string.en,
                        "tr" to R.string.tr,
                    )

                RadioOptions(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    items = languages.keys.toList(),
                    selected =
                        languages.keys.firstOrNull { value ->
                            language.contains(value, ignoreCase = true)
                        },
                    label = { language -> stringResource(id = languages.getValue(language)) },
                    onSelected = onChange,
                )
            }
        }
    }
}
