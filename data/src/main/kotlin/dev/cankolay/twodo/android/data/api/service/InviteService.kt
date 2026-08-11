package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.model.request.invite.CreateInviteRequestDto
import dev.cankolay.twodo.android.data.api.model.request.invite.HandleInviteRequestDto
import dev.cankolay.twodo.android.data.api.model.response.invite.InviteDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import dev.cankolay.twodo.android.domain.model.api.invite.InviteAction
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import javax.inject.Inject

class InviteService @Inject constructor(private val client: KtorClient) {
    suspend fun create(dto: CreateInviteRequestDto) = request<InviteDto> {
        client().post {
            url {
                path(ApiConstants.Endpoints.INVITES)
            }

            setBody(body = dto)
        }
    }

    suspend fun getAll() = request<List<InviteDto>> {
        client().get {
            url {
                path(ApiConstants.Endpoints.INVITES)
            }
        }
    }

    suspend fun handleInvite(id: String, action: InviteAction) = request(noReturn = true) {
        client().patch {
            url {
                path(ApiConstants.Endpoints.INVITES, id)
            }

            setBody(body = HandleInviteRequestDto(action = action.value))
        }
    }
}
