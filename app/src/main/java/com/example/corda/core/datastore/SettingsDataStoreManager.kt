package com.example.corda.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStoreManager(context: Context) {

    private val settingsDataStore = context.dataStore

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val isDarkMode: Flow<Boolean> = settingsDataStore.data.map { it[Keys.DARK_MODE] ?: false }

    suspend fun saveDarkMode(enabled: Boolean) = settingsDataStore.edit { it[Keys.DARK_MODE] = enabled }
}