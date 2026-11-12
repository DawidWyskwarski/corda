package com.example.corda.settings.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.example.corda.R
import com.example.corda.settings.ui.data.Language
import com.example.corda.settings.ui.data.SUPPORTED_LANGUAGES
import com.example.corda.settings.ui.data.nativeDisplayName

@Composable
fun SettingsLanguageDropdown(
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
    val selected = SUPPORTED_LANGUAGES.firstOrNull { lang ->
        lang.locale != null && lang.locale.language == currentLocale?.language
    } ?: SUPPORTED_LANGUAGES.first()

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = languageLabel(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_localisation_language_label)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(
                    ExposedDropdownMenuAnchorType.PrimaryEditable
                )
                .fillMaxWidth(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SUPPORTED_LANGUAGES.forEach { language ->
                DropdownMenuItem(
                    text = { Text(text = languageLabel(language)) },
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(
                            language.tag?.let(LocaleListCompat::forLanguageTags)
                                ?: LocaleListCompat.getEmptyLocaleList()
                        )

                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
private fun languageLabel(language: Language): String =
    language.locale?.nativeDisplayName()
        ?: stringResource(R.string.settings_localisation_language_system_option)
