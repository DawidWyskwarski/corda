package com.example.corda.ui.theme

import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

const val LANGUAGE_EN = "en"
const val LANGUAGE_PL = "pl"

fun normalizeLanguageTag(stored: String?): String = when (stored) {
    LANGUAGE_PL, "Polski" -> LANGUAGE_PL
    else -> LANGUAGE_EN
}

@Composable
fun ProvideAppLocale(
    languageTag: String,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val configuration = LocalConfiguration.current

    // Setting the localization to new language
    val localizedConfiguration = remember(languageTag) {
        Configuration(configuration).apply {
            setLocale(Locale.forLanguageTag(languageTag))
        }
    }

    // Creating context which uses resources appropriate for the localization
    val localizedContext = remember(languageTag) {
        val confContext = baseContext.createConfigurationContext(localizedConfiguration)
        object : ContextWrapper(baseContext) {
            override fun getResources() = confContext.resources
            override fun getAssets() = confContext.assets
        }
    }

    // Overriding LocalContext and LocalConfiguration for the subtree
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        content()
    }
}
