package dev.cankolay.twodo.android.presentation.view.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSummary
import dev.cankolay.twodo.android.presentation.R

internal fun LazyListScope.homePeriodSection(data: PeriodSummary) {
    if (!data.prediction.available) return
    val window = data.prediction.nextPeriodWindow ?: return

    homeHeading(key = "period", title = R.string.overview_period_estimate)

    item(key = "period-preview") {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = homeDate(window.startDate) + " – " + homeDate(window.endDate),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
