package dev.cankolay.twodo.android.domain.model.api

object ApiConstants {
    object Endpoints {
        const val USERS = "users"
        const val USER_ME = "$USERS/me"
        const val USER_PROFILE_REFRESH = "$USER_ME/profile/refresh"
        const val INITIALIZE = "$USERS/initialize"

        const val COUPLES = "couples"
        const val COUPLE_LEAVE = "$COUPLES/leave"

        const val INVITES = "invites"

        const val NOTES = "notes"
        const val TASKS = "tasks"
        const val OVERVIEW = "overview"
        const val COUPLE_ME = "$COUPLES/me"

        const val CALENDAR = "calendar"
        const val PERIODS = "periods"
    }
}

object EnvironmentApiConstants {
    object Debug {
        const val API_URL = "http://192.168.1.13:8081"
        const val AUTH_URL = "http://192.168.1.13:3000"
    }

    object Release {
        const val API_URL = "https://twodo-api.cankolay.dev"
        const val AUTH_URL = "https://trash.cankolay.dev"
    }
}

object AuthApiConstants {
    const val APPLICATION_ID = "8e364c21-9337-4fa7-9820-86f7551bbfc9"
    const val AUTHORIZE_PATH = "/en/authorize"
    const val PERMISSIONS = "user:read"
    const val CALLBACK_URI = "twodo://authenticate"
}
