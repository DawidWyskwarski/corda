package com.example.corda.data.tuner.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.relations.TuningSoundCrossRef
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDaoCrossRefCrudInstrumentedTest : TunerDaoTestBase() {

    @Test
    fun insertCrossRef_shouldLinkTuningAndSound() = runTest {
        val (tuningId, soundId) = setupTuningWithOneSound()
        val detail = dao.getTuningWithSoundsById(tuningId)
        
        assertNotNull(detail)
        assertEquals(1, detail!!.sounds.size)
        assertEquals(soundId, detail.sounds.single().soundId)
    }

    @Test
    fun updateTuningSoundCrossRef_shouldPersist() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 2)).toInt()
        val sound1 = dao.insertSound(Sound(name = "E", frequency = 82.41f, octave = 2, midiNote = 40)).toInt()
        val sound2 = dao.insertSound(Sound(name = "A", frequency = 110f, octave = 2, midiNote = 45)).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Std", instrumentId = instrumentId)).toInt()
        
        dao.insertTuningSoundCrossRef(TuningSoundCrossRef(tuningId = tuningId, soundId = sound1))
        
        val crossRefs = db.openHelper.writableDatabase.query(
            "SELECT id FROM TuningSoundCrossRef WHERE tuning_id = ?",
            arrayOf(tuningId.toString()),
        )
        
        crossRefs.moveToFirst()
        val crossRefId = crossRefs.getInt(0)
        crossRefs.close()
        
        dao.updateTuningSoundCrossRef(
            TuningSoundCrossRef(id = crossRefId, tuningId = tuningId, soundId = sound2),
        )
        
        val detail = dao.getTuningWithSoundsById(tuningId)!!
        
        assertEquals(sound2, detail.sounds.single().soundId)
    }

    @Test
    fun deleteCrossRef_shouldRemoveLinkOnly() = runTest {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 1)).toInt()
        val soundId = dao.insertSound(
            Sound(name = "E", frequency = 82.41f, octave = 2, midiNote = 40),
        ).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Std", instrumentId = instrumentId)).toInt()
        
        dao.insertTuningSoundCrossRef(TuningSoundCrossRef(tuningId = tuningId, soundId = soundId))

        val crossRefs = db.openHelper.writableDatabase.query(
            "SELECT id FROM TuningSoundCrossRef WHERE tuning_id = ?",
            arrayOf(tuningId.toString()),
        )
        crossRefs.moveToFirst()
        val crossRefId = crossRefs.getInt(0)
        crossRefs.close()

        dao.deleteTuningSoundCrossRef(
            TuningSoundCrossRef(id = crossRefId, tuningId = tuningId, soundId = soundId),
        )
        
        val detail = dao.getTuningWithSoundsById(tuningId)
        
        assertNotNull(detail)
        assertTrue(detail!!.sounds.isEmpty())
        assertEquals(1, dao.getAllSounds().size)
    }
}
