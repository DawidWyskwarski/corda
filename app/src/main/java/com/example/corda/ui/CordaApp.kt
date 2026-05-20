package com.example.corda.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.corda.ui.components.DrawerMenuContent
import com.example.corda.ui.navigation.inspirationsEntries
import com.example.corda.ui.navigation.metronomeEntries
import com.example.corda.ui.navigation.tunerEntries
import com.example.corda.ui.navigation.utilityEntries

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
    activity: ComponentActivity,
) {
    val appState = rememberCordaAppState()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = appState.drawerState,
        // Disable the 'swipe-to-open' gesture unless the drawer is already open.
        // you can close it by swiping/clicking away,
        // but you can't swipe to open it
        gesturesEnabled = appState.drawerState.isOpen,
        drawerContent = {
            DrawerMenuContent(
                currentScreen = appState.currentScreen,
                onScreenSelected = appState::navigateTo
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavDisplay(
                backStack = appState.backStack,
                onBack = appState::navigateBack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    tunerEntries(
                        activity = activity,
                        openDrawer = appState::openDrawer,
                        navigateTo = appState::navigateTo,
                        navigateBack = appState::navigateBack,
                    )
                    metronomeEntries(
                        openDrawer = appState::openDrawer,
                        navigateTo = appState::navigateTo,
                        navigateBack = appState::navigateBack
                    )
                    inspirationsEntries(
                        openDrawer = appState::openDrawer,
                        navigateTo = appState::navigateTo,
                        navigateBack = appState::navigateBack,
                    )
                    utilityEntries(
                        activity = activity,
                        navigateBack = appState::navigateBack,
                    )
                }
            )
        }
    }
}
