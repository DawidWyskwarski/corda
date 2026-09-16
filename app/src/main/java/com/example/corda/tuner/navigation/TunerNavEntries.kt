package com.example.corda.tuner.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.core.navigation.Screen
import com.example.corda.tuner.ui.main.screen.MainTunerScreen
import com.example.corda.tuner.ui.update.screen.UpdateTuningScreen
import com.example.corda.tuner.ui.update.screen.UpdateTuningViewModel
import com.example.corda.tuner.ui.settings.screen.TunerSettingsScreen

fun EntryProviderScope<Screen>.tunerEntries(
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Tuner> {
        MainTunerScreen(
            openDrawer = openDrawer,
            openSettings = { navigateTo(Screen.TunerSettings) },
        )
    }
    entry<Screen.TunerSettings> {
        TunerSettingsScreen(
            onBack = navigateBack,
            onAddTuning = { navigateTo(Screen.AddEditTuning()) },
            onEditTuning = { navigateTo(Screen.AddEditTuning(it)) },
        )
    }
    entry<Screen.AddEditTuning> { screen ->
        UpdateTuningScreen(
            onBack = navigateBack,
            viewModel = hiltViewModel<UpdateTuningViewModel, UpdateTuningViewModel.Factory>(
                key = screen.tuningId.toString(),
            ) { factory ->
                factory.create(screen.tuningId)
            }
        )
    }
}
