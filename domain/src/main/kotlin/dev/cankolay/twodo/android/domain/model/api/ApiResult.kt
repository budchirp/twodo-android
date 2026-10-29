package dev.cankolay.twodo.android.domain.model.api

enum class ErrorReason {
    CLIENT,
    SERVER,
    TRANSPORT,
}

data class ApiFailure(
    val message: String,
    val code: String? = null,
    val reason: ErrorReason = ErrorReason.TRANSPORT,
)

sealed interface ApiResult<out T> {
    data object Loading : ApiResult<Nothing>

    data class Success<T>(val data: T) : ApiResult<T>

    data class Failure(val error: ApiFailure) : ApiResult<Nothing>
}

fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> =
    when (this) {
        ApiResult.Loading -> ApiResult.Loading

        is ApiResult.Success -> ApiResult.Success(data = transform(data))

        is ApiResult.Failure -> this
    }
