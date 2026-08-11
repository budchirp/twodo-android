package dev.cankolay.twodo.android.domain.model.api

enum class ErrorReason {
    CLIENT,
    SERVER,
}

sealed class ApiResult<out T> {
    data class Success<out T>(
        val message: String,
        val data: T,
        val code: String = "success"
    ) : ApiResult<T>()

    data class Error(
        val message: String,
        val reason: ErrorReason,
        val code: String = "unknown"
    ) : ApiResult<Nothing>()

    data class Fatal(val exception: Throwable) : ApiResult<Nothing>()

    data object Loading : ApiResult<Nothing>()

    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error || this is Fatal

    val dataOrNull: T? get() = (this as? Success)?.data

    val errorMessage: String?
        get() = when (this) {
            is Error -> message
            is Fatal -> exception.messageOrDefault()
            else -> null
        }

    val errorCode: String? get() = (this as? Error)?.code
}

fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(
        message = message,
        data = transform(data),
        code = code
    )

    is ApiResult.Error -> this
    is ApiResult.Fatal -> this
    is ApiResult.Loading -> ApiResult.Loading
}

inline fun <T, R> ApiResult<T>.fold(
    onLoading: () -> R,
    onSuccess: (T) -> R,
    onError: (message: String, code: String?) -> R
): R = when (this) {
    is ApiResult.Loading -> onLoading()
    is ApiResult.Success -> onSuccess(data)
    is ApiResult.Error -> onError(message, code)
    is ApiResult.Fatal -> onError(exception.messageOrDefault(), null)
}

inline fun <T> ApiResult<T>.onSuccess(action: (data: T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (message: String, code: String?) -> Unit): ApiResult<T> {
    when (this) {
        is ApiResult.Error -> action(message, code)
        is ApiResult.Fatal -> action(exception.messageOrDefault(), null)
        else -> Unit
    }
    return this
}

inline fun <T> ApiResult<T>.onFatal(action: (exception: Throwable) -> Unit): ApiResult<T> {
    if (this is ApiResult.Fatal) action(exception)
    return this
}

inline fun <T> ApiResult<T>.onLoading(action: () -> Unit): ApiResult<T> {
    if (this is ApiResult.Loading) action()
    return this
}

fun <T> ApiResult<T>.getOrDefault(default: T): T = dataOrNull ?: default

fun <T> successResult(data: T, message: String = "success"): ApiResult.Success<T> =
    ApiResult.Success(message = message, data = data)

fun Throwable.messageOrDefault(): String =
    localizedMessage ?: message ?: "Unexpected error"
