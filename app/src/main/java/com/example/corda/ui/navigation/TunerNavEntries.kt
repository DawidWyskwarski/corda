package com.example.corda.ui.navigation

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.data.tuner.repository.TunerRepository
import com.example.corda.ui.screen.tuner.TunerScreen
import com.example.corda.ui.screen.tuner.TunerViewModel
import com.example.corda.ui.screen.tuner.TunerViewModelFactory
import com.example.corda.ui.screen.tuner.addtuning.AddEditTuningScreen
import com.example.corda.ui.screen.tuner.addtuning.AddEditTuningViewModel
import com.example.corda.ui.screen.tuner.addtuning.AddEditTuningViewModelFactory
import com.example.corda.ui.screen.tuner.settings.TunerSettingsScreen
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModel
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModelFactory

@SuppressLint("ContextCastToActivity")
fun EntryProviderScope<Screen>.tunerEntries(
    tunerRepository: TunerRepository,
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Tuner> {
        val activity = LocalContext.current as ComponentActivity

        val tunerViewModel: TunerViewModel = viewModel(
            viewModelStoreOwner = activity,
            factory = TunerViewModelFactory(tunerRepository)
        )

        TunerScreen(
            viewModel = tunerViewModel,
            openDrawer = openDrawer,   
            openSettings = { navigateTo(Screen.TunerSettings) }
        )
    }
    entry<Screen.TunerSettings> {
        val activity = LocalContext.current as ComponentActivity

        val tunerViewModel: TunerViewModel = viewModel(
            viewModelStoreOwner = activity,
            factory = TunerViewModelFactory(tunerRepository)
        )

        val settingsViewModel: TunerSettingsViewModel = viewModel(
            factory = TunerSettingsViewModelFactory(tunerRepository)
        )

        TunerSettingsScreen(
            sharedViewModel = tunerViewModel,
            settingsViewModel = settingsViewModel,
            onBack = navigateBack,
            onAddTuning = { navigateTo(Screen.AddEditTuning()) },
            onEditTuning = { tuningId -> navigateTo(Screen.AddEditTuning(tuningId)) },
        )
    }
    entry<Screen.AddEditTuning> { screen ->
        val addEditViewModel: AddEditTuningViewModel = viewModel(
            factory = AddEditTuningViewModelFactory(tunerRepository, screen.tuningId)
        )
        
        AddEditTuningScreen(
            viewModel = addEditViewModel,
            onBack = navigateBack,
        )
    }
}
