package com.example.corda.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.ui.theme.LANGUAGE_EN
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
) : ViewModel() {

    // StateIn turns Flow into StateFlow, with an always available value for the UI
    val isDarkMode = settingsManager.isDarkMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),   // Active for 5 seconds after final subscriber disappears
        false,
    )
    val language = settingsManager.language.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LANGUAGE_EN,
    )

    var frequencyInput by mutableStateOf("")
        private set

    var isFrequencyError by mutableStateOf(false)
        private set

    // Loading stored freq value to display in text field; done this way because we use an asynchronous operation
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
    fun setLanguage(lang: String) = viewModelScope.launch { settingsManager.saveLanguage(lang) }
}