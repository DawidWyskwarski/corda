package com.example.corda.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.settings.SettingsScreen
import com.example.corda.ui.screen.settings.SettingsViewModel

fun EntryProviderScope<Screen>.utilityEntries(
    navigateBack: () -> Unit,
) {
    entry<Screen.Settings> {
        val settingsViewModel: SettingsViewModel = hiltViewModel()

        SettingsScreen(
            viewModel = settingsViewModel,
            onBack = navigateBack,
        )
    }
}
