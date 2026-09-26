package com.example.corda.tuner.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Junction
import androidx.room.Relation
import com.example.corda.tuner.data.local.entities.DefaultInstrumentName
import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.data.local.entities.TuningSoundCrossRef

/**
 * This class represents info about currently used tuning.
 * Used in a Main Tuner Screen
 *
 * @param tuningName Name of the tuning
 * @param instrumentCustomName User's name of the instrument
 * @param instrumentDefaultName Type of build in instrument
 * @param notes List of notes used in the tuning
 */
data class CurrentTuning(
    @ColumnInfo(name = "tuning_id")
    val tuningId: Int,
    @ColumnInfo(name = "tuning_name")
    val tuningName: String,
    @ColumnInfo(name = "instrument_custom_name")
    val instrumentCustomName: String?,
    @ColumnInfo(name = "instrument_default_name")
    val instrumentDefaultName: DefaultInstrumentName?,
    @Relation(
        parentColumn = "tuning_id",
        entityColumn = "midi_number",
        associateBy = Junction(
            value = TuningSoundCrossRef::class,
            parentColumn = "tuning_id",
            entityColumn = "music_note_id",
        )
    )
    val notes: List<MusicNote>,
)
