package com.example.corda.settings.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

@Composable
fun SettingsClickableItem(
    title: String,
    icon: ImageVector,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = trailingContent,
        overlineContent = null,
        supportingContent = null,
        colors = ListItemDefaults.colors(),
        elevation = ListItemDefaults.elevation(ListItemDefaults.Elevation),
        content = { Text(title, fontSize = 16.sp) },
    )
}
