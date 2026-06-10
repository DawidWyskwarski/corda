package com.example.corda.domain.tuner.pitch

import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.relations.TuningWithInstrumentAndSounds
import com.example.corda.domain.tuner.TuningMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TunerModePitchResolverTest {

    @Test
    fun resolve_chromaticMode_returnsChromaticResult() {
        val resolved = TunerModePitchResolver.resolve(
            smoothedFreq = 440f,
            mode = TuningMode.CHROMATIC,
            tuning = null,
            focusedSoundIndex = null,
        )

        assertTrue(resolved is ResolvedPitch.Chromatic)
        val chromatic = resolved as ResolvedPitch.Chromatic
        assertEquals("A", chromatic.result.pitchClass)
        assertEquals(4, chromatic.result.octave)
    }

    @Test
    fun resolve_standardMode_nullTuning_returnsNull() {
        assertNull(
            TunerModePitchResolver.resolve(
                smoothedFreq = 440f,
                mode = TuningMode.STANDARD,
                tuning = null,
                focusedSoundIndex = null,
            ),
        )
    }

    @Test
    fun resolve_standardMode_emptySounds_returnsNull() {
        val tuning = tuningWithSounds(emptyList())
        assertNull(
            TunerModePitchResolver.resolve(
                smoothedFreq = 440f,
                mode = TuningMode.STANDARD,
                tuning = tuning,
                focusedSoundIndex = null,
            ),
        )
    }

    @Test
    fun resolve_standardMode_focusedIndex_locksToThatString() {
        val sounds = listOf(
            sound(name = "E", midi = 40, octave = 2, frequency = 82.41f),
            sound(name = "A", midi = 45, octave = 2, frequency = 110f),
        )
        val tuning = tuningWithSounds(sounds)

        val resolved = TunerModePitchResolver.resolve(
            smoothedFreq = 111f,
            mode = TuningMode.STANDARD,
            tuning = tuning,
            focusedSoundIndex = 1,
        ) as ResolvedPitch.Standard

        assertEquals(1, resolved.matchingSoundIndex)
        assertEquals("A", resolved.result.pitchClass)
        assertEquals(2, resolved.result.octave)
    }

    @Test
    fun resolve_standardMode_noFocus_autoMatchesClosestString() {
        val sounds = listOf(
            sound(name = "E", midi = 40, octave = 2, frequency = 82.41f),
            sound(name = "A", midi = 45, octave = 2, frequency = 110f),
            sound(name = "D", midi = 50, octave = 3, frequency = 146.83f),
        )
        val tuning = tuningWithSounds(sounds)

        val resolved = TunerModePitchResolver.resolve(
            smoothedFreq = 147f,
            mode = TuningMode.STANDARD,
            tuning = tuning,
            focusedSoundIndex = null,
        ) as ResolvedPitch.Standard

        assertEquals(2, resolved.matchingSoundIndex)
        assertEquals("D", resolved.result.pitchClass)
    }

    private fun tuningWithSounds(sounds: List<Sound>): TuningWithInstrumentAndSounds =
        TuningWithInstrumentAndSounds(
            tuningId = 1,
            tuningName = "Test",
            instrumentId = 1,
            instrumentName = "Guitar",
            lastUsed = 0L,
            sounds = sounds,
        )

    private fun sound(name: String, midi: Int, octave: Int, frequency: Float): Sound =
        Sound(soundId = midi, name = name, frequency = frequency, octave = octave, midiNote = midi)
}
