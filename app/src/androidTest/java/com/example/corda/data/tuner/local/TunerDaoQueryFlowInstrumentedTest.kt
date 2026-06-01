package com.example.corda.data.tuner.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Tuning
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDaoQueryFlowInstrumentedTest : TunerDaoTestBase() {

    @Test
    fun getInstruments_emptyDb_returnsEmptyList() = runTest {
        assertTrue(dao.getInstruments().first().isEmpty())
    }

    @Test
    fun getInstruments_flowUpdatesOnInsert() = runTest {
        assertTrue(dao.getInstruments().first().isEmpty())
        
        dao.insertInstrument(Instrument(name = "Banjo", soundsCount = 5))
        
        assertEquals(1, dao.getInstruments().first().size)
    }

    @Test
    fun getTunings_shouldIncludeSoundsRelation() = runTest {
        val (tuningId, _) = setupTuningWithOneSound()
        val tuning = dao.getTunings().first().single { it.tuningId == tuningId }
        
        assertEquals(1, tuning.sounds.size)
        assertEquals("E", tuning.sounds.single().name)
    }

    @Test
    fun getTunings_shouldOrderByLastUsedDesc() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 6)).toInt()
        
        val older = dao.insertTuning(
            Tuning(name = "Old", instrumentId = instrumentId, lastUsed = 100L),
        ).toInt()
        
        val newer = dao.insertTuning(
            Tuning(name = "New", instrumentId = instrumentId, lastUsed = 500L),
        ).toInt()
        
        val ordered = dao.getTunings().first()
        
        assertEquals(newer, ordered[0].tuningId)
        assertEquals(older, ordered[1].tuningId)
    }

    @Test
    fun getTunings_flowUpdatesOnLastUsedChange() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 6)).toInt()
        
        val tuningId = dao.insertTuning(
            Tuning(name = "Std", instrumentId = instrumentId, lastUsed = 0L),
        ).toInt()
        
        assertEquals(0L, dao.getTunings().first().single().lastUsed)
        
        dao.updateTuningLastUsed(tuningId, 42_000L)
        
        assertEquals(42_000L, dao.getTunings().first().single().lastUsed)
    }

    @Test
    fun updateTuningLastUsed_shouldSetTimestamp() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 6)).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Std", instrumentId = instrumentId)).toInt()
        
        dao.updateTuningLastUsed(tuningId, 1_234_567L)
        
        val loaded = dao.getTuningWithSoundsById(tuningId)
        
        assertEquals(1_234_567L, loaded!!.lastUsed)
    }

    @Test
    fun getReferencePitch_emptyDb_throwsException() = runTest {
        try {
            dao.getReferencePitch()
            fail("Expected exception when reference pitch is missing")
        } catch (e: Exception) {
            assertTrue(
                e.javaClass.simpleName.contains("Empty", ignoreCase = true) ||
                    e.message.orEmpty().contains("empty", ignoreCase = true),
            )
        }
    }

    @Test
    fun getTuningWithSoundsById_existing_returnsTuningAndSounds() = runTest {
        val (tuningId, soundId) = setupTuningWithOneSound()
        val result = dao.getTuningWithSoundsById(tuningId)
        
        assertNotNull(result)
        assertEquals(tuningId, result!!.tuningId)
        assertEquals(soundId, result.sounds.single().soundId)
    }

    @Test
    fun getTuningWithSoundsById_missing_returnsNull() = runTest {
        assertNull(dao.getTuningWithSoundsById(9999))
    }
}
