package com.example.corda.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val keepFocus by viewModel.keepFocus.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val notation by viewModel.notation.collectAsStateWithLifecycle()

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
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            )

            SettingsClickableItem(
                title = "Keep focus",
                icon = Icons.Outlined.Visibility,
                trailingContent = {
                    Switch(
                        checked = keepFocus,
                        onCheckedChange = { viewModel.toggleKeepFocus(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ------ Calibration ------
            SettingsSectionHeader("Calibration")

            OutlinedTextField(
                value = viewModel.frequencyInput,
                onValueChange = { viewModel.updateFrequency(it) },
                label = { Text("Base frequency (Hz)") },
                isError = viewModel.isFrequencyError,
                supportingText = {
                    if (viewModel.isFrequencyError) Text("Wpisz liczbę od 0 do 1000")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { if (viewModel.isFrequencyError) Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ------ Localisation ------
            SettingsSectionHeader("Localisation")

            SettingsDropdown(
                label = "Language",
                selectedOption = language,
                options = listOf("English", "Polski"),
                onOptionSelected = { viewModel.setLanguage(it) }
            )

            SettingsDropdown(
                label = "Notation",
                selectedOption = notation,
                options = listOf("European", "American"),
                onOptionSelected = { viewModel.setNotation(it) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
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