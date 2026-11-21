package dev.cankolay.twodo.android.presentation.view.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import dev.cankolay.twodo.android.presentation.composable.app.AppIconButton
import dev.cankolay.twodo.android.presentation.composable.app.SettingRow
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppLayout
import dev.cankolay.twodo.android.presentation.composable.app.layout.AppRefreshableLazyColumn
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.view.note.NotesActions
import dev.cankolay.twodo.android.presentation.view.note.notesList
import dev.cankolay.twodo.android.presentation.view.task.TaskGrid
import dev.cankolay.twodo.android.presentation.view.task.TaskItemActions
import dev.cankolay.twodo.android.presentation.viewmodel.home.OverviewUiState
import dev.cankolay.twodo.android.presentation.viewmodel.home.OverviewViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.LocalDate
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
        now = now,
        actions = HomeActions(
            onRefresh = { viewModel.refresh(date = date) },
            onToggle = viewModel::toggle,
            onRetryToggle = viewModel::retryToggle,
            onNoteClick = onNoteClick,
            onNavigate = onNavigate,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: OverviewUiState,
    now: ZonedDateTime,
    actions: HomeActions,
) {
    val formattedDate = now.toLocalDate().format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
            .withLocale(LocalLocale.current.platformLocale),
    )
    val greeting = stringResource(
        id = when (now.hour) {
            in 5..11 -> R.string.home_good_morning
            in 12..16 -> R.string.home_good_afternoon
            in 17..21 -> R.string.home_good_evening
            else -> R.string.home_good_night
        },
    )

    AppLayout(route = Route.Home) {
        AppRefreshableLazyColumn(
            isLoading = state.load == ApiResult.Loading || state.toggle == ApiResult.Loading,
            onRefresh = actions.onRefresh,
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                ) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (state.load is ApiResult.Failure) {
                item {
                    ErrorCard(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        error = state.load.error.message,
                        onRefresh = actions.onRefresh,
                    )
                }
            }

            state.overview?.let { overview ->
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                }

                item {
                    HomeSectionHeader(title = R.string.latest_tasks)
                }

                if (state.toggle is ApiResult.Failure) {
                    item {
                        ErrorCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            error = state.toggle.error.message,
                            onRefresh = actions.onRetryToggle,
                        )
                    }
                }

                item {
                    if (overview.lastTasks.isEmpty()) {
                        SettingRow(
                            title = stringResource(id = R.string.tasks_empty_title),
                            onClick = { actions.onNavigate(Route.Task()) },
                        )
                    } else {
                        TaskGrid(
                            tasks = overview.lastTasks.take(4),
                            enabled = state.toggle != ApiResult.Loading,
                            actions = TaskItemActions(
                                onTaskClick = { actions.onNavigate(Route.Task(id = it.id)) },
                                onToggle = actions.onToggle,
                            ),
                        )
                    }
                }

                item {
                    HomeSectionLink(
                        description = R.string.go_to_all_tasks,
                        onClick = { actions.onNavigate(Route.Tasks) },
                    )
                }

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                }

                item {
                    HomeSectionHeader(title = R.string.latest_notes)
                }

                notesList(
                    notes = overview.notes.recentlyUpdated.take(3),
                    load = null,
                    actions = NotesActions(
                        onRefresh = actions.onRefresh,
                        onCreateClick = { actions.onNavigate(Route.Notes) },
                        onNoteClick = actions.onNoteClick,
                    ),
                )

                item {
                    HomeSectionLink(
                        description = R.string.go_to_all_notes,
                        onClick = { actions.onNavigate(Route.Notes) },
                    )
                }

                overview.period?.prediction?.takeIf { it.available }?.nextPeriodWindow?.let { window ->
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                    }

                    item {
                        HomeSectionHeader(title = R.string.overview_period_estimate)
                    }

                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "${homeDate(date = window.startDate)} – ${homeDate(date = window.endDate)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberHomeTime(): ZonedDateTime {
    var now by remember { mutableStateOf(value = ZonedDateTime.now()) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(key1 = lifecycle) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
            while (isActive) {
                val current = ZonedDateTime.now()
                now = current

                val nextMinute = current.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)
                delay(
                    timeMillis = Duration.between(current, nextMinute)
                        .toMillis()
                        .coerceAtLeast(minimumValue = 1L),
                )
            }
        }
    }

    return now
}

@Composable
private fun homeDate(date: LocalDate): String = date.format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(LocalLocale.current.platformLocale),
)

@Composable
private fun HomeSectionHeader(@StringRes title: Int) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = stringResource(id = title),
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
private fun HomeSectionLink(
    @StringRes description: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        AppIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(id = description),
            onClick = onClick,
        )
    }
}

internal data class HomeActions(
    val onRefresh: () -> Unit,
    val onToggle: (Task) -> Unit,
    val onRetryToggle: () -> Unit,
    val onNoteClick: (String) -> Unit,
    val onNavigate: (Route) -> Unit,
)
