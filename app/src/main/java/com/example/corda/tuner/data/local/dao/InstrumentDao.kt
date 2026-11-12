package com.example.corda.tuner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.corda.tuner.data.local.entities.Instrument
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [Instrument] entity.
 *
 *
 */
@Dao
interface InstrumentDao {

    @Query("SELECT * FROM Instrument")
    fun getInstruments(): Flow<List<Instrument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstrument(instrument: Instrument): Long

    @Update
    suspend fun updateInstrument(instrument: Instrument)

    @Query("DELETE FROM Instrument WHERE instrument_id IN (:instrumentId)")
    suspend fun deleteInstrument(instrumentId: Int)

}
