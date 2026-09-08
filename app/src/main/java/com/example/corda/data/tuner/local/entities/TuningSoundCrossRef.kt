package com.example.corda.data.tuner.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Database entity for the many-to-many relationship between [Tuning] and [MusicNote].
 *
 * A tuning can have a single music note assigned multiple times.
 *
 * @param id Auto-generated primary key.
 * @param tuningId Foreign key to [Tuning.id].
 * @param musicNoteId Foreign key to [MusicNote.midiNum].
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Tuning::class,
            parentColumns = ["tuning_id"],
            childColumns = ["tuning_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MusicNote::class,
            parentColumns = ["midi_number"],
            childColumns = ["music_note_id"]
        )
    ]
)
data class TuningSoundCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "tuning_id")
    val tuningId: Int,
    @ColumnInfo(name = "music_note_id")
    val musicNoteId: Int
)
