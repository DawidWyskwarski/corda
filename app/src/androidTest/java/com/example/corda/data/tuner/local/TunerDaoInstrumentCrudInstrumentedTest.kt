package com.example.corda.data.tuner.local

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Tuning
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDaoInstrumentCrudInstrumentedTest : TunerDaoTestBase() {

    @Test
    fun insertInstrument_shouldReturnGeneratedId() = runTest {
        val id = dao.insertInstrument(Instrument(name = "Ukulele", soundsCount = 4))
        
        assertTrue(id > 0)
        assertEquals(1, dao.getInstruments().first().size)
    }

    @Test
    fun updateInstrument_shouldPersistChanges() = runTest {
        val id = dao.insertInstrument(Instrument(name = "Old", soundsCount = 4)).toInt()
        val instrument = Instrument(instrumentId = id, name = "Renamed", soundsCount = 6)
        
        dao.updateInstrument(instrument)
        
        val loaded = dao.getInstruments().first().single()
        
        assertEquals("Renamed", loaded.name)
        assertEquals(6.toByte(), loaded.soundsCount)
    }

    @Test
    fun deleteInstrument_withoutTunings_shouldRemoveRow() = runTest {
        val id = dao.insertInstrument(Instrument(name = "Solo", soundsCount = 1)).toInt()
        val instrument = Instrument(instrumentId = id, name = "Solo", soundsCount = 1)
        
        dao.deleteInstrument(instrument)
        
        assertTrue(dao.getInstruments().first().isEmpty())
    }

    @Test
    fun deleteInstrument_withTunings_shouldThrowConstraintException() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 6)).toInt()
        
        dao.insertTuning(Tuning(name = "Standard", instrumentId = instrumentId))
        
        val instrument = Instrument(instrumentId = instrumentId, name = "Guitar", soundsCount = 6)
        
        try {
            dao.deleteInstrument(instrument)
            fail("Expected SQLiteConstraintException when deleting instrument with tunings")
        } catch (_: SQLiteConstraintException) {
        }
    }
}
