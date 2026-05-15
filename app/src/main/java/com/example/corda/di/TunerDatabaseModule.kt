package com.example.corda.di

import android.content.Context
import com.example.corda.data.tuner.local.TunerDatabase
import com.example.corda.data.tuner.local.dao.TunerDao
import com.example.corda.data.tuner.repository.TunerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TunerDatabaseModule {

    @Provides
    @Singleton
    fun provideTunerDatabase(@ApplicationContext context: Context): TunerDatabase =
        TunerDatabase.getInstance(context)

    @Provides
    fun provideTunerDao(database: TunerDatabase): TunerDao = database.tunerDao

    @Provides
    @Singleton
    fun provideTunerRepository(dao: TunerDao): TunerRepository = TunerRepository(dao)
}
