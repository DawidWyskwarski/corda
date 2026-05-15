package com.example.corda.domain.tuner.audio

import kotlinx.coroutines.CoroutineScope

interface TonePlayer {
    fun play(frequencyHz: Float, scope: CoroutineScope)

    fun stop()
}
