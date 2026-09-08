package com.example.corda.data.tuner.local.database

import androidx.room.withTransaction
import com.example.corda.data.tuner.local.dao.TuningDao
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.MusicNote
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.TuningSoundCrossRef

class TunerDatabasePopulator (
    private val db : TunerDatabase
) {

    suspend fun populate() {
        db.withTransaction {
            insertMusicNotes()
            insertInstrumentsAndTunings()
        }
    }

    private suspend fun insertMusicNotes() {
        val soundDao = db.getMusicNoteDao()

        val pitchClasses: List<String> = listOf(
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B",
        )

        // From C0 (12) to B8 (119)
        val midiRange = 12..119

        var musicNotes = listOf<MusicNote>()

        for (midiNote in midiRange) {
            val octave = (midiNote - 12) / 12
            val noteIndex = (midiNote - 12) % 12
            val pitchClass = pitchClasses[noteIndex]

            musicNotes += MusicNote(
                midiNum = midiNote,
                name = pitchClass,
                octave = octave,
            )
        }

        soundDao.insertNotes(musicNotes)
    }

    private suspend fun insertInstrumentsAndTunings() {
        val instrumentDao = db.getInstrumentDao()
        val tuningDao = db.getTuningDao()

        val guitar6 = instrumentDao.insertInstrument(
            Instrument( name = "Guitar (6-string)", musicNotesCount = 6 ))
        insert6StringGuitarTunings(tuningDao, guitar6)

        val guitar7 = instrumentDao.insertInstrument(
            Instrument( name = "Guitar (7-string)", musicNotesCount = 7 ))
        insert7StringGuitarTunings(tuningDao, guitar7)

        val bass4 = instrumentDao.insertInstrument(
            Instrument( name = "Bass (4-string)", musicNotesCount = 4 ))
        insert4StringBassTunings(tuningDao, bass4)

        val bass5 = instrumentDao.insertInstrument(
            Instrument( name = "Bass (5-string)", musicNotesCount = 5 ))
        insert5StringBassTunings(tuningDao, bass5)
    }

    private suspend fun insert6StringGuitarTunings(tuningDao: TuningDao, guitar6: Long) {
        insertTuningWithMidiNotes(tuningDao, "Standard", guitar6, listOf(40, 45, 50, 55, 59, 64), 1L)
        insertTuningWithMidiNotes(tuningDao, "Drop D", guitar6, listOf(38, 45, 50, 55, 59, 64))
        insertTuningWithMidiNotes(tuningDao, "D Standard", guitar6, listOf(38, 43, 48, 53, 57, 62))
    }

    private suspend fun insert7StringGuitarTunings(tuningDao: TuningDao, guitar7: Long) {
        insertTuningWithMidiNotes(tuningDao, "Standard", guitar7, listOf(35, 40, 45, 50, 55, 59, 64))
        insertTuningWithMidiNotes(tuningDao, "Drop A", guitar7, listOf(33, 40, 45, 50, 55, 59, 64))
        insertTuningWithMidiNotes(tuningDao, "A Standard", guitar7, listOf(33, 38, 43, 48, 53, 57, 62))
    }

    private suspend fun insert4StringBassTunings(tuningDao: TuningDao, bass4: Long) {
        insertTuningWithMidiNotes(tuningDao, "Standard", bass4, listOf(28, 33, 38, 43))
        insertTuningWithMidiNotes(tuningDao, "D Standard", bass4, listOf(26, 31, 36, 41))
    }

    private suspend fun insert5StringBassTunings(tuningDao: TuningDao, bass5: Long) {
        insertTuningWithMidiNotes(tuningDao, "Standard", bass5, listOf(23, 28, 33, 38, 43))
        insertTuningWithMidiNotes(tuningDao, "A Standard", bass5, listOf(21, 26, 31, 36, 41))
    }

    private suspend fun insertTuningWithMidiNotes(
        tuningDao: TuningDao,
        name: String,
        instrumentId: Long,
        midiNotes: List<Int>,
        lastUsed: Long = 0L
    ) {
        val tuningId = tuningDao.insertTuning(
            Tuning(name = name, instrumentId = instrumentId.toInt(), lastUsed = lastUsed)
        )
        midiNotes.forEach { midi ->
            tuningDao.insertTuningSoundCrossRef(
                TuningSoundCrossRef(tuningId = tuningId.toInt(), musicNoteId = midi)
            )
        }
    }
}
