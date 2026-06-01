package com.example.corda.data.tuner.local

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.tuner.local.entities.Sound
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDaoSeedDataInstrumentedTest : TunerDaoTestBase() {

    @Test
    fun getReferencePitch_withSeed_returnsA4() = runTest {
        val seededDb = TunerDatabaseTestHelper.withProductionSeed(context)
        val seededDao = seededDb.tunerDao
        
        try {
            val ref = seededDao.getReferencePitch()
            
            assertEquals("A", ref.name)
            assertEquals(4, ref.octave)
            assertEquals(69, ref.midiNote)
            assertEquals(440f, ref.frequency, 0.5f)
        } finally {
            seededDb.close()
        }
    }

    @Test
    fun getAllSounds_withFullSeed_returns108Sounds() = runTest {
        val seededDb = TunerDatabaseTestHelper.withProductionSeed(context)
        val seededDao = seededDb.tunerDao
        
        try {
            assertEquals(108, seededDao.getAllSounds().size)
            assertEquals(4, seededDao.getInstruments().first().size)
            assertEquals(10, seededDao.getTunings().first().size)
        } finally {
            seededDb.close()
        }
    }

    @Test
    fun insertSound_duplicateMidiNote_shouldThrowException() = runTest {
        val seededDb = TunerDatabaseTestHelper.withProductionSeed(context)
        val seededDao = seededDb.tunerDao
        
        try {
            try {
                seededDao.insertSound(
                    Sound(name = "A", frequency = 440f, octave = 4, midiNote = 69),
                )
                fail("Expected SQLiteConstraintException for duplicate midi_note")
            } catch (_: SQLiteConstraintException) {
            }
        } finally {
            seededDb.close()
        }
    }
}
