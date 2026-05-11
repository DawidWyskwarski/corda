package com.example.corda.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.inspirations.InspirationsScreen

fun EntryProviderScope<Screen>.inspirationsEntries(
    openDrawer: () -> Unit,
) {
    entry<Screen.Inspirations> {
        InspirationsScreen(
            openDrawer = openDrawer
        )
    }
}
