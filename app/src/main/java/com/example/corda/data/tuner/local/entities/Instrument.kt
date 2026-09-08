package com.example.corda.data.tuner.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for a single instrument.
 *
 * @param id Auto-generated primary key.
 * @param name Name of the instrument (e.g. "Guitar").
 * @param musicNotesCount Number of music notes the instrument can tune to (e.g. 6 for Guitar).
 */
@Entity
data class Instrument(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "instrument_id")
    val id: Int = 0,
    @ColumnInfo(name = "instrument_name")
    val name: String,
    @ColumnInfo(name = "music_notes_count")
    val musicNotesCount: Byte // In the context of guitar, bass, etc. this represents the number of strings.
)
