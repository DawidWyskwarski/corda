package com.example.corda.data.tuner.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Instrument(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "instrument_id")
    val instrumentId: Int = 0,
    val name: String,
    @ColumnInfo(name = "sounds_count")
    val soundsCount: Byte
)
