package com.example.corda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.data.SettingsManager
import com.example.corda.ui.CordaApp
import com.example.corda.ui.screen.settings.SettingsViewModel
import com.example.corda.ui.theme.CordaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsManager = SettingsManager(applicationContext)
        val settingsViewModel = SettingsViewModel(settingsManager)

        enableEdgeToEdge()
        setContent {
            // WithLifecycle ensures the app wont waste battery refreshing settings when screen is off
            val isDark by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()

            CordaTheme(darkTheme = isDark) {
                CordaApp(settingsViewModel = settingsViewModel)
            }
        }
    }
}