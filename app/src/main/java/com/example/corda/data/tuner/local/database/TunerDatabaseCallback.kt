package com.example.corda.data.tuner.local.database

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import jakarta.inject.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TunerDatabaseCallback(
    private val databaseProvider: Provider<TunerDatabase>
) : RoomDatabase.Callback() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        applicationScope.launch {
            try {
                TunerDatabasePopulator(databaseProvider.get()).populate()
            } catch (e: Exception) {
                Log.e("TunerDb", "Seed failed", e)
            }
        }
    }
}
