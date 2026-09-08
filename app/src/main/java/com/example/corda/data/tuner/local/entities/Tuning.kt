package com.example.corda.data.tuner.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Database entity for a tuning.
 *
 * @param id Auto-generated primary key.
 * @param name Name of the tuning (e.g. "A Standard").
 * @param instrumentId Foreign key to [Instrument.id].
 * @param lastUsed Timestamp of the last time the tuning was used. For sorting.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Instrument::class,
            parentColumns = ["instrument_id"],
            childColumns = ["instrument_id"]
        )
    ]
)
data class Tuning(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "tuning_id")
    val id: Int = 0,
    @ColumnInfo(name = "tuning_name")
    val name: String,
    @ColumnInfo(name = "instrument_id")
    val instrumentId: Int,
    @ColumnInfo(name = "last_used")
    val lastUsed: Long = 0L
)
