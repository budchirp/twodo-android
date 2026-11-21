package dev.cankolay.twodo.android.presentation.view.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal fun LazyListScope.homeHeading(key: String, @StringRes title: Int) {
    item(key = "$key-heading") {
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = title),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
internal fun HomeSectionLink(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        AppIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = label,
            onClick = onClick,
        )
    }
}

@Composable
internal fun homeDate(date: LocalDate): String = date.format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(LocalLocale.current.platformLocale)
)
