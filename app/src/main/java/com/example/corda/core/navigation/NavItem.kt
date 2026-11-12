package com.example.corda.core.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a top-level navigation destination in the drawer menu.
 *
 * @param icon The icon to display for this destination.
 * @param labelRes The resource ID for the label to display for this destination.
 * @param screen The screen that corresponds to this destination.
 */
data class NavItem(
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    val screen: Screen
)
