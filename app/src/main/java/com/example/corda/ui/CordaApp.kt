package com.example.corda.ui

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.corda.ui.components.DrawerMenuContent
import com.example.corda.ui.navigation.Screen
import com.example.corda.ui.screen.help.HelpAndFeedbackScreen
import com.example.corda.ui.screen.inspirations.InspirationsScreen
import com.example.corda.ui.screen.metronome.MetronomeScreen
import com.example.corda.ui.screen.metronome.settings.MetronomeSettingsScreen
import com.example.corda.ui.screen.settings.SettingsScreen
import com.example.corda.ui.screen.settings.SettingsViewModel
import com.example.corda.ui.screen.tuner.TunerScreen
import com.example.corda.ui.screen.tuner.TunerViewModel
import com.example.corda.ui.screen.tuner.settings.TunerSettingsScreen
import kotlinx.coroutines.launch

/**
 * `CordaApp` - the root UI component of the app
 *
 * Mainly orchestrates the navigation between screens and the drawer menu.
 *
 * ### TODO
 * - fix the transition animations. right now if i quickly double click on the back icon in settings the app goes back twice. (Need to block navigation after the click or something)
 */
@Composable
fun CordaApp(
    modifier: Modifier = Modifier,
    tunerViewModel: TunerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    // The default screen could be set in settings and later read from shared preferences
    // or something like that, so the user can choose which screen to start with
    val backStack = remember { mutableStateListOf<Screen>(Screen.Tuner) }

    /**
     * Some UI changes (like sliding a drawer) take some time
     * and cannot be called directly on the main UI thread.
     * They need to be launched inside a [kotlinx.coroutines.CoroutineScope]
     */
    val scope = rememberCoroutineScope()

    /**
     * Holds a state whether a drawer is opened or closed
     */
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val openDrawer: () -> Unit = {
        scope.launch {
            drawerState.open()
        }
    }

    var lastNavTime by remember { mutableLongStateOf(0L) }

    // Helper to debounce rapid clicks
    val canNavigate = {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavTime > 400) { // 400ms debounce window
            lastNavTime = currentTime
            true
        } else {
            false
        }
    }

    /**
     * Custom Navigation Logic
     * This function handles moving between main screens while preventing
     * duplicate screens from piling up in the history.
     */
    val navigateTo: (Screen) -> Unit = { screen ->
        // Prevent double-clicking on navigation items too
        if (canNavigate()) {
            scope.launch {
                if (backStack.lastOrNull() == screen) {
                    drawerState.close()
                    return@launch
                }

                if (backStack.first() == screen) {
                    backStack.clear()
                }

                if (backStack.find { it == screen } != null) {
                    backStack.remove(screen)
                }

                backStack.add(screen)
                drawerState.close()
            }
        }
    }

    /**
     * Pops the last screen from the backstack and closes the drawer
     */
    val navigateBack: () -> Unit = {
        if (canNavigate()) {
            scope.launch {
                drawerState.close()
                backStack.removeLastOrNull()
            }
        }
    }

    // Composable that provides the slide-out menu layout
    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        // Disable the 'swipe-to-open' gesture unless the drawer is already open.
        // you can close it by swiping/clicking away,
        // but you can't swipe to open it
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerMenuContent(
                currentScreen = backStack.lastOrNull() ?: Screen.Tuner,
                onScreenSelected = navigateTo
            )
        }
    ) {
        // Composable that renders the current screen based on the backstack.
        // entryProvider maps the Screen data object/class to a Composable function.
        // For more information check out the Navigation3 documentation.
        NavDisplay(
            backStack = backStack,
            onBack = navigateBack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<Screen.Tuner> {
                    TunerScreen(
                        viewModel = tunerViewModel,
                        openDrawer = openDrawer,
                        openSettings = { navigateTo(Screen.TunerSettings) }
                    )
                }
                entry<Screen.TunerSettings> {
                    TunerSettingsScreen(
                        viewModel = tunerViewModel,
                        onBack = navigateBack
                    )
                }
                entry<Screen.Metronome> {
                    MetronomeScreen(
                        openDrawer = openDrawer,
                        openSettings = { navigateTo(Screen.MetronomeSettings) }
                    )
                }
                entry<Screen.MetronomeSettings> {
                    MetronomeSettingsScreen(
                        onBack = navigateBack
                    )
                }
                entry<Screen.Inspirations> {
                    InspirationsScreen(
                        openDrawer = openDrawer
                    )
                }
                //TODO add inspiration details/edit/add screen,
                // it will be a little more complicated
                // they all will share the same layout
                // and i don't want to implement it right now
                entry<Screen.Settings> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = navigateBack
                    )
                }
                entry<Screen.Help> {
                    HelpAndFeedbackScreen(
                        onBack = navigateBack
                    )
                }
            }
        )
    }
}