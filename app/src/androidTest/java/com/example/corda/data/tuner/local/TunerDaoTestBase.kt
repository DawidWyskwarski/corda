package com.example.corda.data.tuner.local

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.corda.data.tuner.local.dao.TunerDao
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.relations.TuningSoundCrossRef
import com.example.corda.domain.tuner.pitch.PitchHelpers
import org.junit.After
import org.junit.Before

abstract class TunerDaoTestBase {

    protected lateinit var db: TunerDatabase
    protected lateinit var dao: TunerDao
    protected val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    open fun setUp() {
        db = TunerDatabaseTestHelper.minimal(context)
        dao = db.tunerDao
    }

    @After
    open fun tearDown() {
        db.close()
    }

    protected suspend fun setupTuningWithOneSound(): Pair<Int, Int> {
        val instrumentId = dao.insertInstrument(Instrument(name = "Guitar", soundsCount = 1)).toInt()
        val soundId = dao.insertSound(
            Sound(name = "E", frequency = 82.41f, octave = 2, midiNote = 40),
        ).toInt()
        val tuningId = dao.insertTuning(Tuning(name = "Std", instrumentId = instrumentId)).toInt()
        dao.insertTuningSoundCrossRef(TuningSoundCrossRef(tuningId = tuningId, soundId = soundId))
        return tuningId to soundId
    }

    protected suspend fun soundAtMidi(midi: Int): Sound {
        val id = dao.insertSound(
            Sound(
                name = PitchHelpers.pitchClassAt(midi % 12),
                frequency = PitchHelpers.frequencyFromMidi(midi),
                octave = midi / 12 - 1,
                midiNote = midi,
            ),
        ).toInt()
        return Sound(
            soundId = id,
            name = PitchHelpers.pitchClassAt(midi % 12),
            frequency = PitchHelpers.frequencyFromMidi(midi),
            octave = midi / 12 - 1,
            midiNote = midi,
        )
    }
}
