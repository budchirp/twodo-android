package dev.cankolay.twodo.android.data.api.client

import dev.cankolay.twodo.android.data.api.model.response.EmptySuccessResponse
import dev.cankolay.twodo.android.data.api.model.response.ErrorResponse
import dev.cankolay.twodo.android.data.api.model.response.SuccessResponse
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

@PublishedApi
internal fun reasonForStatus(statusCode: Int) = when (statusCode) {
    in 400..499 -> ErrorReason.CLIENT
    else -> ErrorReason.SERVER
}

@PublishedApi
internal suspend fun HttpResponse.errorResult(reason: ErrorReason): ApiResult.Error {
    return try {
        val body = body<ErrorResponse>()
        ApiResult.Error(
            message = body.message,
            reason = reason,
            code = body.code
        )
    } catch (e: Exception) {
        ApiResult.Error(message = e.message ?: "Unexpected error", reason = reason)
    }
}

@PublishedApi
internal suspend inline fun <R> executeRequest(
    block: suspend () -> HttpResponse,
    parseSuccess: suspend (HttpResponse) -> ApiResult<R>
): ApiResult<R> {
    return try {
        val response = block()
        val statusCode = response.status.value
        if (statusCode in 200..299) {
            parseSuccess(response)
        } else {
            response.errorResult(reason = reasonForStatus(statusCode))
        }
    } catch (e: RedirectResponseException) {
        e.response.errorResult(reason = ErrorReason.SERVER)
    } catch (e: ClientRequestException) {
        e.response.errorResult(reason = ErrorReason.CLIENT)
    } catch (e: ServerResponseException) {
        e.response.errorResult(reason = ErrorReason.SERVER)
    } catch (e: Exception) {
        ApiResult.Fatal(exception = e)
    }
}

suspend inline fun <reified T : @Serializable Any> request(
    noinline block: suspend () -> HttpResponse
): ApiResult<T> = executeRequest(block) { response ->
    val body = response.body<SuccessResponse<T>>()
    if (body.error) {
        ApiResult.Error(message = body.message, reason = ErrorReason.CLIENT, code = body.code)
    } else {
        ApiResult.Success(message = body.message, data = body.data, code = body.code)
    }
}

suspend inline fun <reified T : @Serializable Any> requestNullable(
    noinline block: suspend () -> HttpResponse
): ApiResult<T?> = executeRequest(block) { response ->
    val body = response.body<SuccessResponse<T?>>()
    if (body.error) {
        ApiResult.Error(message = body.message, reason = ErrorReason.CLIENT, code = body.code)
    } else {
        ApiResult.Success(message = body.message, data = body.data, code = body.code)
    }
}

suspend fun request(
    noReturn: Boolean,
    block: suspend () -> HttpResponse
): ApiResult<Nothing?> = executeRequest(block) { response ->
    val body = response.body<EmptySuccessResponse>()
    if (body.error) {
        ApiResult.Error(message = body.message, reason = ErrorReason.CLIENT, code = body.code)
    } else {
        ApiResult.Success(message = body.message, data = null, code = body.code)
    }
}
