package com.example.corda.di

import android.content.Context
import com.example.corda.data.inspirations.local.InspirationsDatabase
import com.example.corda.data.inspirations.local.dao.InspirationsDao
import com.example.corda.data.inspirations.media.InspirationMediaStore
import com.example.corda.data.inspirations.repository.InspirationsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InspirationsDatabaseModule {

    @Provides
    @Singleton
    fun provideInspirationsDatabase(@ApplicationContext context: Context): InspirationsDatabase =
        InspirationsDatabase.getInstance(context)

    @Provides
    fun provideInspirationsDao(database: InspirationsDatabase): InspirationsDao =
        database.inspirationsDao

    @Provides
    @Singleton
    fun provideInspirationsRepository(
        dao: InspirationsDao,
        mediaStore: InspirationMediaStore,
    ): InspirationsRepository = InspirationsRepository(dao, mediaStore)
}
