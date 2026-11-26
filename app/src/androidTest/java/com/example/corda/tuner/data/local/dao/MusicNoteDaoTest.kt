package com.example.corda.tuner.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.tuner.data.local.database.TunerDatabase
import com.example.corda.tuner.data.local.entities.MusicNote
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicNoteDaoTest {

    private lateinit var db: TunerDatabase
    private lateinit var dao: MusicNoteDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TunerDatabase::class.java)
            .build()
        dao = db.getMusicNoteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertNotes_thenGetAllNotes_returnsAllInsertedNotes() = runTest {
        val notes = listOf(
            MusicNote(midiNum = 60, name = "C", octave = 4),
            MusicNote(midiNum = 64, name = "E", octave = 4),
        )
        dao.insertNotes(notes)

        assertThat(dao.getAllNotes()).containsExactlyElementsIn(notes)
    }

    @Test
    fun getAllNotes_returnsNotesOrderedByMidiNumberAscending() = runTest {
        dao.insertNotes(
            listOf(
                MusicNote(midiNum = 64, name = "E", octave = 4),
                MusicNote(midiNum = 60, name = "C", octave = 4),
                MusicNote(midiNum = 62, name = "D", octave = 4),
            ),
        )

        assertThat(dao.getAllNotes().map { it.midiNum })
            .containsExactly(60, 62, 64)
            .inOrder()
    }

    @Test
    fun insertNotes_withDuplicateMidiNumber_replacesExistingNote() = runTest {
        dao.insertNotes(listOf(MusicNote(midiNum = 60, name = "C", octave = 4)))
        dao.insertNotes(listOf(MusicNote(midiNum = 60, name = "C#", octave = 4)))

        val notes = dao.getAllNotes()
        assertThat(notes).hasSize(1)
        assertThat(notes[0]).isEqualTo(MusicNote(midiNum = 60, name = "C#", octave = 4))
    }

    @Test
    fun getAllNotes_onEmptyDatabase_returnsEmptyList() = runTest {
        assertThat(dao.getAllNotes()).isEmpty()
    }
}
