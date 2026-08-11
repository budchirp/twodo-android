package dev.cankolay.twodo.android.domain.model.application

enum class EnvironmentType {
    DEBUG,
    RELEASE
}

data class EnvironmentConfig(
    val type: EnvironmentType,
    val apiUrl: String,
    val authUrl: String
)
