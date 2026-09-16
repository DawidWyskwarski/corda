package com.example.corda.settings.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

@Composable
fun SettingsClickableItem(
    title: String,
    icon: ImageVector,
    trailingContent: @Composable () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontSize = 16.sp) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = trailingContent,
    )
}