package com.example.corda.data.tuner.model

data class PitchResult(
    val frequencyHz: Float,
    val noteName: String,
    val octave: Int,
    val midiNote: Int,
    val centsOff: Float
)
