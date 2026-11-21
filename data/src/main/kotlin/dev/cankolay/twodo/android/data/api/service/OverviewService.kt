package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.model.response.overview.OverviewDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path
import java.time.LocalDate
import javax.inject.Inject

class OverviewService @Inject constructor(private val client: KtorClient) {
    suspend fun get(date: LocalDate) = request<OverviewDto> {
        client().get {
            url { path(ApiConstants.Endpoints.OVERVIEW) }
            parameter(key = "date", value = date.toString())
        }
    }
}
