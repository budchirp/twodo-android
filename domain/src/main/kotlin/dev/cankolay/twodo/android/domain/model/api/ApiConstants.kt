package dev.cankolay.twodo.android.domain.model.api

object ApiConstants {
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
    const val APPLICATION_ID = "cbdbb973-95d6-41a4-b2aa-11050ce7a111"
}