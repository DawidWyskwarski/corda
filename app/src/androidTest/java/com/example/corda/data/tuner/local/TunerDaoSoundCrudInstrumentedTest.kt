package com.example.corda.data.tuner.local

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.relations.TuningSoundCrossRef
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDaoSoundCrudInstrumentedTest : TunerDaoTestBase() {

    @Test
    fun insertSound_shouldPersistFrequencyAndMidi() = runTest {
        val id = dao.insertSound(
            Sound(name = "E", frequency = 82.41f, octave = 2, midiNote = 40),
        )
        
        assertTrue(id > 0)
        
        val sounds = dao.getAllSounds()
        
        assertEquals(1, sounds.size)
        assertEquals(40, sounds.single().midiNote)
        assertEquals(82.41f, sounds.single().frequency, 0.01f)
    }

    @Test
    fun updateSound_shouldUpdateFields() = runTest {
        val id = dao.insertSound(
            Sound(name = "A", frequency = 440f, octave = 4, midiNote = 69),
        ).toInt()
        
        dao.updateSound(Sound(soundId = id, name = "A", frequency = 441f, octave = 4, midiNote = 69))
        
        val updated = dao.getAllSounds().single()
        
        assertEquals(441f, updated.frequency, 0.01f)
    }

    @Test
    fun deleteSound_referencedByTuning_shouldThrowConstraintException() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 1)).toInt()
        val soundId = dao.insertSound(
            Sound(name = "E", frequency = 82.41f, octave = 2, midiNote = 40),
        ).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Std", instrumentId = instrumentId)).toInt()
        dao.insertTuningSoundCrossRef(TuningSoundCrossRef(tuningId = tuningId, soundId = soundId))
        val sound = Sound(soundId = soundId, name = "E", frequency = 82.41f, octave = 2, midiNote = 40)
        try {
            dao.deleteSound(sound)
            fail("Expected SQLiteConstraintException when deleting referenced sound")
        } catch (_: SQLiteConstraintException) {
        }
    }

    @Test
    fun deleteSound_unreferenced_shouldRemoveRow() = runTest {
        val id = dao.insertSound(
            Sound(name = "G", frequency = 196f, octave = 3, midiNote = 55),
        ).toInt()
        dao.deleteSound(Sound(soundId = id, name = "G", frequency = 196f, octave = 3, midiNote = 55))
        assertTrue(dao.getAllSounds().isEmpty())
    }
}
