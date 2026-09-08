package com.example.corda.data.tuner.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for a single sound of a tuning.
 * Table with this entity should be only pre-populated during database creation.
 *
 * The frequency of the sound shall be calculated on the fly.
 *
 * @param midiNum MIDI note number as a primary key (e.g. 60 for C4).
 * @param name Pitch class name, e.g. "C".
 * @param octave Octave number, e.g. 4.
 */
@Entity
data class MusicNote(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "midi_number")
    val midiNum: Int,
    val name: String,
    val octave: Int,
)
