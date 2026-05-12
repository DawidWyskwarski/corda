package com.example.corda.data.tuner.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.entities.Sound
import com.example.corda.data.tuner.local.entities.Tuning
import com.example.corda.data.tuner.local.entities.relations.TuningSoundCrossRef
import com.example.corda.data.tuner.local.entities.relations.TuningWithInstrumentAndSounds
import kotlinx.coroutines.flow.Flow

@Dao
interface TunerDao {

    // Inserts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstrument(instrument: Instrument): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTuning(tuning: Tuning): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSound(sound: Sound): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTuningSoundCrossRef(tuningSoundCrossRef: TuningSoundCrossRef)

    // Updates
    @Update
    suspend fun updateSound(vararg sound: Sound)

    @Update
    suspend fun updateTuning(vararg tuning: Tuning)

    @Update
    suspend fun updateInstrument(vararg instrument: Instrument)

    @Update
    suspend fun updateTuningSoundCrossRef(vararg tuningSoundCrossRef: TuningSoundCrossRef)

    // Deletes
    @Delete
    suspend fun deleteSound(vararg sound: Sound)

    @Delete
    suspend fun deleteTuning(vararg tuning: Tuning)

    @Delete
    suspend fun deleteInstrument(vararg instrument: Instrument)

    @Delete
    suspend fun deleteTuningSoundCrossRef(vararg tuningSoundCrossRef: TuningSoundCrossRef)

    // Queries
    @Query("""
        SELECT * 
        FROM Instrument
        """)
    fun getInstruments(): Flow<List<Instrument>>

    @Transaction
    @Query("""
        SELECT 
            Tuning.tuning_id,
            Tuning.name AS tuningName,
            Instrument.name AS instrumentName,
            Tuning.last_used
        FROM Tuning
        INNER JOIN Instrument ON Tuning.instrument_id = Instrument.instrument_id
        ORDER BY Tuning.last_used DESC
    """)
    fun getTunings(): Flow<List<TuningWithInstrumentAndSounds>>

    @Query("UPDATE Tuning SET last_used = :timestamp WHERE tuning_id = :tuningId")
    suspend fun updateTuningLastUsed(tuningId: Int, timestamp: Long)

    @Query("""
        SELECT * 
        FROM Sound 
        WHERE Sound.name = "A4"
        """)
    suspend fun getReferencePitch(): Sound

    @Query("SELECT * FROM Sound ORDER BY sound_id ASC")
    suspend fun getAllSounds(): List<Sound>

    @Transaction
    @Query("""
        SELECT 
            Tuning.tuning_id,
            Tuning.name AS tuningName,
            Instrument.name AS instrumentName,
            Tuning.last_used
        FROM Tuning
        INNER JOIN Instrument ON Tuning.instrument_id = Instrument.instrument_id
        WHERE Tuning.tuning_id = :tuningId
    """)
    suspend fun getTuningWithSoundsById(tuningId: Int): TuningWithInstrumentAndSounds?

    @Query("UPDATE Tuning SET name = :name WHERE tuning_id = :tuningId")
    suspend fun updateTuningName(tuningId: Int, name: String)

    @Query("DELETE FROM Tuning WHERE tuning_id = :tuningId")
    suspend fun deleteTuningById(tuningId: Int)

    @Query("DELETE FROM TuningSoundCrossRef WHERE tuning_id = :tuningId")
    suspend fun deleteCrossRefsForTuning(tuningId: Int)

    @Transaction
    suspend fun insertTuningWithSounds(tuning: Tuning, sounds: List<Sound>) {
        val tuningId = insertTuning(tuning)
        sounds.forEach { sound ->
            insertTuningSoundCrossRef(
                TuningSoundCrossRef(tuningId = tuningId.toInt(), soundId = sound.soundId)
            )
        }
    }

    @Transaction
    suspend fun updateTuningWithSounds(tuningId: Int, name: String, sounds: List<Sound>) {
        updateTuningName(tuningId, name)
        deleteCrossRefsForTuning(tuningId)
        sounds.forEach { sound ->
            insertTuningSoundCrossRef(
                TuningSoundCrossRef(tuningId = tuningId, soundId = sound.soundId)
            )
        }
    }
}