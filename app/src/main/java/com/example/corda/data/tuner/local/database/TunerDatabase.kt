package com.example.corda.data.tuner.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.corda.data.tuner.local.dao.InstrumentDao
import com.example.corda.data.tuner.local.dao.MusicNoteDao
import com.example.corda.data.tuner.local.dao.TuningDao
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.MusicNote
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.TuningSoundCrossRef

@Database(
    entities = [
        Instrument::class,
        Tuning::class,
        MusicNote::class,
        TuningSoundCrossRef::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class TunerDatabase : RoomDatabase() {

    abstract fun getMusicNoteDao(): MusicNoteDao
    abstract fun getInstrumentDao(): InstrumentDao
    abstract fun getTuningDao(): TuningDao
}
