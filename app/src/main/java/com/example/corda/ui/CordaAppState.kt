package com.example.corda.ui

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.corda.ui.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberCordaAppState(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
): CordaAppState {
    return remember(drawerState, scope) {
        CordaAppState(drawerState, scope)
    }
}

/**
 * State holder for [CordaApp].
 *
 * Encapsulates the navigation back-stack, drawer state, and navigation
 * helpers so that [CordaApp] can focus on composing the UI tree.
 */
@Stable
class CordaAppState(
    val drawerState: DrawerState,
    private val scope: CoroutineScope,
) {
    // The default screen could be set in settings and later read from shared preferences
    val backStack = mutableStateListOf<Screen>(Screen.Tuner)

    val currentScreen: Screen
        get() = backStack.lastOrNull() ?: Screen.Tuner

    private var lastNavTime by mutableLongStateOf(0L)

    private fun canNavigate(): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastNavTime > 400) {
            lastNavTime = currentTime
            true
        } else {
            false
        }
    }

    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    /**
     * Navigates to [screen] while preventing duplicate screens
     * from piling up in the history.
     */
    fun navigateTo(screen: Screen) {
        if (!canNavigate()) return
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

    /**
     * Pops the last screen from the back-stack and closes the drawer.
     */
    fun navigateBack() {
        if (!canNavigate()) return
        scope.launch {
            drawerState.close()
            backStack.removeLastOrNull()
        }
    }

    /**
     * Leaves inspiration detail/edit and returns to the hub list.
     * Used after delete so we do not land on a detail screen for a removed item.
     */
    fun navigateBackToInspirations() {
        scope.launch {
            drawerState.close()
            while (backStack.lastOrNull() is Screen.InspirationDetail ||
                backStack.lastOrNull() is Screen.InspirationAddEdit
            ) {
                backStack.removeLastOrNull()
            }
        }
    }
}
