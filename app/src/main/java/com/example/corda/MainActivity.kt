package com.example.corda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.corda.ui.CordaApp
import com.example.corda.ui.screen.tuner.TunerViewModelFactory
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModelFactory
import com.example.corda.ui.theme.CordaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as CordaApplication).tunerRepository
        val tunerViewModelFactory = TunerViewModelFactory(repository)
        val tunerSettingsViewModelFactory = TunerSettingsViewModelFactory(repository)

        setContent {
            CordaTheme {
                CordaApp(
                    tunerViewModelFactory = tunerViewModelFactory,
                    tunerSettingsViewModelFactory = tunerSettingsViewModelFactory
                )
            }
        }
    }
}
