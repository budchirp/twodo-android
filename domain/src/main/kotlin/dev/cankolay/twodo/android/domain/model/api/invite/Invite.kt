package dev.cankolay.twodo.android.domain.model.api.invite

import dev.cankolay.twodo.android.domain.model.api.user.User

data class Invite(
    val id: String,

    val user: User,

    val type: InviteType,
    val status: InviteStatus,

    val createdAt: String,
)
