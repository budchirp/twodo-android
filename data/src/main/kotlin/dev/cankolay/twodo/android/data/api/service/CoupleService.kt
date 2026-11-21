package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.client.requestUnit
import dev.cankolay.twodo.android.data.api.model.response.user.UserCoupleDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.time.LocalDate
import javax.inject.Inject

class CoupleService @Inject constructor(private val client: KtorClient) {
    suspend fun get() = request<UserCoupleDto> {
        client().get {
            url { path(ApiConstants.Endpoints.COUPLE_ME) }
        }
    }

    suspend fun updateRelationshipDate(date: LocalDate?) = request<UserCoupleDto> {
        client().patch {
            url { path(ApiConstants.Endpoints.COUPLE_ME) }
            setBody(
                buildJsonObject {
                    put("relationshipStartDate", date?.toString()?.let(::JsonPrimitive) ?: JsonNull)
                }
            )
        }
    }

    suspend fun leave() = requestUnit {
        client().post {
            url {
                path(ApiConstants.Endpoints.COUPLE_LEAVE)
            }
        }
    }
}
