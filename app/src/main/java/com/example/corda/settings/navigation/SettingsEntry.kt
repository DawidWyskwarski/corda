package com.example.corda.settings.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.core.navigation.Screen
import com.example.corda.settings.ui.screen.SettingsScreen
import com.example.corda.settings.ui.screen.SettingsViewModel

fun EntryProviderScope<Screen>.settingsEntry(
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
