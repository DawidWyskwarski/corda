package com.example.corda.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.tuner.TunerScreen
import com.example.corda.ui.screen.tuner.TunerViewModel
import com.example.corda.ui.screen.tuner.settings.TunerSettingsScreen
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModel
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModelFactory

fun EntryProviderScope<Screen>.tunerEntries(
    tunerViewModel: TunerViewModel,
    tunerSettingsViewModelFactory: TunerSettingsViewModelFactory,
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Tuner> {
        TunerScreen(
            viewModel = tunerViewModel,
            openDrawer = openDrawer,
            openSettings = { navigateTo(Screen.TunerSettings) }
        )
    }
    entry<Screen.TunerSettings> {
        val settingsViewModel: TunerSettingsViewModel = viewModel(
            factory = tunerSettingsViewModelFactory
        )
        TunerSettingsScreen(
            sharedViewModel = tunerViewModel,
            settingsViewModel = settingsViewModel,
            onBack = navigateBack
        )
    }
}
