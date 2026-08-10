package dev.cankolay.twodo.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.motion.TransitionType
import dev.cankolay.twodo.android.presentation.motion.navigationTransition
import dev.cankolay.twodo.android.presentation.navigation.route.Route
import dev.cankolay.twodo.android.presentation.view.StartupErrorView
import dev.cankolay.twodo.android.presentation.view.calendar.CalendarView
import dev.cankolay.twodo.android.presentation.view.note.NoteView
import dev.cankolay.twodo.android.presentation.view.note.NotesView
import dev.cankolay.twodo.android.presentation.view.onboarding.CoupleSetupView
import dev.cankolay.twodo.android.presentation.view.onboarding.ProfileSetupView
import dev.cankolay.twodo.android.presentation.view.onboarding.WelcomeView
import dev.cankolay.twodo.android.presentation.view.settings.AboutView
import dev.cankolay.twodo.android.presentation.view.settings.CoupleView
import dev.cankolay.twodo.android.presentation.view.settings.LanguagesView
import dev.cankolay.twodo.android.presentation.view.settings.SettingsView
import dev.cankolay.twodo.android.presentation.view.settings.appearance.AppearanceView
import dev.cankolay.twodo.android.presentation.view.settings.appearance.MaterialYouView
import dev.cankolay.twodo.android.presentation.view.settings.appearance.ThemeView
import dev.cankolay.twodo.android.presentation.viewmodel.UserViewModel
import dev.cankolay.twodo.android.presentation.viewmodel.application.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel
) {
    val navBackStack = LocalNavBackStack.current

    NavDisplay(
        backStack = navBackStack,
        onBack = {
            if (navBackStack.size > 1) {
                navBackStack.removeLastOrNull()
            }
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Route.Welcome>(metadata = navigationTransition()) {
                WelcomeView()
            }

            entry<Route.StartupError>(metadata = navigationTransition()) {
                StartupErrorView(
                    userViewModel = userViewModel
                )
            }

            entry<Route.ProfileSetup>(metadata = navigationTransition()) {
                ProfileSetupView(
                    userViewModel = userViewModel,
                    authViewModel = authViewModel
                )
            }

            entry<Route.CoupleSetup>(metadata = navigationTransition()) {
                CoupleSetupView(
                    userViewModel = userViewModel,
                    authViewModel = authViewModel
                )
            }

            entry<Route.Notes>(metadata = navigationTransition(type = TransitionType.FADE)) {
                NotesView()
            }

            entry<Route.Note> {
                NoteView(id = it.id)
            }

            entry<Route.Calendar>(metadata = navigationTransition(type = TransitionType.FADE)) {
                CalendarView(userViewModel = userViewModel)
            }

            entry<Route.Settings>(metadata = navigationTransition(type = TransitionType.FADE)) {
                SettingsView(
                    userViewModel = userViewModel,
                    authViewModel = authViewModel
                )
            }

            entry<Route.Couple>(metadata = navigationTransition()) {
                CoupleView(userViewModel = userViewModel)
            }

            entry<Route.Languages>(metadata = navigationTransition()) {
                LanguagesView()
            }

            entry<Route.Appearance>(metadata = navigationTransition()) {
                AppearanceView()
            }

            entry<Route.Theme>(metadata = navigationTransition()) {
                ThemeView()
            }

            entry<Route.MaterialYou>(metadata = navigationTransition()) {
                MaterialYouView()
            }

            entry<Route.About>(metadata = navigationTransition()) {
                AboutView()
            }
        }
    )
}
