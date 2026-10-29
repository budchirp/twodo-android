package dev.cankolay.twodo.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.cankolay.twodo.android.presentation.composition.LocalNavBackStack
import dev.cankolay.twodo.android.presentation.composition.LocalNotesLayout
import dev.cankolay.twodo.android.presentation.motion.TransitionType
import dev.cankolay.twodo.android.presentation.motion.navigationTransition
import dev.cankolay.twodo.android.presentation.navigation.notes.noteDetailMetadata
import dev.cankolay.twodo.android.presentation.navigation.notes.notesListMetadata
import dev.cankolay.twodo.android.presentation.navigation.route.Route
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

@Composable
fun AppNavigation(
    authUrl: String,
    onLogout: () -> Unit,
    startup: @Composable () -> Unit,
) {
    val navBackStack = LocalNavBackStack.current

    val layout = requireNotNull(value = LocalNotesLayout.current)

    val notes = layout.navigation

    NavDisplay(
        backStack = navBackStack,
        sceneStrategies = listOf(element = layout.strategy),
        sharedTransitionScope = layout.transition,
        onBack = {
            if (navBackStack.size > 1) {
                navBackStack.removeLastOrNull()
            }
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<Route.Welcome>(metadata = navigationTransition()) {
                    WelcomeView(authUrl = authUrl)
                }

                entry<Route.StartupError>(metadata = navigationTransition()) {
                    startup()
                }

                entry<Route.ProfileSetup>(metadata = navigationTransition()) {
                    ProfileSetupView()
                }

                entry<Route.CoupleSetup>(metadata = navigationTransition()) {
                    CoupleSetupView(onLogout = onLogout)
                }

                entry<Route.Notes>(metadata = notesListMetadata()) {
                    NotesView(onNoteClick = notes::select)
                }

                entry<Route.Note>(metadata = noteDetailMetadata()) { note ->
                    NoteView(
                        id = note.id,
                        onBack = { if (notes.id == note.id) notes.close() },
                    )
                }

                entry<Route.Calendar>(metadata = navigationTransition(type = TransitionType.FADE)) {
                    CalendarView()
                }

                entry<Route.Settings>(metadata = navigationTransition(type = TransitionType.FADE)) {
                    SettingsView(
                        onLogout = onLogout,
                        onNavigate = { route -> navBackStack.add(element = route) },
                    )
                }

                entry<Route.Couple>(metadata = navigationTransition()) {
                    CoupleView()
                }

                entry<Route.Languages>(metadata = navigationTransition()) {
                    LanguagesView()
                }

                entry<Route.Appearance>(metadata = navigationTransition()) {
                    AppearanceView(onNavigate = { route -> navBackStack.add(element = route) })
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
            },
    )
}
