package com.example.corda.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.core.navigation.Screen
import com.example.corda.settings.ui.screen.SettingsScreen

fun EntryProviderScope<Screen>.settingsEntry(
    navigateBack: () -> Unit,
) {
    entry<Screen.Settings> {
        SettingsScreen(
            onBack = navigateBack,
        )
    }
}
