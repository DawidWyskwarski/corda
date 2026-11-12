package com.example.corda.tuner.di

import com.example.corda.tuner.data.repository.TunerRepository
import com.example.corda.tuner.data.repository.TunerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTunerRepository(
        tunerRepositoryImpl: TunerRepositoryImpl
    ): TunerRepository
}
