package dev.cankolay.twodo.android.domain.model.api.overview

import dev.cankolay.twodo.android.domain.model.api.calendar.PeriodSummary
import dev.cankolay.twodo.android.domain.model.api.note.Note
import dev.cankolay.twodo.android.domain.model.api.task.Task
import java.time.LocalDate

data class Overview(
    val date: LocalDate,
    val couple: OverviewCouple?,
    val lastTasks: List<Task>,
    val notes: NotesOverview,
    val period: PeriodSummary?,
)

data class OverviewCouple(
    val id: String,
    val memberIds: Set<String>,
    val relationshipStartDate: LocalDate?,
)

data class NotesOverview(val recentlyUpdated: List<Note>)
