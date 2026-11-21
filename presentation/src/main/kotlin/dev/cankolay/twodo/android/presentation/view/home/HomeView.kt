package dev.cankolay.twodo.android.presentation.view.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.task.Task
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.composable.ErrorCard
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppRefreshableLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.viewmodel.home.OverviewUiState
import dev.cankolay.twodo.android.presentation.viewmodel.home.OverviewViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

@Composable
fun HomeView(
    onNoteClick: (String) -> Unit,
    onNavigate: (Route) -> Unit,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val now = rememberHomeTime()
    val date = now.toLocalDate()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        .withLocale(LocalLocale.current.platformLocale)
    val formattedDate = date.format(formatter)
    val greeting = stringResource(
        id = when (now.hour) {
            in 5..11 -> R.string.home_good_morning
            in 12..16 -> R.string.home_good_afternoon
            in 17..21 -> R.string.home_good_evening
            else -> R.string.home_good_night
        },
    )

    LaunchedEffect(lifecycle, date) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            viewModel.refresh(date = date)
            try {
                awaitCancellation()
            } finally {
                viewModel.pause()
            }
        }
    }

    HomeContent(
        state = state,
        greeting = greeting,
        date = formattedDate,
        onRefresh = { viewModel.refresh(date = date) },
        onToggle = viewModel::toggle,
        onRetryToggle = viewModel::retryToggle,
        onNoteClick = onNoteClick,
        onNavigate = onNavigate,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: OverviewUiState,
    greeting: String,
    date: String,
    onRefresh: () -> Unit,
    onToggle: (Task) -> Unit,
    onRetryToggle: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNavigate: (Route) -> Unit,
) {
    AppLayout(route = Route.Home) {
        AppRefreshableLazyColumn(
            isLoading = state.load == ApiResult.Loading || state.toggle == ApiResult.Loading,
            onRefresh = onRefresh,
        ) {
            item(key = "welcome") {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                ) {
                    Text(text = greeting, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val load = state.load
            if (load is ApiResult.Failure) {
                item(key = "overview-error") {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = load.error.message,
                        onRefresh = onRefresh
                    )
                }
            }

            state.overview?.let { data ->
                homeTasksSection(
                    tasks = data.lastTasks,
                    mutation = state.toggle,
                    onRetryToggle = onRetryToggle,
                    onToggle = onToggle,
                    onNavigate = onNavigate,
                )
                homeNotesSection(
                    data = data.notes,
                    onNoteClick = onNoteClick,
                    onNavigate = onNavigate
                )
                data.period?.let { homePeriodSection(data = it) }
            }
        }
    }
}

@Composable
private fun rememberHomeTime(): ZonedDateTime {
    var now by remember { mutableStateOf(value = ZonedDateTime.now()) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            while (isActive) {
                val current = ZonedDateTime.now()
                now = current

                val nextMinute = current.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)
                delay(
                    timeMillis = Duration.between(current, nextMinute).toMillis().coerceAtLeast(1L)
                )
            }
        }
    }

    return now
}
