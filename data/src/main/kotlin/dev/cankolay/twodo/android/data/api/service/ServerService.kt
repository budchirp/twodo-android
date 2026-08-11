package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.model.response.server.VersionDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.get
import io.ktor.http.path
import javax.inject.Inject

class ServerService @Inject constructor(private val client: KtorClient) {
    suspend fun version() = request<VersionDto> {
        client().get {
            url {
                path(ApiConstants.Endpoints.SERVER_VERSION)
            }
        }
    }
}
