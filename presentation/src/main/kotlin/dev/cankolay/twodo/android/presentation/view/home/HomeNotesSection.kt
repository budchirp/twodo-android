package dev.cankolay.twodo.android.presentation.view.home

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import dev.cankolay.twodo.android.domain.model.api.overview.NotesOverview
import dev.cankolay.twodo.android.presentation.R
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.view.note.notesList

internal fun LazyListScope.homeNotesSection(
    data: NotesOverview,
    onNoteClick: (String) -> Unit,
    onNavigate: (Route) -> Unit,
) {
    homeHeading(key = "notes", title = R.string.latest_notes)

    notesList(
        notes = data.recentlyUpdated.take(3),
        load = null,
        onNoteClick = onNoteClick,
        onCreate = { onNavigate(Route.Notes) },
    )

    item(key = "all-notes") {
        HomeSectionLink(
            label = stringResource(id = R.string.go_to_all_notes),
            onClick = { onNavigate(Route.Notes) },
        )
    }
}
