package com.example.corda.core.datastore.di

import android.content.Context
import com.example.corda.core.datastore.TunerDataStoreManager
import com.example.corda.core.datastore.SettingsDataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreManagerModule {

    @Provides
    @Singleton
    fun provideSettingsDataStoreManager(@ApplicationContext context: Context): SettingsDataStoreManager =
        SettingsDataStoreManager(context)

    @Provides
    @Singleton
    fun provideTunerDataStoreManager(@ApplicationContext context: Context): TunerDataStoreManager =
        TunerDataStoreManager(context)
}