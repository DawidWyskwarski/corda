package com.example.corda

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.ui.screen.settings.SettingsManager
import com.example.corda.ui.CordaApp
import com.example.corda.ui.theme.CordaTheme
import com.example.corda.ui.theme.LANGUAGE_EN
import com.example.corda.ui.theme.ProvideAppLocale
import com.example.corda.ui.theme.applyWindowTheme
import com.example.corda.ui.util.findComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Blocking the thread until value is retrieved - avoiding a flash
        val initialDark = runBlocking { settingsManager.isDarkMode.first() }
        applyWindowTheme(this, initialDark)

        setContent {
            val activity = checkNotNull(LocalContext.current.findComponentActivity())
            val isDark by settingsManager.isDarkMode.collectAsStateWithLifecycle(initialValue = initialDark)
            val languageTag by settingsManager.language.collectAsStateWithLifecycle(initialValue = LANGUAGE_EN)

            DisposableEffect(isDark) {
                activity.enableEdgeToEdge(
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

            LaunchedEffect(languageTag) {
                Locale.setDefault(Locale.forLanguageTag(languageTag))
            }

            ProvideAppLocale(languageTag = languageTag) {
                CordaTheme(darkTheme = isDark) {
                    CordaApp(activity = activity)
                }
            }
        }
    }
}
