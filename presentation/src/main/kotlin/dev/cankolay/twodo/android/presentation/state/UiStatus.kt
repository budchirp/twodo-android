package dev.cankolay.twodo.android.presentation.state

import dev.cankolay.twodo.android.domain.model.api.ApiResult

sealed interface UiStatus {
    data object Idle : UiStatus
    data object Loading : UiStatus
    data class Success(val message: String? = null) : UiStatus
    data class Error(val message: String, val code: String? = null) : UiStatus
}

val UiStatus.isLoading: Boolean get() = this is UiStatus.Loading
val UiStatus.errorMessage: String? get() = (this as? UiStatus.Error)?.message
val UiStatus.errorCode: String? get() = (this as? UiStatus.Error)?.code

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (message: String, code: String?) -> Unit): ApiResult<T> {
    when (this) {
        is ApiResult.Error -> action(message, code)
        is ApiResult.Fatal -> action(
            exception.localizedMessage ?: exception.message ?: "Unexpected error", null
        )

        else -> Unit
    }
    return this
}
