package com.example.corda.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.tuner.TunerScreen
import com.example.corda.ui.screen.tuner.TunerViewModel
import com.example.corda.ui.screen.tuner.addtuning.AddEditTuningScreen
import com.example.corda.ui.screen.tuner.addtuning.AddEditTuningViewModel
import com.example.corda.ui.screen.tuner.settings.TunerSettingsScreen
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModel

@SuppressLint("ContextCastToActivity")
fun EntryProviderScope<Screen>.tunerEntries(
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Tuner> {
        val activity = LocalContext.current as ComponentActivity

        // Activity-scoped so Tuner and TunerSettings share one TunerViewModel (selected tuning, mode).
        val tunerViewModel: TunerViewModel = hiltViewModel(viewModelStoreOwner = activity)

        TunerScreen(
            viewModel = tunerViewModel,
            openDrawer = openDrawer,
            openSettings = { navigateTo(Screen.TunerSettings) },
        )
    }
    entry<Screen.TunerSettings> {
        val activity = LocalContext.current as ComponentActivity

        // Same activity store as Tuner so selection/mode survive navigating to settings and back.
        val tunerViewModel: TunerViewModel = hiltViewModel(viewModelStoreOwner = activity)

        val settingsViewModel: TunerSettingsViewModel = hiltViewModel()

        TunerSettingsScreen(
            sharedViewModel = tunerViewModel,
            settingsViewModel = settingsViewModel,
            onBack = navigateBack,
            onAddTuning = { navigateTo(Screen.AddEditTuning()) },
            onEditTuning = { tuningId -> navigateTo(Screen.AddEditTuning(tuningId)) },
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
