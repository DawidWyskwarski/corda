package com.example.corda.tuner.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for a single instrument.
 *
 * @param id Auto-generated primary key.
 * @param customName Name of the instrument given by the user.(e.g. "Guitar").
 * @param defaultName Enum representing a name of the default/seeded instrument. Used for translations.
 *  Set at seed time. Not mutable by user
 * @param musicNotesCount Number of music notes the instrument can tune to (e.g. 6 for Guitar).
 */
@Entity
data class Instrument(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "instrument_id")
    val id: Int = 0,
    @ColumnInfo(name = "instrument_custom_name")
    val customName: String? = null,
    @ColumnInfo(name = "instrument_default_name")
    val defaultName: DefaultInstrumentName? = null,
    @ColumnInfo(name = "music_notes_count")
    val musicNotesCount: Byte, // In the context of guitar, bass, etc. this represents the number of strings.
)

enum class DefaultInstrumentName {
    GUITAR,
    BASS
}

val INSTRUMENT_MUSIC_NOTES_COUNT_BRACKET = 2..24
