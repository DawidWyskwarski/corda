package com.example.corda.di

import android.content.Context
import androidx.room.Room
import com.example.corda.data.tuner.local.database.TunerDatabase
import com.example.corda.data.tuner.local.database.TunerDatabaseCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TunerDatabaseModule {

    @Provides
    @Singleton
    fun provideTunerDatabase(
        @ApplicationContext context: Context,
        provider: Provider<TunerDatabase>
    ): TunerDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = TunerDatabase::class.java,
            name = "tuner_db"
        ).addCallback(
            TunerDatabaseCallback(provider)
        ).build()
    }
    @Provides
    fun provideSoundDao(db: TunerDatabase) = db.getMusicNoteDao()

    @Provides
    fun provideInstrumentDao(db: TunerDatabase) = db.getInstrumentDao()

    @Provides
    fun provideTuningDao(db: TunerDatabase) = db.getTuningDao()
}
