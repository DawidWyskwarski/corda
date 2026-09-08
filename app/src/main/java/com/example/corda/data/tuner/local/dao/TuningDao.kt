package com.example.corda.data.tuner.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.corda.data.tuner.local.entities.MusicNote
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.TuningSoundCrossRef
import com.example.corda.data.tuner.local.models.TuningDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TuningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTuning(tuning: Tuning): Long

    @Update
    suspend fun updateTuning(tuning: Tuning)

    @Transaction
    suspend fun updateTuning(tuning: Tuning, musicNotes: List<MusicNote>) {
        updateTuning(tuning)
        deleteCrossRefsForTuning(tuning.id)

        musicNotes.forEach { sound ->
            insertTuningSoundCrossRef(
                TuningSoundCrossRef(tuningId = tuning.id, musicNoteId = sound.midiNum)
            )
        }
    }

    @Query("UPDATE Tuning SET last_used = :timestamp WHERE tuning_id = :tuningId")
    suspend fun updateTuningLastUsed(tuningId: Int, timestamp: Long)

    @Transaction
    suspend fun insertTuningWithSounds(tuning: Tuning, musicNotes: List<MusicNote>) {
        val tuningId = insertTuning(tuning)
        musicNotes.forEach { sound ->
            insertTuningSoundCrossRef(
                TuningSoundCrossRef(tuningId = tuningId.toInt(), musicNoteId = sound.midiNum)
            )
        }
    }

    @Query("DELETE FROM Tuning WHERE tuning_id = :tuningId")
    suspend fun deleteTuning(tuningId: Int)

    @Transaction
    @Query("""
        SELECT 
            Tuning.tuning_id,
            Tuning.tuning_name AS tuning_name,
            Instrument.instrument_id,
            Instrument.instrument_name AS instrument_name,
            Tuning.last_used
        FROM Tuning
        INNER JOIN Instrument ON Tuning.instrument_id = Instrument.instrument_id
        ORDER BY Tuning.last_used DESC
    """)
    fun getTunings(): Flow<List<TuningDetails>>

    @Transaction
    @Query("""
        SELECT 
            Tuning.tuning_id,
            Tuning.tuning_name AS tuning_name,
            Instrument.instrument_id,
            Instrument.instrument_name AS instrument_name,
            Tuning.last_used
        FROM Tuning
        INNER JOIN Instrument ON Tuning.instrument_id = Instrument.instrument_id
        WHERE Tuning.tuning_id = :tuningId
    """)
    suspend fun getTuningDetails(tuningId: Int): TuningDetails?

    @Transaction
    @Query("""
        SELECT 
            Tuning.tuning_id,
            Tuning.tuning_name AS tuning_name,
            Instrument.instrument_id,
            Instrument.instrument_name AS instrument_name,
            Tuning.last_used
        FROM Tuning
        INNER JOIN Instrument ON Tuning.instrument_id = Instrument.instrument_id
        ORDER BY Tuning.last_used DESC
        LIMIT 1
    """)
    fun getMostRecentTuning(): Flow<TuningDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTuningSoundCrossRef(tuningSoundCrossRef: TuningSoundCrossRef)

    @Query("DELETE FROM TuningSoundCrossRef WHERE tuning_id = :tuningId")
    suspend fun deleteCrossRefsForTuning(tuningId: Int)

}