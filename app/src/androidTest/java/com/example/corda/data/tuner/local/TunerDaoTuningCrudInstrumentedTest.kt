package com.example.corda.data.tuner.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.relations.TuningSoundCrossRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDaoTuningCrudInstrumentedTest : TunerDaoTestBase() {

    @Test
    fun insertTuning_shouldLinkToInstrument() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Bass", soundsCount = 4)).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Standard", instrumentId = instrumentId))
        
        assertTrue(tuningId > 0)
        
        val tunings = dao.getTunings().first()
        
        assertEquals(1, tunings.size)
        assertEquals(instrumentId, tunings.single().instrumentId)
    }

    @Test
    fun updateTuning_shouldUpdateLastUsed() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 6)).toInt()
        val tuningId = dao.insertTuning(
            Tuning(name = "Std", instrumentId = instrumentId, lastUsed = 0L),
        ).toInt()
        
        dao.updateTuning(Tuning(tuningId = tuningId, name = "Std", instrumentId = instrumentId, lastUsed = 999L))
        
        val loaded = dao.getTunings().first().single()
        
        assertEquals(999L, loaded.lastUsed)
    }

    @Test
    fun deleteTuning_shouldCascadeCrossRefs() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 1)).toInt()
        val soundId = dao.insertSound(
            Sound(name = "E", frequency = 82.41f, octave = 2, midiNote = 40),
        ).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Std", instrumentId = instrumentId)).toInt()
        
        dao.insertTuningSoundCrossRef(TuningSoundCrossRef(tuningId = tuningId, soundId = soundId))
        dao.deleteTuning(Tuning(tuningId = tuningId, name = "Std", instrumentId = instrumentId))
        
        assertTrue(dao.getTunings().first().isEmpty())
        
        val detail = dao.getTuningWithSoundsById(tuningId)
        
        assertNull(detail)
    }

    @Test
    fun updateTuningName_shouldRenameTuning() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 6)).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Old Name", instrumentId = instrumentId)).toInt()
        
        dao.updateTuningName(tuningId, "Custom Drop D")
        
        assertEquals("Custom Drop D", dao.getTuningWithSoundsById(tuningId)!!.tuningName)
    }

    @Test
    fun deleteTuningById_shouldRemoveTuningAndCrossRefs() = runTest {
        val (tuningId, _) = setupTuningWithOneSound()
        
        dao.deleteTuningById(tuningId)
        
        assertNull(dao.getTuningWithSoundsById(tuningId))
        assertTrue(dao.getTunings().first().isEmpty())
    }

    @Test
    fun deleteCrossRefsForTuning_shouldLeaveTuningRow() = runTest {
        val (tuningId, _) = setupTuningWithOneSound()
        
        dao.deleteCrossRefsForTuning(tuningId)
        
        val detail = dao.getTuningWithSoundsById(tuningId)
        
        assertNotNull(detail)
        assertTrue(detail!!.sounds.isEmpty())
        assertEquals("Std", detail.tuningName)
    }
}
