package dev.cankolay.twodo.android.data.api.service

import dev.cankolay.twodo.android.data.api.client.KtorClient
import dev.cankolay.twodo.android.data.api.client.request
import dev.cankolay.twodo.android.data.api.model.request.calendar.CalendarEntryRequestDto
import dev.cankolay.twodo.android.data.api.model.response.calendar.CalendarEntryDto
import dev.cankolay.twodo.android.domain.model.api.ApiConstants
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.path
import java.time.LocalDate
import javax.inject.Inject

class CalendarService
@Inject
constructor(private val client: KtorClient) {
    suspend fun create(dto: CalendarEntryRequestDto) = request<CalendarEntryDto> {
        client().post {
            url {
                path(ApiConstants.Endpoints.CALENDAR)
            }

            setBody(body = dto)
        }
    }

    suspend fun getRange(startDate: LocalDate? = null, endDate: LocalDate? = null) =
        request<List<CalendarEntryDto>> {
            client().get {
                url {
                    path(ApiConstants.Endpoints.CALENDAR)
                    startDate?.let { parameters.append(name = "startDate", value = it.toString()) }
                    endDate?.let { parameters.append(name = "endDate", value = it.toString()) }
                }
            }
        }

    suspend fun get(id: String) = request<CalendarEntryDto> {
        client().get {
            url {
                path(ApiConstants.Endpoints.CALENDAR, id)
            }
        }
    }

    suspend fun update(id: String, dto: CalendarEntryRequestDto) = request<CalendarEntryDto> {
        client().patch {
            url {
                path(ApiConstants.Endpoints.CALENDAR, id)
            }

            setBody(body = dto)
        }
    }

    suspend fun delete(id: String) = request(no_return = true) {
        client().delete {
            url {
                path(ApiConstants.Endpoints.CALENDAR, id)
            }
        }
    }
}
