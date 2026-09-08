package com.example.corda.domain.tuner.pitch

import com.example.corda.data.tuner.local.entities.MusicNote
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow

fun MusicNote.frequency(baseFrequency: Int): Float {
    return PitchHelpers.frequencyFromMidi(
        this.midiNum,
        baseFrequency = baseFrequency
    )
}

/**
 * Pitch-class math, MIDI/frequency conversion, and closest-note matching for the tuner.
 */
object PitchHelpers {

    const val REFERENCE_MIDI_NOTE = 69 // A4

    fun frequencyFromMidi(midiNote: Int, baseFrequency: Int): Float =
        (baseFrequency * 2.0.pow((midiNote - REFERENCE_MIDI_NOTE) / 12.0)).toFloat()

    fun centsFromTarget(detected: Float, target: Float): Float =
        (1200.0 * log2(detected.toDouble() / target.toDouble())).toFloat()

    fun findClosestNoteIndex(frequencyHz: Float, targets: List<Float>): Int =
        targets.indices.minBy { abs(centsFromTarget(frequencyHz, targets[it])) }
}
