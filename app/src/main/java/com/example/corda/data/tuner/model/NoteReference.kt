package com.example.corda.data.tuner.model

import com.example.corda.data.tuner.local.entities.Sound
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow

data class ChromaticNote(
    val name: String,
    val octave: Int,
    val midiNote: Int,
    val frequency: Float
)

object NoteReference {

    private const val REFERENCE_FREQUENCY = 440.0
    private const val REFERENCE_MIDI_NOTE = 69 // A4

    private val NOTE_NAMES = listOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    )

    val allNotes: List<ChromaticNote> = buildList {
        for (octave in 0..8) {
            for ((noteIndex, noteName) in NOTE_NAMES.withIndex()) {
                val midi = (octave + 1) * 12 + noteIndex
                val freq = (REFERENCE_FREQUENCY * 2.0.pow((midi - REFERENCE_MIDI_NOTE) / 12.0)).toFloat()
                add(ChromaticNote(name = "$noteName$octave", octave = octave, midiNote = midi, frequency = freq))
            }
        }
    }

    fun centsFromTarget(detected: Float, target: Float): Float {
        return (1200.0 * log2(detected.toDouble() / target.toDouble())).toFloat()
    }

    fun findClosestNote(frequencyHz: Float): PitchResult {
        val best = allNotes.minBy { abs(centsFromTarget(frequencyHz, it.frequency)) }
        val cents = centsFromTarget(frequencyHz, best.frequency)
        return PitchResult(
            frequencyHz = frequencyHz,
            noteName = best.name,
            octave = best.octave,
            midiNote = best.midiNote,
            centsOff = cents.coerceIn(-50f, 50f)
        )
    }

    fun findClosestNoteInSet(frequencyHz: Float, targets: List<Sound>): PitchResult {
        val best = targets.minBy { abs(centsFromTarget(frequencyHz, it.frequency)) }
        val cents = centsFromTarget(frequencyHz, best.frequency)
        val chromatic = allNotes.firstOrNull { it.name == best.name }
        return PitchResult(
            frequencyHz = frequencyHz,
            noteName = best.name,
            octave = chromatic?.octave ?: extractOctave(best.name),
            midiNote = chromatic?.midiNote ?: 0,
            centsOff = cents.coerceIn(-50f, 50f)
        )
    }

    fun findClosestToSingleTarget(frequencyHz: Float, target: Sound): PitchResult {
        val cents = centsFromTarget(frequencyHz, target.frequency)
        val chromatic = allNotes.firstOrNull { it.name == target.name }
        return PitchResult(
            frequencyHz = frequencyHz,
            noteName = target.name,
            octave = chromatic?.octave ?: extractOctave(target.name),
            midiNote = chromatic?.midiNote ?: 0,
            centsOff = cents.coerceIn(-50f, 50f)
        )
    }

    private fun extractOctave(name: String): Int {
        return name.lastOrNull()?.digitToIntOrNull() ?: 0
    }
}
