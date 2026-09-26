package com.example.corda.tuner.ui.main.data

import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.ui.helpers.InstrumentName

sealed interface TuningTarget {
    val notes: List<MusicNote>

    data class Standard (
        val tuningName: String,
        val instrumentName: InstrumentName,
        override val notes: List<MusicNote>,
    ): TuningTarget

    data class Chromatic (
        override val notes: List<MusicNote>,
    ): TuningTarget
}
