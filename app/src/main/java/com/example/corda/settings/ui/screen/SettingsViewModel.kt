package com.example.corda.settings.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.corda.core.datastore.SettingsDataStoreManager
import com.example.corda.core.datastore.TunerDataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStoreManager: SettingsDataStoreManager,
    private val tunerDataStoreManager: TunerDataStoreManager,
) : ViewModel() {

    // StateIn turns Flow into StateFlow, with an always available value for the UI
    val isDarkMode = settingsDataStoreManager.isDarkMode.stateIn(
        viewModelScope,
        // Active for 5 seconds after the final subscriber disappears
        SharingStarted.WhileSubscribed(5000),
        false,
    )

    var frequencyInput by mutableStateOf("")
        private set

    var isFrequencyError by mutableStateOf(false)
        private set

    // Load stored frequency into the text field via an async collect.
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

    fun toggleDarkMode(enabled: Boolean) = viewModelScope.launch {
        settingsDataStoreManager.saveDarkMode(enabled)
    }
}
