package com.example.corda.di

import com.example.corda.domain.tuner.audio.AudioProcessingService
import com.example.corda.domain.tuner.audio.PitchDetector
import com.example.corda.domain.tuner.audio.ToneGenerator
import com.example.corda.domain.tuner.audio.TonePlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TunerAudioModule {

    @Provides
    @Singleton
    fun providePitchDetector(): PitchDetector = AudioProcessingService()

    @Provides
    @Singleton
    fun provideTonePlayer(): TonePlayer = ToneGenerator()
}
