package dev.cankolay.twodo.android.domain.model.api.user

import java.time.LocalDate

data class Couple(
    val users: List<User>,

    val createdAt: String,
    val id: String = createdAt,
    val relationshipStartDate: LocalDate? = null,
)
