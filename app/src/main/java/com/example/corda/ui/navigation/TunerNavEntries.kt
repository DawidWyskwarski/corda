package com.example.corda.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.tuner.TunerScreen
import com.example.corda.ui.screen.tuner.TunerViewModel
import com.example.corda.ui.screen.tuner.addtuning.AddEditTuningScreen
import com.example.corda.ui.screen.tuner.addtuning.AddEditTuningViewModel
import com.example.corda.ui.screen.tuner.settings.TunerSettingsScreen
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModel

fun EntryProviderScope<Screen>.tunerEntries(
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Tuner> {

        val tunerViewModel: TunerViewModel = hiltViewModel()

        TunerScreen(
            viewModel = tunerViewModel,
            openDrawer = openDrawer,
            openSettings = { navigateTo(Screen.TunerSettings) },
        )
    }
    entry<Screen.TunerSettings> {
        val settingsViewModel: TunerSettingsViewModel = hiltViewModel()

        TunerSettingsScreen(
            viewModel = settingsViewModel,
            onBack = navigateBack,
            onAddTuning = { navigateTo(Screen.AddEditTuning()) },
            onEditTuning = { navigateTo(Screen.AddEditTuning(it)) },
        )
    }
    entry<Screen.AddEditTuning> { screen ->
        val addEditViewModel: AddEditTuningViewModel = hiltViewModel<AddEditTuningViewModel, AddEditTuningViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(screen.tuningId)
            },
        )

        AddEditTuningScreen(
            viewModel = addEditViewModel,
            onBack = navigateBack,
        )
    }
}
