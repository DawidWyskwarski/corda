package com.example.corda.tuner.data.local.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.tuner.data.local.entities.DefaultInstrumentName
import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.data.local.models.TuningDetails
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TunerDatabasePopulatorTest {

    private lateinit var db: TunerDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TunerDatabase::class.java)
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun populate_insertsMusicNotesFromC0ToB8() = runTest {
        TunerDatabasePopulator(db).populate()

        val notes = db.getMusicNoteDao().getAllNotes()
        assertThat(notes).hasSize(108) // midi 12..119 inclusive
        assertThat(notes.first()).isEqualTo(MusicNote(midiNum = 12, name = "C", octave = 0))
        assertThat(notes.last()).isEqualTo(MusicNote(midiNum = 119, name = "B", octave = 8))
        assertThat(notes.find { it.midiNum == 69 })
            .isEqualTo(MusicNote(midiNum = 69, name = "A", octave = 4))
        assertThat(notes.map { it.midiNum }).isInOrder()
    }

    @Test
    fun populate_insertsExpectedInstruments() = runTest {
        TunerDatabasePopulator(db).populate()

        val instruments = db.getInstrumentDao().getInstruments().first()
        assertThat(instruments).hasSize(4)
        assertThat(instruments.map { it.defaultName to it.musicNotesCount })
            .containsExactly(
                DefaultInstrumentName.GUITAR to 6.toByte(),
                DefaultInstrumentName.GUITAR to 7.toByte(),
                DefaultInstrumentName.BASS to 4.toByte(),
                DefaultInstrumentName.BASS to 5.toByte(),
            )
        assertThat(instruments.all { it.customName == null }).isTrue()
    }

    @Test
    fun populate_insertsExpectedTuningsWithCorrectNotes() = runTest {
        TunerDatabasePopulator(db).populate()

        val instruments = db.getInstrumentDao().getInstruments().first()
            .associateBy { it.defaultName to it.musicNotesCount }
        val tunings = db.getTuningDao().getTunings().first()
            .groupBy { it.instrumentId }

        val guitar6 = instruments.getValue(DefaultInstrumentName.GUITAR to 6)
        val guitar7 = instruments.getValue(DefaultInstrumentName.GUITAR to 7)
        val bass4 = instruments.getValue(DefaultInstrumentName.BASS to 4)
        val bass5 = instruments.getValue(DefaultInstrumentName.BASS to 5)

        assertTuningNotes(tunings, guitar6.id, "E Standard", listOf(40, 45, 50, 55, 59, 64))
        assertTuningNotes(tunings, guitar6.id, "Drop D", listOf(38, 45, 50, 55, 59, 64))
        assertTuningNotes(tunings, guitar6.id, "D Standard", listOf(38, 43, 48, 53, 57, 62))

        assertTuningNotes(tunings, guitar7.id, "B Standard", listOf(35, 40, 45, 50, 55, 59, 64))
        assertTuningNotes(tunings, guitar7.id, "Drop A", listOf(33, 40, 45, 50, 55, 59, 64))
        assertTuningNotes(tunings, guitar7.id, "A Standard", listOf(33, 38, 43, 48, 53, 57, 62))

        assertTuningNotes(tunings, bass4.id, "E Standard", listOf(28, 33, 38, 43))
        assertTuningNotes(tunings, bass4.id, "D Standard", listOf(26, 31, 36, 41))

        assertTuningNotes(tunings, bass5.id, "B Standard", listOf(23, 28, 33, 38, 43))
        assertTuningNotes(tunings, bass5.id, "A Standard", listOf(21, 26, 31, 36, 41))

        assertThat(tunings.values.sumOf { it.size }).isEqualTo(10)
    }

    @Test
    fun populate_marksSixStringEStandardAsMostRecentlyUsed() = runTest {
        TunerDatabasePopulator(db).populate()

        val mostRecent = db.getTuningDao().getMostRecentTuning().first()
        assertThat(mostRecent).isNotNull()
        assertThat(mostRecent!!.tuningName).isEqualTo("E Standard")
        assertThat(mostRecent.instrumentDefaultName).isEqualTo(DefaultInstrumentName.GUITAR)
        assertThat(mostRecent.notes.map { it.midiNum })
            .containsExactlyElementsIn(listOf(40, 45, 50, 55, 59, 64))
    }

    private fun assertTuningNotes(
        tuningsByInstrument: Map<Int, List<TuningDetails>>,
        instrumentId: Int,
        tuningName: String,
        expectedMidi: List<Int>,
    ) {
        val tuning = tuningsByInstrument.getValue(instrumentId)
            .single { it.tuningName == tuningName }
    
        assertThat(tuning.musicNotes.map { it.midiNum })
            .containsExactlyElementsIn(expectedMidi)
    }
}
