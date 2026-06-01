package com.example.corda.data.tuner.local

import android.content.ContentValues
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.corda.domain.tuner.pitch.PitchHelpers

object TunerDatabaseTestHelper {

    fun minimal(context: Context): TunerDatabase =
        Room.inMemoryDatabaseBuilder(context, TunerDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    fun withProductionSeed(context: Context): TunerDatabase =
        Room.inMemoryDatabaseBuilder(context, TunerDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(ProductionSeedCallback())
            .build()

    private class ProductionSeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.beginTransaction()
            try {
                populateSounds(db)
                populateInstrumentsAndTunings(db)
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_Sound_midi_note ON Sound(midi_note)",
                )
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun populateSounds(db: SupportSQLiteDatabase) {
        for (octave in 0..8) {
            for ((noteIndex, pitchClass) in PitchHelpers.pitchClasses.withIndex()) {
                val midi = PitchHelpers.midiNote(octave, noteIndex)
                val frequency = PitchHelpers.frequencyFromMidi(midi)
                db.insert("Sound", 0, ContentValues().apply {
                    put("name", pitchClass)
                    put("frequency", frequency)
                    put("octave", octave)
                    put("midi_note", midi)
                })
            }
        }
    }

    private fun populateInstrumentsAndTunings(db: SupportSQLiteDatabase) {
        val guitar6 = insertInstrument(db, name = "Guitar (6-string)", soundsCount = 6)
        val guitar7 = insertInstrument(db, name = "Guitar (7-string)", soundsCount = 7)
        val bass4 = insertInstrument(db, name = "Bass (4-string)", soundsCount = 4)
        val bass5 = insertInstrument(db, name = "Bass (5-string)", soundsCount = 5)

        insertTuningWithSounds(db, "Standard", guitar6, listOf(40, 45, 50, 55, 59, 64))
        insertTuningWithSounds(db, "Drop D", guitar6, listOf(38, 45, 50, 55, 59, 64))
        insertTuningWithSounds(db, "D Standard", guitar6, listOf(38, 43, 48, 53, 57, 62))

        insertTuningWithSounds(db, "Standard", guitar7, listOf(35, 40, 45, 50, 55, 59, 64))
        insertTuningWithSounds(db, "Drop A", guitar7, listOf(33, 40, 45, 50, 55, 59, 64))
        insertTuningWithSounds(db, "A Standard", guitar7, listOf(33, 38, 43, 48, 53, 57, 62))

        insertTuningWithSounds(db, "Standard", bass4, listOf(28, 33, 38, 43))
        insertTuningWithSounds(db, "D Standard", bass4, listOf(26, 31, 36, 41))

        insertTuningWithSounds(db, "Standard", bass5, listOf(23, 28, 33, 38, 43))
        insertTuningWithSounds(db, "A Standard", bass5, listOf(21, 26, 31, 36, 41))
    }

    private fun insertInstrument(db: SupportSQLiteDatabase, name: String, soundsCount: Int): Long =
        db.insert("Instrument", 0, ContentValues().apply {
            put("name", name)
            put("sounds_count", soundsCount)
        })

    private fun insertTuningWithSounds(
        db: SupportSQLiteDatabase,
        name: String,
        instrumentId: Long,
        midiNotes: List<Int>,
    ) {
        val tuningId = db.insert("Tuning", 0, ContentValues().apply {
            put("name", name)
            put("instrument_id", instrumentId)
            put("last_used", 0L)
        })

        midiNotes.forEach { midi ->
            val soundId = querySoundIdByMidi(db, midi)
            checkNotNull(soundId) { "Sound with midi_note=$midi not found in seed data." }
            db.insert("TuningSoundCrossRef", 0, ContentValues().apply {
                put("tuning_id", tuningId)
                put("sound_id", soundId)
            })
        }
    }

    private fun querySoundIdByMidi(db: SupportSQLiteDatabase, midiNote: Int): Long? {
        val cursor = db.query(
            "SELECT sound_id FROM Sound WHERE midi_note = ?",
            arrayOf(midiNote.toString()),
        )
        return if (cursor.moveToFirst()) {
            cursor.getLong(0).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }
}
