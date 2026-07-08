package com.example.corda.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.ui.components.SingleClickIconButton
import com.example.corda.ui.screen.settings.components.SettingsClickableItem
import com.example.corda.ui.screen.settings.components.SettingsLanguageDropdown
import com.example.corda.ui.screen.settings.components.SettingsSectionHeader

private val screenPadding = 16.dp
private val dividerPadding = Modifier.padding(vertical = 8.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val languageTag by viewModel.language.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.SemiBold) },
                navigationIcon = { SingleClickIconButton(onClick = onBack) },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = screenPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(R.string.settings_display))

            SettingsClickableItem(
                title = stringResource(R.string.dark_mode),
                icon = Icons.Outlined.DarkMode,
                trailingContent = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            )

            HorizontalDivider(modifier = dividerPadding)

            SettingsSectionHeader(stringResource(R.string.settings_calibration))

            OutlinedTextField(
                value = viewModel.frequencyInput,
                onValueChange = { viewModel.updateFrequency(it) },
                label = { Text(stringResource(R.string.base_frequency)) },
                isError = viewModel.isFrequencyError,
                supportingText = {
                    if (viewModel.isFrequencyError) Text(stringResource(R.string.frequency_error))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (viewModel.isFrequencyError) {
                        Icon(
                            Icons.Default.Error,
                            stringResource(R.string.error),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
            )

            HorizontalDivider(modifier = dividerPadding)

            SettingsSectionHeader(stringResource(R.string.settings_localisation))

            SettingsLanguageDropdown(
                selectedLanguageTag = languageTag,
                onLanguageSelected = { viewModel.setLanguage(it) },
            )
        }
    }
}