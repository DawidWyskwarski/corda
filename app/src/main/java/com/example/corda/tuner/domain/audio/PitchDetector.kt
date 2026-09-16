package com.example.corda.tuner.domain.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PitchDetector {
    val pitchFlow: SharedFlow<Float?>
    val isListening: StateFlow<Boolean>

    fun start(scope: CoroutineScope)

    fun stop()
}
