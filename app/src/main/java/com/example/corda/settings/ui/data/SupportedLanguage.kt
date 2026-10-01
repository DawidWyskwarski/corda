package com.example.corda.settings.ui.data

import java.util.Locale

/**
 * A language the user can pick in settings. A `null` [tag] means "follow the system language".
 */
data class Language(val tag: String?) {
    val locale: Locale? = tag?.let(Locale::forLanguageTag)
}

val SUPPORTED_LANGUAGES = listOf(
    Language(null),
    Language("en-US"),
    Language("pl"),
)

/** The language's name written in that language, e.g. "Polski" for Polish. */
fun Locale.nativeDisplayName(): String =
    getDisplayLanguage(this).replaceFirstChar { it.titlecase(this) }
