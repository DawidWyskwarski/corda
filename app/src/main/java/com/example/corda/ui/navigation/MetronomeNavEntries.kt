package com.example.corda.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.metronome.MetronomeScreen
import com.example.corda.ui.screen.metronome.settings.MetronomeSettingsScreen

fun EntryProviderScope<Screen>.metronomeEntries(
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
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
}
