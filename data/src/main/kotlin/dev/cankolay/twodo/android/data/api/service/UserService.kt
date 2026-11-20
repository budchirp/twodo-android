package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.model.request.user.UpdateProfileRequestDto
import dev.cankolay.twodo.android.data.api.model.response.user.UserDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import javax.inject.Inject

class UserService @Inject constructor(private val client: KtorClient) {
    suspend fun initialize() = request<UserDto> {
        client().post {
            url {
                path(ApiConstants.Endpoints.INITIALIZE)
            }
        }
    }

    suspend fun get() = request<UserDto> {
        client().get {
            url {
                path(ApiConstants.Endpoints.USER_ME)
            }
        }
    }

    suspend fun refreshProfile() = request<UserDto> {
        client().post {
            url {
                path(ApiConstants.Endpoints.USER_PROFILE_REFRESH)
            }
        }
    }

    suspend fun updateProfile(dto: UpdateProfileRequestDto) = request<UserDto> {
        client().patch {
            url {
                path(ApiConstants.Endpoints.USER_ME, "profile")
            }

            setBody(body = dto)
        }
    }
}
