package com.example.corda.tuner.data.local.dao

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.corda.tuner.data.local.database.TunerDatabase
import com.example.corda.tuner.data.local.entities.DefaultInstrumentName
import com.example.corda.tuner.data.local.entities.Instrument
import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.data.local.entities.Tuning
import com.example.corda.tuner.data.local.models.TuningDetails
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TuningDaoTest {

    private lateinit var db: TunerDatabase
    private lateinit var tuningDao: TuningDao
    private lateinit var instrumentDao: InstrumentDao
    private lateinit var musicNoteDao: MusicNoteDao

    private val noteC4 = MusicNote(midiNum = 60, name = "C", octave = 4)
    private val noteE4 = MusicNote(midiNum = 64, name = "E", octave = 4)
    private val noteG4 = MusicNote(midiNum = 67, name = "G", octave = 4)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TunerDatabase::class.java)
            .build()
        tuningDao = db.getTuningDao()
        instrumentDao = db.getInstrumentDao()
        musicNoteDao = db.getMusicNoteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun insertInstrument(
        customName: String = "Guitar",
        defaultName: DefaultInstrumentName? = DefaultInstrumentName.GUITAR,
        musicNotesCount: Byte = 6,
    ): Int = instrumentDao.insertInstrument(
        Instrument(
            customName = customName,
            defaultName = defaultName,
            musicNotesCount = musicNotesCount,
        ),
    ).toInt()

    private suspend fun seedNotes(vararg notes: MusicNote = arrayOf(noteC4, noteE4, noteG4)) {
        musicNoteDao.insertNotes(notes.toList())
    }

    private suspend fun firstTuningDetails(): TuningDetails =
        tuningDao.getTunings().first().single()

    private fun crossRefCount(tuningId: Int): Int {
        db.openHelper.readableDatabase.query(
            "SELECT COUNT(*) FROM TuningSoundCrossRef WHERE tuning_id = ?",
            arrayOf(tuningId.toString()),
        ).use { cursor ->
            cursor.moveToFirst()
            return cursor.getInt(0)
        }
    }

    @Test
    fun insertTuning_returnsGeneratedId_andRowIsPersisted() = runTest {
        val instrumentId = insertInstrument()
        val tuningId = tuningDao.insertTuning(
            Tuning(name = "Standard", instrumentId = instrumentId, lastUsed = 100L),
        ).toInt()

        val details = tuningDao.getTuningDetails(tuningId)
        assertThat(details).isNotNull()
        assertThat(details!!.tuningId).isEqualTo(tuningId)
        assertThat(details.tuningName).isEqualTo("Standard")
        assertThat(details.instrumentId).isEqualTo(instrumentId)
        assertThat(details.instrumentCustomName).isEqualTo("Guitar")
        assertThat(details.instrumentDefaultName).isEqualTo(DefaultInstrumentName.GUITAR)
        assertThat(details.musicNotes).isEmpty()
    }

    @Test
    fun insertTuning_withUnknownInstrumentId_throwsConstraintException() = runTest {
        try {
            tuningDao.insertTuning(
                Tuning(name = "Orphan", instrumentId = 999, lastUsed = 0L),
            )
            throw AssertionError("Expected SQLiteConstraintException")
        } catch (e: SQLiteConstraintException) {
            assertThat(e).isInstanceOf(SQLiteConstraintException::class.java)
        }
    }

    @Test
    fun updateTuning_updatesSimpleFields_leavesCrossRefsUntouched() = runTest {
        seedNotes()
        val instrumentId = insertInstrument()
        tuningDao.insertTuningWithSounds(
            Tuning(name = "Before", instrumentId = instrumentId, lastUsed = 10L),
            listOf(noteC4, noteE4),
        )
        val tuningId = firstTuningDetails().tuningId

        tuningDao.updateTuning(
            Tuning(
                id = tuningId,
                name = "After",
                instrumentId = instrumentId,
                lastUsed = 20L,
            ),
        )

        val details = tuningDao.getTuningDetails(tuningId)!!
        assertThat(details.tuningName).isEqualTo("After")
        assertThat(details.musicNotes).containsExactly(noteC4, noteE4)
    }

    @Test
    fun updateTuning_withNotes_replacesAllAssociatedNotes() = runTest {
        seedNotes()
        val instrumentId = insertInstrument()
        tuningDao.insertTuningWithSounds(
            Tuning(name = "Drop", instrumentId = instrumentId, lastUsed = 1L),
            listOf(noteC4, noteE4),
        )
        val tuningId = firstTuningDetails().tuningId

        tuningDao.updateTuning(
            Tuning(id = tuningId, name = "Drop", instrumentId = instrumentId, lastUsed = 1L),
            listOf(noteG4),
        )

        val details = tuningDao.getTuningDetails(tuningId)!!
        assertThat(details.musicNotes).containsExactly(noteG4)
        assertThat(crossRefCount(tuningId)).isEqualTo(1)
    }

    @Test
    fun updateTuningLastUsed_updatesTimestamp_andAffectsOrdering() = runTest {
        val instrumentId = insertInstrument()
        val olderId = tuningDao.insertTuning(
            Tuning(name = "Older", instrumentId = instrumentId, lastUsed = 100L),
        ).toInt()
        val newerId = tuningDao.insertTuning(
            Tuning(name = "Newer", instrumentId = instrumentId, lastUsed = 200L),
        ).toInt()

        tuningDao.getTunings().test {
            assertThat(awaitItem().map { it.tuningId }).containsExactly(newerId, olderId).inOrder()
            cancelAndIgnoreRemainingEvents()
        }

        tuningDao.updateTuningLastUsed(olderId, 300L)

        tuningDao.getTunings().test {
            assertThat(awaitItem().map { it.tuningId }).containsExactly(olderId, newerId).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
        tuningDao.getMostRecentTuning().test {
            assertThat(awaitItem()!!.tuningId).isEqualTo(olderId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertTuningWithSounds_insertsTuningAndCrossRefsTransactionally() = runTest {
        seedNotes()
        val instrumentId = insertInstrument(
            customName = "Bass",
            defaultName = DefaultInstrumentName.BASS,
            musicNotesCount = 4,
        )

        tuningDao.insertTuningWithSounds(
            Tuning(name = "E Standard", instrumentId = instrumentId, lastUsed = 50L),
            listOf(noteC4, noteE4, noteG4),
        )

        tuningDao.getTunings().test {
            val list = awaitItem()
            assertThat(list).hasSize(1)
            val details = list.single()
            assertThat(details.tuningName).isEqualTo("E Standard")
            assertThat(details.instrumentCustomName).isEqualTo("Bass")
            assertThat(details.instrumentDefaultName).isEqualTo(DefaultInstrumentName.BASS)
            assertThat(details.musicNotes).containsExactly(noteC4, noteE4, noteG4)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteTuning_cascadesToDeleteCrossRefs() = runTest {
        seedNotes()
        val instrumentId = insertInstrument()
        tuningDao.insertTuningWithSounds(
            Tuning(name = "Doomed", instrumentId = instrumentId, lastUsed = 1L),
            listOf(noteC4, noteE4),
        )
        val tuningId = firstTuningDetails().tuningId

        assertThat(crossRefCount(tuningId)).isEqualTo(2)

        tuningDao.deleteTuning(tuningId)

        assertThat(tuningDao.getTuningDetails(tuningId)).isNull()
        assertThat(crossRefCount(tuningId)).isEqualTo(0)
    }

    @Test
    fun getTunings_emitsListOrderedByLastUsedDescending() = runTest {
        val guitarId = insertInstrument(customName = "Guitar")
        val bassId = insertInstrument(
            customName = "Bass",
            defaultName = DefaultInstrumentName.BASS,
            musicNotesCount = 4,
        )
        val first = tuningDao.insertTuning(
            Tuning(name = "First", instrumentId = guitarId, lastUsed = 10L),
        ).toInt()
        val second = tuningDao.insertTuning(
            Tuning(name = "Second", instrumentId = bassId, lastUsed = 30L),
        ).toInt()
        val third = tuningDao.insertTuning(
            Tuning(name = "Third", instrumentId = guitarId, lastUsed = 20L),
        ).toInt()

        tuningDao.getTunings().test {
            assertThat(awaitItem().map { it.tuningId })
                .containsExactly(second, third, first)
                .inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTuningDetails_returnsNull_forNonExistentTuningId() = runTest {
        assertThat(tuningDao.getTuningDetails(999)).isNull()
    }

    @Test
    fun getMostRecentTuning_emitsNullWhenEmpty_andHighestLastUsedWhenPresent() = runTest {
        tuningDao.getMostRecentTuning().test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }

        val instrumentId = insertInstrument()
        tuningDao.insertTuning(
            Tuning(name = "Low", instrumentId = instrumentId, lastUsed = 1L),
        )
        val highId = tuningDao.insertTuning(
            Tuning(name = "High", instrumentId = instrumentId, lastUsed = 99L),
        ).toInt()

        tuningDao.getMostRecentTuning().test {
            val current = awaitItem()
            assertThat(current).isNotNull()
            assertThat(current!!.tuningId).isEqualTo(highId)
            assertThat(current.tuningName).isEqualTo("High")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteCrossRefsForTuning_removesOnlyTargetTuningCrossRefs() = runTest {
        seedNotes()
        val instrumentId = insertInstrument()
        tuningDao.insertTuningWithSounds(
            Tuning(name = "A", instrumentId = instrumentId, lastUsed = 1L),
            listOf(noteC4, noteE4),
        )
        tuningDao.insertTuningWithSounds(
            Tuning(name = "B", instrumentId = instrumentId, lastUsed = 2L),
            listOf(noteG4),
        )

        val byName = tuningDao.getTunings().first().associateBy { it.tuningName }
        val tuningAId = byName.getValue("A").tuningId
        val tuningBId = byName.getValue("B").tuningId

        tuningDao.deleteCrossRefsForTuning(tuningAId)

        assertThat(crossRefCount(tuningAId)).isEqualTo(0)
        assertThat(crossRefCount(tuningBId)).isEqualTo(1)

        val detailsA = tuningDao.getTuningDetails(tuningAId)!!
        val detailsB = tuningDao.getTuningDetails(tuningBId)!!
        assertThat(detailsA.musicNotes).isEmpty()
        assertThat(detailsB.musicNotes).containsExactly(noteG4)
    }
}
