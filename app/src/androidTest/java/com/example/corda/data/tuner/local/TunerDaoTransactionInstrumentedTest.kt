package com.example.corda.data.tuner.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Tuning
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDaoTransactionInstrumentedTest : TunerDaoTestBase() {

    @Test
    fun insertTuningWithSounds_shouldCreateTuningAndLinks() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 3)).toInt()
        val sounds = listOf(
            soundAtMidi(40),
            soundAtMidi(45),
            soundAtMidi(50),
        )
        
        dao.insertTuningWithSounds(
            Tuning(name = "Open", instrumentId = instrumentId),
            sounds = sounds,
        )
        
        val tuning = dao.getTunings().first().single()
        
        assertEquals("Open", tuning.tuningName)
        assertEquals(3, tuning.sounds.size)
    }

    @Test
    fun insertTuningWithSounds_shouldPreserveStringOrder() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 3)).toInt()
        val highE = soundAtMidi(64)
        val lowE = soundAtMidi(40)
        val a = soundAtMidi(45)
        
        dao.insertTuningWithSounds(
            Tuning(name = "Custom", instrumentId = instrumentId),
            sounds = listOf(highE, lowE, a),
        )
        
        val tuningId = dao.getTunings().first().single().tuningId
        val midiOrder = dao.getTuningWithSoundsById(tuningId)!!.sounds.map { it.midiNote }
        
        assertEquals(listOf(64, 40, 45), midiOrder)
    }

    @Test
    fun updateTuningWithSounds_shouldReplaceNameAndSounds() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 2)).toInt()
        val s1 = soundAtMidi(40)
        val s2 = soundAtMidi(45)
        
        dao.insertTuningWithSounds(
            Tuning(name = "Before", instrumentId = instrumentId),
            sounds = listOf(s1),
        )
        
        val tuningId = dao.getTunings().first().single().tuningId
        
        dao.updateTuningWithSounds(tuningId, "After", listOf(s1, s2))
        
        val result = dao.getTuningWithSoundsById(tuningId)!!
        
        assertEquals("After", result.tuningName)
        assertEquals(2, result.sounds.size)
    }

    @Test
    fun updateTuningWithSounds_reorderedSounds_shouldMatchUiOrder() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 2)).toInt()
        val s40 = soundAtMidi(40)
        val s45 = soundAtMidi(45)
        
        dao.insertTuningWithSounds(
            Tuning(name = "T", instrumentId = instrumentId),
            sounds = listOf(s40, s45),
        )
        
        val tuningId = dao.getTunings().first().single().tuningId
        
        dao.updateTuningWithSounds(tuningId, "T", listOf(s45, s40))
        
        val midiOrder = dao.getTuningWithSoundsById(tuningId)!!.sounds.map { it.midiNote }
        
        assertEquals(listOf(45, 40), midiOrder)
    }
}
