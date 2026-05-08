package com.example.corda.ui.screen.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("App settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ------ Display ------
            SettingsSectionHeader("Display")

            SettingsClickableItem(
                title = "Dark mode",
                icon = Icons.Outlined.DarkMode,
                trailingContent = {
                    Switch(
                        checked = viewModel.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            )

            SettingsClickableItem(
                title = "Keep focus",
                icon = Icons.Outlined.Visibility,
                trailingContent = { Switch(checked = true, onCheckedChange = {}) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ------ Calibration ------
            SettingsSectionHeader("Calibration")

            SettingsClickableItem(
                title = "Base frequency",
                icon = Icons.Rounded.BarChart,
                trailingContent = {
                    Box(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("440 Hz", fontSize = 14.sp)
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ------ Localisation ------
            SettingsSectionHeader("Localisation")

            SettingsClickableItem(
                title = "Language",
                icon = Icons.Outlined.Language,
                trailingContent = {
                    ValueWithArrow("English")
                }
            )

            SettingsClickableItem(
                title = "Notation",
                icon = Icons.Outlined.MusicNote,
                trailingContent = {
                    ValueWithArrow("European")
                }
            )
        }
    }
}

/**
 * Pomocniczy komponent dla nagłówka sekcji
 */
@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

/**
 * Generyczny komponent rzędu ustawień
 */
@Composable
fun SettingsClickableItem(
    title: String,
    icon: ImageVector,
    trailingContent: @Composable () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontSize = 16.sp) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = trailingContent
    )
}

/**
 * Komponent dla wartości tekstowej z opcją przejścia (strzałką)
 */
@Composable
fun ValueWithArrow(value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}