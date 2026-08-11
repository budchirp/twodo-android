package dev.cankolay.twodo.android.domain.model.api

object ApiConstants {
    const val API_URL = "192.168.1.13"
    const val API_PORT = 8081

    object Endpoints {
        const val SERVER_VERSION = "server/version"

        const val USERS = "users"
        const val USER_ME = "$USERS/me"
        const val INITIALIZE = "$USERS/initialize"

        const val COUPLES = "couples"
        const val COUPLE_ME = "$COUPLES/me"
        const val COUPLE_LEAVE = "$COUPLES/leave"

        const val INVITES = "invites"

        const val NOTES = "notes"

        const val CALENDAR = "calendar"
        const val CALENDAR_PREDICTIONS_SUMMARY = "$CALENDAR/predictions/summary"
    }
}

object AuthApiConstants {
    const val AUTH_API_URL = "http://192.168.1.13:3000"

    private const val AUTH_APPLICATION_ID = "cbdbb973-95d6-41a4-b2aa-11050ce7a111"
    const val AUTH_URL =
        "${AUTH_API_URL}/en/authorize?id=${AUTH_APPLICATION_ID}&permissions=user:read&callback=twodo://authenticate"
}
