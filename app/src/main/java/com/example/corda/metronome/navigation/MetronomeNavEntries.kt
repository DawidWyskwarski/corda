package com.example.corda.metronome.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.core.navigation.Screen
import com.example.corda.metronome.ui.main.screen.MainMetronomeScreen
import com.example.corda.metronome.ui.MetronomeViewModel
import com.example.corda.metronome.ui.settings.screen.MetronomeSettingsScreen

fun EntryProviderScope<Screen>.metronomeEntries(
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Metronome> {
        // Activity-scoped so MetronomeScreen and MetronomeSettingsScreen share one ViewModel.
        val activity = LocalActivity.current as ComponentActivity // TODO this needs to change. I will probably store data in a DataStore instead.
        val viewModel: MetronomeViewModel = hiltViewModel(viewModelStoreOwner = activity)
        MainMetronomeScreen(
            viewModel = viewModel,
            openDrawer = openDrawer,
            openSettings = { navigateTo(Screen.MetronomeSettings) },
        )
    }
    entry<Screen.MetronomeSettings> {
        val activity = LocalActivity.current as ComponentActivity
        val viewModel: MetronomeViewModel = hiltViewModel(viewModelStoreOwner = activity)
        MetronomeSettingsScreen(
            viewModel = viewModel,
            onBack = navigateBack,
        )
    }
}
