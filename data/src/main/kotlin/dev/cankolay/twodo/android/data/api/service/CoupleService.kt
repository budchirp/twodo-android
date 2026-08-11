package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.client.requestNullable
import dev.cankolay.twodo.android.data.api.model.response.user.CoupleDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.path
import javax.inject.Inject

class CoupleService @Inject constructor(private val client: KtorClient) {
    suspend fun getMe() = requestNullable<CoupleDto> {
        client().get {
            url {
                path(ApiConstants.Endpoints.COUPLE_ME)
            }
        }
    }

    suspend fun leave() = request(noReturn = true) {
        client().post {
            url {
                path(ApiConstants.Endpoints.COUPLE_LEAVE)
            }
        }
    }
}
