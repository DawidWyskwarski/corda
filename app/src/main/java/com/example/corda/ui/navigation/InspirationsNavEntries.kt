package com.example.corda.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.example.corda.ui.screen.inspirations.InspirationsScreen
import com.example.corda.ui.screen.inspirations.addedit.InspirationEditScreen
import com.example.corda.ui.screen.inspirations.detail.InspirationDetailScreen

fun EntryProviderScope<Screen>.inspirationsEntries(
    openDrawer: () -> Unit,
    navigateTo: (Screen) -> Unit,
    navigateBack: () -> Unit,
) {
    entry<Screen.Inspirations> {
        InspirationsScreen(
            openDrawer = openDrawer,
            onInspirationClick = { id -> navigateTo(Screen.InspirationDetail(id)) },
            onAddClick = { navigateTo(Screen.InspirationAddEdit()) }
        )
    }

    entry<Screen.InspirationDetail> { destination ->
        InspirationDetailScreen(
            id = destination.id,
            onBack = navigateBack,
            onEdit = { id -> navigateTo(Screen.InspirationAddEdit(id)) }
        )
    }

    entry<Screen.InspirationAddEdit> { destination ->
        InspirationEditScreen(
            id = destination.id,
            onBack = navigateBack,
            onSaved = navigateBack
        )
    }
}
