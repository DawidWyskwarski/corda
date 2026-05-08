package com.example.corda.ui.screen.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var isDarkMode by mutableStateOf(false)
        private set

    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode = enabled
    }

}