package dev.cankolay.twodo.android.data.api.client

import dev.cankolay.twodo.android.data.api.model.response.EmptySuccessResponse
import dev.cankolay.twodo.android.data.api.model.response.ErrorResponse
import dev.cankolay.twodo.android.data.api.model.response.SuccessResponse
import dev.cankolay.twodo.android.domain.model.api.ApiFailure
import dev.cankolay.twodo.android.domain.model.api.ApiResult
import dev.cankolay.twodo.android.domain.model.api.ErrorReason
import dev.cankolay.twodo.android.domain.model.api.FieldError
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@PublishedApi
internal suspend fun HttpResponse.errorResult(reason: ErrorReason): ApiResult.Failure {
    return try {
        val body = body<ErrorResponse>()
        ApiResult.Failure(
            error = ApiFailure(
                message = body.message.ifBlank { status.description },
                reason = reason,
                code = body.code,
                fieldErrors = validationErrors(body.details ?: body.data),
                statusCode = status.value,
            )
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ApiResult.Failure(
            error =
                ApiFailure(
                    message = status.description,
                    reason = reason,
                    statusCode = status.value,
                )
        )
    }
}

@PublishedApi
internal suspend inline fun <R> executeRequest(
    block: suspend () -> HttpResponse,
    parseSuccess: suspend (HttpResponse) -> ApiResult<R>,
): ApiResult<R> {
    return try {
        val response = block()
        val statusCode = response.status.value
        if (statusCode in 200..299) {
            try {
                parseSuccess(response)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ApiResult.Failure(
                    error = ApiFailure(
                        message = "Invalid server response",
                        reason = ErrorReason.SERVER,
                        statusCode = statusCode,
                    )
                )
            }
        } else {
            response.errorResult(
                reason = if (statusCode in 400..499) ErrorReason.CLIENT else ErrorReason.SERVER
            )
        }
    } catch (e: RedirectResponseException) {
        e.response.errorResult(reason = ErrorReason.SERVER)
    } catch (e: ClientRequestException) {
        e.response.errorResult(reason = ErrorReason.CLIENT)
    } catch (e: ServerResponseException) {
        e.response.errorResult(reason = ErrorReason.SERVER)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ApiResult.Failure(error = ApiFailure(message = e.message ?: "Unexpected error"))
    }
}

suspend inline fun <reified T> request(noinline block: suspend () -> HttpResponse): ApiResult<T> =
    executeRequest(block = block) { response ->
        val body = response.body<SuccessResponse<T>>()
        if (body.error) {
            ApiResult.Failure(
                error =
                    ApiFailure(
                        message = body.message,
                        reason = ErrorReason.CLIENT,
                        code = body.code,
                        statusCode = response.status.value,
                    )
            )
        } else {
            ApiResult.Success(data = body.data)
        }
    }

suspend fun requestUnit(block: suspend () -> HttpResponse): ApiResult<Unit> =
    executeRequest(block = block) { response ->
        val body = response.body<EmptySuccessResponse>()
        if (body.error) {
            ApiResult.Failure(
                error =
                    ApiFailure(
                        message = body.message,
                        reason = ErrorReason.CLIENT,
                        code = body.code,
                        statusCode = response.status.value,
                    )
            )
        } else {
            ApiResult.Success(data = Unit)
        }
    }

private fun validationErrors(value: JsonElement?): List<FieldError> {
    val entries = value as? JsonArray ?: return emptyList()
    return entries.mapNotNull { entry ->
        val item = entry as? JsonObject ?: return@mapNotNull null
        val field = (item["field"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
        val messages = (item["messages"] as? JsonArray)?.mapNotNull {
            (it as? JsonPrimitive)?.contentOrNull
        }.orEmpty()
        FieldError(field = field, messages = messages)
    }
}
