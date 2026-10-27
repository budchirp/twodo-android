package dev.cankolay.twodo.android.data.api.model.response.user

import dev.cankolay.twodo.android.domain.model.api.user.Couple
import dev.cankolay.twodo.android.domain.model.api.user.Gender
import dev.cankolay.twodo.android.domain.model.api.user.User
import kotlinx.serialization.Serializable


@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val name: String,
    val picture: String?,
    val gender: String?,
    val profileCompleted: Boolean = true,
    val couple: UserCoupleDto?
)

@Serializable
data class UserSummaryDto(
    val id: String,
    val username: String,
    val name: String,
    val picture: String?,
    val gender: String?
)

@Serializable
data class UserCoupleDto(
    val id: String,
    val users: List<UserSummaryDto>,
    val createdAt: String,
    val updatedAt: String
)

fun UserDto.toDomain() = User(
    id = id,
    username = username,
    name = name,
    picture = picture,
    gender = gender?.let { Gender.fromValue(value = it) },
    profileCompleted = profileCompleted,
    couple = couple?.toDomain()
)

fun UserSummaryDto.toDomain() = User(
    id = id,
    username = username,
    name = name,
    picture = picture,
    gender = gender?.let { Gender.fromValue(value = it) },
    profileCompleted = true,
    couple = null
)

fun UserCoupleDto.toDomain() = Couple(
    users = users.map { it.toDomain() },
    createdAt = createdAt
)
