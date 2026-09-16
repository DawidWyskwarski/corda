package com.example.corda.tuner.data.repository

import com.example.corda.tuner.data.local.entities.Instrument
import com.example.corda.tuner.data.local.entities.MusicNote
import com.example.corda.tuner.data.local.entities.Tuning
import com.example.corda.tuner.data.local.models.TuningDetails
import kotlinx.coroutines.flow.Flow

interface TunerRepository {

    suspend fun getAllSounds(): List<MusicNote>
    fun getInstrumentsFlow(): Flow<List<Instrument>>
    suspend fun insertInstrument(instrument: Instrument): Result<Long>
    suspend fun updateInstrument(instrument: Instrument): Result<Unit>
    suspend fun deleteInstrument(instrumentId: Int): Result<Unit>
    fun getAllTunings(): Flow<List<TuningDetails>>
    suspend fun getTuning(tuningId: Int): TuningDetails?
    fun getMostRecentTuning(): Flow<TuningDetails?>
    suspend fun insertTuning(tuning: Tuning, musicNotes: List<MusicNote>): Result<Unit>
    suspend fun updateTuningLastUsed(tuningId: Int): Result<Unit>
    suspend fun updateTuning(tuning: Tuning, musicNotes: List<MusicNote>): Result<Unit>
    suspend fun deleteTuning(tuningId: Int): Result<Unit>
}
