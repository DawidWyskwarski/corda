package com.example.corda.data.tuner.local.models

import androidx.room.ColumnInfo
import androidx.room.Junction
import androidx.room.Relation
import com.example.corda.data.tuner.local.entities.MusicNote
import com.example.corda.data.tuner.local.entities.TuningSoundCrossRef
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.Instrument

/**
 * A class containing aggregated data for a [Tuning]. Primary model used in the UI layer.
 *
 * @param tuningId [Tuning.id]
 * @param tuningName [Tuning.name]
 * @param instrumentId [Instrument.id]
 * @param instrumentName [Instrument.name]
 * @param musicNotes [TuningSoundCrossRef] joined with [MusicNote]
 * @param lastUsed [Tuning.lastUsed]
 */
data class TuningDetails (
    @ColumnInfo(name = "tuning_id")
    val tuningId: Int,
    @ColumnInfo(name = "tuning_name")
    val tuningName: String,
    @ColumnInfo(name = "instrument_id")
    val instrumentId: Int,
    @ColumnInfo(name = "instrument_name")
    val instrumentName: String,
    @Relation(
        parentColumn = "tuning_id",
        entityColumn = "midi_number",
        associateBy = Junction(
            value = TuningSoundCrossRef::class,
            parentColumn = "tuning_id",
            entityColumn = "music_note_id",
        )
    )
    val musicNotes: List<MusicNote>,
    @ColumnInfo(name = "last_used")
    val lastUsed: Long,
)
