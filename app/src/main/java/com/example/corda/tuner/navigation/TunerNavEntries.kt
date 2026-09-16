package com.example.corda.tuner.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.core.navigation.Screen
import com.example.corda.tuner.ui.main.screen.MainTunerScreen
import com.example.corda.tuner.ui.main.screen.MainTunerViewModel
import com.example.corda.tuner.ui.update.screen.UpdateTuningScreen
import com.example.corda.tuner.ui.update.screen.UpdateTuningViewModel
import com.example.corda.tuner.ui.settings.screen.TunerSettingsViewModel
import com.example.corda.tuner.ui.settings.screen.TunerSettingsScreen

fun EntryProviderScope<Screen>.tunerEntries(
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Tuner> {

        val mainTunerViewModel: MainTunerViewModel = hiltViewModel()

        MainTunerScreen(
            viewModel = mainTunerViewModel,
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
        val addEditViewModel: UpdateTuningViewModel = hiltViewModel<UpdateTuningViewModel, UpdateTuningViewModel.Factory>(
            creationCallback = { factory ->
                factory.create(screen.tuningId)
            },
        )

        UpdateTuningScreen(
            viewModel = addEditViewModel,
            onBack = navigateBack,
        )
    }
}
