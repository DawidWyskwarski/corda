package com.example.corda.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.help.HelpAndFeedbackScreen
import com.example.corda.ui.screen.settings.SettingsScreen

fun EntryProviderScope<Screen>.utilityEntries(
    navigateBack: () -> Unit,
) {
    entry<Screen.Settings> {
        SettingsScreen(
            onBack = navigateBack
        )
    }
    entry<Screen.Help> {
        HelpAndFeedbackScreen(
            onBack = navigateBack
        )
    }
}
