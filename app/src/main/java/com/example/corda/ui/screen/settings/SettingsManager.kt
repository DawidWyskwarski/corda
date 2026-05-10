package com.example.corda.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.corda.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {
    /*
    * Class responsible for storing user preferences
    * */
    private val settingsDataStore = context.dataStore

    val defaultLanguage = context.getString(R.string.language_english)
    val defaultNotation = context.getString(R.string.european)

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEEP_FOCUS = booleanPreferencesKey("keep_focus")
        val BASE_FREQUENCY = intPreferencesKey("base_frequency")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTATION = stringPreferencesKey("notation")
    }

    val isDarkMode: Flow<Boolean> = settingsDataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val keepFocus: Flow<Boolean> = settingsDataStore.data.map { it[Keys.KEEP_FOCUS] ?: true }
    val baseFrequency: Flow<Int> = settingsDataStore.data.map { it[Keys.BASE_FREQUENCY] ?: 440 }
    val language: Flow<String> = settingsDataStore.data.map { it[Keys.LANGUAGE] ?: defaultLanguage }
    val notation: Flow<String> = settingsDataStore.data.map { it[Keys.NOTATION] ?: defaultNotation }

    suspend fun saveDarkMode(enabled: Boolean) = settingsDataStore.edit { it[Keys.DARK_MODE] = enabled }
    suspend fun saveKeepFocus(enabled: Boolean) = settingsDataStore.edit { it[Keys.KEEP_FOCUS] = enabled }
    suspend fun saveBaseFrequency(hz: Int) = settingsDataStore.edit { it[Keys.BASE_FREQUENCY] = hz }
    suspend fun saveLanguage(lang: String) = settingsDataStore.edit { it[Keys.LANGUAGE] = lang }
    suspend fun saveNotation(type: String) = settingsDataStore.edit { it[Keys.NOTATION] = type }
}