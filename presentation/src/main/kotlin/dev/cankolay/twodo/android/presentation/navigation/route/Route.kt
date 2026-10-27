package dev.cankolay.twodo.android.presentation.navigation.route

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation3.runtime.NavKey
import dev.cankolay.twodo.android.presentation.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object StartupError : Route

    @Serializable
    data object ProfileSetup : Route

    @Serializable
    data object CoupleSetup : Route

    @Serializable
    data object Notes : Route

    @Serializable
    data class Note(val id: String) : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Couple : Route

    @Serializable
    data object Appearance : Route

    @Serializable
    data object Theme : Route

    @Serializable
    data object MaterialYou : Route

    @Serializable
    data object Languages : Route

    @Serializable
    data object About : Route
}

@Composable
fun Route.getDetails(): RouteDetail =
    when (this) {
        Route.Notes,
        is Route.Note ->
            RouteDetail(
                title = stringResource(id = R.string.notes),
                icon = RouteDetailIcon(default = Icons.Filled.Edit, outlined = Icons.Outlined.Edit),
            )

        Route.Calendar ->
            RouteDetail(
                title = stringResource(id = R.string.calendar),
                description = stringResource(id = R.string.calendar_desc),
                icon =
                    RouteDetailIcon(
                        default = Icons.Filled.CalendarMonth,
                        outlined = Icons.Outlined.CalendarMonth,
                    ),
            )

        Route.Settings ->
            RouteDetail(
                title = stringResource(id = R.string.settings),
                icon =
                    RouteDetailIcon(
                        default = Icons.Filled.Settings,
                        outlined = Icons.Outlined.Settings,
                    ),
            )

        Route.Welcome ->
            RouteDetail(
                title = stringResource(id = R.string.welcome),
                description = stringResource(id = R.string.welcome_desc),
                icon = RouteDetailIcon(default = Icons.Default.Person),
            )

        Route.StartupError ->
            RouteDetail(
                title = stringResource(id = R.string.startup_error_title),
                description = stringResource(id = R.string.startup_error_desc),
                icon = RouteDetailIcon(default = Icons.Default.CloudOff),
            )

        Route.ProfileSetup ->
            RouteDetail(
                title = stringResource(id = R.string.profile_setup),
                description = stringResource(id = R.string.profile_setup_desc),
                icon = RouteDetailIcon(default = Icons.Default.Person),
            )

        Route.CoupleSetup ->
            RouteDetail(
                title = stringResource(id = R.string.couple_setup),
                description = stringResource(id = R.string.couple_setup_desc),
                icon =
                    RouteDetailIcon(
                        default = ImageVector.vectorResource(id = R.drawable.partner_heart_24px)
                    ),
            )

        Route.Couple ->
            RouteDetail(
                title = stringResource(id = R.string.couple),
                icon =
                    RouteDetailIcon(
                        default = ImageVector.vectorResource(id = R.drawable.partner_heart_24px)
                    ),
            )

        Route.Appearance ->
            RouteDetail(
                title = stringResource(id = R.string.appearance),
                description = stringResource(id = R.string.appearance_desc),
                icon = RouteDetailIcon(default = Icons.Default.Palette),
            )

        Route.Theme ->
            RouteDetail(
                title = stringResource(id = R.string.theme),
                description = stringResource(id = R.string.theme_desc),
                icon = RouteDetailIcon(default = Icons.Default.DarkMode),
            )

        Route.MaterialYou ->
            RouteDetail(
                title = stringResource(id = R.string.material_you),
                description = stringResource(id = R.string.material_you_desc),
                icon = RouteDetailIcon(default = Icons.Default.Colorize),
            )

        Route.Languages ->
            RouteDetail(
                title = stringResource(id = R.string.languages),
                description = stringResource(id = R.string.languages_desc),
                icon = RouteDetailIcon(default = Icons.Default.Translate),
            )

        Route.About ->
            RouteDetail(
                title = stringResource(id = R.string.about),
                description = stringResource(id = R.string.about_desc),
                icon = RouteDetailIcon(default = Icons.Default.Info),
            )
    }

val onboardingRoutes =
    listOf(Route.Welcome, Route.ProfileSetup, Route.StartupError, Route.CoupleSetup)
val navigationRoutes = listOf(Route.Notes, Route.Calendar, Route.Settings)
