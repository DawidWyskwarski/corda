package com.example.corda.domain.tuner.pitch

import com.example.corda.data.tuner.local.entities.Sound
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.pow

class PitchHelpersTest {

    @Test
    fun frequencyFromMidi_a4_is440Hz() {
        assertEquals(440f, PitchHelpers.frequencyFromMidi(69), 0.01f)
    }

    @Test
    fun midiNote_octave4_a_is69() {
        assertEquals(69, PitchHelpers.midiNote(4, 9))
    }

    @Test
    fun centsFromTarget_exactMatch_isZero() {
        assertEquals(0f, PitchHelpers.centsFromTarget(440f, 440f), 0.01f)
    }

    @Test
    fun centsFromTarget_oneSemitoneSharp_isAbout100Cents() {
        val sharp = (440.0 * 2.0.pow(1.0 / 12.0)).toFloat()
        assertEquals(100f, PitchHelpers.centsFromTarget(sharp, 440f), 0.5f)
    }

    @Test
    fun findClosestNote_440Hz_isA4() {
        val result = PitchHelpers.findClosestNote(440f)
        assertEquals("A", result.pitchClass)
        assertEquals(4, result.octave)
        assertEquals(69, result.midiNote)
    }

    @Test
    fun findClosestNoteInSet_picksClosestGuitarString() {
        val openStrings = listOf(
            sound(name = "E", midi = 40, octave = 2, frequency = 82.41f),
            sound(name = "A", midi = 45, octave = 2, frequency = 110f),
            sound(name = "D", midi = 50, octave = 3, frequency = 146.83f),
            sound(name = "G", midi = 55, octave = 3, frequency = 196f),
            sound(name = "B", midi = 59, octave = 3, frequency = 246.94f),
            sound(name = "E", midi = 64, octave = 4, frequency = 329.63f),
        )

        val result = PitchHelpers.findClosestNoteInSet(110f, openStrings)
        assertEquals("A", result.pitchClass)
        assertEquals(2, result.octave)
    }

    @Test
    fun findClosestNoteInSetIndex_returnsCorrectIndex() {
        val targets = listOf(
            sound(name = "C", midi = 60, octave = 4, frequency = 261.63f),
            sound(name = "E", midi = 64, octave = 4, frequency = 329.63f),
        )
        assertEquals(1, PitchHelpers.findClosestNoteInSetIndex(330f, targets))
    }

    @Test
    fun findClosestNoteInSetIndex_emptyList_returnsMinusOne() {
        assertEquals(-1, PitchHelpers.findClosestNoteInSetIndex(440f, emptyList()))
    }

    @Test
    fun pitchResultFromSound_clampsCentsToFifty() {
        val target = sound(name = "A", midi = 69, octave = 4, frequency = 440f)
        val verySharp = (440.0 * 2.0.pow(2.0 / 12.0)).toFloat()
        val result = PitchHelpers.pitchResultFromSound(verySharp, target)
        assertEquals(50f, result.centsOff, 0.01f)
    }

    @Test
    fun compositeLabel_formatsPitchClassAndOctave() {
        assertEquals("A4", PitchHelpers.compositeLabel("A", 4))
    }

    @Test
    fun allChromaticSounds_has108Entries() {
        assertEquals(108, PitchHelpers.allChromaticSounds().size)
    }

    private fun sound(name: String, midi: Int, octave: Int, frequency: Float): Sound =
        Sound(soundId = midi, name = name, frequency = frequency, octave = octave, midiNote = midi)
}
