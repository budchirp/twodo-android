package dev.cankolay.twodo.android.data.api.model.response.overview

import dev.cankolay.twodo.android.data.api.model.response.calendar.PeriodSummaryDto
import dev.cankolay.twodo.android.data.api.model.response.calendar.toDomain
import dev.cankolay.twodo.android.data.api.model.response.note.NoteDto
import dev.cankolay.twodo.android.data.api.model.response.note.toDomain
import dev.cankolay.twodo.android.data.api.model.response.task.TaskDto
import dev.cankolay.twodo.android.data.api.model.response.task.toDomain
import dev.cankolay.twodo.android.domain.model.api.overview.NotesOverview
import dev.cankolay.twodo.android.domain.model.api.overview.Overview
import dev.cankolay.twodo.android.domain.model.api.overview.OverviewCouple
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class OverviewDto(
    val date: String,
    val couple: OverviewCoupleDto?,
    val tasks: OverviewTasksDto,
    val notes: NotesOverviewDto,
    val period: PeriodSummaryDto?,
)

@Serializable
data class NotesOverviewDto(val recentlyUpdated: List<NoteDto>)

@Serializable
data class OverviewTasksDto(val lastTasks: List<TaskDto>)

@Serializable
data class OverviewCoupleDto(
    val id: String,
    val users: List<OverviewMemberDto>,
    val relationshipStartDate: String? = null,
)

@Serializable
data class OverviewMemberDto(val id: String)

fun OverviewDto.toDomain() = Overview(
    date = LocalDate.parse(date),
    lastTasks = tasks.lastTasks.map { it.toDomain() },
    couple = couple?.let {
        OverviewCouple(
            id = it.id,
            memberIds = it.users.map { member -> member.id }.toSet(),
            relationshipStartDate = it.relationshipStartDate?.let(LocalDate::parse),
        )
    },
    notes = NotesOverview(recentlyUpdated = notes.recentlyUpdated.map { it.toDomain() }),
    period = period?.toDomain(),
)
