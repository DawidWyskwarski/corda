package com.example.corda.tuner.domain.audio

import kotlinx.coroutines.CoroutineScope

interface TonePlayer {
    fun play(frequencyHz: Float, scope: CoroutineScope)

    fun stop()
}
