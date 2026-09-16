package com.example.corda.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.corda.core.tuner.TuningMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "tuner")

class TunerDataStoreManager(context: Context) { // TODO I think it would be better to divide it. Think of a good way to do that.

    private val tunerDataStore = context.dataStore

    private object Keys {
        val MODE = stringPreferencesKey("mode")
        val BASE_FREQUENCY = intPreferencesKey("base_frequency")
    }

    val tunerMode: Flow<TuningMode> = tunerDataStore.data.map { stored ->
        (TuningMode.entries.find { it.name == stored[Keys.MODE] } ?: TuningMode.STANDARD)
    }
    val baseFrequency: Flow<Int> = tunerDataStore.data.map { it[Keys.BASE_FREQUENCY] ?: 440 }

    suspend fun setTunerMode(tunerMode: TuningMode) = tunerDataStore.edit { it[Keys.MODE] = tunerMode.name }
    suspend fun saveBaseFrequency(hz: Int) = tunerDataStore.edit { it[Keys.BASE_FREQUENCY] = hz }
}
