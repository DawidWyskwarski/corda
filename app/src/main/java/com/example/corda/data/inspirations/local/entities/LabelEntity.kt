package com.example.corda.data.inspirations.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Label",
    indices = [Index(value = ["name"], unique = true)],
)
data class LabelEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "label_id")
    val labelId: Long = 0,
    val name: String,
)
