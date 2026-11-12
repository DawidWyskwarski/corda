package com.example.corda

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.core.datastore.SettingsDataStoreManager
import com.example.corda.core.ui.theme.CordaTheme
import com.example.corda.core.ui.system.applyWindowTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsDataStoreManager: SettingsDataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Blocking the thread until value is retrieved - avoiding a flash
        val initialDark = runBlocking { settingsDataStoreManager.isDarkMode.first() }
        applyWindowTheme(this, initialDark)

        setContent {
            val isDark by settingsDataStoreManager.isDarkMode.collectAsStateWithLifecycle(initialValue = initialDark)

            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDark) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            CordaTheme(darkTheme = isDark) {
                CordaApp()
            }
        }
    }
}
