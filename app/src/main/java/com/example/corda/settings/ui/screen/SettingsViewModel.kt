package com.example.corda.settings.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.core.datastore.SettingsDataStoreManager
import com.example.corda.core.datastore.TunerDataStoreManager
import com.example.corda.core.ui.system.LANGUAGE_EN
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStoreManager: SettingsDataStoreManager,
    private val tunerDataStoreManager: TunerDataStoreManager
) : ViewModel() {

    // StateIn turns Flow into StateFlow, with an always available value for the UI
    val isDarkMode = settingsDataStoreManager.isDarkMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),   // Active for 5 seconds after final subscriber disappears
        false,
    )
    val language = settingsDataStoreManager.language.stateIn(
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
            tunerDataStoreManager.baseFrequency.collect { hz ->
                frequencyInput = hz.toString()
            }
        }
    }

    fun updateFrequency(input: String) {
        frequencyInput = input
        val value = input.toIntOrNull()

        if (value != null && value in 0..1000) {
            isFrequencyError = false
            viewModelScope.launch { tunerDataStoreManager.saveBaseFrequency(value) }
        } else {
            isFrequencyError = true
        }
    }

    fun toggleDarkMode(enabled: Boolean) = viewModelScope.launch { settingsDataStoreManager.saveDarkMode(enabled) }
    fun setLanguage(lang: String) = viewModelScope.launch { settingsDataStoreManager.saveLanguage(lang) }
}