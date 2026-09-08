package com.example.corda.ui.screen.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.corda.ui.theme.normalizeLanguageTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SystemStateManager(context: Context) {

    private val settingsDataStore = context.dataStore

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val isDarkMode: Flow<Boolean> = settingsDataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val language: Flow<String> = settingsDataStore.data.map {
        normalizeLanguageTag(it[Keys.LANGUAGE])
    }

    suspend fun saveDarkMode(enabled: Boolean) = settingsDataStore.edit { it[Keys.DARK_MODE] = enabled }
    suspend fun saveLanguage(lang: String) = settingsDataStore.edit { it[Keys.LANGUAGE] = lang }
}