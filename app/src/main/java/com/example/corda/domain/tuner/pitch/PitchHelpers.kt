package com.example.corda.domain.tuner.pitch

import com.example.corda.data.tuner.local.entities.Sound
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow

/**
 * Pitch-class math, MIDI/frequency conversion, and closest-note matching for the tuner.
 */
object PitchHelpers {

    const val REFERENCE_FREQUENCY = 440.0
    const val REFERENCE_MIDI_NOTE = 69 // A4

    val pitchClasses: List<String> = listOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
    )

    fun midiNote(octave: Int, noteIndex: Int): Int = (octave + 1) * 12 + noteIndex

    fun frequencyFromMidi(midiNote: Int): Float =
        (REFERENCE_FREQUENCY * 2.0.pow((midiNote - REFERENCE_MIDI_NOTE) / 12.0)).toFloat()

    fun pitchClassAt(noteIndex: Int): String = pitchClasses[noteIndex]

    /**
     * Full chromatic ladder (octaves 0..8) as in-memory [Sound] rows.
     * [Sound.soundId] is set to [midiNote] for stable UI keys outside the DB.
     */
    fun allChromaticSounds(): List<Sound> = buildList {
        for (octave in 0..8) {
            for ((noteIndex, pitchClass) in pitchClasses.withIndex()) {
                val midi = midiNote(octave, noteIndex)
                add(
                    Sound(
                        soundId = midi,
                        name = pitchClass,
                        frequency = frequencyFromMidi(midi),
                        octave = octave,
                        midiNote = midi,
                    ),
                )
            }
        }
    }

    val allSounds: List<Sound> by lazy { allChromaticSounds() }

    fun centsFromTarget(detected: Float, target: Float): Float =
        (1200.0 * log2(detected.toDouble() / target.toDouble())).toFloat()

    fun findClosestNote(frequencyHz: Float): PitchResult {
        val best = allSounds.minBy { abs(centsFromTarget(frequencyHz, it.frequency)) }
        return pitchResultFromSound(frequencyHz, best)
    }

    fun findClosestNoteInSet(frequencyHz: Float, targets: List<Sound>): PitchResult {
        val best = targets.minBy { abs(centsFromTarget(frequencyHz, it.frequency)) }
        return pitchResultFromSound(frequencyHz, best)
    }

    fun findClosestNoteInSetIndex(frequencyHz: Float, targets: List<Sound>): Int {
        if (targets.isEmpty()) return -1
        val best = targets.minBy { abs(centsFromTarget(frequencyHz, it.frequency)) }
        return targets.indexOfFirst { it.soundId == best.soundId && it.midiNote == best.midiNote }
    }

    fun findClosestToSingleTarget(frequencyHz: Float, target: Sound): PitchResult {
        val cents = centsFromTarget(frequencyHz, target.frequency)
        return PitchResult(
            frequencyHz = frequencyHz,
            pitchClass = target.name,
            octave = target.octave,
            midiNote = target.midiNote,
            centsOff = cents.coerceIn(-50f, 50f),
        )
    }

    fun pitchResultFromSound(frequencyHz: Float, sound: Sound): PitchResult {
        val cents = centsFromTarget(frequencyHz, sound.frequency)
        return PitchResult(
            frequencyHz = frequencyHz,
            pitchClass = sound.name,
            octave = sound.octave,
            midiNote = sound.midiNote,
            centsOff = cents.coerceIn(-50f, 50f),
        )
    }

    /** Plain text label (e.g. for [TunerUiState.detectedNote] without subscript). */
    fun compositeLabel(pitchClass: String, octave: Int): String = "$pitchClass$octave"
}
