package com.example.corda.data.tuner.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val tuningId: Int = 0,
    val name: String,
    @ColumnInfo(name = "instrument_id")
    val instrumentId: Int,
    @ColumnInfo(name = "last_used")
    val lastUsed: Long = 0L
)
