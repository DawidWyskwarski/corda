package com.example.corda.domain.tuner.pitch

data class PitchResult(
    val frequencyHz: Float,
    val pitchClass: String,
    val octave: Int,
    val midiNote: Int,
    val centsOff: Float,
)
