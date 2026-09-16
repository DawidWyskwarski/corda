package com.example.corda.tuner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.corda.tuner.data.local.entities.MusicNote

/**
 * Data Access Object for the [MusicNote] entity.
 *
 * Because sounds table should be static there is no need for more CRUD operations.
 */
@Dao
interface MusicNoteDao {
    /**
     * Get all sounds from the database.
     */
    @Query("SELECT * FROM MusicNote ORDER BY midi_number ASC")
    suspend fun getAllNotes(): List<MusicNote>

    /**
     * Insert a list of sounds into the database.
     * Should be used only during database creation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(musicNotes: List<MusicNote>)
}
