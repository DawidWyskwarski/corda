package com.example.corda.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {
    val isDarkMode = settingsManager.isDarkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val keepFocus = settingsManager.keepFocus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val language = settingsManager.language.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), settingsManager.defaultLanguage)
    val notation = settingsManager.notation.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), settingsManager.defaultNotation)

    var frequencyInput by mutableStateOf("")
        private set

    var isFrequencyError by mutableStateOf(false)
        private set

    // Loading stored freq value to display in text field
    init {
        viewModelScope.launch {
            settingsManager.baseFrequency.collect { hz ->
                frequencyInput = hz.toString()
            }
        }
    }

    fun updateFrequency(input: String) {
        frequencyInput = input
        val value = input.toIntOrNull()

        if (value != null && value in 0..1000) {
            isFrequencyError = false
            viewModelScope.launch { settingsManager.saveBaseFrequency(value) }
        } else {
            isFrequencyError = true
        }
    }

    fun toggleDarkMode(enabled: Boolean) = viewModelScope.launch { settingsManager.saveDarkMode(enabled) }
    fun toggleKeepFocus(enabled: Boolean) = viewModelScope.launch { settingsManager.saveKeepFocus(enabled) }
    fun setLanguage(lang: String) = viewModelScope.launch { settingsManager.saveLanguage(lang) }
    fun setNotation(type: String) = viewModelScope.launch { settingsManager.saveNotation(type) }
}