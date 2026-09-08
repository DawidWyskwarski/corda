package com.example.corda.di

import android.content.Context
import com.example.corda.ui.screen.settings.SystemStateManager
import com.example.corda.ui.screen.tuner.TunerStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StateManagerModule {

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SystemStateManager =
        SystemStateManager(context)

    @Provides
    @Singleton
    fun provideTunerStateManager(@ApplicationContext context: Context): TunerStateManager =
        TunerStateManager(context)
}
