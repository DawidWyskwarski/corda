package com.example.corda.tuner.ui.main.data

import com.example.corda.tuner.data.local.entities.MusicNote

data class TunerReading(
    val note: MusicNote? = null,
    val frequency: Float? = null,
    val noteIndex: Int? = null,
    val centsOff: Float? = null,
)
