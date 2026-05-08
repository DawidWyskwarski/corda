package com.example.corda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.corda.ui.CordaApp
import com.example.corda.ui.screen.settings.SettingsViewModel
import com.example.corda.ui.theme.CordaTheme

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CordaTheme(darkTheme = settingsViewModel.isDarkMode) {
                CordaApp(
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}